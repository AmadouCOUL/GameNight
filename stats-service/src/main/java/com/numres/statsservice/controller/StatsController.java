package com.numres.statsservice.controller;

import com.numres.statsservice.client.PartyClient;
import com.numres.statsservice.client.PlayerClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final PartyClient partyClient;
    private final PlayerClient playerClient;

    public StatsController(PartyClient partyClient, PlayerClient playerClient) {
        this.partyClient = partyClient;
        this.playerClient = playerClient;
    }

    @GetMapping("/{partyId}")
    @CircuitBreaker(name = "playerService", fallbackMethod = "fallbackStats")
    @Retry(name = "playerService")
    public Map<String, Object> getStats(@PathVariable Long partyId) {
        // 1. Appel au Party Service
        Map<String, Object> party = partyClient.getPartyById(partyId);

        // 2. Appel au Player Service
        var players = playerClient.getPlayersByParty(partyId);

        // 3. Construction de la réponse finale
        Map<String, Object> response = new HashMap<>();
        response.put("partyName", party.get("name"));
        response.put("gameType", party.get("gameType"));
        response.put("playersCount", players.size());

        return response;
    }

    public Map<String, Object> fallbackStats(Long partyId, Throwable t) {
        Map<String, Object> fallbackResponse = new HashMap<>();

        fallbackResponse.put("partyName", "Indisponible");
        fallbackResponse.put("gameType", "N/A");
        fallbackResponse.put("playersCount", -1); // Valeur demandée par l'énoncé

        return fallbackResponse;
    }
}