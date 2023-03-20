package clausewitz_coding.focus;

import clausewitz_coding.code.trigger.Trigger;

import java.awt.*;
import java.util.Set;

public class Focus {
    private String id;
    private String locName;
    protected String icon;
    protected Set<Focus> prerequisite;          // can be multiple, but hoi4 code is simply "prerequisite"
    protected Set<Focus> mutually_exclusive;
    private Trigger available;
    private int x;
    private int y;
    protected String relative_position_id;
    private int cost;       // cost of focus (typically in weeks unless changed in defines)
    protected Set<FocusSearchFilter> focus_search_filters;
    private boolean available_if_capitulated;
    private boolean cancel_if_invalid;
    private boolean continue_if_invalid;

    public Focus(String focus_id) {
        this.id = focus_id;
    }
//    private AIWillDo ai_will_do; // todo
    //select effect
    //completion award

    public String id() {
        return id; 
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Point position() {
        return new Point(x, y);
    }

    public String locName() {
        return locName;
    }

    public void setFocusLoc(String focus_loc) {
        this.locName = focus_loc;
    }
    public String toString() {
        return id(); 
    }
}
