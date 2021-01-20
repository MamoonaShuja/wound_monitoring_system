package com.android.woundmonitoringsystem;

public class UserProfile {
    private String name , email , age , gender , region;

    public UserProfile(String name, String email, String age, String gender, String region) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.region = region;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getRegion() {
        return region;
    }
}
