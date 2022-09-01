package com.solace.maas.ep.runtime.agent.repository.model.scan;

import com.solace.maas.ep.runtime.agent.repository.model.file.DataCollectionFileEntity;
import com.solace.maas.ep.runtime.agent.repository.model.mesagingservice.MessagingServiceEntity;
import com.solace.maas.ep.runtime.agent.repository.model.route.RouteEntity;
import com.solace.maas.ep.runtime.agent.repository.model.scheduler.SchedulerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "SCAN")
@Entity
public class ScanEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ID")
    private String id;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @Column(name = "SCAN_TYPE", nullable = false)
    private String scanType;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGING_SERVICE_ID", referencedColumnName = "ID", nullable = false)
    private MessagingServiceEntity messagingService;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROUTE_ID", referencedColumnName = "ROUTE_ID")
    private List<RouteEntity> route;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<ScanDestinationEntity> destinations;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<ScanRecipientEntity> recipients;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<DataCollectionFileEntity> dataCollectionFiles;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULER_ID", referencedColumnName = "ID")
    private SchedulerEntity scheduler;

    public String toString() {
        return "ScanEntity " + id;
    }

}
