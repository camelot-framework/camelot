package ru.yandex.qatools.camelot;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringDecoder;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.core.kafka.KafkaPluginUriBuilder;
import ru.yandex.qatools.camelot.core.service.EmbeddedKafkaCluster;
import ru.yandex.qatools.camelot.core.service.EmbeddedZookeeper;

import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:test-camelot-kafka-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
@MockEndpoints("*")
public class BasicKafkaRoutesTest implements CamelContextAware {

    public static final String TOPIC = "test";
    CamelContext camelContext;
    @Autowired
    EmbeddedKafkaCluster kafka;
    @Autowired
    EmbeddedZookeeper zookeeper;
    @Autowired
    KafkaPluginUriBuilder uriBuilder;

    @EndpointInject(uri = "mock:middle")
    private MockEndpoint middle;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint to;
    private Endpoint from;

    private Producer<String, String> producer;

    @Before
    public void before() {
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:" + kafka.getPort());
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "kafka.producer.DefaultPartitioner");

        ProducerConfig config = new ProducerConfig(props);
        producer = new Producer<>(config);
    }

    @After
    public void after() {
        producer.close();
    }

    @Test
    public void testMessageIsConsumedByCamel() throws InterruptedException, IOException {
        middle.expectedMessageCount(5);
        middle.expectedBodiesReceivedInAnyOrder("message-0", "message-1", "message-2", "message-3", "message-4");
        to.expectedMessageCount(5);
        to.expectedBodiesReceivedInAnyOrder("message-0-P", "message-1-P", "message-2-P", "message-3-P", "message-4-P");

        for (int k = 0; k < 5; k++) {
            String msg = "message-" + k;
            KeyedMessage<String, String> data = new KeyedMessage<>(TOPIC, "1", msg);
            producer.send(data);
        }
        middle.assertIsSatisfied(3000);
        to.assertIsSatisfied(3000);
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        from = camelContext.getEndpoint(format("%s&topic=%s&groupId=group1", uriBuilder.kafkaBaseUri(), TOPIC));
        try {
            camelContext.addRoutes(new RouteBuilder() {

                @Override
                public void configure() throws Exception {
                    from(from).to(middle).process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            exchange.getIn().setBody(
                                    new StringDecoder(null).fromBytes((byte[]) exchange.getIn().getBody()) + "-P"
                            );
                        }
                    }).to(to);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
