package clausewitz_coding.focus;

import clausewitz_coding.code.trigger.Trigger;
import clausewitz_parser.Expression;

import java.awt.*;
import java.util.*;

public class Focus {
    private final int DEFAULT_FOCUS_COST = 10;  // default cost (weeks) when making new focus or etc.

    protected String id;
    protected String locName;
    protected String icon;
    protected Set<Focus> prerequisite;          // can be multiple, but hoi4 code is simply "prerequisite"
    protected Set<Focus> mutually_exclusive;
    protected Trigger available;
    protected int x;
    protected int y;
    protected String relative_position_id;
    protected int cost;                           // cost of focus (typically in weeks unless changed in defines)
    protected Set<FocusSearchFilter> focus_search_filters;
    protected boolean available_if_capitulated;
    protected boolean cancel_if_invalid;
    protected boolean continue_if_invalid;

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

    public String toString() {
        return id(); 
    }

    /**
     * Adds focus attributes (prerequisite, mutually exclusive, etc...) to focus
     * by parsing expressions for each potential attribute.
     * @param exp Expression representing focus - must include "focus".
     */
    public void loadAttributes(Expression exp) {
        if(exp.get("focus") == null) {
            System.err.println(this + " - Not valid focus expression/definition.");
            return;
        }

        Expression focusExp = exp.get("focus");

        setID(exp.getSubexpression(id));
        setXY(focusExp.getImmediate("x"), focusExp.getImmediate("y"));
        //setFocusLoc();
        setIcon(focusExp.getSubexpression("icon"));
        setPrerequisite(focusExp.getSubexpression("prerequisite"));
        setMutuallyExclusive(focusExp.getSubexpression("mutually_exclusive"));
        setAvailable(focusExp.getSubexpression("available"));
    }

    public void setID(String id) {
        this.id = id;
    }

    private void setID(Expression exp) {
        if (exp == null) {
            id = null;
        }
        id = exp.getText();
    }

    /**
     * Sets new xy-coordinates, returns previous xy-coordinates.
     * @param x focus new x-coordinate
     * @param y focus new y-coordinate
     * @return previous x and y
     */
    public Point setXY(int x, int y) {
        Point prev = new Point(this.x, this.y);
        this.x = x;
        this.y = y;
        return prev;
    }

    public Point setXY(Point xy) {
        return setXY(xy.x, xy.y);
    }

    private Point setXY(Expression x, Expression y) {
        if (x == null && y == null) {
            return setXY(0, 0);
        }
        if (x == null) {
            return setXY(0, y.getValue());
        }
        if (y == null) {
            return setXY(x.getValue(), 0);
        }
        return setXY(x.getValue(), y.getValue());
    }

    public void setCost() {
        setCost(DEFAULT_FOCUS_COST);
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setCost(Expression exp) {
        if (exp == null) {
            cost = 0;
        } else {
            this.cost = exp.getValue();
        }
    }

    public void setFocusLoc() {
        setFocusLoc(id);
    }

    public void setFocusLoc(String focus_loc) {
        this.locName = focus_loc;
        // todo?
    }

    public void setIcon(Expression exp) {
        if (exp == null) {
            icon = null;
        }
    }

    /**
     * Sets focus icon id
     * @param icon
     */
    public void setIcon(String icon) {
        // null string -> set no (null) icon
        // icon == null check required to not throw access exception
        if (icon == null || icon.equals("")) {
            this.icon = null;
        }
        this.icon = icon;
    }

    public void setPrerequisite(Expression exp) {
        if (exp == null) {
            prerequisite = null;
        }
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
        if (exp == null) {
            mutually_exclusive = null;
        }
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
        if (exp == null) {
            available = null;
        }
    }

    /**
     * Sets available trigger of focus
     * @param availableTrigger trigger which controls focus availability
     */
    public void setAvailable(Trigger availableTrigger) {
        this.available = availableTrigger;
    }
}
