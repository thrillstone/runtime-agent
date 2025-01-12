package com.solace.maas.ep.runtime.agent.plugin.route.handler.delegate;

import com.solace.maas.ep.runtime.agent.TestConfig;
import com.solace.maas.ep.runtime.agent.plugin.route.RouteBundle;
import com.solace.maas.ep.runtime.agent.plugin.route.delegate.SolaceRouteDelegateImpl;
import com.solace.maas.ep.runtime.agent.plugin.route.enumeration.SolaceScanType;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ActiveProfiles("TEST")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
public class SolaceRouteDelegateImplTests {
    @InjectMocks
    private SolaceRouteDelegateImpl solaceRouteDelegate;

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
    public void testGenerateSolaceQueueListingRouteList() {
        List<RouteBundle> routeBundles =
                solaceRouteDelegate.generateRouteList(destinations, List.of(), SolaceScanType.SOLACE_QUEUE_LISTING.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateSolaceQueuenConfigRouteList() {
        List<RouteBundle> routeBundles =
                solaceRouteDelegate.generateRouteList(destinations, List.of(), SolaceScanType.SOLACE_QUEUE_CONFIG.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateSolaceSubscriptionConfigRouteList() {
        List<RouteBundle> routeBundles =
                solaceRouteDelegate.generateRouteList(destinations, List.of(), SolaceScanType.SOLACE_SUBSCRIPTION_CONFIG.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }

    @Test
    public void testGenerateSolaceAllRouteList() {
        List<RouteBundle> routeBundles =
                solaceRouteDelegate.generateRouteList(destinations, List.of(), SolaceScanType.SOLACE_ALL.name(),
                        "service1");

        assertThatNoException();
        assertThat(!routeBundles.isEmpty());
    }
}
