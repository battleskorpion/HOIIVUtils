// Generated from org\jgrapht\nio\csv\CSV.g4 by ANTLR 4.12.0
package org.jgrapht.nio.csv;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
class CSVLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, SEPARATOR=3, TEXT=4, STRING=5;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "SEPARATOR", "TEXT", "TEXTCHAR", "STRING"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\\r'", "'\\n'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "SEPARATOR", "TEXT", "STRING"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	    char sep = ',';

	    public void setSep(char sep)
	    {
	        this.sep = sep;
	    }

	    private char getSep()
	    {
	        return sep;
	    }


	public CSVLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CSV.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 2:
			return SEPARATOR_sempred((RuleContext)_localctx, predIndex);
		case 4:
			return TEXTCHAR_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean SEPARATOR_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return  _input.LA(1) == sep ;
		}
		return true;
	}
	private boolean TEXTCHAR_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return  (_input.LA(1) != sep && _input.LA(1) != '\n' && _input.LA(1) != '\r' && _input.LA(1) != '"') ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0000\u0005\'\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0004\u0003"+
		"\u0016\b\u0003\u000b\u0003\f\u0003\u0017\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005!\b"+
		"\u0005\n\u0005\f\u0005$\t\u0005\u0001\u0005\u0001\u0005\u0000\u0000\u0006"+
		"\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0000\u000b\u0005\u0001"+
		"\u0000\u0001\u0001\u0000\"\"(\u0000\u0001\u0001\u0000\u0000\u0000\u0000"+
		"\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000"+
		"\u0007\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0001"+
		"\r\u0001\u0000\u0000\u0000\u0003\u000f\u0001\u0000\u0000\u0000\u0005\u0011"+
		"\u0001\u0000\u0000\u0000\u0007\u0015\u0001\u0000\u0000\u0000\t\u0019\u0001"+
		"\u0000\u0000\u0000\u000b\u001c\u0001\u0000\u0000\u0000\r\u000e\u0005\r"+
		"\u0000\u0000\u000e\u0002\u0001\u0000\u0000\u0000\u000f\u0010\u0005\n\u0000"+
		"\u0000\u0010\u0004\u0001\u0000\u0000\u0000\u0011\u0012\u0004\u0002\u0000"+
		"\u0000\u0012\u0013\t\u0000\u0000\u0000\u0013\u0006\u0001\u0000\u0000\u0000"+
		"\u0014\u0016\u0003\t\u0004\u0000\u0015\u0014\u0001\u0000\u0000\u0000\u0016"+
		"\u0017\u0001\u0000\u0000\u0000\u0017\u0015\u0001\u0000\u0000\u0000\u0017"+
		"\u0018\u0001\u0000\u0000\u0000\u0018\b\u0001\u0000\u0000\u0000\u0019\u001a"+
		"\u0004\u0004\u0001\u0000\u001a\u001b\t\u0000\u0000\u0000\u001b\n\u0001"+
		"\u0000\u0000\u0000\u001c\"\u0005\"\u0000\u0000\u001d\u001e\u0005\"\u0000"+
		"\u0000\u001e!\u0005\"\u0000\u0000\u001f!\b\u0000\u0000\u0000 \u001d\u0001"+
		"\u0000\u0000\u0000 \u001f\u0001\u0000\u0000\u0000!$\u0001\u0000\u0000"+
		"\u0000\" \u0001\u0000\u0000\u0000\"#\u0001\u0000\u0000\u0000#%\u0001\u0000"+
		"\u0000\u0000$\"\u0001\u0000\u0000\u0000%&\u0005\"\u0000\u0000&\f\u0001"+
		"\u0000\u0000\u0000\u0004\u0000\u0017 \"\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}