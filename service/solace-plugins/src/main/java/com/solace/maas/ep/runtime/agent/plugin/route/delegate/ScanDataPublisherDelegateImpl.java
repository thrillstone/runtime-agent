package com.solace.maas.ep.runtime.agent.plugin.route.delegate;

import com.solace.maas.ep.runtime.agent.plugin.route.RouteBundle;
import com.solace.maas.ep.runtime.agent.plugin.route.delegate.base.MessagingServiceRouteDelegateImpl;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScanDataPublisherDelegateImpl extends MessagingServiceRouteDelegateImpl {
    public ScanDataPublisherDelegateImpl() {
        super("EVENT_PORTAL");
    }

    @Override
    public List<RouteBundle> generateRouteList(List<RouteBundle> destinations, List<RouteBundle> recipients,
                                               String scanType, String messagingServiceId) {
        return List.of(createRouteBundle(destinations, recipients, scanType, messagingServiceId,
                "seda:eventPortal", false));
    }
}
