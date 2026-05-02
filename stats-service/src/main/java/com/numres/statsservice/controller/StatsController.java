package com.numres.statsservice.controller;

import com.numres.statsservice.model.PartyStats;
import com.numres.statsservice.service.StatsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/{partyId}")
    public PartyStats getStats(@PathVariable Long partyId) {
        return statsService.getStats(partyId);
    }
}