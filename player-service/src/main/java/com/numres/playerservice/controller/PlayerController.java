package com.numres.playerservice.controller;

import com.numres.playerservice.model.Player;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerController {
    private final List<Player> players = new ArrayList<>();

    @PostMapping
    public Player addPlayer(@RequestBody Player player) {
        players.add(player);
        return player;
    }

    @GetMapping("/party/{partyId}")
    public List<Player> getPlayersByParty(@PathVariable Long partyId) {
        return players.stream()
                .filter(p -> p.partyId().equals(partyId))
                .toList();
    }
}
