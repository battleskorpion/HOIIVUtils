package clausewitz_parser_new;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	private String input;
	private Pattern pattern;
	private Matcher matcher;
	private int find_start_index = 0;
	private boolean peekOccurred = false;

	// todo ex: id = SMI_Support_Black_Legio... too long?, no good.
	public Tokenizer(String input) {
		this.input = input;
		this.pattern = createPattern();
		this.matcher = this.pattern.matcher(input);
	}

	// todo unknown if good
	public Token next() {
		try {
		/* matcher.find(int) resets the matcher, therefore,
		only use this call when necessary */
		if (peekOccurred) {
			if (matcher.find(find_start_index)) {
				String value = matcher.group(0);
				int start = matcher.start();
				peekOccurred = false;
				return new Token(value, start);
			}
		}
		else if (matcher.find()) {
			String value = matcher.group(0);
			int start = matcher.start();
			/* peek occurred already false */
			return new Token(value, start);
		}
		return null;
		} catch (StackOverflowError e) {
			System.err.println("EEEE"); // todo
			System.err.println(matcher.group());
			return null;
		}
	}

	/**
	 * <code> Matcher </code> has no peek() function therefore this logic is necessary.
	 * returns the next token from matcher, the next <code> find() </code> call to
	 * <code> matcher </code> however will be from the same start index.
	 * @see Matcher
	 * @return Next token from matcher
	 */
	public Token peek() {
		if (peekOccurred) {
			if (matcher.find(find_start_index)) {
				String value = matcher.group();
				int start = matcher.start();
				/* find_start_index remains the same */
				/* peek occurred already true */
				return new Token(value, start);
			}
		} else if (matcher.find()) {
			String value = matcher.group();
			int start = matcher.start();
			find_start_index = matcher.start();
			peekOccurred = true;
			return new Token(value, start);
		}

		return null;
	}

	private Pattern createPattern() {
		StringBuilder patternBuilder = new StringBuilder();

		for (TokenType tokenType : TokenType.values()) {
			Pattern tokenRegex = Token.tokenRegex.get(tokenType);
			if (tokenRegex != null) {
				patternBuilder.append(String.format("|(?<%s>%s)", tokenType, tokenRegex.pattern()));
			}
		}

		if (!patternBuilder.isEmpty()) {
			return Pattern.compile(patternBuilder.substring(1));  // Skip the leading "|"
		} else {
			throw new IllegalStateException("No patterns found for token types.");
		}
	}
}
