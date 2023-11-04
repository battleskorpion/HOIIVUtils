package hoi4utils.clausewitz_code.effect;

import hoi4utils.clausewitz_code.scope.ScopeType;

import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * For information: <a href="https://hoi4.paradoxwikis.com/Effect">Effects Wiki</a>
*/
public class Effect {
	public static SortedMap<String, Effect> effects = new TreeMap<>();

	private final String identifier;

	private EffectParameter parameters = null;  // null by default

	private final EnumSet<ScopeType> supportedScopes;
	private final EnumSet<ScopeType> supportedTargets;   // can be null

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes, EnumSet<ScopeType> supportedTargets) {
		this.identifier = identifier;
		this.supportedScopes = supportedScopes;
		this.supportedTargets = supportedTargets;

		effects.put(identifier, this);
		System.out.println("effect: " + identifier);
	}

	public Effect(String identifier, EnumSet<ScopeType> supportedScopes) {
		this(identifier, supportedScopes, null);
	}

	public static Effect of(String identifier, ScopeType scope, ScopeType targetScope) {
		Effect effect = effects.get(identifier);
		if (!effect.supportedScopes.contains(scope)) {
			System.err.println("Effect was not returned: " + identifier
					+ " does not support scope " + targetScope);
			return null;
		}
		if (effect.supportedTargets != null && !effect.supportedTargets.contains(targetScope)) {
			System.err.println("Effect was not returned: " + identifier
					+ " does not support target scope " + targetScope);
			return null;
		}
		return effect;
	}

	public String identifier() {
		return identifier;
	}

	public EffectParameter parameters() {
		return parameters;
	}

	public void setParameters(EffectParameter parameters) {
		this.parameters = parameters;
	}

	public EnumSet<ScopeType> supportedScopes() {
		return supportedScopes;
	}

	public EnumSet<ScopeType> supportedTargets() {
		return supportedTargets;
	}

}
