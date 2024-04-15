package com.example.model;

import lombok.Data;

@Data
public class PenaltyUser implements Model{
    public String userId;

    public PenaltyUser(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return userId;
    }
}
