package com.hoi4utils.clausewitz;

public enum BoolType {
    TRUE_FALSE("true", "false"),
    YES_NO("yes", "no");

    private final String trueResponse;
    private final String falseResponse;

    BoolType(String trueResponse, String falseResponse) {
        this.trueResponse = trueResponse;
        this.falseResponse = falseResponse;
    }

    public String trueResponse() {
        return trueResponse;
    }

    public String falseResponse() {
        return falseResponse;
    }
    
    public boolean matches(String value) {
        if (value.equalsIgnoreCase(trueResponse)) {
            return true;
        } else if (value.equalsIgnoreCase(falseResponse)) {
            return true;
        }
        return false;
    }
    
    public Boolean parse(String value) {
        if (value.equalsIgnoreCase(trueResponse)) {
            return true;
        } else if (value.equalsIgnoreCase(falseResponse)) {
            return false;
        }
        return null;
    }
}
