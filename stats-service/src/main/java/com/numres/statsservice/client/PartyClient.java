package com.numres.statsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "party-service") // Nom exact défini dans le application.yml du Party Service
public interface PartyClient {

    @GetMapping("/parties/{id}")
    Map<String, Object> getPartyById(@PathVariable("id") Long id);
}