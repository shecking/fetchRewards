package com.self.fetchrewards.model;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class Transaction {
  
    private int transactionId;

    private String payer;
  
    private int points;
  
    private Timestamp timestamp;

 }
