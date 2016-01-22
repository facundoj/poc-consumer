import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.codehaus.jackson.JsonProcessingException;
import org.imsglobal.caliper.entities.DigitalResource;
import org.imsglobal.caliper.entities.LearningObjective;
import org.imsglobal.caliper.entities.outcome.Result;
import org.imsglobal.caliper.events.BaseEventContext;
import org.imsglobal.caliper.events.EventType;
import org.imsglobal.caliper.events.OutcomeEvent;
import org.k12.caliper.poc.parser.CaliperParser;
import org.k12.caliper.poc.parser.CaliperParserFactory;
import org.k12.caliper.poc.persistence.HiveContextSingleton;
import org.k12.caliper.poc.persistence.StudentPerformanceRow;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Usage:
 *
 spark-submit \
 --driver-class-path /home/cloudera/infra/spark-1.3.0-jars/htrace-core-3.1.0-incubating.jar \
 --conf spark.executor.extraClassPath="/opt/cloudera/parcels/CDH-<CDH_VERSION>/lib/hive/lib/*" \
 --jars /home/cloudera/infra/spark-1.3.0-jars/spark-streaming-kafka-assembly_2.10-1.3.0.jar,caliper-java-1.0.0.jar \
 --class POCConsumer --master local ecs-1.0-SNAPSHOT.jar \
 <zookeeper> <consumer-group>
 */
public class POCConsumer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: POCConsumer <zk> <group>");
            System.exit(1);
        }

        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("POCConsumer");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(250));

        // Topics map
        Map<String, Integer> topics = new HashMap<String, Integer>();
        topics.put("caliper_events", 1);

        JavaPairReceiverInputDStream<String, String> caliperEvents =
                KafkaUtils.createStream(jssc, args[0], args[1], topics);

        // START: Running total - Spark logic **********************************

        // flattenResult -- ((usr, lo), (obtained, total))
        JavaPairDStream<Tuple2<String, String>, Tuple2<Double, Double>> flattenResult = caliperEvents.flatMapToPair(new PairFlatMapFunction<Tuple2<String, String>, Tuple2<String, String>, Tuple2<Double, Double>>() {
            public Iterable<Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>> call(Tuple2<String, String> message) throws Exception {
                // Caliper Model
                BaseEventContext event = getCaliperModel(message._2());
                // Map output
                List<Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>> output = new ArrayList<Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>>();

                if (event == null) return null;

                // OutcomeEvents only
                if (EventType.OUTCOME.getValue().equals(event.getType())) {
                    OutcomeEvent oEvent = (OutcomeEvent) event;
                    String student = oEvent.getActor().getId();
                    Result result = (Result) oEvent.getGenerated();



                    if (result == null) return null;

                    DigitalResource assessmentItem = result.getAssignable();

                    for (LearningObjective lo : assessmentItem.getLearningObjectives()) {
                        output.add(new Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>(
                                new Tuple2<String, String>(student, lo.getId()), // (usr, lo)
                                new Tuple2<Double, Double>(result.getNormalScore(), result.getTotalScore()) // (obtained, total)
                        ));
                    }
                }

                return output;
            }
        });

        // result -- ((usr, lo), (obtained, total))
        JavaPairDStream<Tuple2<String, String>, Tuple2<Double, Double>> result = flattenResult.reduceByKey(new Function2<Tuple2<Double, Double>, Tuple2<Double, Double>, Tuple2<Double, Double>>() {
            public Tuple2<Double, Double> call(Tuple2<Double, Double> performance1, Tuple2<Double, Double> performance2) throws Exception {
                return new Tuple2<Double, Double>(
                        performance1._1() + performance2._1(),
                        performance1._2() + performance2._2()
                );
            }
        });

        //result.print();

        // Quick and dirty persist to hive

        // This method is deprecated with this parameters, but it is the way to do it according to the docs for this
        // spark version (1.3.0)
        result.foreachRDD(
                new Function2<JavaPairRDD<Tuple2<String, String>, Tuple2<Double, Double>>, Time, Void>() {
                    public Void call(JavaPairRDD<Tuple2<String, String>, Tuple2<Double, Double>> tuple2Tuple2JavaPairRDD, Time time) throws Exception {
                        HiveContext hiveContext = HiveContextSingleton.getInstance(tuple2Tuple2JavaPairRDD.context());

                        // Convert RDD to serializable
                        JavaRDD<StudentPerformanceRow> rowRDD = tuple2Tuple2JavaPairRDD.map(new Function<Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>, StudentPerformanceRow>() {
                            public StudentPerformanceRow call(Tuple2<Tuple2<String, String>, Tuple2<Double, Double>> tuple2Tuple2Tuple2) throws Exception {
                                StudentPerformanceRow studentPerformanceRow = new StudentPerformanceRow();
                                studentPerformanceRow.setStudentId(tuple2Tuple2Tuple2._1()._1());
                                studentPerformanceRow.setObjectiveId(tuple2Tuple2Tuple2._1()._2());
                                studentPerformanceRow.setObtainedScore(tuple2Tuple2Tuple2._2()._1());
                                studentPerformanceRow.setTotalScore(tuple2Tuple2Tuple2._2()._2());
                                return studentPerformanceRow;
                            }
                        });

                        // Create data frame and insert it into the destination table
                        DataFrame studentPerformanceFrame = hiveContext.createDataFrame(rowRDD, StudentPerformanceRow.class);
                        for (Row s : studentPerformanceFrame.collect()) {
                            System.out.println(s.toString());
                        }

                        studentPerformanceFrame.select(new Column("studentId"),
                                new Column("objectiveId"),
                                new Column("obtainedScore"),
                                new Column("totalScore")).insertInto("student_performance");
                        return null;
                    }
                }
        );


        // END: Running total - Spark logic ************************************

        jssc.start();
        jssc.awaitTermination();
    }

    /**
     * Parses message (JSON) into a Caliper Event.
     *
     * @param message {String}
     * @return event {BaseEventContext}
     */
    private static BaseEventContext getCaliperModel(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonEvent = mapper.readTree(message).get("data").get(0);

            String type = jsonEvent.get("@type").asText();
            CaliperParser<?> parser = CaliperParserFactory
                    .getParser(type);

            if (parser != null) {
                return (BaseEventContext) parser.parseCaliperObject(jsonEvent);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
