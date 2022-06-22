package com.self.fetchrewards.model;

import java.sql.Timestamp;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Transaction {
    
    @NotNull
    private int transactionId;
    @NotBlank
    private String payer;
    @NotNull
    private int points;
    @NotNull
    private Timestamp timestamp;

 }
