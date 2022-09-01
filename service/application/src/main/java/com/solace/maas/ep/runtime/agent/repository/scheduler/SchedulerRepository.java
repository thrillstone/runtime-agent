package com.solace.maas.ep.runtime.agent.repository.scheduler;

import com.solace.maas.ep.runtime.agent.repository.model.scheduler.SchedulerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerRepository extends CrudRepository<SchedulerEntity, String> {
}
