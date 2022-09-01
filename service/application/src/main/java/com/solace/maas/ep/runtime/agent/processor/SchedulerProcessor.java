package com.solace.maas.ep.runtime.agent.processor;

import com.solace.maas.ep.runtime.agent.plugin.route.RouteBundle;
import com.solace.maas.ep.runtime.agent.service.ScanService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.List;

//@Component
public class SchedulerProcessor implements Processor {
    private final ScanService scanService;

    public SchedulerProcessor(ScanService scanService) {
        this.scanService = scanService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String schedulerId = exchange.getIn().getHeader("SCHEDULER_ID", String.class);
        Integer numExpectedCompletionMessages =
                exchange.getIn().getHeader("NUM_EXPECTED_COMPLETION_MESSAGES", Integer.class);
        @SuppressWarnings("unchecked")
        List<RouteBundle> routeBundles = exchange.getIn().getBody(List.class);

        scanService.performScan(routeBundles, numExpectedCompletionMessages, schedulerId);
    }
}
