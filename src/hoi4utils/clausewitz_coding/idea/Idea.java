package hoi4utils.clausewitz_coding.idea;

import hoi4utils.clausewitz_coding.code.modifier.Modifier;
import hoi4utils.clausewitz_coding.country.CountryTags;
import hoi4utils.clausewitz_parser.Expression;
import hoi4utils.clausewitz_parser.Parser;

import java.io.File;
import java.util.ArrayList;

import static hoi4utils.HOIIVUtils.usefulData;
/**
 * This is the Idea file.
 */
public abstract class Idea {

	/* static */
	protected static ArrayList<Idea> idea_list; // todo change to hash etc.

	/* idea */
	protected String ideaID;
	// private ArrayList<String> loc_name;
	protected File file; // file idea is defined in
	protected ArrayList<Modifier> modifiers;
	protected int removalCost = -1; // -1 default

	protected Idea(String ideaID) {
		this.ideaID = ideaID;
	}

	protected void addModifier(Modifier modifier) {
		modifiers.add(modifier);
	}

	public Idea getIdea(String ideaID) {
		for (Idea idea : idea_list) {
			if (idea.ideaID.equals(ideaID)) {
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

	@SuppressWarnings("SuspiciousMethodCalls")
	public static ArrayList<String> load(File idea_file) {
		// Scanner ideaReader = new Scanner(idea_file);
		idea_list = new ArrayList<>();

		// make a list of all idea names
		int idea_list_index; // index of idea name in string
		Parser ideaParser = new Parser(idea_file);
		Expression[] data = ideaParser.findAll();
		for (Expression exp : data) {
			String s = exp.getText();
			if (s == null) {
				continue;
			}
			s = s.trim();

			// need enough data length before checks are made to prevent error
			if (usefulData(s) && s.length() >= 5) {
				if (CountryTags.list().contains(s.substring(0, 3)) && s.startsWith("={", s.length() - 2)) {
					// if here, ***should*** be good! data is an idea name,
					// once we clean it up

					// return idea var name, remove "={"
					String ideaName = s.substring(0, s.length() - 2);
					Idea idea;
					/* find idea type and instantiate idea */
					if (true) { // todo
						idea = new CountryIdea(ideaName);
					} /* else {
							idea = new ManpowerIdea(ideaName);
						}*/
					idea_list.add(idea);
				}
			}
		}
		// return idea_list;
		return null;
	}

}