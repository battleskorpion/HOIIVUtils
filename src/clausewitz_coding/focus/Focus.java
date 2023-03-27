package clausewitz_coding.focus;

import clausewitz_coding.code.trigger.Trigger;
import clausewitz_parser.Expression;

import java.awt.*;
import java.util.*;

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

    /**
     * Sets focus icon id
     * @param icon
     */
    public void setIcon(String icon) {
        // null string -> set no (null) icon
        if (icon.equals("")) {
            this.icon = null;
        }
        this.icon = icon;
    }

    public void setPrerequisite(Expression exp) {

    }

    /**
     * sets focus prerequisite focuses
     * @param prerequisite Set of prerequisite focuses. Can not include this focus.
     */
    public void setPrerequisite(Set<Focus> prerequisite) {      // TODO can have prerequisites where 1 necessary, all necessary, etc.
        // focus can not be its own prerequisite
        if (prerequisite.contains(this)) {
            throw new IllegalArgumentException("Focus can not be its own prerequisite");
        }

        this.prerequisite = prerequisite;
    }

    public void setMutuallyExclusive(Expression exp) {

    }

    /**
     * Sets mutually exclusive focuses
     * @param mutually_exclusive Set of mutually exclusive focus(es) with this focus. Should not include this focus.
     */
    public void setMutuallyExclusive(Set<Focus> mutually_exclusive) {
        // focus can not be mutually exclusive with itself
        if (mutually_exclusive.contains(this)) {
            throw new IllegalArgumentException("Focus can not be mutually exclusive with itself");
        }

        this.mutually_exclusive = mutually_exclusive;
    }

    /**
     * Sets mutually exclusive focus
     * @param mutually_exclusive mutually exclusive focus
     */
    public void setMutuallyExclusive(Focus mutually_exclusive) {
        HashSet<Focus> set = new HashSet<>();
        set.add(mutually_exclusive);
        setMutuallyExclusive(set);
    }

    public void addMutuallyExclusive(Focus mutually_exclusive) {
        if (this.mutually_exclusive == null) {
            this.mutually_exclusive = new HashSet<>();
            this.mutually_exclusive.add(mutually_exclusive);
        }
    }

    public void setAvailable(Expression exp) {

    }

    /**
     * Sets available trigger of focus
     * @param availableTrigger trigger which controls focus availability
     */
    public void setAvailable(Trigger availableTrigger) {
        this.available = availableTrigger;
    }
}
