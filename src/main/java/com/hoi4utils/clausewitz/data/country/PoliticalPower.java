package com.hoi4utils.clausewitz.data.country;

public class PoliticalPower {
    private int politicalPower;

    public PoliticalPower(int politicalPower) {
        this.politicalPower = politicalPower;
    }

    public int amt() {
        return politicalPower;
    }

    public void set(int politicalPower) {
        this.politicalPower = politicalPower;
    }
}
