package com.HOIIVUtils.clauzewitz.code.effect;

import java.util.List;


public enum ParameterValueType {
	ace_type,
	ai_strategy,
	character,
	country {
		public List<String> identifiers() {
			return List.of("country", "tag");
		}
	},
	cw_bool {
		public List<String> identifiers() {
			return List.of("bool", "boolean");
		}
	},
	cw_float {
		public List<String> identifiers() {
			return List.of("float", "fraction");
		}
	} //		public List<String> modifiers() {
	//			return List.of(range);
	//		}
	,
	cw_int {
		public List<String> identifiers() {
			return List.of("int", "integer");
		}
	},
	cw_string {
		public List<String> identifiers() {
			return List.of("string");
		}
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
	},
	advisor_slot,
	event,
	wargoal {
		public List<String> identifiers() {
			return List.of("wargoal", "war_goal");
		}
	},
	;

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
					return p;
				}
			}
		}
		return null;
	}

	public List<String> identifiers() {
		return List.of(this.name());
	}

	// todo better
//	public List<String> modifiers() {
//		return null;
//	}

//	final String range = "@range";
}
