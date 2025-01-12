package com.solace.maas.ep.runtime.agent.repository.model.file.aggregation;

import com.solace.maas.ep.runtime.agent.repository.model.file.DataCollectionFileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "AGGREGATED_FILE")
@Entity
public class AggregatedFileEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ID")
    private String id;

    @Column(name = "PATH", nullable = false, unique = true)
    private String path;

    @Column(name = "PURGED", nullable = false)
    private boolean purged;

    @ManyToMany(targetEntity = DataCollectionFileEntity.class, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<DataCollectionFileEntity> files;
}
