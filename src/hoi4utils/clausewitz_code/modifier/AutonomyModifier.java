package hoi4utils.clausewitz_code.modifier;

public class AutonomyModifier implements Modifier {
	public AutonomyModifier() {
//		super(Scope.autonomy);
	}

	@Override
	public Scope getScope() {
		return Scope.autonomy;
	}

	public enum modifiers {
		autonomy_gain,
		autonomy_gain_global_factor,
		autonomy_gain_ll_to_overlord,
		autonomy_gain_ll_to_overlord_factor,
		autonomy_gain_ll_to_subject,
		autonomy_gain_ll_to_subject_factor,
		autonomy_gain_trade,
		autonomy_gain_trade_factor,
		autonomy_gain_warscore,
		autonomy_gain_warscore_factor,
		autonomy_manpower_share,
		can_master_build_for_us,
		cic_to_overlord_factor,
		extra_trade_to_overlord_factor,
		license_subject_master_purchase_cost,
		master_build_autonomy_factor,
		mic_to_overlord_factor,
		overlord_trade_cost_factor,
		subjects_autonomy_gain,
	}
}
