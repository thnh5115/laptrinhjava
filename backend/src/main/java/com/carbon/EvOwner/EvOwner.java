package com.carbon.EvOwner;

//TODO dummy class waiting for implementation

import jakarta.persistence.*;

@Entity
@Table (name = "ev-owners")
public class EvOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
