package hoi4utils.clausewitz_code.effect;

import clausewitz_parser.NodeValue;

public class Parameter implements EffectParameter {
	final String identifier;
	ParameterValueType parameterValueType;
	ParameterValue value;

	public Parameter(String name, NodeValue v) {
		this.identifier = name;
		this.value = new ParameterValue(v);
	}

	public Parameter(NodeValue v) {
		this.identifier = null;
		this.value = new ParameterValue(v);
	}

	@Override
	public String displayParameter() {
		return "[parameter]";
	}

	private class ParameterValue {
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

//		@Override
//		public String displayParameter() {
//			if (value instanceof String) {
//				return (String) value;
//			}
//			else {
//				return value.toString();
//			}
//		}
	}
}
