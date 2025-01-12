package com.solace.maas.ep.runtime.agent.plugin.route.handler.delegate;

import com.solace.maas.ep.runtime.agent.TestConfig;
import com.solace.maas.ep.runtime.agent.plugin.route.RouteBundle;
import com.solace.maas.ep.runtime.agent.plugin.route.delegate.KafkaRouteDelegateImpl;
import com.solace.maas.ep.runtime.agent.plugin.route.enumeration.KafkaScanType;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ActiveProfiles("TEST")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
public class KafkaRouteDelegateImplTests {
    @InjectMocks
    private KafkaRouteDelegateImpl kafkaRouteDelegate;

    private List<RouteBundle> destinations = List.of(
            RouteBundle.builder()
                    .destinations(List.of())
                    .recipients(List.of())
                    .routeId("testRoute")
                    .firstRouteInChain(false)
                    .messagingServiceId("service1")
                    .build()
    );

    @Test
    public void testGenerateKafkaTopicListingRouteList() {
        List<RouteBundle> routeBundles =
                kafkaRouteDelegate.generateRouteList(destinations, List.of(), KafkaScanType.KAFKA_TOPIC_LISTING.name(),
                "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateKafkaTopicConfigurationRouteList() {
        List<RouteBundle> routeBundles =
                kafkaRouteDelegate.generateRouteList(destinations, List.of(), KafkaScanType.KAFKA_TOPIC_CONFIGURATION.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateKafkaTopicConfigurationFullRouteList() {
        List<RouteBundle> routeBundles =
                kafkaRouteDelegate.generateRouteList(destinations, List.of(), KafkaScanType.KAFKA_TOPIC_CONFIGURATION_FULL.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateKafkaConsumerGroupsRouteList() {
        List<RouteBundle> routeBundles =
                kafkaRouteDelegate.generateRouteList(destinations, List.of(), KafkaScanType.KAFKA_CONSUMER_GROUPS.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateKafkaConsumerGroupsConfigRouteList() {
        List<RouteBundle> routeBundles =
                kafkaRouteDelegate.generateRouteList(destinations, List.of(),
                        KafkaScanType.KAFKA_CONSUMER_GROUPS_CONFIGURATION.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateKafkaAllRouteList() {
        List<RouteBundle> routeBundles =
                kafkaRouteDelegate.generateRouteList(destinations, List.of(), KafkaScanType.KAFKA_ALL.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }
}
