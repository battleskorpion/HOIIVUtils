package clausewitz_coding.localization;

public record Localization(String ID, String text) {

    public String toString() {
        return ID + ":0" + " " + "\"" + text + "\"";
    }
}
