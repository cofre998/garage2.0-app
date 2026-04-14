package com.ger.garage.model;

public class Mechanic {

    private String id;
    private String name;
    private String email;

    public Mechanic() {} // 🔥 necesario para Firebase

    public Mechanic(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
}