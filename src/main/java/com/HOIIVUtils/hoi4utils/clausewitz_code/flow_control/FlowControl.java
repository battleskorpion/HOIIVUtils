package com.HOIIVUtils.hoi4utils.clausewitz_code.flow_control;//package hoi4utils.clausewitz_code.flow_control;
//
//import hoi4utils.clausewitz_code.HOI4Script;
//import hoi4utils.clausewitz_code.effect.EffectParameter;
//
//import java.util.HashMap;
//
///**
// * These are triggers that serve as more of a way to establish a connection
// * in how triggers are evaluated. Each one serves as a trigger scope with additional
// * arguments and can be used regardless of scope.
// * <a href="https://hoi4.paradoxwikis.com/Triggers">Hoi4 Wiki</a>
// */
//public class FlowControl implements EffectParameter, HOI4Script {
//	public static HashMap<String, FlowControl> flowControls = new HashMap<>();
//
//	private final String identifier;
//	private final boolean validTriggerParameter;
//	private final boolean validEffectParameter;
//
//	protected FlowControl(String identifier, boolean validTriggerParameter, boolean validEffectParameter) {
//		this.identifier = identifier;
//		this.validTriggerParameter = validTriggerParameter;
//		this.validEffectParameter = validEffectParameter;
//
//		flowControls.put(identifier, this);
//	}
//
//	public static FlowControl of(String identifier, HOI4Script within) {
//		if (within.isTrigger()) {
//
//		} else if (within.isEffect()) {
//
//		} else if (within.isFlowControl()) {
//
//		} else {
//			return null;
//		}
//
//	}
//
////	public enum FlowControlType {
////		AND,
////		OR,
////		NOT,
////		count_triggers,
////		if
////		for_loop_effect,
////		while_loop_effect,
////	}
//
//	@Override
//	public String displayParameter() {
//		return "[flow control]";
//	}
//
//	@Override
//	public boolean isFlowControl() {
//		return true;
//	}
//
//	static {
//		new FlowControl("AND", true, false);
//		new FlowControl("OR", true, false);
//		new FlowControl("NOT", true, false);
//		new FlowControl("if", true, true);
//	}
//}
