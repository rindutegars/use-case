# Case Study :: User Access Control: User Data

![Technology - Spring Boot](https://img.shields.io/badge/Technology-Spring_Boot-blue)
![Tracing Difficulty - Easy](https://img.shields.io/badge/Tracing_Difficulty-Easy-green)
![Implementation Difficulty - Medium](https://img.shields.io/badge/Implementation_Difficulty-Medium-yellow)

## The Condition

You are developing an application, in which you must implement a custom JWT-based login.

There are 2 dummy users, called:

```text
username: USER_A
password: USER_A
```

```text
username: USER_B
password: USER_B
```

The Application is about creating a simple note.

The requirement told you that each user must only be able to see their own data, without any ability to see other's data.

## The Problem

You have logged in, either with user A or user B, but both user could see each other data.

## The Objective

Implement a constraint that limits access for both users.

## The Expected Result

Each user can only see data owned by themselves.
