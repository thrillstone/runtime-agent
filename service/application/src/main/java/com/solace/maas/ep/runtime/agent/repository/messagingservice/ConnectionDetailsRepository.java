package com.solace.maas.ep.runtime.agent.repository.messagingservice;

import com.solace.maas.ep.runtime.agent.repository.model.mesagingservice.ConnectionDetailsEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectionDetailsRepository extends CrudRepository<ConnectionDetailsEntity, String> {
    Optional<ConnectionDetailsEntity> findConnectionDetailsEntityByMessagingServiceId(String messagingServiceId);
}
