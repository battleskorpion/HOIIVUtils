package hoi4utils.clausewitz_coding.idea;

import hoi4utils.clausewitz_parser.Parser;

import java.io.File;
import java.util.ArrayList;

public class IdeaFile extends File {

	private final ArrayList<Idea> ideaList;
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

		ideaList = new ArrayList<>();
		ideaFileParser = new Parser(this);
	}

	public Parser getParser() {
		return ideaFileParser;
	}

	public ArrayList<Idea> listIdeas() {
		return ideaList;
	}
}
