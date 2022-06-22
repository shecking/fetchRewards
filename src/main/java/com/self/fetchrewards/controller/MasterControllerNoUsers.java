package com.self.fetchrewards.controller;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.self.fetchrewards.model.SpendPoints;
import com.self.fetchrewards.model.Transaction;

@RestController
@RequestMapping("/fetch")
public class MasterControllerNoUsers {
    
    // logger
    Logger logger = LoggerFactory.getLogger(MasterControllerNoUsers.class);

    private int totalPoints = 0;
    private int countTransactions = 0;
    
    // map that will contain current payers and points balances, irrespective of queue
    LinkedHashMap<String, Integer> currentPayerPoints = new LinkedHashMap<>();
    
    // creating transaction queue with sorting by timestamp
    PriorityQueue<Transaction> transactionQueue = new PriorityQueue<>(new Comparator<Transaction>() {

        @Override
        public int compare(Transaction ts1, Transaction ts2) {
            long time1 = ts1.getTimestamp().getTime();
            long time2 = ts2.getTimestamp().getTime();
        
            if (time2 < time1) {
                return 1;
            }
            else if (time1 < time2) {
                return -1;
            }
            else {
                return 0;
            }
        }
    });
    
  
    // ADD TRANSACTION
    // returns string stating what was added (not strictly necessary but good QOL)
    @PostMapping("points/add")
    public ResponseEntity<String> addPayerPoints(@Validated @RequestBody Transaction transaction) {  
    
        // transaction information
        transaction.setTransactionId(countTransactions);
        countTransactions++;
        String payer = transaction.getPayer();
        int points = transaction.getPoints();
        Timestamp timestamp = transaction.getTimestamp();
        
        // if points are positive
        if (points > 0) {
            transactionQueue.add(transaction);
            
            // if payer doesn't exist, create it
            if (!currentPayerPoints.containsKey(payer)) {
                logger.info("Adding " + points +" points to new payer " + payer);
                currentPayerPoints.put(payer, points);
            }
            // if payer exists, add to it
            else {
                logger.info("Adding " + points + " points to existing payer " + payer);
                currentPayerPoints.replace(payer,  currentPayerPoints.get(payer) + points);
            }
        } 
        // if points are negative
        else if (points < 0) {
            
            // if payer doesn't exist
            if (!currentPayerPoints.containsKey(payer)) {
                throw new RuntimeException("Invalid transaction, no points from payer " + payer + " available");
            }
            // if payer exists
            else {

                // if current points + transaction points >= 0
                if (currentPayerPoints.get(payer) + points >= 0) {
                    transactionQueue.add(transaction);
                    logger.info("Removing " + Math.abs(points) +" points from payer " + payer);
                    currentPayerPoints.replace(payer, currentPayerPoints.get(payer) + points);
                } else {
                    throw new RuntimeException("Invalid transaction, not enough points from payer " + payer + " available");
                }
            }
        } 
        // if points are 0
        else {
            throw new RuntimeException("Invalid transaction, transaction has 0 points");
        }
        
        // update total points
        totalPoints = totalPoints + points;
    
        return new ResponseEntity<>(points + " points added from payer " + payer + " at " + timestamp, HttpStatus.CREATED);
    }

    // SPEND POINTS
    // returns list of spent points per payer
    @PatchMapping("points/spend")
    public ResponseEntity<JSONObject> spendPayerPoints(@Validated @RequestBody SpendPoints pointsToSpend) {
        
        int spendPoints = pointsToSpend.getPoints();
        
        // map to hold spent point values to return
        LinkedHashMap<String, Integer> spentPayerPoints = new LinkedHashMap<>();
        
        // if not enough total points to cover spend
        if (spendPoints > totalPoints) {
            throw new RuntimeException("Invalid spending, not enough points");
        }
        
        // while there are still spend points and transactions remaining in the queue
        while (spendPoints > 0 && !transactionQueue.isEmpty()) {
            
            // look into transaction queue for the transaction at the front
            Transaction front = transactionQueue.peek();
            // get front payer and points
            String frontPayer = front.getPayer();
            int frontPoints = front.getPoints();
            
            // if frontPoints are less than or equal to spendPoints
            if (frontPoints <= spendPoints) {
                
                spendPoints = spendPoints - frontPoints;
                totalPoints = totalPoints - frontPoints;
                
                // put or replace in spentPayerPoints map to keep track of spent points
                if (spentPayerPoints.containsKey(frontPayer)) {
                    spentPayerPoints.replace(frontPayer, spentPayerPoints.get(frontPayer) - frontPoints);
                } else {
                    spentPayerPoints.put(frontPayer, -frontPoints);
                }
                
                // update currentPayerPoints map
                currentPayerPoints.replace(frontPayer, currentPayerPoints.get(frontPayer) - frontPoints);
                
                // remove front transaction from queue
                transactionQueue.remove();
                
            } 
            // if frontPoints are greater than spendPoints
            else {
                
                totalPoints = totalPoints - spendPoints;
                
                // edit transaction queue point value (reflecting a partial transaction)
                front.setPoints(front.getPoints() - spendPoints);
                
                if (spentPayerPoints.containsKey(frontPayer)) {
                    spentPayerPoints.replace(frontPayer, spentPayerPoints.get(frontPayer) - spendPoints);
                } else {
                    spentPayerPoints.put(frontPayer, -spendPoints);
                }
                
                // update currentPayerPoints map
                currentPayerPoints.replace(frontPayer, currentPayerPoints.get(frontPayer) - spendPoints);
                
                spendPoints = 0;
                
            }
        }
        
        JSONObject json = new JSONObject(spentPayerPoints);
        
        return new ResponseEntity<JSONObject>(json, HttpStatus.OK);
    }
    
    // RETURN POINTS BALANCE
    // returns a list of current point balances itemized by payer
    @GetMapping("points/balance")
    public ResponseEntity<JSONObject> getAllPayerPointBalances() {
        
        // return JSON object of payers with values
        JSONObject json = new JSONObject(currentPayerPoints);
        
        return new ResponseEntity<JSONObject>(json, HttpStatus.OK);
    }
  
}

// console statements
// System.out.println("Current transactionQueue: " + transactionQueue);
// System.out.println("spendPoints: " + spendPoints);
// System.out.println("totalPoints: " + totalPoints);
// System.out.println("currentPayerPoints: " + currentPayerPoints);
// System.out.println("spentPayerPoints: " + spentPayerPoints);