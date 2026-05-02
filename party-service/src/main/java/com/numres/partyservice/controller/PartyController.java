package com.numres.partyservice.controller;

import com.numres.partyservice.model.Party;
import com.numres.partyservice.repository.PartyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parties")
public class PartyController {

    private final PartyRepository repository;

    public PartyController(PartyRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Party create(@RequestBody Party party) {
        return repository.save(party);
    }

    @GetMapping
    public List<Party> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Party> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}