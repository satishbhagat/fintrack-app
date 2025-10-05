package com.fintrack.client.models;

import java.util.UUID;

public class User {
    public UUID id;
    public String email;
    public String passwordHash;
    public double monthlySalary;
    public String createdAt;
    public String updatedAt;
}