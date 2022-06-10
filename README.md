# Fetch Rewards Coding Exercise - Backend Software Engineering

## S. Hecking

## Instructions 

### Required Software

- Install [latest Java version](https://www.oracle.com/java/technologies/downloads/)
- Install [Spring Tools Suite 4](https://spring.io/tools) to open and view the project.
- Install [Postman](https://www.postman.com/downloads) for sending and receiving requests. 

### Run Project in STS

- Open the project in STS, right click the file FetchRewardsApplication.java, and choose Run as Spring Boot App.
- Open Postman and create/send requests using the formatting below.

### Routes

Note: Before sending add and spend requests, be sure the request body is formatted as raw JSON.

#### 1. Add transactions for a specific payer and date (returns string message)

Endpoint to add transaction:
```
http://localhost:8080/fetch/points/add
```

HTTP verb: POST

Request body example (additional add calls from exercise omitted for clarity):
```
{ 
    "payer": "DANNON", 
    "points": 1000, 
    "timestamp": "2020-11-02T14:00:00Z"
}
```


Expected response(s):
```
1000 points added from payer DANNON at 2020-11-02 09:00:00.0
```


#### 2. Spend points (returns spent points per payer as a JSONObject)

Endpoint to spend points:
```
http://localhost:8080/fetch/points/spend
```

HTTP verb: PATCH

Request body example:
```
{
    "points" : 5000
}
```

Expected response (after running additional add calls listed in exercise):
```
{
    "UNILEVER": -200,
    "MILLER COORS": -4700,
    "DANNON": -100
}
```


#### 3. Retrieve all payer point balances (returns points per payer as a JSON object)

Endpoint to retrieve balances:
```
http://localhost:8080/fetch/points/balance
```

HTTP verb: GET

Request body: none required

Expected response:
```
{
    "UNILEVER": 0,
    "MILLER COORS": 5300,
    "DANNON": 1000
}
```

### Notes

- This project is a simple proof of concept for a point management system. No user-specific functionality was required or included, but may be added in the future.
- Per the exercise's instructions, no data storage software was integrated and all calculations are performed in-memory.