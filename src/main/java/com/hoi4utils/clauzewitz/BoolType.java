package main.java.com.hoi4utils.clauzewitz;

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
}
