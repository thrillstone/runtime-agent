package com.solace.maas.ep.runtime.agent.plugin.processor.solace;

import com.solace.maas.ep.runtime.agent.plugin.processor.solace.semp.SolaceHttpSemp;
import com.solace.maas.ep.runtime.agent.plugin.constants.RouteConstants;
import com.solace.maas.ep.runtime.agent.plugin.processor.base.ResultProcessorImpl;
import com.solace.maas.ep.runtime.agent.plugin.processor.solace.event.SolaceQueueConfigurationEvent;
import com.solace.maas.ep.runtime.agent.plugin.service.MessagingServiceDelegateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SolaceQueueConfigurationProcessor extends ResultProcessorImpl<List<SolaceQueueConfigurationEvent>, Object> {
    private final MessagingServiceDelegateService messagingServiceDelegateService;

    @Autowired
    public SolaceQueueConfigurationProcessor(MessagingServiceDelegateService messagingServiceDelegateService) {
        super();
        this.messagingServiceDelegateService = messagingServiceDelegateService;
    }

    @Override
    @SuppressWarnings("PMD")
    public List<SolaceQueueConfigurationEvent> handleEvent(Map<String, Object> properties, Object body) throws Exception {
        String messagingServiceId = (String) properties.get(RouteConstants.MESSAGING_SERVICE_ID);

        SolaceHttpSemp sempClient = messagingServiceDelegateService.getMessagingServiceClient(messagingServiceId);

        List<Map<String, Object>> queueResponse = sempClient.getQueues();

        return queueResponse.stream()
                .map(qConfig -> SolaceQueueConfigurationEvent.builder()
                        .name(qConfig.get("queueName").toString())
                        .configuration(qConfig)
                        .build())
                .sorted(Comparator.comparing(SolaceQueueConfigurationEvent::getName))
                .collect(Collectors.toUnmodifiableList());
    }
}