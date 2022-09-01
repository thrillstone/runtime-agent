package com.solace.maas.ep.runtime.agent.repository.model.scheduler;

import com.solace.maas.ep.runtime.agent.repository.model.scan.ScanEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "SCHEDULER")
@Entity
public class SchedulerEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ID")
    private String id;

    @Column(name = "ROUTE_ID", nullable = false)
    private String routeId;

    @Column(name = "CRON_EXPRESSION", nullable = false)
    private String expression;

    @OneToMany(mappedBy = "scheduler", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<ScanEntity> scans;

    @OneToMany(mappedBy = "scheduler", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<SchedulerExecutionEntity> executions;
}
