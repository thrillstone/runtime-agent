package com.solace.maas.ep.runtime.agent.plugin.processor.solace;

import com.solace.maas.ep.runtime.agent.plugin.constants.RouteConstants;
import com.solace.maas.ep.runtime.agent.plugin.processor.base.ResultProcessorImpl;
import com.solace.maas.ep.runtime.agent.plugin.processor.solace.semp.SolaceHttpSemp;
import com.solace.maas.ep.runtime.agent.plugin.service.MessagingServiceDelegateService;
import com.solace.messaging.MessagingService;
import com.solace.messaging.config.SolaceProperties;
import com.solace.messaging.config.profile.ConfigurationProfile;
import com.solace.messaging.receiver.DirectMessageReceiver;
import com.solace.messaging.receiver.MessageReceiver;
import com.solace.messaging.resources.TopicSubscription;
import com.solace.messaging.util.CompletionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
public class SolaceTopicCollectionProcessor extends ResultProcessorImpl<List<String>, Void> {
    private final MessagingServiceDelegateService messagingServiceDelegateService;

    private static final String SAMPLE_NAME = SolaceTopicCollectionProcessor.class.getSimpleName();
    private static final String TOPIC_PREFIX = "solace/samples/";  // used as the topic "root"
    private static final String API = "Java";

    private static volatile int msgRecvCounter = 0;                   // num messages sent
    private static volatile boolean hasDetectedDiscard = false;  // detected any discards yet?
    private static volatile boolean isShutdown = false;          // are we done yet?

    @Autowired
    public SolaceTopicCollectionProcessor(MessagingServiceDelegateService messagingServiceDelegateService) {
        super();
        this.messagingServiceDelegateService = messagingServiceDelegateService;
    }

    @Override
    @SuppressWarnings("PMD")
    public List<String> handleEvent(Map<String, Object> properties, Void body) throws Exception {
        String messagingServiceId = (String) properties.get(RouteConstants.MESSAGING_SERVICE_ID);

        return List.of();
    }

    public static void main(String[] args) {
        List<String> collectedTopics = new ArrayList<>();
        // Collection duration
        long COLLECTION_DURATION_SECONDS = 10l;

        final MessagingService messagingService = createMessagingService();
        configureMessagingService(messagingService);

        final DirectMessageReceiver receiver = createAndStartMessageReceiver(messagingService);

        receiver.setReceiveFailureListener(failedReceiveEvent -> {
            System.out.println("### FAILED RECEIVE EVENT " + failedReceiveEvent);
        });

        createMessageHandler(receiver, collectedTopics);

        long endTime = System.currentTimeMillis() + COLLECTION_DURATION_SECONDS * 1000;
        System.out.println(API + " " + SAMPLE_NAME + " connected, and running. Press [ENTER] to quit.");
        try {
            while (System.currentTimeMillis() < endTime && !isShutdown) {
                Thread.sleep(1000);  // wait 1 second
                System.out.printf("Received msgs/s: %,d%n",msgRecvCounter);  // simple way of calculating message rates
                msgRecvCounter = 0;
                if (hasDetectedDiscard) {
                    System.out.println("*** Egress discard detected *** : "
                            + SAMPLE_NAME + " unable to keep up with full message rate");
                    hasDetectedDiscard = false;  // only show the error once per second
                }
            }
        } catch (InterruptedException e) {
            // Thread.sleep() interrupted... probably getting shut down
        }
        isShutdown = true;
        receiver.terminate(500);
        messagingService.disconnect();
        System.out.println("Main thread quitting.");
        collectedTopics.stream().forEach(
                topic -> System.out.println(topic)
        );
    }

    private static void createMessageHandler(DirectMessageReceiver receiver, List<String> collectedTopics) {
        final MessageReceiver.MessageHandler messageHandler = (inboundMessage) -> {
            collectedTopics.add(inboundMessage.getDestinationName());
            // do not print anything to console... too slow!
            msgRecvCounter++;
            // since Direct messages, check if there have been any lost any messages
            if (inboundMessage.getMessageDiscardNotification().hasBrokerDiscardIndication() ||
                    inboundMessage.getMessageDiscardNotification().hasInternalDiscardIndication()) {
                // If the consumer is being over-driven (i.e. publish rates too high), the broker might discard some messages for this consumer
                // check this flag to know if that's happened
                // to avoid discards:
                //  a) reduce publish rate
                //  b) use multiple-threads or shared subscriptions for parallel processing
                //  c) increase size of consumer's D-1 egress buffers (check client-profile) (helps more with bursts)
                hasDetectedDiscard = true;  // set my own flag
            }
        };
        receiver.receiveAsync(messageHandler);
    }

    private static DirectMessageReceiver createAndStartMessageReceiver(MessagingService messagingService) {
        // build the Direct receiver object
        final DirectMessageReceiver receiver = messagingService.createDirectMessageReceiverBuilder()
                .withSubscriptions(TopicSubscription.of(">"))
                // add more subscriptions here if you want
                .build();
        receiver.start();
        return receiver;
    }

    private static void configureMessagingService(MessagingService messagingService) {
        messagingService.connect();  // blocking connect to the broker
        messagingService.addServiceInterruptionListener(serviceEvent -> {
            System.out.println("### SERVICE INTERRUPTION: "+serviceEvent.getCause());
            isShutdown = true;
        });
        messagingService.addReconnectionAttemptListener(serviceEvent -> {
            System.out.println("### RECONNECTING ATTEMPT: "+serviceEvent);
        });
        messagingService.addReconnectionListener(serviceEvent -> {
            System.out.println("### RECONNECTED: "+serviceEvent);
        });
    }

    private static MessagingService createMessagingService() {
        final Properties properties = new Properties();
        properties.setProperty(SolaceProperties.TransportLayerProperties.HOST, "tcps://mroyppj81pus7.messaging.solace.cloud:55443");          // host:port
        properties.setProperty(SolaceProperties.ServiceProperties.VPN_NAME,  "greg-s-ivmr");     // message-vpn
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_USER_NAME, "solace-cloud-client");      // client-username
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_PASSWORD, "c0bl3i0n946pml6p69rv483e98");  // client-password
        properties.setProperty(SolaceProperties.ServiceProperties.RECEIVER_DIRECT_SUBSCRIPTION_REAPPLY, "true");  // subscribe Direct subs after reconnect
        properties.setProperty(SolaceProperties.TransportLayerProperties.RECONNECTION_ATTEMPTS, "20");  // recommended settings
        properties.setProperty(SolaceProperties.TransportLayerProperties.CONNECTION_RETRIES_PER_HOST, "5");
        // https://docs.solace.com/Solace-PubSub-Messaging-APIs/API-Developer-Guide/Configuring-Connection-T.htm

        final MessagingService messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(properties)
                .build();
        return messagingService;
    }
}
