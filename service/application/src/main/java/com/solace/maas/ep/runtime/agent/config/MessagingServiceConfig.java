package com.solace.maas.ep.runtime.agent.config;

import com.solace.maas.ep.runtime.agent.event.MessagingServiceEvent;
import com.solace.maas.ep.runtime.agent.plugin.config.ClientConnectionDetails;
import com.solace.maas.ep.runtime.agent.plugin.config.enumeration.MessagingServiceType;
import com.solace.maas.ep.runtime.agent.plugin.jacoco.ExcludeFromJacocoGeneratedReport;
import com.solace.maas.ep.runtime.agent.plugin.messagingService.event.ConnectionDetailsEvent;
import com.solace.maas.ep.runtime.agent.service.MessagingServiceDelegateServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ExcludeFromJacocoGeneratedReport
@Data
@PropertySource("classpath:application.yml")
@Configuration
@ConfigurationProperties(prefix = "plugins")
@Slf4j
@Profile("!TEST")
public class MessagingServiceConfig implements ApplicationRunner {
    private List<MessagingServicePluginProperties> messagingServices;
    private final MessagingServiceDelegateServiceImpl messagingServiceDelegateService;
    private final ClientConnectionDetails clientConnectionDetails;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Objects.nonNull(messagingServices)) {
            List<MessagingServiceEvent> messagingServiceEvents = messagingServices.stream()
                    .map(messagingService -> {
                        List<ConnectionDetailsEvent> connectionDetails = new ArrayList<>();

                        messagingService.getManagement().getConnections()
                                .forEach(messagingServiceConnection -> {
                                    ConnectionDetailsEvent connectionDetailsEvent = clientConnectionDetails.createConnectionDetails(
                                            messagingService.getId(), messagingServiceConnection, MessagingServiceType.SOLACE);

                                    connectionDetails.add(connectionDetailsEvent);
                                });

                        return MessagingServiceEvent.builder()
                                .id(messagingService.getId())
                                .name(messagingService.getName())
                                .messagingServiceType(messagingService.getType())
                                .connectionDetails(connectionDetails)
                                .build();
                    }).collect(Collectors.toUnmodifiableList());

            messagingServiceDelegateService.addMessagingServices(messagingServiceEvents)
                    .forEach(messagingServiceEntity -> log.info("Created Messaging Service: {} {} {}",
                            messagingServiceEntity.getId(), messagingServiceEntity.getName(),
                            messagingServiceEntity.getMessagingServiceType()));

        }
    }
}