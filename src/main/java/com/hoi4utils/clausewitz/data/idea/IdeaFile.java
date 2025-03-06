package com.hoi4utils.clausewitz.data.idea;

import com.hoi4utils.clausewitz_parser.Node;
import com.hoi4utils.clausewitz_parser.Parser;
import com.hoi4utils.clausewitz_parser.ParserException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import scala.jdk.javaapi.CollectionConverters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

	/**
	 * <p>format of an idea file:
	 * ideas = {
	 * idea_category_1 = {
	 * idea_1 = {
	 * }
	 * idea_2 = {
	 * }
	 * }
	 * idea_category_2 = {
	 * idea_3 = {
	 * }
	 * }
	 * }
	 * </p>
	 */
	private void parse() {
		if (!this.exists()) {
			System.err.println(this + "Idea file does not exist.");
			return;
		}

		/* parser */
		Node ideaCategoryExps;
		try {
			ideaCategoryExps = ideaFileParser.parse();
			ideaCategoryExps = ideaCategoryExps.find("ideas").getOrElse(null);
		} catch (ParserException e) {
			throw new RuntimeException(e);
		}

	List<Node> ideaCategoryNodeList = ideaCategoryExps != null ? ideaCategoryExps.$list().getOrElse(null) : null;
	/* return null when no ideas */
	if (ideaCategoryNodeList == null) {
		return;
	}
	
	/* focuses */
	ArrayList<String> idea_names = new ArrayList<>();
	for (Node ideaCategoryNode : ideaCategoryNodeList) {
		List<Node> ideasInCategoryList = null;
		try {
			ideasInCategoryList = CollectionConverters.asJava(ideaCategoryNode
					.filter(Node::isParent).toList());
		} catch (NullPointerException e) {
			System.err.println("Error converting Scala collection to Java: " + e.getMessage());
		}
		if (ideasInCategoryList == null) {
			continue;
		}
		String ideaCategory = ideaCategoryNode.name();
		for (Node ideaExp : ideasInCategoryList) {
			Idea idea;
	
			/* idea id */
			String idea_id = ideaExp.name();
			//idea = Idea.loadIdea(idea_id, ideaExp, ideaCategory); // TODO?
//			idea_names.add(idea_id);
//			ideas.put(idea_id, idea);
		}
	}

		System.out.println("Num ideas loaded: " + idea_names.size());
	}
}
