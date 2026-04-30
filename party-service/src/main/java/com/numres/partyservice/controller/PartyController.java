package com.numres.partyservice.controller;



import com.numres.partyservice.model.Party;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/parties")
public class PartyController {

    private final List<Party> parties = new ArrayList<>();

    @PostMapping
    public Party createParty(@RequestBody Party party) {
        parties.add(party);
        return party;
    }

    @GetMapping
    public List<Party> getAllParties() {
        return parties;
    }

    @GetMapping("/{id}")
    public Party getPartyById(@PathVariable Long id) {
        return parties.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Party not found"));
    }
}
