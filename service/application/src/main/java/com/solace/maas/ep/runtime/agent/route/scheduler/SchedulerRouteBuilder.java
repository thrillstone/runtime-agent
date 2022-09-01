package com.solace.maas.ep.runtime.agent.route.scheduler;

import com.solace.maas.ep.runtime.agent.plugin.route.RouteBundle;
import com.solace.maas.ep.runtime.agent.service.ScanService;
import com.solace.maas.ep.runtime.agent.service.SchedulerService;
import org.apache.camel.builder.RouteBuilder;

import java.util.List;

public class SchedulerRouteBuilder extends RouteBuilder {
    private final String routeId;

    private String messagingServiceId;

    private final String schedulerId;

    private final List<RouteBundle> routeBundles;

    private final int numExpectedCompletionMessages;

    private final Long interval;

    private final Long repeatCount;

    public SchedulerRouteBuilder(String routeId, String messagingServiceId, String schedulerId,
                                 List<RouteBundle> routeBundles, int numExpectedCompletionMessages, Long interval,
                                 Long repeatCount) {
        this.routeId = routeId;
        this.messagingServiceId = messagingServiceId;
        this.schedulerId = schedulerId;
        this.routeBundles = routeBundles;
        this.numExpectedCompletionMessages = numExpectedCompletionMessages;
        this.interval = interval;
        this.repeatCount = repeatCount;
    }

    @Override
    public void configure() throws Exception {
        from("quartz://scan/" + routeId + "?trigger.repeatInterval=" + interval + "&trigger.repeatCount="
                + repeatCount)
                .routeId(routeId)
                .setHeader("ROUTE_ID", constant(routeId))
                .setHeader("MESSAGING_SERVICE_ID", constant(messagingServiceId))
                .setHeader("SCHEDULER_ID", constant(schedulerId))
                .setHeader("NUM_EXPECTED_COMPLETION_MESSAGES", constant(numExpectedCompletionMessages))
                .setBody(constant(routeBundles))
                .bean(ScanService.class, "performScan(${header.MESSAGING_SERVICE_ID}, ${body}, " +
                        "${header.NUM_EXPECTED_COMPLETION_MESSAGES}, ${header.SCHEDULER_ID})")
                .bean(SchedulerService.class, "updateExecutionTime(${header.SCHEDULER_ID})");
    }
}
