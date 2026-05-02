package com.numres.partyservice.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Party {
    private Long id;
    private String name;
    private String gameType;
    private LocalDate date;
}