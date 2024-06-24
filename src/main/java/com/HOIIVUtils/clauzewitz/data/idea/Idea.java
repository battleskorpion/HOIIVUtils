package com.HOIIVUtils.clauzewitz.data.idea;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clauzewitz.code.effect.EffectParameter;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clauzewitz.code.modifier.Modifier;
import com.HOIIVUtils.clauzewitz.localization.Localization;
import javafx.beans.property.SimpleStringProperty;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This is the Idea file.
 */
public class Idea implements Localizable, EffectParameter, Comparable<Idea> {

	/* static */
	protected static ArrayList<Idea> idea_list; // todo change to hash etc.

	/* idea */
	protected SimpleStringProperty id;
	// private ArrayList<String> loc_name;
	protected File file; // file idea is defined in
	protected ArrayList<Modifier> modifiers;
	protected int removalCost = -1; // -1 default
	protected Localization localization;

	protected Idea(String id) {
		this.id = new SimpleStringProperty(id);
		//this.setLocalization();
	}

	public static List<Function<Idea, ?>> getDataFunctions() {
		List<Function<Idea, ?>> dataFunctions = new ArrayList<>(2);         // for optimization, limited number of data functions.

		dataFunctions.add(idea -> idea.localization.ID());
		dataFunctions.add(idea -> idea.localization.text());

		return dataFunctions;
	}

	protected void addModifier(Modifier modifier) {
		modifiers.add(modifier);
	}

	public Idea getIdea(String ideaID) {
		for (Idea idea : idea_list) {
			if (idea.id().equals(ideaID)) {
				return idea;
			}
		}

		return null;
	}

	public int getRemovalCost() {
		return removalCost;
	}

	public void setRemovalCost(int removalCost) {
		if (removalCost < -1) {
			this.removalCost = -1;
		} else {
			this.removalCost = removalCost;
		}
	}

	public static ArrayList<Idea> getIdeas() {
		return idea_list;
	}

	public static ArrayList<Idea> getIdeas(File file) {
		if (!file.exists() || file.isDirectory()) {
			return null;
		}

		ArrayList<Idea> ideas = new ArrayList<>();

		for (Idea idea : idea_list) {
			if (idea.file.equals(file)) {
				ideas.add(idea);
			}
		}

		if (ideas.isEmpty()) {
			return null;
		}
		return ideas;
	}

	public String toString() {
		return name();
	}

	private String name() {
		return null; // todo
	}

	public String id() {
		if (id == null) {
			return null;
		}
		return id.get();
	}

	public void setID(String id) {
		this.id.set(id);
	}

//	private void setID(Expression exp) {
//		if (exp == null) {
//			id = null;
//			FXWindow.openGlobalErrorWindow("Expression was null for setting idea ID.");
//			return;
//		}
//
//		id = new SimpleStringProperty(exp.getText());
//	}


//	public void setLocalization() {
//		setLocalization(id(), Localization.Status.DEFAULT);
//	}

//	public void setLocalization(LocalizationFile localization) {
//        this.localization = localization.getLocalization(id());
//	}

//	/**
//	 * Sets name localization and decides the status.
//	 * @param text
//	 */
//	public void setLocalization(String text) {
//		if (localization == null) {
//			setLocalization(text, Localization.Status.DEFAULT);
//			return;
//		}
//
//		Localization.Status status;
//
//		if (localization.status() == Localization.Status.NEW)
//		{
//			status = Localization.Status.NEW;
//		}
//		else {
//			// including if nameLocalization.status() == Localization.Status.DEFAULT, itll now be updated
//			status = Localization.Status.UPDATED;
//		}
//
//		setLocalization(text, status);
//	}

//	/**
//	 * Sets localization with a specific status. Only use if specifying status is necessary.
//	 * @param text
//	 * @param status
//	 */
//	public void setLocalization(String text, Localization.Status status) {
//		if (localization == null) {
//			localization = new Localization(id(), text, null, status);
//			return;
//		}
//
//		String id = localization.ID();
//		localization = new Localization(id, text, null, status);
//	}

	public Localization localization() {
		return localization;
	}

	public static Idea loadIdea(String ideaId, Node ideaExp, String ideaCategory) {

		if (ideaCategory == null) {
			System.out.println("error: null category for idea: " + ideaId + ", idea exp: ");
			System.out.println(ideaExp);
			return null;    // todo area
		}
		return switch (ideaCategory) {
//			case "economy" -> new EconomyIdea(ideaId);
//			case "country" -> new CountryIdea(ideaId);
//			case "manpower" -> new ManpowerIdea(ideaId);
			default -> ideaId == null ? null : new Idea(ideaId);
		};
	}

	public void setLocalization(Localization newLoc) {
		this.localization = newLoc;
	}

	@Override
	public String displayScript() {
		return "[n/a - idea]";
	}

	@Override
	public int compareTo(@NotNull Idea o) {
		return id().compareTo(o.id());
	}

	@Override
	public @NotNull Map<Property, String> getLocalizableProperties() {
		return Map.of(Property.NAME, id());
	}

	@Override
	public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
		return List.of(this);
		// in future return list of ideas used in similar files.
	}
}
