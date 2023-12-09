// Generated from org\jgrapht\nio\gml\Gml.g4 by ANTLR 4.12.0
package org.jgrapht.nio.gml;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
class GmlLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, NUMBER=3, STRING=4, ID=5, COMMENT=6, WS=7;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "NUMBER", "DIGIT", "LETTER", "STRING", "ID", "COMMENT", 
			"WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "']'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "NUMBER", "STRING", "ID", "COMMENT", "WS"
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


	public GmlLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Gml.g4"; }

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

	public static final String _serializedATN =
		"\u0004\u0000\u0007Y\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0003\u0002\u0019\b\u0002\u0001\u0002\u0001\u0002\u0004"+
		"\u0002\u001d\b\u0002\u000b\u0002\f\u0002\u001e\u0001\u0002\u0004\u0002"+
		"\"\b\u0002\u000b\u0002\f\u0002#\u0001\u0002\u0001\u0002\u0005\u0002(\b"+
		"\u0002\n\u0002\f\u0002+\t\u0002\u0003\u0002-\b\u0002\u0003\u0002/\b\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0005\u00059\b\u0005\n\u0005\f\u0005<\t\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006"+
		"C\b\u0006\n\u0006\f\u0006F\t\u0006\u0001\u0007\u0001\u0007\u0005\u0007"+
		"J\b\u0007\n\u0007\f\u0007M\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0004\bT\b\b\u000b\b\f\bU\u0001\b\u0001\b\u0002:"+
		"K\u0000\t\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0000\t\u0000\u000b"+
		"\u0004\r\u0005\u000f\u0006\u0011\u0007\u0001\u0000\u0003\u0001\u00000"+
		"9\u0004\u0000AZ__az\u0080\u00ff\u0003\u0000\t\n\r\r  b\u0000\u0001\u0001"+
		"\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001"+
		"\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000"+
		"\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000"+
		"\u0000\u0000\u0001\u0013\u0001\u0000\u0000\u0000\u0003\u0015\u0001\u0000"+
		"\u0000\u0000\u0005\u0018\u0001\u0000\u0000\u0000\u00070\u0001\u0000\u0000"+
		"\u0000\t2\u0001\u0000\u0000\u0000\u000b4\u0001\u0000\u0000\u0000\r?\u0001"+
		"\u0000\u0000\u0000\u000fG\u0001\u0000\u0000\u0000\u0011S\u0001\u0000\u0000"+
		"\u0000\u0013\u0014\u0005[\u0000\u0000\u0014\u0002\u0001\u0000\u0000\u0000"+
		"\u0015\u0016\u0005]\u0000\u0000\u0016\u0004\u0001\u0000\u0000\u0000\u0017"+
		"\u0019\u0005-\u0000\u0000\u0018\u0017\u0001\u0000\u0000\u0000\u0018\u0019"+
		"\u0001\u0000\u0000\u0000\u0019.\u0001\u0000\u0000\u0000\u001a\u001c\u0005"+
		".\u0000\u0000\u001b\u001d\u0003\u0007\u0003\u0000\u001c\u001b\u0001\u0000"+
		"\u0000\u0000\u001d\u001e\u0001\u0000\u0000\u0000\u001e\u001c\u0001\u0000"+
		"\u0000\u0000\u001e\u001f\u0001\u0000\u0000\u0000\u001f/\u0001\u0000\u0000"+
		"\u0000 \"\u0003\u0007\u0003\u0000! \u0001\u0000\u0000\u0000\"#\u0001\u0000"+
		"\u0000\u0000#!\u0001\u0000\u0000\u0000#$\u0001\u0000\u0000\u0000$,\u0001"+
		"\u0000\u0000\u0000%)\u0005.\u0000\u0000&(\u0003\u0007\u0003\u0000\'&\u0001"+
		"\u0000\u0000\u0000(+\u0001\u0000\u0000\u0000)\'\u0001\u0000\u0000\u0000"+
		")*\u0001\u0000\u0000\u0000*-\u0001\u0000\u0000\u0000+)\u0001\u0000\u0000"+
		"\u0000,%\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-/\u0001\u0000"+
		"\u0000\u0000.\u001a\u0001\u0000\u0000\u0000.!\u0001\u0000\u0000\u0000"+
		"/\u0006\u0001\u0000\u0000\u000001\u0007\u0000\u0000\u00001\b\u0001\u0000"+
		"\u0000\u000023\u0007\u0001\u0000\u00003\n\u0001\u0000\u0000\u00004:\u0005"+
		"\"\u0000\u000056\u0005\\\u0000\u000069\u0005\"\u0000\u000079\t\u0000\u0000"+
		"\u000085\u0001\u0000\u0000\u000087\u0001\u0000\u0000\u00009<\u0001\u0000"+
		"\u0000\u0000:;\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000;=\u0001"+
		"\u0000\u0000\u0000<:\u0001\u0000\u0000\u0000=>\u0005\"\u0000\u0000>\f"+
		"\u0001\u0000\u0000\u0000?D\u0003\t\u0004\u0000@C\u0003\t\u0004\u0000A"+
		"C\u0003\u0007\u0003\u0000B@\u0001\u0000\u0000\u0000BA\u0001\u0000\u0000"+
		"\u0000CF\u0001\u0000\u0000\u0000DB\u0001\u0000\u0000\u0000DE\u0001\u0000"+
		"\u0000\u0000E\u000e\u0001\u0000\u0000\u0000FD\u0001\u0000\u0000\u0000"+
		"GK\u0005#\u0000\u0000HJ\t\u0000\u0000\u0000IH\u0001\u0000\u0000\u0000"+
		"JM\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000KI\u0001\u0000\u0000"+
		"\u0000LN\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000NO\u0005\n\u0000"+
		"\u0000OP\u0001\u0000\u0000\u0000PQ\u0006\u0007\u0000\u0000Q\u0010\u0001"+
		"\u0000\u0000\u0000RT\u0007\u0002\u0000\u0000SR\u0001\u0000\u0000\u0000"+
		"TU\u0001\u0000\u0000\u0000US\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000"+
		"\u0000VW\u0001\u0000\u0000\u0000WX\u0006\b\u0000\u0000X\u0012\u0001\u0000"+
		"\u0000\u0000\r\u0000\u0018\u001e#),.8:BDKU\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}