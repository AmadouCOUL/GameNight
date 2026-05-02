package com.numres.partyservice.repository;


import com.numres.partyservice.model.Party;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PartyRepository {

    private final Map<Long, Party> store = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public PartyRepository() {
        // Données de démo
        save(new Party(null, "Poker Night Friday", "POKER", LocalDate.of(2026, 6, 15)));
        save(new Party(null, "Catan Sunday", "CATAN", LocalDate.of(2026, 6, 20)));
    }

    public Party save(Party party) {
        if (party.getId() == null) {
            party.setId(counter.getAndIncrement());
        }
        store.put(party.getId(), party);
        return party;
    }

    public Optional<Party> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Party> findAll() {
        return new ArrayList<>(store.values());
    }
}
