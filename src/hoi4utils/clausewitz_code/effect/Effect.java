package hoi4utils.clausewitz_code.effect;

import clausewitz_parser.Node;
import clausewitz_parser.NodeValue;
import hoi4utils.clausewitz_code.scope.Scope;
import hoi4utils.clausewitz_code.scope.ScopeType;

import java.util.*;

/**
 * For information: <a href="https://hoi4.paradoxwikis.com/Effect">Effects Wiki</a>
*/
public class Effect implements EffectParameter, Cloneable {
	public static final SortedMap<String, Effect> effects = new TreeMap<>();

	private final String identifier;
	//////private EffectParameter parameters = null;  // null by default
	private NodeValue parametersNode;
	private List<EffectParameter> parameters = null;
	private List<Parameter> requiredParameters;

	private final EnumSet<ScopeType> supportedScopes;
	private final EnumSet<ScopeType> supportedTargets;   // can be null

	private Scope withinScope = null;
	private Scope targetScope;

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes, EnumSet<ScopeType> supportedTargets, List<Parameter> requiredParameters) {
		this.identifier = identifier;
		this.supportedScopes = supportedScopes;
		this.supportedTargets = supportedTargets;
		this.requiredParameters = requiredParameters;

		effects.put(identifier, this);
		System.out.println("effect: " + identifier);
	}

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes, List<Parameter> requiredParameters) {
		this(identifier, supportedScopes, null, requiredParameters);
	}

//	public static Effect of(String identifier, ScopeType scope, ScopeType targetScope) {
//		Effect effect = effects.get(identifier);
//		if (!effect.supportedScopes.contains(scope)) {
//			System.err.println("Effect was not returned: " + identifier
//					+ " does not support scope " + targetScope);
//			return null;
//		}
//		if (effect.supportedTargets != null && !effect.supportedTargets.contains(targetScope)) {
//			System.err.println("Effect was not returned: " + identifier
//					+ " does not support target scope " + targetScope);
//			return null;
//		}
//		return effect;
//	}

	public static Effect of(String identifier, Scope scope) {
		Effect effect;
		effect = effects.get(identifier);
		if (effect == null) {
			return null;
		}
		if (!effect.isSupportedInScope(scope)) {
			System.err.println("Effect was not returned: " + identifier
					+ " is not supported in scope " + scope);
			return null;
		}
		//		if (effect.checkSupportedTarget(scope)) {
//			System.err.println("Effect was not returned: " + identifier
//					+ " does not support target scope " + scope.targetScope);
//			return null;
//		}

		try {
			effect = (Effect) effect.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

		effect.withinScope = scope;
		return effect;
	}

	public static Effect of(String identifier, Scope scope, NodeValue params) throws InvalidEffectParameterException {
		Effect effect;
		effect = of(identifier, scope);
		if (effect == null) return null;

		effect.setParameters(params);
		return effect;
	}

	public boolean isSupportedInScope(Scope scope) {
		if (this.supportedScopes.contains(ScopeType.any)) return true;
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

//	public EffectParameter parameters() {
//		return parameters;
//	}

//	public void setParameters(EffectParameter parameters) {
//		this.parameters = parameters;
//	}

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
	public void setParameters(NodeValue value) throws InvalidEffectParameterException {
		this.parametersNode = value;
//		if (parametersNode.isList()) {
//			List<Node> list = parametersNode.list();
//			for (Node n : list) {
//				NodeValue v = n.value();
//				if (!v.isList()) parameters.add(new Parameter(n.name, v));
//				else {
//					Effect subeffect = Effect.of(n.name, this.withinScope(), n.value());
//					if (subeffect != null) {
////						s = scope;
//						/* if target, add effect with target */
//						if (subeffect.hasSupportedTargets()) {
//							try {
////								effect.setTarget(n.value().string(), scope);
//								// todo how to set target in this case
//							} catch (Exception e) {
//								throw new RuntimeException(e); // todo
//							}
//						}
////						subeffect.setParameters(n.value()); use new of() func.
//						parameters.add(subeffect);
//					} else {
//						System.out.println("Effect param unknown: " + n.name);
//					}
//				}
//			}
//		}
//		else {
//			parameters.add(new Parameter(value));
//		}
		if (parametersNode.isList()) {
			List<Node> list = parametersNode.list();
			for (Node n : list) {
				NodeValue v = n.value();
				//if (!v.isList()) parameters.add(new Parameter(n.name, v));
				if (!v.isList()) parameters.add(Parameter.of(n.name, v));
				else {
					Effect subeffect = Effect.of(n.name, this.withinScope(), n.value());
					if (subeffect != null) {
//						s = scope;
//						/* if target, add effect with target */
//						if (subeffect.hasSupportedTargets()) {
//							try {
//								effect.setTarget(n.value().string(), scope);
//								// todo how to set target in this case
//							} catch (Exception e) {
//								throw new RuntimeException(e); // todo
//							}
//						}
						parameters.add(subeffect);
					} else if (!Parameter.isParameter(n.name, v)) {
						throw new InvalidEffectParameterException("Effect parameter unknown: " + n.name);
					} else {
						//parameters.add(new Parameter(n.name, v));
						parameters.add(Parameter.of(n.name, v));
					}
				}
			}
		} else {
			//parameters.add(new Parameter(value));
			parameters.add(Parameter.of(null, value));
			if (!acceptsParameter(value)) {
				throw new InvalidEffectParameterException("Invalid parameter for effect " + this + ": " + value);
			}
		}

		// handle required/acceptable parameters
	}

	private boolean acceptsParameter(NodeValue value) {
		// TODO
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
		if (!parametersNode.isList()) return parametersNode.asString();
		else return "[parameter block]";
	}

	// todo to parameters() ?
	public List<EffectParameter> parameterList() {
//		if (parametersNode.isList()) return parametersNode.list().stream().map((v) -> v.value().asString()).toList();
//		else {
//			return List.of(value());
//		}
		return parameters;
	}

	public boolean isScope(Scope of) {
		return targetScope != null && targetScope.name.equals(of.name);
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
		if (parametersNode == null) return false;
		return parametersNode.isList();
	}

	@Override
	public String displayParameter() {
		StringBuilder s = new StringBuilder();

		s.append(name());
		s.append(" = ");
		if (!parametersNode.isList()) {
			s.append(parametersNode.asString());
		}
		else {
			s.append("{\n");
			for (EffectParameter p : parameters) {
				s.append("\t");
				s.append(p == null ? "[effect parameter was null]" : p.displayParameter());
				s.append("\n");
			}
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
}
