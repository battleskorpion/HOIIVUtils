package com.hoi4utils.clausewitz.code.effect;

import com.hoi4utils.clausewitz.data.technology.TechCategory;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.clausewitz.script.*;

import java.util.List;


public enum ParameterValueType {
	ace_type,
	ai_strategy,
	character,
	country {
		public List<String> identifiers() {
			return List.of("country", "tag");
		}
		//public Class<?> type() { return CountryTag.class; }
	},
	cw_bool {
		public List<String> identifiers() {
			return List.of("bool", "boolean");
		}
		public Class<?> type() { return Boolean.class; }
	},
	cw_float {
		public List<String> identifiers() {
			return List.of("float", "fraction");
		}
		public Class<?> type() { return Double.class; }

	} //		public List<String> modifiers() {
	//			return List.of(range);
	//		}
	,
	cw_int {
		public List<String> identifiers() {
			return List.of("int", "integer");
		}
		public Class<?> type() { return Integer.class; }
	},
	cw_string {
		public List<String> identifiers() {
			return List.of("string");
		}
		public Class<?> type() { return String.class; }
	},
//	cw_variable {
//		public List<String> identifiers() {
//			return List.of("variable", "var");
//		}
//	},
	decision,
	doctrine_category,
	flag,
	idea,
	mission,
	modifier,
	scope,
	state {
		public List<String> identifiers() {
			return List.of("state", "state_id");
		}
		public Class<?> type() { return State.class; }
	},
	trait,
	equipment,
	strategic_region {
		public List<String> identifiers() {
			return List.of("strategic_region", "strat_region");
		}
	},
	building,
	operation_token,
	ideology,
	sub_ideology,
	list,
	province,
	resource,
	tech_category {
		public List<String> identifiers() {
			return List.of("tech_category", "technology_category");
		}
		public Class<?> type() { return TechCategory.class; }
	},
	advisor_slot,
	event,
	wargoal {
		public List<String> identifiers() {
			return List.of("wargoal", "war_goal");
		}
	},
	;

	private String identifier;

	public static boolean isParameterValueType(String s) {
		if (s.startsWith("<") && s.endsWith(">")) {
			s = s.substring(1, s.length() - 1); // remove ends
			s = s.replaceAll("([^\\s])(<)", "$1 $2");
			String[] args = s.split(" ");
			// 1st arg should be identifier of parameter value type
			// other args should be @<modifier name> .... <optional modifier arguments>
			for (ParameterValueType p : ParameterValueType.values()) {
				if (p.identifiers().contains(args[0])) {
					return true;
				}
			}
		} else {
			return false;
		}
		return false;
	}

	public static ParameterValueType of(String s) {
		if (s.startsWith("<") && s.endsWith(">")) {
			s = s.substring(1, s.length() - 1); // remove ends
			s = s.replaceAll("([^\\s])(<)", "$1 $2");
			String[] args = s.split(" ");
			// 1st arg should be identifier of parameter value type
			// other args should be @<modifier name> .... <optional modifier arguments>
			for (ParameterValueType p : ParameterValueType.values()) {
				if (p.identifiers().contains(args[0])) {
					p.setIdentifier(args[0]);
					return p;
				}
			}
		}
		return null;
	}

	private void setIdentifier(String arg) {
		this.identifier = arg;
	}

	public List<String> identifiers() {
		return List.of(this.name());
	}
	public String identifier() { return this.identifier; }

	public Class<?> type() { return String.class; }

	@SuppressWarnings("unchecked")
	public <T> PDXScript<T> createPDXScript(Class<T> typeClass) {
		PDXScript<T> result;
		if (typeClass.equals(Boolean.class)) {
			//result = new BooleanPDX(identifier());
			return null; // todo
		} else if (typeClass.equals(Double.class)) {
			result = (PDXScript<T>) new DoublePDX(identifier());
		} else if (typeClass.equals(Integer.class)) {
			result = (PDXScript<T>) new IntegerPDX(identifier());
		} else if (typeClass.equals(String.class)) {
			result = (PDXScript<T>) new StringPDX(identifier());
		} else {
			throw new IllegalArgumentException("Unsupported type: " + typeClass);
		}
//		return typeClass.cast(result);
	}

	// todo better
//	public List<String> modifiers() {
//		return null;
//	}

//	final String range = "@range";
}
