package com.HOIIVUtils.clauzewitz.code.effect;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;
import com.HOIIVUtils.clauzewitz.code.scope.NotPermittedInScopeException;
import com.HOIIVUtils.clauzewitz.code.scope.Scope;
import com.HOIIVUtils.clauzewitz.code.scope.ScopeType;
import com.HOIIVUtils.clauzewitz.exceptions.InvalidEffectParameterException;
import com.HOIIVUtils.clauzewitz.exceptions.NullParameterTypeException;

import java.util.*;

/**
 * For information: <a href="https://hoi4.paradoxwikis.com/Effect">Effects
 * Wiki</a>
 */
public class Effect implements EffectParameter, Cloneable {
	protected static final SortedMap<String, Effect> effects = new TreeMap<>();

	private final String identifier;
	private NodeValue parametersNode;
	private List<EffectParameter> parameters = null;
	@SuppressWarnings("unused")
	private List<Parameter> requiredParameters;
	@SuppressWarnings("unused")
	private List<Parameter> optionalParameters;

	private final EnumSet<ScopeType> supportedScopes;
	private final EnumSet<ScopeType> supportedTargets; // can be null

	private Scope withinScope = null;
	private Scope targetScope;
	private final boolean isScope;

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes, EnumSet<ScopeType> supportedTargets,
			List<Parameter> requiredParameters, List<Parameter> optionalParameters) {
		this.identifier = identifier;
		this.supportedScopes = supportedScopes;
		this.supportedTargets = supportedTargets;
		this.requiredParameters = requiredParameters;
		this.optionalParameters = optionalParameters;
		this.isScope = Scope.of(this) != null;
		if (this.isScope) {
			try {
				optionalParameters.add(new Parameter("limit", ParameterValueType.scope));
			} catch (NullParameterTypeException e) {
				throw new RuntimeException(e);
			}
		}

		effects.put(identifier, this);
		// System.out.println("effect: " + identifier);
	}

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes, EnumSet<ScopeType> supportedTargets,
			List<Parameter> requiredParameters) {
		this(identifier, supportedScopes, supportedTargets, requiredParameters, null);
	}

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes, List<Parameter> requiredParameters,
			List<Parameter> optionalParameters) {
		this(identifier, supportedScopes, null, requiredParameters, optionalParameters);
	}

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes, List<Parameter> requiredParameters) {
		this(identifier, supportedScopes, null, requiredParameters, null);
	}

	// public static Effect of(String identifier, ScopeType scope, ScopeType
	// targetScope) {
	// Effect effect = effects.get(identifier);
	// if (!effect.supportedScopes.contains(scope)) {
	// System.err.println("Effect was not returned: " + identifier
	// + " does not support scope " + targetScope);
	// return null;
	// }
	// if (effect.supportedTargets != null &&
	// !effect.supportedTargets.contains(targetScope)) {
	// System.err.println("Effect was not returned: " + identifier
	// + " does not support target scope " + targetScope);
	// return null;
	// }
	// return effect;
	// }

	public static Effect of(String identifier, Scope scope) throws NotPermittedInScopeException {
		Effect effect;
		effect = effects.get(identifier);
		if (effect == null) {
			return null;
		}
		if (!effect.isSupportedInScope(scope)) {
			throw new NotPermittedInScopeException("Effect was not returned: " + identifier
					+ " is not supported in scope " + scope);
		}
		// if (effect.checkSupportedTarget(scope)) {
		// System.err.println("Effect was not returned: " + identifier
		// + " does not support target scope " + scope.targetScope);
		// return null;
		// }

		try {
			effect = (Effect) effect.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

		effect.withinScope = scope;
		return effect;
	}

	public static Effect of(String identifier, Scope scope, NodeValue params)
			throws InvalidEffectParameterException, NotPermittedInScopeException {
		Effect effect;
		effect = of(identifier, scope);
		if (effect == null)
			return null;

		effect.setParameters(params);
		return effect;
	}

	public boolean isSupportedInScope(Scope scope) {
		if (this.supportedScopes == null) {
			System.out.println("Error: supported scopes was null for effect " + this);
			return false;
		}
		if (this.supportedScopes.contains(ScopeType.any))
			return true;
		return this.supportedScopes.contains(scope.targetScopeType());
	}

	public boolean checkSupportedTarget(Scope scope) {
		return scope.isPotentialEffectTarget()
				&& this.supportedTargets != null
				&& !this.supportedTargets.contains(scope.targetScopeType());
	}

	public String identifier() {
		return identifier;
	}

	// public EffectParameter parameters() {
	// return parameters;
	// }

	// public void setParameters(EffectParameter parameters) {
	// this.parameters = parameters;
	// }

	public EnumSet<ScopeType> supportedScopes() {
		return supportedScopes;
	}

	public EnumSet<ScopeType> supportedTargets() {
		return supportedTargets;
	}

	public boolean hasSupportedTargets() {
		return (supportedTargets != null && !supportedTargets.isEmpty());
	}

	public void setTarget(Scope target) {
		this.targetScope = target;
	}

	public void setTarget(String string, Scope within) throws Exception {
		setTarget(Scope.of(string, within));
	}

	// todo wip
	public void setParameters(NodeValue value) throws InvalidEffectParameterException, NotPermittedInScopeException {
		StringBuilder excMessage = new StringBuilder();

		this.parametersNode = value;
		if (parametersNode.isList()) {
			List<Node> list = parametersNode.list();
			for (Node n : Objects.requireNonNull(list)) {
				NodeValue v = n.value();
				if (!v.isList()) {
					Parameter p = Parameter.of(n.name(), v);
					if (p == null) {
						/*
						 * this is done here (rather than impl with Parameter) as effects need the scope
						 * of parent effect
						 * thus it makes it simpler to treat Effect as a valid parameter type for other
						 * Effects as well.
						 */
						Effect subeffect;
						if (Scope.of(this) != null) {
							subeffect = Effect.of(n.name(), Scope.of(this), v);
						} else {
							subeffect = Effect.of(n.name(), this.withinScope(), v);
						}
						if (subeffect != null) {
							parameters.add(subeffect);
						} else {
							if (excMessage.isEmpty())
								excMessage.append("In Effect ").append(this.name()).append(": ");
							excMessage.append("\n\t").append("Effect parameter invalid or effect unknown, effect: ")
									.append(n.name())
									.append(", value: ").append(v.asString());
						}
					} else {
						parameters.add(p);
					}
				} else {
					// todo try parameter first?
					/*
					 * this is done here as effects need the scope of parent effect
					 * thus it makes it simpler to treat Effect as a valid parameter type for other
					 * Effects as well.
					 */
					Effect subeffect;
					if (Scope.of(this) != null) {
						subeffect = Effect.of(n.name(), Scope.of(this), v);
					} else {
						subeffect = Effect.of(n.name(), this.withinScope(), v);
					}
					if (subeffect != null) {
						parameters.add(subeffect);
					} else if (!Parameter.isParameter(n.name(), v)) {
						if (excMessage.isEmpty())
							excMessage.append("In Effect ").append(this.name()).append(": ");
						excMessage.append("\n\t").append("Effect parameter unknown: ").append(n.name());
					} else {
						Parameter p = Parameter.of(n.name(), v);
						parameters.add(p);
					}
				}
			}
		} else {
			parameters.add(Parameter.of(null, value));
			if (!acceptsParameter(value)) {
				throw new InvalidEffectParameterException("Invalid parameter for effect " + this + ": " + value);
			}
		}

		if (!excMessage.isEmpty()) {
			throw new InvalidEffectParameterException(excMessage.toString());
		}
		// handle required/acceptable parameters
	}

	private boolean acceptsParameter(NodeValue value) {
		// TODO Build out this method to do whatever "acceptsParameter(NodeValue value)"
		return true;
	}

	private Scope withinScope() {
		return withinScope;
	}

	public String name() {
		return identifier;
	}

	public String value() {
		if (parametersNode == null) {
			return null;
		}
		if (!parametersNode.isList())
			return parametersNode.asString();
		else
			return "[parameter block]";
	}

	// todo to parameters() ?
	public List<EffectParameter> parameterList() {
		// if (parametersNode.isList()) return parametersNode.list().stream().map((v) ->
		// v.value().asString()).toList();
		// else {
		// return List.of(value());
		// }
		return parameters;
	}

	public boolean isScope() {
		// return targetScope != null && targetScope.name.equals(of.name);
		return isScope;
	}

	public Scope targetScope() {
		return targetScope;
	}

	public String target() {
		if (targetScope == null) {
			return "[null target]";
		}
		return targetScope.name;
	}

	public boolean hasTarget() {
		return targetScope != null;
	}

	public boolean hasParameterBlock() {
		if (parametersNode == null)
			return false;
		return parametersNode.isList();
	}

	@Override
	public String displayScript() {
		// StringBuilder s = new StringBuilder();
		//
		// s.append(name());
		// s.append(" = ");
		// if (!parametersNode.isList()) {
		// s.append(parametersNode.asString());
		// }
		// else {
		// s.append("{\n");
		// for (EffectParameter p : parameters) {
		// s.append("\t");
		// s.append(p == null ? "[effect parameter was null]" : p.displayParameter());
		// s.append("\n");
		// }
		// s.append("}\n");
		// }
		// return s.toString();
		return displayScript(1);
	}

	public String displayScript(int tabs) {
		StringBuilder s = new StringBuilder();

		s.append("\t".repeat(Math.max(0, tabs - 1)));
		s.append(name());
		s.append(" = ");
		if (!parametersNode.isList()) {
			s.append(parametersNode.asString());
		} else {
			s.append("{\n");
			for (EffectParameter p : parameters) {
				s.append("\t".repeat(tabs));
				s.append(p == null ? "[effect parameter was null]" : p.displayScript());
				s.append("\n");
			}
			s.append("\t".repeat(Math.max(0, tabs - 1)));
			s.append("}\n");
		}
		return s.toString();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Effect c = (Effect) super.clone();
		c.parameters = new ArrayList<>();

		return c;
	}

	@Override
	public String toString() {
		if (this.name() != null) {
			return this.name();
		} else {
			return super.toString();
		}
	}
}
