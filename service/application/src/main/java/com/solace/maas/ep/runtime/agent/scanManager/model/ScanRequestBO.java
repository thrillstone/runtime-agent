package com.solace.maas.ep.runtime.agent.scanManager.model;

import com.solace.maas.ep.runtime.agent.model.AbstractBaseBO;
import com.solace.maas.ep.common.model.ScanType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD")
public class ScanRequestBO extends AbstractBaseBO<String> {

    private String messagingServiceId;

    private String scanId;

    private ScanType scanType;

    private List<String> entityTypes;

    private List<String> destinations;

}
