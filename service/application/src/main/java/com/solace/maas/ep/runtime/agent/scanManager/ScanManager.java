package com.solace.maas.ep.runtime.agent.scanManager;

import com.solace.maas.ep.runtime.agent.repository.model.mesagingservice.MessagingServiceEntity;
import com.solace.maas.ep.runtime.agent.scanManager.model.ScanRequestBO;
import com.solace.maas.ep.runtime.agent.service.MessagingServiceDelegateServiceImpl;
import com.solace.maas.ep.runtime.agent.service.ScanService;
import com.solace.maas.ep.runtime.agent.plugin.manager.loader.PluginLoader;
import com.solace.maas.ep.runtime.agent.plugin.route.RouteBundle;
import com.solace.maas.ep.runtime.agent.plugin.route.handler.base.MessagingServiceRouteDelegate;
import com.solace.maas.ep.runtime.agent.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanManager {

    private final MessagingServiceDelegateServiceImpl messagingServiceDelegateService;
    private final ScanService scanService;
    private final SchedulerService schedulerService;


    @Autowired
    public ScanManager(MessagingServiceDelegateServiceImpl messagingServiceDelegateService,
                       ScanService scanService, SchedulerService schedulerService) {
        this.messagingServiceDelegateService = messagingServiceDelegateService;
        this.scanService = scanService;
        this.schedulerService = schedulerService;
    }

    public String scan(ScanRequestBO scanRequestBO) {
        List<RouteBundle> routes = retrieveRouteBundle(scanRequestBO);

        return scanService.singleScan(scanRequestBO.getMessagingServiceId(), routes, routes.size());
    }

    public String scheduleScan(ScanRequestBO scanRequestBO, String expression) throws Exception {
        List<RouteBundle> routes = retrieveRouteBundle(scanRequestBO);

        return schedulerService.scheduleScan(scanRequestBO.getMessagingServiceId(), routes, routes.size(), expression);
    }

    public void stopScheduler(String schedulerId) throws Exception {
        schedulerService.stopScheduler(schedulerId);
    }

    private List<RouteBundle> retrieveRouteBundle(ScanRequestBO scanRequestBO) {
        String messagingServiceId = scanRequestBO.getMessagingServiceId();

        MessagingServiceEntity messagingServiceEntity = retrieveMessagingServiceEntity(messagingServiceId);

        MessagingServiceRouteDelegate scanDelegate =
                PluginLoader.findPlugin(messagingServiceEntity.getMessagingServiceType().name());

        List<String> scanDestinations = Objects.requireNonNullElseGet(scanRequestBO.getDestinations(), List::of);

        List<RouteBundle> destinations = scanDestinations.stream()
                .map(scanDestination -> {
                    MessagingServiceRouteDelegate delegate = PluginLoader.findPlugin(scanDestination);
                    return delegate.generateRouteList(
                                    List.of(),
                                    List.of(),
                                    Objects.requireNonNull(
                                                    scanRequestBO.getEntityTypes()
                                            ).stream()
                                            .findFirst()
                                            .orElseThrow(),
                                    messagingServiceId
                            ).stream()
                            .findFirst()
                            .orElseThrow();
                }).collect(Collectors.toUnmodifiableList());

        List<String> brokerScanTypes = Objects.requireNonNull(scanRequestBO.getEntityTypes());
        return brokerScanTypes.stream()
                .distinct()
                .flatMap(brokerScanType -> scanDelegate.generateRouteList(destinations, List.of(),
                                brokerScanType, messagingServiceId)
                        .stream())
                .collect(Collectors.toUnmodifiableList());
    }

    private MessagingServiceEntity retrieveMessagingServiceEntity(String messagingServiceId) {
        return messagingServiceDelegateService.getMessagingServiceById(messagingServiceId);
    }
}
