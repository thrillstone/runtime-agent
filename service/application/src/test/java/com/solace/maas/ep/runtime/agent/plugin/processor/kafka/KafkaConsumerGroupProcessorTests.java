package com.solace.maas.ep.runtime.agent.plugin.processor.kafka;

import com.solace.maas.ep.runtime.agent.TestConfig;
import com.solace.maas.ep.runtime.agent.plugin.processor.kafka.consumer.KafkaConsumerGroupProcessor;
import com.solace.maas.ep.runtime.agent.service.MessagingServiceDelegateServiceImpl;
import com.solace.maas.ep.runtime.agent.plugin.constants.RouteConstants;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("TEST")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
public class KafkaConsumerGroupProcessorTests {

    @Mock
    private MessagingServiceDelegateServiceImpl messagingServiceDelegateService;

    @InjectMocks
    private KafkaConsumerGroupProcessor kafkaConsumerGroupProcessor;

    @SneakyThrows
    @Test
    public void testHandleEvent() {
        AdminClient adminClient = mock(AdminClient.class);
        ListConsumerGroupsResult listConsumerGroupsResult = mock(ListConsumerGroupsResult.class);
        ConsumerGroupListing consumerGroupListings = mock(ConsumerGroupListing.class);
        KafkaFuture<Collection<ConsumerGroupListing>> future = mock(KafkaFuture.class);

        when(messagingServiceDelegateService.getMessagingServiceClient("messagingServiceId"))
                .thenReturn(adminClient);

        when(adminClient.listConsumerGroups())
                .thenReturn(listConsumerGroupsResult);

        when(listConsumerGroupsResult.all())
                .thenReturn(future);

        when(future.get(30, TimeUnit.SECONDS))
                .thenReturn(List.of(consumerGroupListings));

        kafkaConsumerGroupProcessor.handleEvent(Map.of(RouteConstants.MESSAGING_SERVICE_ID, "messagingServiceId"), null);

        adminClient.close();

        assertThatNoException();
    }
}
