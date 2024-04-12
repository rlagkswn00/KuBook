package com.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Kcube {
    public String name;
    public String room;
    public String max;
}
