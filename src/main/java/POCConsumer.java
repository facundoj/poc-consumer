import com.clearspring.analytics.util.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.codehaus.jackson.JsonProcessingException;
import org.imsglobal.caliper.entities.LearningObjective;
import org.imsglobal.caliper.entities.assessment.AssessmentItem;
import org.imsglobal.caliper.events.AssessmentItemEvent;
import org.imsglobal.caliper.events.BaseEventContext;
import org.imsglobal.caliper.events.EventType;
import org.k12.caliper.poc.parser.CaliperParser;
import org.k12.caliper.poc.parser.CaliperParserFactory;
import scala.Tuple2;

import java.io.IOException;
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

        caliperEvents.flatMap(new FlatMapFunction<Tuple2<String,String>, Tuple2<String,String>>() {
            public Iterable<Tuple2<String,String>> call(Tuple2<String, String> msg) throws Exception {
                BaseEventContext event = getCaliperModel(msg._2());

                if (EventType.ASSESSMENT_ITEM.getValue().equals(event.getType())) {
                    AssessmentItemEvent aiEvent = (AssessmentItemEvent) event;
                    AssessmentItem object = (AssessmentItem) aiEvent.getObject();
                    List<Tuple2<String,String>> output = Lists.newArrayList();

                    for (LearningObjective lo : object.getLearningObjectives()) {
                        output.add(new Tuple2<String, String>(object.getId(), lo.getId()));
                    }

                    return output;
                }

                return null;
            }
        });

        caliperEvents.print();

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
