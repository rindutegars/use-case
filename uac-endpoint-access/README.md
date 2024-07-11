# Case Study :: User Access Control: Endpoint Access

![Technology - Spring Boot](https://img.shields.io/badge/Technology-Spring_Boot-blue)
![Tracing Difficulty - Easy](https://img.shields.io/badge/Tracing_Difficulty-Easy-green)
![Implementation Difficulty - Medium](https://img.shields.io/badge/Implementation_Difficulty-Medium-yellow)

## The Condition

You are developing an application, in which you must implement a custom JWT-based login.

There are 2 dummy users, called:

```text
username: USER_A
password: USER_A
roles: ROLE_A
```

```text
username: USER_B
password: USER_B
roles: ROLE_B
```

The requirement told you that ROLE_A must only be able to access Data A and Data C endpoints. While ROLE_B must only be able to access Data B and Data C endpoints.

## The Problem

You have logged in, either with user A or user B, but both user could access any endpoint as long as they have the login token. There is currently no limitation on how they access server's resources based on their role.

## The Objective

Implement a constraint that limits access for both users based on their roles.

## The Expected Result

User A could access Data A and Data C endpoint, and User B could access Data B and Data C endpoint.
