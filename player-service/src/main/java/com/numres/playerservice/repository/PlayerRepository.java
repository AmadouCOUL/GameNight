package com.numres.playerservice.repository;

import com.numres.playerservice.model.Player;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PlayerRepository {

    private final Map<Long, Player> store = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public PlayerRepository() {
        // Données de démo : 3 joueurs dans party 1, 2 dans party 2
        save(new Player(null, 1L, "Alice"));
        save(new Player(null, 1L, "Bob"));
        save(new Player(null, 1L, "Charlie"));
        save(new Player(null, 2L, "Diana"));
        save(new Player(null, 2L, "Eve"));
    }

    public Player save(Player player) {
        if (player.getId() == null) {
            player.setId(counter.getAndIncrement());
        }
        store.put(player.getId(), player);
        return player;
    }

    public List<Player> findByPartyId(Long partyId) {
        return store.values().stream()
                .filter(p -> p.getPartyId().equals(partyId))
                .collect(Collectors.toList());
    }
}
