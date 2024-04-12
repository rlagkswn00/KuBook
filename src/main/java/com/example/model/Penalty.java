package com.example.model;

import lombok.Data;

@Data
public class Penalty {
    public String userId;

    public Penalty(String userId) {
        this.userId = userId;
    }
}
