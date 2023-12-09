package hoi4utils.clausewitz_code.trigger;

import hoi4utils.clausewitz_code.scope.ScopeType;

import java.util.EnumSet;

public interface Trigger {
	/**
	 * Return scope target type, different triggers can only be used within certain scopes.
	 */
	ScopeType scopeTargetType();

	EnumSet<TriggerParameter> parameters();


}
