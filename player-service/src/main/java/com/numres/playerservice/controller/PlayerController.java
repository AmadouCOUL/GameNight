package com.numres.playerservice.controller;

import com.numres.playerservice.model.Player;
import com.numres.playerservice.repository.PlayerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerRepository repository;

    public PlayerController(PlayerRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Player create(@RequestBody Player player) {
        return repository.save(player);
    }

    @GetMapping("/party/{partyId}")
    public List<Player> getByParty(@PathVariable Long partyId) {
        return repository.findByPartyId(partyId);
    }
}