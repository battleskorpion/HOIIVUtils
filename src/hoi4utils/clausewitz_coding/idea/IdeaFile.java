package hoi4utils.clausewitz_coding.idea;

import hoi4utils.clausewitz_parser.Expression;
import hoi4utils.clausewitz_parser.Parser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.*;

public class IdeaFile extends File {

	private final HashMap<String, Idea> ideas;
	private final Parser ideaFileParser;

	/**
	 * Creates a new {@code File} instance by converting the given
	 * pathname string into an abstract pathname.  If the given string is
	 * the empty string, then the result is the empty abstract pathname.
	 *
	 * @param pathname A pathname string
	 * @throws NullPointerException If the {@code pathname} argument is {@code null}
	 */
	public IdeaFile(String pathname) {
		super(pathname);

		ideas = new HashMap<>();
		ideaFileParser = new Parser(this);
		parse();
	}

	public Parser getParser() {
		return ideaFileParser;
	}

	public HashSet<Idea> ideas() {
		return new HashSet<>(ideas.values());
	}

	public ObservableList<Idea> listIdeas() {
		ObservableList<Idea> ideaList = FXCollections.observableArrayList();
		ideaList.addAll(ideas());
		return ideaList;
	}

	private ArrayList<String> parse() {
		/* format of an idea file:
			ideas = {
				idea_category_1 = {
					idea_1 = {
					}
					idea_2 = {
					}
				}
				idea_category_2 = {
					idea_3 = {
					}
				}
			}
		*/
		if (!this.exists()) {
			System.err.println(this + "Idea file does not exist.");
			return null;
		}

		/* parser */
		Expression ideaFileExp = ideaFileParser.expression();

		/*
		.get("ideas={").getSubexpressions() will be the subexps for
		idea categories.
		then the further .getSubexpressions() will be for each idea.
		 */
		Expression[] ideasCategoriesExps = ideaFileExp.get("ideas={").getSubexpressions();
		ArrayList<ArrayList<Expression>> ideasExps = new ArrayList<>();
		for (Expression ideaCategoryExp : ideasCategoriesExps) {
			ArrayList<Expression> ideasList
					= new ArrayList<>(Arrays.stream(ideaCategoryExp.getSubexpressions())
					.filter(IdeaFile::isValidIdeaIdentifierExpression).toList());
			System.out.println(ideasList);
			ideasExps.add(ideasList);
		}

		if (ideasExps.isEmpty()) {
			System.err.println("ideasExps empty " + this.getClass());
			return null;
		}
		System.out.println("Num focuses detected: " + ideasExps.size());

		/* focuses */
		ArrayList<String> idea_names = new ArrayList<>();

		for (int i = 0; i < ideasExps.size(); i++) {
			for (Expression ideaExp : ideasExps.get(i)) {
				Idea idea;

				/* idea id */
				{
//					Expression ideaIDExp = ideaExp.get("id=");
//					Expression ideaIDExp = ideaExp.getText();
//					if (ideaIDExp == null) {
//						continue; // id important
//					}
					String idea_id = ideaExp.getText(); // gets the ##### from "id = #####"
					if (idea_id == null) {
						continue; // id important
					}
					idea = Idea.loadIdea(idea_id, ideaExp, ideasCategoriesExps[i]);
					idea_names.add(idea_id);
					ideas.put(idea_id, idea);
				}
			}
		}

		return idea_names;
	}

	public static boolean isValidIdeaIdentifierExpression(Expression exp) {
		if (exp.expression().matches("\\s*[[A-Z][a-z]_]*=\\{\\s*")) {
			return true;
		}

		return false;
	}

}
