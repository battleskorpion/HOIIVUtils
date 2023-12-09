package com.HOIIVUtils.hoi4utils.clausewitz_data.focus;

import clausewitz_parser.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PendingFocusReference {
	private final String pendingFocusReferenceID;
	private final Map<Consumer<List<Node>>, List<Node>> pendingActionMap;

	public PendingFocusReference(String pendingFocusId, List<Consumer<List<Node>>> pendingActionList, List<List<Node>> argsList) {
		this.pendingFocusReferenceID = pendingFocusId;
		this.pendingActionMap = new HashMap<>();
		addPendingActions(pendingActionList, argsList);
	}

	public PendingFocusReference(String pendingFocusId, Consumer<List<Node>> pendingAction, List<Node> args) {
		this.pendingFocusReferenceID = pendingFocusId;
//		this.pendingActionMap = List.of(pendingAction);
		this.pendingActionMap = new HashMap<>();
		pendingActionMap.put(pendingAction, args);
	}

	/**
	 * id of the pending focus being referenced
	 * @return
	 */
	public String id() {
		return pendingFocusReferenceID;
	}

	public Map<Consumer<List<Node>>, List<Node>> pendingActionMap() {
		return pendingActionMap;
	}

	public void addPendingAction(Consumer<List<Node>> pendingAction, List<Node> args) {
		this.pendingActionMap.put(pendingAction, args);
	}


	public void addPendingActions(List<Consumer<List<Node>>> pendingActions, List<List<Node>> argsList) {
		for (int i = 0; i < pendingActions.size(); i++) {
			pendingActionMap.put(pendingActions.get(i), argsList.get(i));
		}
	}

	public void resolve() {
		pendingActionMap.forEach(Consumer::accept);
	}
}
