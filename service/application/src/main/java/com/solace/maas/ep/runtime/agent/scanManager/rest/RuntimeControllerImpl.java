package com.solace.maas.ep.runtime.agent.scanManager.rest;

import com.solace.maas.ep.runtime.agent.scanManager.ScanManager;
import com.solace.maas.ep.runtime.agent.scanManager.mapper.ScanRequestMapper;
import com.solace.maas.ep.runtime.agent.scanManager.model.ScanRequestBO;
import com.solace.maas.ep.common.model.ScanRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v2/runtime/messagingServices")
public class RuntimeControllerImpl implements RuntimeController {

    private final ScanRequestMapper scanRequestMapper;
    private final ScanManager scanManager;

    @Autowired
    public RuntimeControllerImpl(ScanRequestMapper scanRequestMapper, ScanManager scanManager) {
        this.scanRequestMapper = scanRequestMapper;
        this.scanManager = scanManager;
    }

    @Override
    @PostMapping(value = "{messagingServiceId}/scan")
    public ResponseEntity<String> scan(@PathVariable(value = "messagingServiceId") String messagingServiceId,
                                       @RequestBody @Valid ScanRequestDTO body) {
        try {
            ScanRequestBO scanRequestBO = scanRequestMapper.map(body);
            scanRequestBO.setMessagingServiceId(messagingServiceId);
            String result = scanManager.scan(scanRequestBO);

            log.info("Successfully started the scan {}", result);
            return ResponseEntity.ok().body(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "{messagingServiceId}/schedule/scan")
    public ResponseEntity<String> scheduleScan(@PathVariable(value = "messagingServiceId") String messagingServiceId,
                                       @RequestBody @Valid ScanRequestDTO body,
                                               @RequestParam("expression") String expression) {
        ScanRequestBO scanRequestBO = scanRequestMapper.map(body);
        scanRequestBO.setMessagingServiceId(messagingServiceId);

        try {
            String result = scanManager.scheduleScan(scanRequestBO, expression);

            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping(value = "{messagingServiceId}/schedule/{scheduleId}")
    public ResponseEntity<?> stopScheduler(@PathVariable("scheduleId") String schedulerId) {
        try {
            scanManager.stopScheduler(schedulerId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
