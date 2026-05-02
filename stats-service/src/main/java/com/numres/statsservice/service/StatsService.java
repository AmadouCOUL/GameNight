package com.numres.statsservice.service;

import com.numres.statsservice.model.PartyStats;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final RestTemplate restTemplate;

    @Value("${services.party-url}")
    private String partyUrl;

    @Value("${services.player-url}")
    private String playerUrl;

    public StatsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "playerService", fallbackMethod = "fallbackStats")
    @Retry(name = "playerService")
    public PartyStats getStats(Long partyId) {
        Map partyData = restTemplate.getForObject(partyUrl + "/parties/" + partyId, Map.class);
        String partyName = (String) partyData.get("name");
        String gameType  = (String) partyData.get("gameType");

        List players = restTemplate.getForObject(playerUrl + "/players/party/" + partyId, List.class);
        int count = players != null ? players.size() : 0;

        return new PartyStats(partyName, gameType, count);
    }

    public PartyStats fallbackStats(Long partyId, Throwable t) {
        try {
            Map partyData = restTemplate.getForObject(partyUrl + "/parties/" + partyId, Map.class);
            String partyName = (String) partyData.get("name");
            String gameType  = (String) partyData.get("gameType");
            return new PartyStats(partyName, gameType, -1);
        } catch (Exception e) {
            return new PartyStats("Unknown", "Unknown", -1);
        }
    }
}
