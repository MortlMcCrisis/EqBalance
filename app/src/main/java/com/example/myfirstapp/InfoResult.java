package com.example.myfirstapp;

public class InfoResult {

    private final int politics;

    private final int control;

    private final int criticism;

    private final int commitment;

    public InfoResult(int politics, int control, int criticism, int commitment) {
        this.politics = politics;
        this.control = control;
        this.criticism = criticism;
        this.commitment = commitment;
    }

    public int getPolitics() {
        return politics;
    }

    public int getControl() {
        return control;
    }

    public int getCriticism() {
        return criticism;
    }

    public int getCommitment() {
        return commitment;
    }
}
