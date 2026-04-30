package com.numres.statsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "player-service")
public interface PlayerClient {
    @GetMapping("/players/party/{partyId}")
    List<Object> getPlayersByParty(@PathVariable("partyId") Long partyId);
}