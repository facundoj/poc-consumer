import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.codehaus.jackson.JsonProcessingException;
import org.imsglobal.caliper.entities.LearningObjective;
import org.imsglobal.caliper.entities.assessment.AssessmentItem;
import org.imsglobal.caliper.entities.assignable.Attempt;
import org.imsglobal.caliper.entities.outcome.Result;
import org.imsglobal.caliper.entities.response.Response;
import org.imsglobal.caliper.events.AssessmentItemEvent;
import org.imsglobal.caliper.events.BaseEventContext;
import org.imsglobal.caliper.events.EventType;
import org.imsglobal.caliper.events.OutcomeEvent;
import org.k12.caliper.poc.parser.CaliperParser;
import org.k12.caliper.poc.parser.CaliperParserFactory;
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
 --jars /home/cloudera/infra/spark-1.3.0-jars/spark-streaming-kafka-assembly_2.10-1.3.0.jar \
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
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(5000));

        // Topics map
        Map<String, Integer> topics = new HashMap<String, Integer>();
        topics.put("caliper_events", 1);

        JavaPairReceiverInputDStream<String, String> caliperEvents =
                KafkaUtils.createStream(jssc, args[0], args[1], topics);

        // START: Running total - Spark logic **********************************

        // items -- (key, (usr, [LOs]))
        JavaPairDStream<String, Tuple2<String, Iterable<String>>> itemsStream = caliperEvents.mapToPair(new PairFunction<Tuple2<String,String>, String, Tuple2<String, Iterable<String>>>() {
            public Tuple2<String, Tuple2<String, Iterable<String>>> call(Tuple2<String, String> msg) throws Exception {
                BaseEventContext event = getCaliperModel(msg._2());

                // Filter AssessmentItemEvents
                if (EventType.ASSESSMENT_ITEM.getValue().equals(event.getType())) {
                    AssessmentItemEvent aiEvent = (AssessmentItemEvent) event;
                    AssessmentItem ai = (AssessmentItem) aiEvent.getObject();
                    Response response = (Response) aiEvent.getGenerated();

                    // key: {attemptId}-{itemId}
                    String key = response.getAttempt().getId() + "-" + ai.getId();

                    List<String> learningObjectivesIds = new ArrayList<String>();
                    for (LearningObjective lo : ai.getLearningObjectives()) {
                        learningObjectivesIds.add(lo.getId());
                    }

                    return new Tuple2<String, Tuple2<String, Iterable<String>>>(
                            key,
                            new Tuple2<String, Iterable<String>>(
                                    aiEvent.getActor().getId(), // Student
                                    learningObjectivesIds // LOs
                            )
                    );
                }

                return null;
            }
        });

        // results -- (key, (usr, (obtained, total)))
        JavaPairDStream<String, Tuple2<String, Tuple2<Double, Double>>> resultsStream = caliperEvents.mapToPair(new PairFunction<Tuple2<String, String>, String, Tuple2<String, Tuple2<Double, Double>>>() {
            public Tuple2<String, Tuple2<String, Tuple2<Double, Double>>> call(Tuple2<String, String> msg) throws Exception {
                BaseEventContext event = getCaliperModel(msg._2());

                // Filter OutcomeEvents
                if (EventType.OUTCOME.getValue().equals(event.getType())) {
                    OutcomeEvent oEvent = (OutcomeEvent) event;
                    Attempt attempt = (Attempt) oEvent.getObject();
                    AssessmentItem ai = (AssessmentItem) oEvent.getTarget();
                    Result result = (Result) oEvent.getGenerated();

                    // key: {attemptId}-{itemId}
                    String key = attempt.getId() + "-" + ai.getId();

                    return new Tuple2<String, Tuple2<String, Tuple2<Double, Double>>>(
                            key,
                            new Tuple2<String, Tuple2<Double, Double>>(
                                    oEvent.getActor().getId(), // Student
                                    new Tuple2<Double, Double>( // Performance
                                            result.getNormalScore(), // Obtained
                                            result.getTotalScore() // Total
                                    )
                            )
                    );
                }
                return null;
            }
        });

        // joined -- (key, ( (usr, [LOs]), (usr, (obtained, total)) ))
        JavaPairDStream<String, Tuple2<Tuple2<String, Iterable<String>>, Tuple2<String, Tuple2<Double, Double>>>> joined = itemsStream.join(resultsStream);

        // result -- ((usr, loId), (obtained, total))
        JavaPairDStream<Tuple2<String, String>, Tuple2<Double, Double>> resultFlatten = joined.flatMapToPair(new PairFlatMapFunction<Tuple2<String, Tuple2<Tuple2<String, Iterable<String>>, Tuple2<String, Tuple2<Double, Double>>>>, Tuple2<String, String>, Tuple2<Double, Double>>() {
            public Iterable<Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>> call(Tuple2<String, Tuple2<Tuple2<String, Iterable<String>>, Tuple2<String, Tuple2<Double, Double>>>> tuple) throws Exception {
                List<Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>> out = new ArrayList<Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>>();

                String user = tuple._2()._1()._1();
                Tuple2<Double, Double> performance = tuple._2()._2()._2();

                for (String loId : tuple._2()._1()._2()) {
                    out.add(
                            new Tuple2<Tuple2<String, String>, Tuple2<Double, Double>>(
                                    new Tuple2<String, String>(user, loId), // (Student, LO)
                                    performance // (Obtained, Total)
                            )
                    );
                }

                return out;
            }
        });

        // result -- ((usr, loId), (obtained, total))
        JavaPairDStream<Tuple2<String, String>, Tuple2<Double, Double>> result = resultFlatten.reduceByKey(new Function2<Tuple2<Double, Double>, Tuple2<Double, Double>, Tuple2<Double, Double>>() {
            public Tuple2<Double, Double> call(Tuple2<Double, Double> value1, Tuple2<Double, Double> value2) throws Exception {
                return new Tuple2<Double, Double>(
                        value1._1() + value2._1(), // Total obtained
                        value1._2() + value2._2() // Accumulated total
                );
            }
        });

        result.print();

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
