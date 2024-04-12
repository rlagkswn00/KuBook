package com.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KLog {
    public String userId;
    public String time;
}
