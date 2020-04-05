package main.models;

/*
    class to represent WHERE predicates
 */

public class PredicatePair {
    private String operation;
    private String value;

    public PredicatePair(String operation, String value){
        this.operation = operation;
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public String getValue() {
        return value;
    }
}
