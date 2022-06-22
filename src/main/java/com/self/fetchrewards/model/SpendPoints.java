package com.self.fetchrewards.model;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpendPoints {
    
    @NotNull
    private int points;
}
