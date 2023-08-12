package clausewitz_coding.focus;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.code.trigger.Trigger;
import clausewitz_coding.gfx.Interface;
import clausewitz_parser.Expression;
import ddsreader.DDSReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Focus {
    private static final int FOCUS_COST_FACTOR = 7;
    private final int DEFAULT_FOCUS_COST = 10;  // default cost (weeks) when making new focus or etc.
    private static final HashSet<String> focusIDs = new HashSet<>();

    protected FocusTree focusTree;
    Expression focusExp;
    protected String id;
    protected String locName;
    protected String icon;
    protected BufferedImage ddsImage;
    protected Set<Set<Focus>> prerequisite;              // can be multiple, but hoi4 code is simply "prerequisite"
    protected Set<Focus> mutually_exclusive;
    protected Trigger available;
    protected int x;                                // if relative, relative x
    protected int y;                                // if relative, relative y
    protected String relative_position_id;          // if null, position is not relative
    protected int cost;                             // cost of focus (typically in weeks unless changed in defines)
    protected Set<FocusSearchFilter> focus_search_filters;
    protected boolean available_if_capitulated;
    protected boolean cancel_if_invalid;
    protected boolean continue_if_invalid;
    //    private AIWillDo ai_will_do; // todo
    //select effect
    //completion award

    public Focus(String focus_id, FocusTree focusTree) {
        if (focusIDs.contains(focus_id)) {
            System.err.println("Error: focus id " + focus_id + " already exists.");     // todo throw exception instead
            return;
        }
        this.id = focus_id;
        this.focusTree = focusTree;
        focusIDs.add(focus_id);
    }

    public String id() {
        return id; 
    }

    /**
     * if relative, relative x
     * @return
     */
    public int x() {
        return x;
    }

    /**
     * if relative, relative y
     * @return
     */
    public int y() {
        return y;
    }

    public int absoluteX() {
        if (relative_position_id == null) {
            return x;
        } else {
            return absolutePosition().x;
        }
    }

    public int absoluteY() {
        if (relative_position_id == null) {
            return y;
        } else {
            return absolutePosition().y;
        }
    }

    /**
     * if relative, relative position
     * @return point representing xy location, or relative xy if relative.
     */
    public Point position() {
        return new Point(x, y);
    }

    /**
     * Absolute focus xy-position.
     * @return Point representing absolute position of focus.
     * @implNote Should only be called after all focuses in focus tree are instantiated.
     */
    public Point absolutePosition() {
        if (relative_position_id == null) {
            return position();
        }

        Focus relative_position_focus = focusTree.getFocus(relative_position_id);
        if (relative_position_focus == null) {
            System.err.println("focus id " + relative_position_id + " not a focus");
        }
        Point adjPoint = relative_position_focus.absolutePosition();
        adjPoint = new Point(adjPoint.x + x, adjPoint.y + y);
//        System.out.println(adjPoint + ", " + id + ", " + relative_position_focus.id + ", " + relative_position_focus.position());

        return adjPoint;
    }

    public String locName() {
        return locName;
    }
    public String icon() {
        return icon;
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

        focusExp = exp.get("focus=");

        setID(exp.getSubexpression(id));
        setXY(focusExp.getImmediate("x="), focusExp.getImmediate("y="));
        setRelativePositionID(focusExp.getSubexpression("relative_position_id="));
        //setFocusLoc();
        setIcon(focusExp.getSubexpression("icon="));
        setPrerequisite(focusExp.getAllSubexpressions("prerequisite="));
        setMutuallyExclusive(focusExp.getSubexpression("mutually_exclusive="));
        setAvailable(focusExp.getSubexpression("available="));
    }

    public Expression getFocusExpression() {
        return focusExp;
    }

    public void setID(String id) {
        this.id = id;
    }

    private void setID(Expression exp) {
        if (exp == null) {
            id = null;
            return;
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

    private void setRelativePositionID(Expression exp) {
        if (exp == null) {
            relative_position_id = null;       // perfectly acceptable
            return;
        }
        relative_position_id = exp.getText();   // todo new focus instance eeehhhh??
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
            return;
        }

        this.cost = exp.getValue();
    }

    public void setFocusLoc() {
        setFocusLoc(id);
    }

    public void setFocusLoc(String focus_loc) {
        this.locName = focus_loc;
        // todo?
    }

    //todo implement icon lookup
    public void setIcon(Expression exp) {
        if (exp == null) {
//            icon = null;
//            return;
        }

        setIcon(exp.getText());
    }

    /**
     * Sets focus icon id
     * @param icon
     */
    public void setIcon(String icon) {
        // null string -> set no (null) icon
        // icon == null check required to not throw access exception
        if (icon == null || icon.equals("")) {
            //this.icon = null;
//            return;
        }

        this.icon = icon;

        /* dds binary data buffer */
        /* https://github.com/npedotnet/DDSReader */
        try {
            String gfx = Interface.getGFX(icon);

            FileInputStream fis;
            if (gfx == null) {
                System.err.println("GFX was not found for " + icon);
                fis = new FileInputStream(HOI4Fixes.hoi4_dir_name + "\\gfx\\interface\\goals\\focus_ally_cuba.dds");
            } else {
                fis = new FileInputStream(Interface.getGFX(icon));
            }
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            int[] ddspixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
            int ddswidth = DDSReader.getWidth(buffer);
            int ddsheight = DDSReader.getHeight(buffer);

            ddsImage = new BufferedImage(ddswidth, ddsheight, BufferedImage.TYPE_INT_ARGB);
            ddsImage.setRGB(0, 0, ddswidth, ddsheight, ddspixels, 0, ddswidth);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    public void setPrerequisite(Expression exp) {
        setPrerequisite(new Expression[]{exp});
    }

    /**
     * accepts groups of prerequisites
     * @param exps
     */
    public void setPrerequisite(Expression[] exps) {
        if (exps == null) {
            prerequisite = null;
            return;
        }
        for (Expression exp : exps) {
            for (Expression subexp : exp.getSubexpressions()) {
                if (subexp.getText() == null) {
                    prerequisite = null;
                    System.err.println("Focus prerequisite invalid, " + this.id + ", " + exp);
                    return;
                }
            }
        }

        Set<Set<Focus>> prerequisites = new HashSet<>();

        /* sort through prerequisite={ expressions */
        for (Expression exp : exps) {
            HashSet<Focus> subset = new HashSet<>();

            if (exp == null || exp.getSubexpression("focus=") == null) {
                continue;
            }

            for (String prereqStr : exp.subexpressionSplit("focus=", false)) {
                if (prereqStr == null) {
                    continue;
                }

                prereqStr = prereqStr.trim();
                if (!prereqStr.matches("[\\S]+")) {
                    System.err.println("Focus prerequisite is invalid, " + this.id + ", " + prereqStr);
                    prerequisite = null;
                    return;
                }

                if (focusTree.getFocus(prereqStr) != null) {
                    subset.add(focusTree.getFocus(prereqStr));           // todo error check someday
                } else {
                    System.err.println("Focus prerequisite is invalid (not focus), " + this.id + ", " + prereqStr);
                }
                System.out.println(prereqStr);
            }

            if (subset.size() > 0) {
                prerequisites.add(subset);
            }
        }

        setPrerequisite(prerequisites);
    }

    /**
     * sets focus prerequisite focuses
     * @param prerequisite Set of prerequisite focuses. Can not include this focus.
     */
    public void setPrerequisite(Set<Set<Focus>> prerequisite) {      // TODO can have prerequisites where 1 necessary, all necessary, etc.
        // focus can not be its own prerequisite
        // todo
//        if (prerequisite.contains(this)) {
//            throw new IllegalArgumentException("Focus can not be its own prerequisite");
//        }

        this.prerequisite = prerequisite;
    }

    public void setMutuallyExclusive(Expression exp) {
        if (exp == null) {
            mutually_exclusive = null;
            return;
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
            return;
        }
    }

    /**
     * Sets available trigger of focus
     * @param availableTrigger trigger which controls focus availability
     */
    public void setAvailable(Trigger availableTrigger) {
        this.available = availableTrigger;
    }

    public Image getDDSImage() {
        return ddsImage;
    }

    public boolean hasPrerequisites() {
        return !(prerequisite == null || prerequisite.size() == 0);
    }

    public Set<Set<Focus>> getPrerequisites() {
        return prerequisite;
    }

    public int cost() {
        return cost;
    }

    public int completionTime() {
        return cost * FOCUS_COST_FACTOR;
    }
}
