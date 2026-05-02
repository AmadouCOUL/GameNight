package com.numres.statsservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyStats {
    private String partyName;
    private String gameType;
    private int playersCount;


    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public int getPlayersCount() { return playersCount; }
    public void setPlayersCount(int playersCount) { this.playersCount = playersCount; }

}
