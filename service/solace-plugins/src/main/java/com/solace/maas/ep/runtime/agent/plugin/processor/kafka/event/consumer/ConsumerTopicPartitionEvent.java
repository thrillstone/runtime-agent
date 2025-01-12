package com.solace.maas.ep.runtime.agent.plugin.processor.kafka.event.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConsumerTopicPartitionEvent {
    private Integer partition;

    private String topic;
}
