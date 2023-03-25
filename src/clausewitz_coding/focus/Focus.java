package clausewitz_coding.focus;

import clausewitz_coding.code.trigger.Trigger;
import clausewitz_parser.Expression;

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

    /**
     * Adds focus attributes (prerequisite, mutually exclusive, etc...) to focus
     * by parsing expressions for each potential attribute
     * @param focusExp Expression representing focus - must include "focus"
     */
    public void loadAttributes(Expression focusExp) {
        if(focusExp.get("focus") == null) {
            System.err.println(this + " - Not valid focus expression/definition.");
            return;
        }


    }

    public void setIcon(Expression exp) {

    }

    public void setIcon(String icon) {

    }

    public void setPrerequisite(Expression exp) {

    }

    public void setPrerequisite(Set<Focus> prerequisite) {
        this.prerequisite = prerequisite;
    }

    public void setMutuallyExclusive(Expression exp) {

    }

    /**
     * 
     * @param mutually_exclusive
     */
    public void setMutuallyExclusive(Set<Focus> mutually_exclusive) {
        this.mutually_exclusive = mutually_exclusive;
    }

    public void setAvailable(Expression exp) {

    }

    /**
     * Sets available trigger of focus
     * @param trigger
     */
    public void setAvailable(Trigger trigger) {

    }
}
