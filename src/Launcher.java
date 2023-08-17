import clausewitz_coding.HOI4Fixes;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args) {
        try {
            HOI4Fixes.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
