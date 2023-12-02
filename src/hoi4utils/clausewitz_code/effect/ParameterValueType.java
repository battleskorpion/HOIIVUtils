package hoi4utils.clausewitz_code.effect;

import java.util.List;



public enum ParameterValueType {
	character,
	mission,
	decision,
	country {
		public List<String> identifiers() {
			return List.of("country", "tag");
		}
	},
	cw_string {
		public List<String> identifiers() {
			return List.of("string");
		}
	},
	cw_bool {
		public List<String> identifiers() {
			return List.of("bool", "boolean");
		}
	},
	cw_int {
		public List<String> identifiers() {
			return List.of("int", "integer");
		}
	},
	cw_float {
		public List<String> identifiers() {
			return List.of("float");
		}
//		public List<String> modifiers() {
//			return List.of(range);
//		}
	},
	cw_variable {
		public List<String> identifiers() {
			return List.of("variable", "var");
		}
	},
	flag
	;

	public static boolean isParameterValueType(String s) {
		if (s.startsWith("<") && s.endsWith(">")) {
			s = s.substring(1, s.length() - 1); // remove ends
			for (ParameterValueType p : ParameterValueType.values()) {
				if (p.identifiers().contains(s)) {
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
			for (ParameterValueType p : ParameterValueType.values()) {
				if (p.identifiers().contains(s)) {
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
