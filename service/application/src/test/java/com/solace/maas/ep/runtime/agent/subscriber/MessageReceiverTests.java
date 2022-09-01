package com.solace.maas.ep.runtime.agent.subscriber;

import com.solace.maas.ep.runtime.agent.TestConfig;
import com.solace.maas.ep.runtime.agent.config.SolaceConfiguration;
import com.solace.maas.ep.runtime.agent.scanManager.ScanManager;
import com.solace.maas.ep.runtime.agent.scanManager.mapper.ScanRequestMapper;
import com.solace.maas.ep.runtime.agent.plugin.mop.MOPConstants;
import com.solace.messaging.receiver.InboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("TEST")
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
@Slf4j
public class MessageReceiverTests {

    @Mock
    SolaceConfiguration solaceConfiguration;

    @Mock
    ScanManager scanManager;

    @Mock
    SolaceSubscriber solaceSubscriber;

    @Mock
    InboundMessage inboundMessage;

    @Autowired
    ScanRequestMapper scanRequestMapper;

    @Test
    public void scanReceiver() {

        String payload = "{\n" +
                "  \"mopVer\" : \"1\",\n" +
                "  \"mopProtocol\" : \"event\",\n" +
                "  \"mopMsgType\" : \"generic\",\n" +
                "  \"msgUh\" : \"ignore\",\n" +
                "  \"repeat\" : false,\n" +
                "  \"isReplyMessage\" : false,\n" +
                "  \"msgPriority\" : 4,\n" +
                "  \"traceId\" : \"80817f0d335b6221\",\n" +
                "  \"scanRequest\" : {\n" +
                "    \"scanType\" : \"one-time\",\n" +
                "    \"entityTypes\" : [ \"queues\" ]\n" +
                "  },\n" +
                "  \"messagingServiceId\" : \"someId\"\n" +
                "}";

        when(inboundMessage.getPayloadAsString()).thenReturn(payload);
        when(inboundMessage.getProperty(MOPConstants.MOP_MSG_META_DECODER)).thenReturn(
                "com.solace.maas.ep.common.messages.ScanCommandMessage");
        when(inboundMessage.getDestinationName()).thenReturn("anyTopic");

        ScanCommandMessageHandler scanCommandMessageHandler = new ScanCommandMessageHandler(solaceConfiguration,
                solaceSubscriber, scanManager, scanRequestMapper);
        String topic = scanCommandMessageHandler.getTopicString();
        log.info("topic: {}", topic);
        scanCommandMessageHandler.onMessage(inboundMessage);
    }

    @Test
    public void testBadClass() {

        Exception e = assertThrows(RuntimeException.class, () -> {
            when(inboundMessage.getProperty(MOPConstants.MOP_MSG_META_DECODER)).thenReturn("badClass");
            ScanCommandMessageHandler scanCommandMessageHandler = new ScanCommandMessageHandler(solaceConfiguration,
                    solaceSubscriber, scanManager, scanRequestMapper);
            scanCommandMessageHandler.onMessage(inboundMessage);
        });
    }

    @Test
    public void heartbeatReceiverTest() {
        String payload = "{\n" +
                "  \"mopVer\" : \"1\",\n" +
                "  \"mopProtocol\" : \"event\",\n" +
                "  \"mopMsgType\" : \"generic\",\n" +
                "  \"msgUh\" : \"ignore\",\n" +
                "  \"repeat\" : false,\n" +
                "  \"isReplyMessage\" : false,\n" +
                "  \"msgPriority\" : 4,\n" +
                "  \"traceId\" : \"80817f0d335b6221\",\n" +
                "  \"runtimeAgentId\" : \"someId\",\n" +
                "  \"timestamp\" : \"2022-07-21T20:16:21.982427Z\"\n" +
                "}";

        when(inboundMessage.getPayloadAsString()).thenReturn(payload);
        when(inboundMessage.getProperty(MOPConstants.MOP_MSG_META_DECODER)).thenReturn(
                "com.solace.maas.ep.common.messages.HeartbeatMessage");
        when(inboundMessage.getDestinationName()).thenReturn("anyTopic");

        HeartbeatMessageHandler heartbeatMessageHandler = new HeartbeatMessageHandler(solaceConfiguration,
                solaceSubscriber);
        heartbeatMessageHandler.onMessage(inboundMessage);

    }
}
