package com.solace.maas.ep.runtime.agent.service;

import com.solace.maas.ep.runtime.agent.TestConfig;
import com.solace.maas.ep.runtime.agent.repository.file.aggregation.AggregatedFileRepository;
import com.solace.maas.ep.runtime.agent.repository.model.file.DataCollectionFileEntity;
import com.solace.maas.ep.runtime.agent.repository.model.file.aggregation.AggregatedFileEntity;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("TEST")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
@SuppressWarnings("PMD")
public class AggregatedFileServiceTests {
    @Mock
    private AggregatedFileRepository repository;

    @Mock
    private ProducerTemplate producerTemplate;

    @InjectMocks
    private AggregatedFileService aggregatedFileService;

    @Test
    public void testSave() {
        when(repository.save(any(AggregatedFileEntity.class)))
                .thenReturn(
                        AggregatedFileEntity.builder()
                                .id(UUID.randomUUID().toString())
                                .files(List.of())
                                .path("/test/file.json")
                                .purged(false)
                                .build()
                );

        aggregatedFileService.save(AggregatedFileEntity.builder().build());

        assertThatNoException();
    }

    @Test
    public void testFindByPath() {
        when(repository.findByPath(any(String.class)))
                .thenReturn(Optional.empty());

        aggregatedFileService.findByPath("/test/file.json");

        assertThatNoException();
    }

    @Test
    public void testAggregate() {
        Exchange exchange = mock(Exchange.class);

        List<DataCollectionFileEntity> entities = List.of(
                DataCollectionFileEntity.builder()
                        .path("/test/file.json")
                        .purged(false)
                        .aggregatedFiles(
                            List.of(
                                    AggregatedFileEntity.builder()
                                            .id(UUID.randomUUID().toString())
                                            .files(List.of())
                                            .path("/test/aggregated.json")
                                            .purged(false)
                                            .build()
                            )
                        ).id(UUID.randomUUID().toString())
                        .build()
        );

        aggregatedFileService.aggregate(entities);

        assertThatNoException();
    }
}
