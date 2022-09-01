package com.solace.maas.ep.runtime.agent.config;

import com.solace.maas.ep.runtime.agent.config.eventPortal.EventPortalProperties;
import com.solace.maas.ep.runtime.agent.event.MessagingServiceEvent;
import com.solace.maas.ep.runtime.agent.plugin.config.ClientConnectionDetails;
import com.solace.maas.ep.runtime.agent.plugin.config.enumeration.MessagingServiceType;
import com.solace.maas.ep.runtime.agent.plugin.jacoco.ExcludeFromJacocoGeneratedReport;
import com.solace.maas.ep.runtime.agent.plugin.messagingService.MessagingServiceProperties;
import com.solace.maas.ep.runtime.agent.plugin.messagingService.event.ConnectionDetailsEvent;
import com.solace.maas.ep.runtime.agent.plugin.properties.KafkaProperties;
import com.solace.maas.ep.runtime.agent.plugin.properties.SolacePluginProperties;
import com.solace.maas.ep.runtime.agent.service.MessagingServiceDelegateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.stream.Collectors;


@ExcludeFromJacocoGeneratedReport
@Slf4j
@Configuration
@Profile("!TEST")
public class RuntimeAgentConfig implements ApplicationRunner {

    private final KafkaProperties kafkaProperties;
    private final SolacePluginProperties solaceProperties;
    private final MessagingServiceDelegateServiceImpl messagingServiceDelegateService;
    private final ClientConnectionDetails clientConnectionDetails;

    @Autowired
    public RuntimeAgentConfig(KafkaProperties kafkaProperties, SolacePluginProperties solaceProperties,
                              MessagingServiceDelegateServiceImpl messagingServiceDelegateService,
                              EventPortalProperties eventPortalProperties,
                              ClientConnectionDetails clientConnectionDetails) {
        this.kafkaProperties = kafkaProperties;
        this.solaceProperties = solaceProperties;
        this.messagingServiceDelegateService = messagingServiceDelegateService;
        this.clientConnectionDetails = clientConnectionDetails;
    }

    @Override
    public void run(ApplicationArguments args) {
        createKafkaMessagingServices();
        createSolaceMessagingServices();
    }

    private void createSolaceMessagingServices() {
        List<MessagingServiceProperties> solaceMessagingServices = solaceProperties.getMessagingServices();

        List<MessagingServiceEvent> messagingServiceEvents = solaceMessagingServices.stream()
                .map(solaceMessagingService -> {
                    List<ConnectionDetailsEvent> connectionDetails = solaceMessagingService.getManagement().getConnections()
                            .stream()
                            .map(solaceMessagingServiceConnection -> clientConnectionDetails.createConnectionDetails(
                                    solaceMessagingService.getId(), solaceMessagingServiceConnection, MessagingServiceType.SOLACE))
                            .collect(Collectors.toUnmodifiableList());

                    return MessagingServiceEvent.builder()
                            .id(solaceMessagingService.getId())
                            .name(solaceMessagingService.getName())
                            .messagingServiceType(MessagingServiceType.SOLACE)
                            .connectionDetails(connectionDetails)
                            .build();
                }).collect(Collectors.toUnmodifiableList());

        messagingServiceDelegateService.addMessagingServices(messagingServiceEvents);
    }

    private void createKafkaMessagingServices() {
        List<MessagingServiceProperties> kafkaMessagingServices = kafkaProperties.getMessagingServices();

        List<MessagingServiceEvent> messagingServiceEvents = kafkaMessagingServices.stream()
                .map(kafkaMessagingService -> {
                    List<ConnectionDetailsEvent> connectionDetails = kafkaMessagingService.getManagement().getConnections()
                            .stream()
                            .map(kafkaMessagingServiceConnection -> clientConnectionDetails.createConnectionDetails(
                                    kafkaMessagingService.getId(), kafkaMessagingServiceConnection, MessagingServiceType.KAFKA))
                            .collect(Collectors.toUnmodifiableList());

                    return MessagingServiceEvent.builder()
                            .id(kafkaMessagingService.getId())
                            .name(kafkaMessagingService.getName())
                            .messagingServiceType(MessagingServiceType.KAFKA)
                            .connectionDetails(connectionDetails)
                            .build();
                }).collect(Collectors.toUnmodifiableList());

        messagingServiceDelegateService.addMessagingServices(messagingServiceEvents);
    }

//    private void createKafkaMessagingServices() {
//        List<MessagingServiceProperties> kafkaMessagingServices = kafkaProperties.getMessagingServices();
//        List<ConnectionDetailsEvent> connectionDetails = new ArrayList<>();
//
//        kafkaMessagingServices.forEach(kafkaMessagingService -> {
//            kafkaMessagingService.getManagement().getConnections()
//                    .forEach(kafkaMessagingServiceConnection -> {
//                        ConnectionDetailsEvent connectionDetailsEvent =
//                                clientConnectionDetails
//                                        .createConnectionDetails(
//                                                kafkaMessagingService.getId(), kafkaMessagingServiceConnection, MessagingServiceType.KAFKA);
//
//                        connectionDetails.add(connectionDetailsEvent);
//                    });
//
//            MessagingServiceEvent messagingServiceEvent = MessagingServiceEvent.builder()
//                    .id(kafkaMessagingService.getId())
//                    .name(kafkaMessagingService.getName())
//                    .messagingServiceType(MessagingServiceType.KAFKA)
//                    .connectionDetails(connectionDetails)
//                    .build();
//
//            MessagingServiceEntity addedMessagingServiceEntity =
//                    messagingServiceDelegateService.addMessagingService(messagingServiceEvent);
//
//            log.info("Created Kafka messaging service: {} {}", kafkaMessagingService.getId(), addedMessagingServiceEntity.getName());
//        });
//    }
}
