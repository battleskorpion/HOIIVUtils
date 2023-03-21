package clausewitz_coding.idea;

import clausewitz_coding.code.modifier.Modifier;

import java.io.File;
import java.util.ArrayList;

public abstract class Idea {

	/* static */
	protected static ArrayList<Idea> idea_list;

	/* idea */
	protected String ideaID;
//	private ArrayList<String> loc_name;
	protected ArrayList<Modifier> modifiers;
	protected File file; 			// file idea is defined in

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

		if(ideas.size() == 0) {
			return null;
		}
		return ideas;
	}
	
	public String toString() {
		return name(); 
	}

	private String name() {
		return null; 		//TODO 
	}

	// todo this had a purpose likely, what was it :(
//	public static ArrayList<String> find(File idea_file) throws IOException {
//		Scanner ideaReader = new Scanner(idea_file);
//		idea_list = new ArrayList<String>();
//
//		// make a list of all idea names
//		//boolean findIdeaName = false;
//		//int idea_list_index;  // index of focus name in string
//		while (ideaReader.hasNextLine()) {
//			String data = ideaReader.nextLine().replaceAll("\\s", "");
////
//			if(usefulData(data)) {
//				// need enough data length before checks are made to prevent error
//				if(data.length() >= 5) {
//					// if theoretical TAG at beginning of idea name is in the list of tags
//					// (this means the text we have is likely an idea since the tag exists)
//					if (CountryTags.list().contains(data.trim().substring(0, 3))) {
//						// if this is likely an idea, then check if there is " = {"
//						// at the end to confirm likelihood
//						// also, a tag is TAG, all caps. check for this after.
//						if (data.trim().substring(data.length() - 2, data.length()).equals("={")) {
//							if(data.trim().substring(0, 3).equals(data.trim().substring(0, 3).toUpperCase())) {
//								// if here, ***should*** be good! data is an idea name,
//								// once we clean it up
//
//								// return idea var name, remove "={"
//								idea_list.add(data.trim().substring(0, data.length() - 2));
//							}
//						}
//					}
//				}
//			}
//		}
//		ideaReader.close();
//
//		return idea_list;
//	}

}

