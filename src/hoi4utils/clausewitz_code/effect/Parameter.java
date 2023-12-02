package hoi4utils.clausewitz_code.effect;

import clausewitz_parser.NodeValue;

import java.util.*;

public class Parameter implements EffectParameter, Cloneable {
	private static final HashMap<String, Parameter> allParameters = new HashMap<>();
//	private static final HashSet<Parameter> allValidParameters = new HashSet<>();
//	static {
//		Parameter p = new Parameter(null, ParameterValueType.);
//		allValidParameters.add(p);
//	}
final List<ParameterValueType> allowedParameterValueTypes;


	final String identifier;
	ParameterValueType parameterValueType;  // todo necessary?
	ParameterValue value;

	Parameter(String name, List<ParameterValueType> allowedParameterValueTypes) {
		this.identifier = name;
		this.allowedParameterValueTypes = allowedParameterValueTypes;
		allParameters.put(name, this);
	}

	Parameter(String name, ParameterValueType allowedParameterValueType) {
		this.identifier = name;
		if (allowedParameterValueType == null) {
			this.allowedParameterValueTypes = null;
		} else {
			this.allowedParameterValueTypes = List.of(allowedParameterValueType);
		}
		allParameters.put(name, this);
	}

	Parameter(String name) {
		this(name, (ParameterValueType) null);
	}

	public static Parameter of(String name, NodeValue v) {
		Parameter parameter = getClone(name);
		if (parameter == null) return null;
		parameter.value = new ParameterValue(v);
		return parameter;
	}

	public static Parameter of(NodeValue v) {
		return of(null, v);
	}

	private static Parameter getClone(String name) {
		Parameter clone;
		try {
			Parameter parameter = allParameters.get(name);
			if (parameter == null) return null;
			clone = (Parameter) parameter.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		return clone;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Parameter c = (Parameter) super.clone();
		c.value = null;
		return c;
	}

//	public static void addValidParameter(String identifier, Set<ParameterValueType> validParameterValueTypes) {
////		if (!allParameters.keySet().contains(identifier)) {
////			Parameter p = new Parameter(identifier, validParameterValueTypes);
////			allParameters.put(identifier, p);
////		} else {
////			allParameters.get(identifier).addValidParameterValueTypes(validParameterValueTypes);
////		}
//	}

	public static boolean isParameter(String name, NodeValue v) {
		if (v.isString()) {
			return allParameters.containsKey(v.string());
		}
		if (v.isNumber()) {
			return true;
		}
		if (v.isList()) {
			// todo not sure
			return true;    // maybe? hm..
		}

		return false;
	}

	@Override
	public String displayParameter() {
//		return "[parameter]";
		if (identifier == null) {
			return value.toString();
		}
		return identifier + " = " + value.toString();
	}

	private static class ParameterValue {
		public Object value;

		public ParameterValue(String value) {
			this.value = value;
		}

		public ParameterValue(NodeValue value) {
			if (!value.isList()) {
				this.value = value.valueObject();
			} else {
				System.err.println("effect value can not be list.");
			}
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			if (value == null) {
				return "[null parameter value]";
			}

			if (value instanceof List) {
//				s.append(identifier);
//				s.append(" = ");
//				s.append("{\n");
//				for (var n : (List<Node>) value) {
//					s.append("\t");
//					s.append(n.);
//					s.append("\n");
//				}
//				s.append("}\n");
				s.append("[parameter -> list]");
			} else {
				s.append(value.toString());
			}
			return s.toString();
		}
	}

}
