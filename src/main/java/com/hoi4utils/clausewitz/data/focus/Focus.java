package com.hoi4utils.clausewitz.data.focus;

import com.hoi4utils.clausewitz.*;
import com.hoi4utils.clausewitz.script.*;
import com.hoi4utils.clausewitz.code.effect.Effect;
import com.hoi4utils.clausewitz.code.scope.NotPermittedInScopeException;
import com.hoi4utils.clausewitz.code.scope.Scope;
import com.hoi4utils.clausewitz.code.scope.ScopeCategory;
import com.hoi4utils.clausewitz.localization.Localizable;
import com.hoi4utils.clausewitz.data.country.CountryTagsManager;
import com.hoi4utils.clausewitz_parser.Node;
import com.hoi4utils.clausewitz.exceptions.InvalidEffectParameterException;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Focus class represents an individual focus of a National Focus (Focus Tree).
 */
public class Focus extends StructuredPDX implements Localizable, Comparable<Focus>, DataFunctionProvider<Focus> {
	private static final int FOCUS_COST_FACTOR = 7; // turn into from defines, ora default 7. (get default vanilla define
	// instead?)
	private final double DEFAULT_FOCUS_COST = 10.0; // default cost (in weeks by default) when making a new focus.

	/* attributes */
	protected FocusTree focusTree;
	@NotNull public final StringPDX id;
	@NotNull public final MultiPDX<Icon> icon;
	@NotNull public final IntegerPDX x; // if relative, relative x
	@NotNull public final IntegerPDX y; // if relative, relative y
	@NotNull public final MultiPDX<PrerequisiteSet> prerequisites;
	@NotNull public final MultiPDX<MutuallyExclusiveSet> mutually_exclusive;
	//public final PDXScript<Trigger> available;
	@NotNull public final ReferencePDX<Focus> relativePosition; // if null, position is not relative
	@NotNull public final DoublePDX cost; // cost of focus (typically in weeks unless changed in defines)
	@NotNull public final BooleanPDX available_if_capitulated;
	@NotNull public final BooleanPDX cancel_if_invalid;
	@NotNull public final BooleanPDX continue_if_invalid;
	// private AIWillDo ai_will_do; // todo
	// select effect
	protected Image ddsImage;

	/**
	 * completion raward
	 * null: no definition within focus = {...} block
	 * empty: definition exists, but contents is empty, focus = { }
	 * // todo pdx script time :)
	 */
//	protected List<Effect> completionReward;
	@NotNull public final CompletionReward completionReward;

	public Focus(FocusTree focusTree) {
		super("focus");
		// todo do not check this here! let this be controlled elsewhere?
		this.id = new StringPDX("id");
		this.icon = new MultiPDX<>(Icon::new, "icon");
		this.x = new IntegerPDX("x");
		this.y = new IntegerPDX("y");
		this.prerequisites = new MultiPDX<>(PrerequisiteSet::new,
				"prerequisite");
		this.mutually_exclusive = new MultiPDX<>(() -> new MutuallyExclusiveSet(focusTree::focuses),
				"mutually_exclusive");
		this.relativePosition = new ReferencePDX<>(focusTree::focuses, (f) -> f.id.get(),
				"relative_position_id");
		this.cost = new DoublePDX("cost");
		this.available_if_capitulated = new BooleanPDX("available_if_capitulated",
				false, BoolType.YES_NO);
		this.cancel_if_invalid = new BooleanPDX("cancel_if_invalid",
				true, BoolType.YES_NO);
		this.continue_if_invalid = new BooleanPDX("continue_if_invalid",
				false, BoolType.YES_NO);
		obj.addAll(childScripts());

		this.focusTree = focusTree;
	}

	public Focus(FocusTree focusTree, Node node) throws DuplicateFocusException, UnexpectedIdentifierException, NodeValueTypeException{
		this(focusTree);
		loadPDX(node);
	}

	@Override
	protected Collection<? extends PDXScript<?>> childScripts() {
		return List.of(this.id, this.icon, this.x, this.y, this.prerequisites, this.mutually_exclusive,
				this.relativePosition, this.cost, this.available_if_capitulated,
				this.cancel_if_invalid, this.continue_if_invalid);
	}

	/**
	 * Obtain data functions intended for displaying focus properties across a table
	 *
	 * @return
	 */
	public static List<Function<Focus, ?>> getDataFunctions() {
		// for optimization, set number of data functions (why not)
		List<Function<Focus, ?>> dataFunctions = new ArrayList<>(3);

		dataFunctions.add(focus -> focus.id.get());
		dataFunctions.add(focus -> focus.localizationText(Property.NAME));
		dataFunctions.add(focus -> focus.localizationText(Property.DESCRIPTION));

		return dataFunctions;
	}

	/**
	 * Calculates the focus X position, if the focus has a relative position.
	 * <p>
	 * Otherwise, equivalent to <code>x()</code>.
	 * Use when knowing the real xy-position of the focus in the focus tree is
	 * necessary.
	 * </p>
	 * 
	 * @return
	 */
	public int absoluteX() {
		return absolutePosition().x;
	}

	/**
	 * Calculates the focus Y position, if the focus has a relative position.
	 * <p>
	 * Otherwise, equivalent to <code>y()</code>.
	 * Use when knowing the real xy-position of the focus in the focus tree is
	 * necessary.
	 * </p>
	 * 
	 * @return
	 */
	public int absoluteY() {
		return absolutePosition().y;
	}

	/**
	 * Returns the defined focus xy-position.
	 * <p>
	 * If focus position is relative, returns the relative position.
	 * Otherwise, equivalent to <code>absolutePosition()</code>.
	 * </p>
	 *
	 * @return point representing xy location, or relative xy if relative.
	 */
	public Point position() {
		return new Point(x.getOrElse(0), y.getOrElse(0));
	}

	/**
	 * Absolute focus xy-position.
	 * <p>
	 * If the defined focus position is relative to another focus,
	 * calculates and returns the real xy-position in the focus tree.
	 * </p>
	 *
	 * @return Point representing absolute position of focus.
	 * @implNote Should only be called after all focuses in focus tree are
	 *           instantiated.
	 */
	public @NotNull Point absolutePosition() {
		if (relativePosition.isUndefined()) {
			return position();
		}
		// todo improve comparability
		//if (relative_position_id.get().id.get().equals(this.id.get())) {
		if (relativePosition.nodeEquals(id)) {
			/*
			todo not an error of this program necessarily, issue should be handled
			 differently?
			 */
			System.err.println("Relative position id same as focus id for " + this);
			return position();
		}

		Focus relative_position_focus = relativePosition.get();
		if (relative_position_focus == null) {
			System.err.println("focus id " + relativePosition.getReferenceName() + " not a focus");
			return position();
		}
		Point adjPoint = relative_position_focus.absolutePosition();
		adjPoint = new Point(adjPoint.x + x.getOrElse(0), adjPoint.y + y.getOrElse(0));
		// System.out.println(adjPoint + ", " + id + ", " + relative_position_focus.id +
		// ", " + relative_position_focus.position());

		return adjPoint;
	}

	/**
	 * When printing this focus, or otherwise using {@code toString()}, return the
	 * focus ID.
	 * 
	 * @return
	 */
	public String toString() {
		return id.get();
	}

//	/**
//	 * Adds focus attributes (prerequisite, mutually exclusive, etc...) to focus
//	 * by parsing expressions for each potential attribute.
//	 *
//	 * @param exp Node representing focus - must include "focus".
//	 */
//	public void loadAttributes(Node exp) throws UnexpectedIdentifierException {
//		usingIdentifier(exp);
//
//		id.load(exp);
//		x.load(exp);
//		y.load(exp);
//		relative_position_id.load(exp);
//		icon.load(exp);
//		cost.loadOrElse(exp, DEFAULT_FOCUS_COST);
//		prerequisites.load(exp);
//		mutually_exclusive.load(exp);
////		available.load(exp);
////		setAvailable(exp.findFirst("available"));
//		setCompletionReward(exp.findFirst("completion_reward"));
//	}

	/**
	 * Sets new xy-coordinates, returns previous xy-coordinates. This sets the defined
	 * x and y values of the focus. If a relative position id is set,
	 * the focus position will be offset from the relative focus by the defined x and y values.
	 *
	 * @param x focus new x-coordinate
	 * @param y focus new y-coordinate
	 * @return previous x and y
	 */
	public Point setXY(int x, int y) {
		Point prev = new Point(this.x.getOrElse(0), this.y.getOrElse(0));
		this.x.set(x);
		this.y.set(y);
		return prev;
	}

	/**
	 * Sets new xy-coordinates, returns previous xy-coordinates. This sets the absolute
	 * x and y coordinates of the focus, and is not the same as setting the focus x and y values.
	 * If a relative position id is set, the focus position will be offset accordingly from the relative focus
	 * @param x
	 * @param y
	 * @return
	 */
	public Point setAbsoluteXY(int x, int y) {
		Point prev = new Point(this.x.getOrElse(0), this.y.getOrElse(0));
		this.x.set(x);
		this.y.set(y);
		this.relativePosition.setNull();
		return prev;
	}

	/**
	 * Sets new xy-coordinates, returns previous xy-coordinates.
	 * 
	 * @param xy focus new xy-coordinates
	 * @return previous xy-coordinates
	 */
	public Point setXY(Point xy) {
		return setXY(xy.x, xy.y);
	}

	/**
	 * Sets new xy-coordinates, returns previous xy-coordinates.
	 * 
	 * @param x focus new x-coordinate
	 * @param y focus new y-coordinate
	 * @return previous xy-coordinates
	 */
	private Point setXY(Node x, Node y) {
		if (x == null && y == null) {
			return setXY(0, 0);
		}
		if (x == null) {
			return setXY(0, y.value().integer());
		}
		if (y == null) {
			return setXY(x.value().integer(), 0);
		}
		return setXY(x.value().integer(), y.value().integer());
	}

	/**
	 * Sets focus cost to default cost
	 */
	public void setCost() {
		setCost(DEFAULT_FOCUS_COST);
	}

	/**
	 * Sets focus cost (in weeks by default)
	 *
	 * @param cost focus cost
	 *             todo: add defines support
	 */
	public void setCost(Number cost) {
		this.cost.set(cost.doubleValue());
	}

//	/**
//	 * Sets focus icon id
//	 *
//	 * @param icon focus icon id
//	 */
//	public void setIcon(String icon) {
//		// null string -> set no (null) icon
//		// icon == null check required to not throw access exception
//		if (icon == null || icon.isEmpty()) {
//			// this.icon = null;
//			// return;
//		}
//
//		this.icon.set(icon);
//
//		/* dds binary data buffer */
//		/* https://github.com/npedotnet/DDSReader */
//		try {
//			String gfx = Interface.getGFX(icon);
//
//			FileInputStream fis;
//			if (gfx == null) {
//				// System.err.println("GFX was not found for " + icon); // too much right now
//				try {
//					fis = new FileInputStream(Settings.MOD_PATH + "\\gfx\\interface\\goals\\focus_ally_cuba.dds");
//				} catch (FileNotFoundException exc) {
//					ddsImage = null;
//					return;
//				}
//			} else {
//				fis = new FileInputStream(Interface.getGFX(icon));
//			}
//			byte[] buffer = new byte[fis.available()];
//			fis.read(buffer);
//			fis.close();
//			int[] ddspixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
//			int ddswidth = DDSReader.getWidth(buffer);
//			int ddsheight = DDSReader.getHeight(buffer);
//
//			// ddsImage = new BufferedImage(ddswidth, ddsheight,
//			// BufferedImage.TYPE_INT_ARGB);
//			// ddsImage.setRGB(0, 0, ddswidth, ddsheight, ddspixels, 0, ddswidth);
//			ddsImage = JavaFXImageUtils.imageFromDDS(ddspixels, ddswidth, ddsheight);
//		} catch (IOException exc) {
//			exc.printStackTrace();
//		}
//	}

//	/**
//	 * accepts groups of prerequisites
//	 *
//	 * @param exps list of prerequisite={...} expressions
//	 */
//	public void setPrerequisite(List<Node> exps) {
//		if (exps == null) {
//			prerequisites.clear();
//			return;
//		}
//		removePrerequisites();
//		List<List<Focus>> prerequisites = new ArrayList<>();
//
//		/* sort through prerequisite={ expressions */
//		for (Node prereqExp : exps) {
//			if (prereqExp == null || !prereqExp.contains("focus")) {
//				continue;
//			}
//
//			List<Focus> subset = new ArrayList<>();
//			for (Node prereqNode : prereqExp.filter("focus").toList()) {
//				if (prereqNode.value() == null) {
//					// todo better error reporting
//					System.err.println("Expected a value associated with prerequisite focus assignment");
//					continue;
//				}
//
//				String prereq = prereqNode.value().string();
//
//				if (focusTree.getFocus(prereq) != null) {
//					subset.add(focusTree.getFocus(prereq)); // todo 'error' (not us?) check someday
//				} else {
//					addPendingFocusReference(prereq, this::setPrerequisite, exps);
//				}
//			}
//
//			if (!subset.isEmpty())
//				prerequisites.add(subset);
//		}
//
//		setPrerequisite(prerequisites);
//	}

	/**
	 * Removes all defined focus prerequisites
	 */
	private void removePrerequisites() {
		prerequisites.clear();
	}

	/**
	 * Removes all defined focus mutually exclusive focuses
	 */
	private void removeMutuallyExclusive() {
		if (!this.mutually_exclusive.isUndefined()) {
			this.mutually_exclusive.clear();
		}
	}

//	/**
//	 * <p>
//	 * From <a href="https://hoi4.paradoxwikis.com/National_focus_modding">National
//	 * Focus Modding</a>:
//	 * </p>
//	 * <p>
//	 * "Mutual exclusivity to multiple focuses is usually done by putting several of
//	 * focus = TAG_focusname
//	 * in the same mutually_exclusive, but defining several of mutually_exclusive is
//	 * also possible."
//	 * </p>
//	 *
//	 * @param exps
//	 */
//	public void setMutuallyExclusive(List<Node> exps) {
//		if (exps == null) {
//			mutually_exclusive = null;
//			return;
//		}
//		removeMutuallyExclusive();
//		mutually_exclusive = new HashSet<>();
//
//		/* sort through prerequisite={ expressions */
//		for (Node exclusiveExp : exps) {
//			if (exclusiveExp == null || !exclusiveExp.contains("focus")) {
//				continue;
//			}
//
//			for (Node exclusiveNode : exclusiveExp.filter("focus").toList()) {
//				if (exclusiveNode.value() == null) {
//					// todo better error reporting
//					System.err.println("Expected a value associated with mutually exclusive focus assignment");
//					continue;
//				}
//
//				String exclusive_id = exclusiveNode.value().string();
//
//				if (focusTree.getFocus(exclusive_id) != null) {
//					mutually_exclusive.add(focusTree.getFocus(exclusive_id)); // todo error check someday
//				} else {
//					addPendingFocusReference(exclusive_id, this::setMutuallyExclusive, exps);
//				}
//			}
//		}
//	}

	/**
//	 * Sets focus available trigger
//	 *
//	 * @param mutually_exclusive mutually exclusive focus
//	 */
//	public void addMutuallyExclusive(Focus mutually_exclusive) {
//		if (this.mutually_exclusive == null) {
//			this.mutually_exclusive = new HashSet<>();
//			this.mutually_exclusive.add(mutually_exclusive);
//		}
//	}

	// /**
	// * Sets available trigger of focus
	// *
	// * @param availableTrigger trigger which controls focus availability
	// */
	// public void setAvailable(Trigger availableTrigger) {
	// this.available = availableTrigger;
	// }

	/**
	 * Gets focus icon DDS image
	 * 
	 * @return
	 */
	public Image getDDSImage() {
		return ddsImage;
	}

	/**
	 * Returns true if the focus has a prerequisite focus.
	 * 
	 * @return true if the focus has a prerequisite focus
	 */
	public boolean hasPrerequisites() {
		return !(prerequisites.isUndefined() || prerequisites.isEmpty());
	}

	/**
	 * Returns true if the focus has a mutually exclusive focus.
	 * 
	 * @return true if the focus has a mutually exclusive focus
	 */
	public boolean isMutuallyExclusive() {
		return !(mutually_exclusive.isUndefined() || mutually_exclusive.isEmpty());
	}

	/**
	 * Returns the clausewitz displayed/gameplay focus completion time, which is the
	 * truncate/floor of the
	 * defined completion time (cost * weeks (default).
	 * 
	 * @return
	 */
	public double displayedCompletionTime() {
		return Math.floor(preciseCompletionTime());
	}

	/**
	 * Returns the precise focus completion time, which is the defined completion
	 * time (cost * weeks (default)).
	 * 
	 * @return precise focus completion time
	 */
	public double preciseCompletionTime() {
		return cost.getOrElse(DEFAULT_FOCUS_COST) * FOCUS_COST_FACTOR;
	}

	@Override
	public @NotNull Map<Property, String> getLocalizableProperties() {
		// lets us map null if we use hashmap instead of generic of() method
		HashMap<Property, String> properties = new HashMap<>();
		properties.put(Property.NAME, id.get());
		properties.put(Property.DESCRIPTION, id.get() + "_desc");
		return properties;
	}

	@Override
	public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
		if (focusTree == null) {
			return List.of(this);
		}
		return focusTree.getLocalizableGroup();
	}

	/**
	 *
	 * @return String representing focus completion reward effects,
	 *         with custom tooltips, not including hidden effects,
	 */
	@NotNull
	public String toScript() {
//		NumberFormat df = DecimalFormat.getIntegerInstance();
//
//		StringBuilder details = new StringBuilder();
//		/* id */
//		details.append("ID: ");
//		details.append(id.get());
//		details.append("\n");
//		/* completion time */
//		details.append("Completion time: ");
//		details.append(df.format(displayedCompletionTime()));
//		details.append("\n");
//
//		/* prerequisites */
//		for (PrerequisiteSet prereqSet: this.prerequisites) {
//			if (prereqSet.size() > 1) {
//				details.append("Requires one of the following: \n");
//				for (Focus f : prereqSet) {
//					details.append("- ");
//					details.append(f.localizationText(Property.NAME));
//					details.append("\n");
//				}
//			} else {
//				details.append("Requires: ");
//				details.append(prereqSet.iterator().next().localizationText(Property.NAME));
//				details.append("\n");
//			}
//		}
//
//		if (hasCompletionReward()) {
//			/* completion reward */
//			details.append("\nEffect: \n");
//			for (Effect effect : completionReward) {
//				details.append(effect.displayScript());
//				details.append("\n");
//			}
//		}
//		return details.toString();
		StringBuilder details = new StringBuilder();
		for (var property : childScripts()) {
			String text = property.toScript();
			if (text != null) {
				details.append(text);
			}
		}
		return details.toString();
	}

	public List<Effect> completionReward() {
		return completionReward;
	}

	public void setCompletionReward(List<Effect> completionReward) {
		this.completionReward = completionReward;
	}

	/**
	 * Sets completion reward of focus
	 * 
	 * @param completionRewardNode format: "completion_reward = { ... }"
	 *                             (assumed to be correct)
	 */
	public void setCompletionReward(Node completionRewardNode) {
		completionReward = new ArrayList<>();
		if (completionRewardNode.valueIsNull()) {
			return; // keep newly set empty list, showing completion reward
					// was defined but had no effects
		}

		String cr_identifier = completionRewardNode.name();

		setCompletionRewardsOfNode(completionRewardNode);
	}

	private void setCompletionRewardsOfNode(Node completionRewardNode) {
		setCompletionRewardsOfNode(completionRewardNode, Scope.of(this.focusTree.country.get()));
	}

	/**
	 * todo stuff
	 * 
	 * @param completionRewardNode
	 * @param scope
	 */
	private void setCompletionRewardsOfNode(Node completionRewardNode, Scope scope) {
		for (Node n : completionRewardNode.value().list()) {
			/* check if its a deeper scope first */
			if (n.value().isList()) {
				// todo
				// // todo could this be handled better more generically with some other unique
				// scoping stuff like country tags like.... ? ....
				Scope s = null;
				if (CountryTagsManager.exists(n.name())) {
					s = Scope.of(CountryTagsManager.get(n.name()));
				} else {
					try {
						s = Scope.of(n.name(), scope);
					} catch (NotPermittedInScopeException e) {
						System.out.println(e.getMessage());
						break;
					}
				}

				if (s == null || s.scopeCategory == ScopeCategory.EFFECT) {
					/* if its not a scope may be an effect */
					// todo refactor area
					Effect effect = null;
					try {
						effect = Effect.of(n.name(), scope, n.value());
					} catch (InvalidEffectParameterException exc) {
						System.out.println(exc.getMessage());
					} catch (NotPermittedInScopeException exc) {
						System.out.println(exc.getMessage() + ", scope: " + scope + ", list? " + n.name());
					}
					if (effect != null) {
						completionReward.add(effect);
					} else {
						System.out.println("Scope " + n.name() + " unknown.");
					}
				} else {
					setCompletionRewardsOfNode(n, s);
				}
				// System.out.println(n.name);
			} else if (!n.valueIsNull()) {
				/* else if target, add effect with target */
				Effect effect = null; // todo
				try {
					effect = Effect.of(n.name(), scope, n.value());
				} catch (InvalidEffectParameterException e) {
					System.out.println(e.getMessage());
				} catch (NotPermittedInScopeException e) {
					System.out.println(e.getMessage() + ", scope: " + scope);
				}
				if (effect == null) {
					System.err.println("effect not found: " + n.name());
					continue;
				}
				if (effect.hasSupportedTargets()) {
					try {
						effect.setTarget(n.value().string(), scope);
					} catch (IllegalStateException e) {
						e.printStackTrace();
						continue; // todo idk tbh.
					} catch (Exception e) {
						throw new RuntimeException(e); // todo
					}
				}
				// effect.setParameters(n.value()); use new of() func.
				completionReward.add(effect);
			} else {
				/* else add effect */
				Effect effect = null; // todo
				try {
					effect = Effect.of(n.name(), scope);
				} catch (NotPermittedInScopeException e) {
					System.out.println(e.getMessage());
					continue;
				}
				if (effect == null) {
					System.err.println("effect not found: " + n.name());
					continue;
				}
				effect.setTarget(scope);
				completionReward.add(effect);
			}
		}
	}

	/**
	 * Returns true if the focus has a completion reward.
	 * 
	 * @return true if the focus has a completion reward
	 */
	private boolean hasCompletionReward() {
		return (completionReward != null && !completionReward.isEmpty());
	}

	@Override
	public int compareTo(@NotNull Focus o) {
		return this.id.get().compareTo(o.id.get());
	}

	public boolean hasAbsolutePosition(int x, int y) {
		return this.absolutePosition().equals(new Point(x, y));
	}

	@Override
	public boolean nodeEquals(PDXScript<?> other) {
		return this.id.nodeEquals(((Focus) other).id);
	}

	public class PrerequisiteSet extends MultiReferencePDX<Focus> {
		public PrerequisiteSet() {
			this(() -> focusTree.focuses());
		}

		public PrerequisiteSet(Supplier<Collection<Focus>> referenceFocusesSupplier) {
			super(referenceFocusesSupplier, (f) -> f.id.get(), "prerequisite", "focus");
		}
	}

	/**
	 * mutually exclusive is a multi-reference of focuses
	 */
	public class MutuallyExclusiveSet extends MultiReferencePDX<Focus> {
		public MutuallyExclusiveSet(Supplier<Collection<Focus>> referenceFocusesSupplier) {
			super(referenceFocusesSupplier, (f) -> f.id.get(),"mutually_exclusive", "focus");
		}
	}

	public static class Icon extends DynamicPDX<String, StructuredPDX> {
		public Icon() {
			super(() -> new StringPDX("icon"),
					new StructuredPDX("icon") {
						@NotNull
						private final StringPDX value = new StringPDX("value");

						@Override
						protected Collection<? extends PDXScript<?>> childScripts() {
							return List.of(value);
						}

						@Override
						public boolean nodeEquals(PDXScript<?> other) {
							if (other instanceof Icon icon) {
								return value.objEquals(icon.get());
							}
							return false;
						}
					},
					"value");
		}
	}

	private class CompletionReward extends CollectionPDXScript<Effect<?>> {
		public CompletionReward() {
			super("completion_reward");
		}

        @Override
		public void loadPDX(Node expression) throws UnexpectedIdentifierException {
			super.loadPDX(expression);
		}

		@Override
		protected Effect<?> newChildScript(Node expression) {

		}
	}
}
