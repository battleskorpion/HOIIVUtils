//package com.hoi4utils.clausewitz.code.modifier;
//
////import java.sql.Array;
//
////import java.util.ArrayList;
//
//import com.hoi4utils.clausewitz.script.PDXScript;
//
//import java.util.EnumSet;
//import java.util.SortedMap;
//import java.util.TreeMap;
//
///**
// * For information: <a href="https://hoi4.paradoxwikis.com/Modifiers">Modifiers
// * Wiki</a>
// * Notes:
// * - A modifier with the value of 0 "will always do nothing."
// * - Negative modifiers will always work and have the opposite effect
// * - Opinion modifiers are not regular modifiers, and should therefore be
// * implemented separately.
// * - modifiers do not support if statements
// */
//public class Modifier {
//	public static SortedMap<String, Modifier> modifiers = new TreeMap<>();
//
//	private final String identifier;
//	private final ColorType colorType; // = ColorType.good;
//	private final ValueType valueType; // = ValueType.percentage;
//	private final ValuePostfix postfix; // default: ValuePostfix.none;
//	private final EnumSet<ModifierCategory> category;
//	/**
//	 * HOI4 inherently does not support precision > 3.
//	 */
//	int precision; // default: 1
//
//	protected Modifier(String identifier, ColorType colorType, ValueType valueType, int precision, ValuePostfix postfix,
//			EnumSet<ModifierCategory> category) {
//		this.identifier = identifier;
//		this.colorType = colorType;
//		this.valueType = valueType;
//		this.precision = precision;
//		this.postfix = postfix;
//		this.category = category;
//
//		modifiers.put(identifier, this);
//	}
//
//	protected Modifier(String identifier, ColorType colorType, ValueType valueType, int precision, ValuePostfix postfix,
//			ModifierCategory category) {
//		this(identifier, colorType, valueType, precision, postfix, EnumSet.of(category));
//	}
//
//	/**
//	 * decides the colour of the modifier's value itself. There are three values,
//	 * good, bad, and neutral. neutral is permamently yellow, while good turns the
//	 * positive values green and negative values red. bad is the reversal of good.
//	 */
//	public enum ColorType {
//		good,
//		bad,
//		neutral
//	}
//
//	public enum ValueType {
//		number,
//		percentage,
//		percentage_in_hundred,
//		yes_no
//	}
//
//	public enum ValuePostfix {
//		none, // default
//		days,
//		hours,
//		daily
//	}
//
//	// private String modifierID;
//	//// List<ModifierCategory> scope = null; // can have multiple categories? //
//	// todo this needed in some way?
//
//	// public Modifier(Scope scope) {
//	// this.scope = new ArrayList<>();
//	// this.scope.add(scope);
//	// }
//
//	public static Modifier of(String identifier) {
//		return modifiers.get(identifier);
//	}
//
//	/**
//	 * Returns the category of the modifier
//	 * 
//	 * @return modifier's category
//	 */
//	EnumSet<ModifierCategory> category() {
//		return category;
//	}
//
//	public String identifier() {
//		return identifier;
//	}
//
//	public ColorType colorType() {
//		return colorType;
//	}
//
//	public ValueType valueType() {
//		return valueType;
//	}
//
//	public ValuePostfix postfix() {
//		return postfix;
//	}
//
//	boolean isInCategory(ModifierCategory checkedCategory) {
//		return category.contains(checkedCategory);
//	}
//
//	/* Add base game modifiers */
//	static {
//		// new Modifier("" , , , , );
//	}
//}
