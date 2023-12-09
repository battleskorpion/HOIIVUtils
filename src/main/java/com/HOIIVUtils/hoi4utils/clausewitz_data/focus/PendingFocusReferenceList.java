package com.HOIIVUtils.hoi4utils.clausewitz_data.focus;

import clausewitz_parser.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PendingFocusReferenceList {
	List<PendingFocusReference> pendingFocusReferences;

	public PendingFocusReferenceList() {
		this.pendingFocusReferences = new ArrayList<>();
	}


	public void addReference(String pendingFocusId, Consumer<List<Node>> pendingAction, List<Node> args) {
		List<String> pendingFocusIDList = pendingFocusReferences.parallelStream()
				.map(PendingFocusReference::id).toList();

		if (pendingFocusIDList.contains(pendingFocusId)) {
			for (PendingFocusReference pfr : pendingFocusReferences) {
				if (pfr.id().equals(pendingFocusId)) {
					pfr.addPendingAction(pendingAction, args);
				}
			}
		} else {
			pendingFocusReferences.add(new PendingFocusReference(pendingFocusId, pendingAction, args));
		}
	}

	public void addReference(String pendingFocusId, List<Consumer<List<Node>>> pendingActions, List<List<Node>> args) {
		List<String> pendingFocusIDList = pendingFocusReferences.parallelStream()
				.map(PendingFocusReference::id).toList();

		if (pendingFocusIDList.contains(pendingFocusId)) {
			for (PendingFocusReference pfr : pendingFocusReferences) {
				if (pfr.id().equals(pendingFocusId)) {
					pfr.addPendingActions(pendingActions, args);
				}
			}
		} else {
			pendingFocusReferences.add(new PendingFocusReference(pendingFocusId, pendingActions, args));
		}
	}

	public boolean removeReference(String pendingFocusId) {
		for (PendingFocusReference pfr : pendingFocusReferences) {
			if (pfr.id().equals(pendingFocusId)) {
				return pendingFocusReferences.remove(pfr);
			}
		}

		return false;
	}

	// todo add a resolve all
	public boolean resolve(String id) {
		for (var reference : pendingFocusReferences) {
			if (reference.id().equals(id)) {
				reference.resolve();
				pendingFocusReferences.remove(reference);
				return true;
			}
		}

		return false;
	}
}
