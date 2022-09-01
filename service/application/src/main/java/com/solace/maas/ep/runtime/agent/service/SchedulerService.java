package com.solace.maas.ep.runtime.agent.service;

import com.solace.maas.ep.runtime.agent.repository.model.scheduler.SchedulerExecutionEntity;
import com.solace.maas.ep.runtime.agent.repository.scheduler.SchedulerExecutionRepository;
import com.solace.maas.ep.runtime.agent.route.scheduler.CronRouteBuilder;
import com.solace.maas.ep.runtime.agent.route.scheduler.SchedulerRouteBuilder;
import com.solace.maas.ep.runtime.agent.plugin.route.RouteBundle;
import com.solace.maas.ep.runtime.agent.repository.model.scheduler.SchedulerEntity;
import com.solace.maas.ep.runtime.agent.repository.scheduler.SchedulerRepository;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SchedulerService {
    private final SchedulerRepository repository;

    private final SchedulerExecutionRepository executionRepository;

    private final CamelContext camelContext;

    public SchedulerService(SchedulerRepository repository, SchedulerExecutionRepository executionRepository,
                            CamelContext camelContext) {
        this.repository = repository;
        this.executionRepository = executionRepository;
        this.camelContext = camelContext;
    }

    public String scheduleScan(String messagingServiceId, List<RouteBundle> routeBundles,
                               int numExpectedCompletionMessages, String expression) throws Exception {
        return createCron(messagingServiceId, routeBundles, numExpectedCompletionMessages, expression);
//        return create(messagingServiceId, routeBundles, numExpectedCompletionMessages, interval, repeatCount);
    }

    public String create(String messagingServiceId, List<RouteBundle> routeBundles, int numExpectedCompletionMessages,
                         String expression) throws Exception {
        String routeId = UUID.randomUUID().toString();

        SchedulerEntity savedSchedulerEntity = createScheduledScan(routeId, expression);

        SchedulerRouteBuilder routeBuilder =
                new SchedulerRouteBuilder(routeId, messagingServiceId, savedSchedulerEntity.getId(), routeBundles,
                        numExpectedCompletionMessages, 100L, 1L);

        camelContext.addRoutes(routeBuilder);

        return savedSchedulerEntity.getId();
    }

    public String createCron(String messagingServiceId, List<RouteBundle> routeBundles,
                             int numExpectedCompletionMessages, String expression) throws Exception {
        String routeId = UUID.randomUUID().toString();

        SchedulerEntity savedSchedulerEntity = createScheduledScan(routeId, expression);

        CronRouteBuilder routeBuilder =
                new CronRouteBuilder(routeId, messagingServiceId, savedSchedulerEntity.getId(), routeBundles,
                        expression, numExpectedCompletionMessages);

        camelContext.addRoutes(routeBuilder);

        return savedSchedulerEntity.getId();
    }

    private SchedulerEntity createScheduledScan(String routeId, String expression) {
        SchedulerEntity entity = SchedulerEntity.builder()
                .id(UUID.randomUUID().toString())
                .routeId(routeId)
                .expression(expression)
                .build();

        return repository.save(entity);
    }

    public SchedulerEntity findById(String id) {
        return repository.findById(id)
                .orElseThrow();
    }

    public SchedulerExecutionEntity updateExecutionTime(String schedulerId, Long time) {
        SchedulerEntity entity = findById(schedulerId);

        return updateExecutionTime(entity, time);
    }

    public SchedulerExecutionEntity updateExecutionTime(String schedulerId) {
        SchedulerEntity entity = findById(schedulerId);

        return updateExecutionTime(entity, Instant.now().getEpochSecond());
    }

    public SchedulerExecutionEntity updateExecutionTime(SchedulerEntity scheduler, Long time) {
        SchedulerExecutionEntity executionEntity = SchedulerExecutionEntity.builder()
                .scheduler(scheduler)
                .createdTime(time)
                .build();

        return executionRepository.save(executionEntity);
    }

    public void stopScheduler(String groupId) throws Exception {
        SchedulerEntity entity = repository.findById(groupId)
                .orElseThrow();

        String routeId = entity.getRouteId();

        camelContext.getRouteController().stopRoute(routeId);
        camelContext.removeRoute(routeId);
    }
}
