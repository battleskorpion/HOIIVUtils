// Generated from org\jgrapht\nio\dot\DOT.g4 by ANTLR 4.12.0
package org.jgrapht.nio.dot;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
class DOTParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, STRICT=11, GRAPH=12, DIGRAPH=13, NODE=14, EDGE=15, SUBGRAPH=16, 
		Numeral=17, String=18, Id=19, HtmlString=20, WS=21, COMMENT=22, LINE_COMMENT=23, 
		PREPROC=24;
	public static final int
		RULE_graph = 0, RULE_compoundStatement = 1, RULE_graphHeader = 2, RULE_graphIdentifier = 3, 
		RULE_statement = 4, RULE_identifierPairStatement = 5, RULE_attributeStatement = 6, 
		RULE_attributesList = 7, RULE_aList = 8, RULE_edgeStatement = 9, RULE_nodeStatement = 10, 
		RULE_nodeStatementNoAttributes = 11, RULE_nodeIdentifier = 12, RULE_port = 13, 
		RULE_subgraphStatement = 14, RULE_identifierPair = 15, RULE_identifier = 16;
	private static String[] makeRuleNames() {
		return new String[] {
			"graph", "compoundStatement", "graphHeader", "graphIdentifier", "statement", 
			"identifierPairStatement", "attributeStatement", "attributesList", "aList", 
			"edgeStatement", "nodeStatement", "nodeStatementNoAttributes", "nodeIdentifier", 
			"port", "subgraphStatement", "identifierPair", "identifier"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "';'", "'}'", "'['", "']'", "','", "'->'", "'--'", "':'", 
			"'='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "STRICT", 
			"GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "Numeral", "String", 
			"Id", "HtmlString", "WS", "COMMENT", "LINE_COMMENT", "PREPROC"
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

	@Override
	public String getGrammarFileName() { return "DOT.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DOTParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GraphContext extends ParserRuleContext {
		public GraphHeaderContext graphHeader() {
			return getRuleContext(GraphHeaderContext.class,0);
		}
		public CompoundStatementContext compoundStatement() {
			return getRuleContext(CompoundStatementContext.class,0);
		}
		public GraphContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graph; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterGraph(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitGraph(this);
		}
	}

	public final GraphContext graph() throws RecognitionException {
		GraphContext _localctx = new GraphContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_graph);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34);
			graphHeader();
			setState(35);
			compoundStatement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompoundStatementContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public CompoundStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compoundStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterCompoundStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitCompoundStatement(this);
		}
	}

	public final CompoundStatementContext compoundStatement() throws RecognitionException {
		CompoundStatementContext _localctx = new CompoundStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_compoundStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			match(T__0);
			setState(44);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2084866L) != 0)) {
				{
				{
				setState(38);
				statement();
				setState(40);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(39);
					match(T__1);
					}
				}

				}
				}
				setState(46);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(47);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GraphHeaderContext extends ParserRuleContext {
		public TerminalNode GRAPH() { return getToken(DOTParser.GRAPH, 0); }
		public TerminalNode DIGRAPH() { return getToken(DOTParser.DIGRAPH, 0); }
		public TerminalNode STRICT() { return getToken(DOTParser.STRICT, 0); }
		public GraphIdentifierContext graphIdentifier() {
			return getRuleContext(GraphIdentifierContext.class,0);
		}
		public GraphHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graphHeader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterGraphHeader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitGraphHeader(this);
		}
	}

	public final GraphHeaderContext graphHeader() throws RecognitionException {
		GraphHeaderContext _localctx = new GraphHeaderContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_graphHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STRICT) {
				{
				setState(49);
				match(STRICT);
				}
			}

			setState(52);
			_la = _input.LA(1);
			if ( !(_la==GRAPH || _la==DIGRAPH) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(54);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1966080L) != 0)) {
				{
				setState(53);
				graphIdentifier();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GraphIdentifierContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public GraphIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graphIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterGraphIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitGraphIdentifier(this);
		}
	}

	public final GraphIdentifierContext graphIdentifier() throws RecognitionException {
		GraphIdentifierContext _localctx = new GraphIdentifierContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_graphIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public NodeStatementContext nodeStatement() {
			return getRuleContext(NodeStatementContext.class,0);
		}
		public EdgeStatementContext edgeStatement() {
			return getRuleContext(EdgeStatementContext.class,0);
		}
		public AttributeStatementContext attributeStatement() {
			return getRuleContext(AttributeStatementContext.class,0);
		}
		public IdentifierPairStatementContext identifierPairStatement() {
			return getRuleContext(IdentifierPairStatementContext.class,0);
		}
		public SubgraphStatementContext subgraphStatement() {
			return getRuleContext(SubgraphStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_statement);
		try {
			setState(63);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				nodeStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(59);
				edgeStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(60);
				attributeStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(61);
				identifierPairStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(62);
				subgraphStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierPairStatementContext extends ParserRuleContext {
		public IdentifierPairContext identifierPair() {
			return getRuleContext(IdentifierPairContext.class,0);
		}
		public IdentifierPairStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierPairStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterIdentifierPairStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitIdentifierPairStatement(this);
		}
	}

	public final IdentifierPairStatementContext identifierPairStatement() throws RecognitionException {
		IdentifierPairStatementContext _localctx = new IdentifierPairStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_identifierPairStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			identifierPair();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeStatementContext extends ParserRuleContext {
		public AttributesListContext attributesList() {
			return getRuleContext(AttributesListContext.class,0);
		}
		public TerminalNode GRAPH() { return getToken(DOTParser.GRAPH, 0); }
		public TerminalNode NODE() { return getToken(DOTParser.NODE, 0); }
		public TerminalNode EDGE() { return getToken(DOTParser.EDGE, 0); }
		public AttributeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterAttributeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitAttributeStatement(this);
		}
	}

	public final AttributeStatementContext attributeStatement() throws RecognitionException {
		AttributeStatementContext _localctx = new AttributeStatementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_attributeStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 53248L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(68);
			attributesList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributesListContext extends ParserRuleContext {
		public List<AListContext> aList() {
			return getRuleContexts(AListContext.class);
		}
		public AListContext aList(int i) {
			return getRuleContext(AListContext.class,i);
		}
		public AttributesListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributesList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterAttributesList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitAttributesList(this);
		}
	}

	public final AttributesListContext attributesList() throws RecognitionException {
		AttributesListContext _localctx = new AttributesListContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_attributesList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(70);
				match(T__3);
				setState(72);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1966080L) != 0)) {
					{
					setState(71);
					aList();
					}
				}

				setState(74);
				match(T__4);
				}
				}
				setState(77); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__3 );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AListContext extends ParserRuleContext {
		public List<IdentifierPairContext> identifierPair() {
			return getRuleContexts(IdentifierPairContext.class);
		}
		public IdentifierPairContext identifierPair(int i) {
			return getRuleContext(IdentifierPairContext.class,i);
		}
		public AListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterAList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitAList(this);
		}
	}

	public final AListContext aList() throws RecognitionException {
		AListContext _localctx = new AListContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_aList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(79);
				identifierPair();
				setState(81);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1 || _la==T__5) {
					{
					setState(80);
					_la = _input.LA(1);
					if ( !(_la==T__1 || _la==T__5) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				}
				}
				setState(85); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 1966080L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EdgeStatementContext extends ParserRuleContext {
		public List<NodeStatementNoAttributesContext> nodeStatementNoAttributes() {
			return getRuleContexts(NodeStatementNoAttributesContext.class);
		}
		public NodeStatementNoAttributesContext nodeStatementNoAttributes(int i) {
			return getRuleContext(NodeStatementNoAttributesContext.class,i);
		}
		public List<SubgraphStatementContext> subgraphStatement() {
			return getRuleContexts(SubgraphStatementContext.class);
		}
		public SubgraphStatementContext subgraphStatement(int i) {
			return getRuleContext(SubgraphStatementContext.class,i);
		}
		public AttributesListContext attributesList() {
			return getRuleContext(AttributesListContext.class,0);
		}
		public EdgeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edgeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterEdgeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitEdgeStatement(this);
		}
	}

	public final EdgeStatementContext edgeStatement() throws RecognitionException {
		EdgeStatementContext _localctx = new EdgeStatementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_edgeStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Numeral:
			case String:
			case Id:
			case HtmlString:
				{
				setState(87);
				nodeStatementNoAttributes();
				}
				break;
			case T__0:
			case SUBGRAPH:
				{
				setState(88);
				subgraphStatement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(96); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(91);
				_la = _input.LA(1);
				if ( !(_la==T__6 || _la==T__7) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(94);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case Numeral:
				case String:
				case Id:
				case HtmlString:
					{
					setState(92);
					nodeStatementNoAttributes();
					}
					break;
				case T__0:
				case SUBGRAPH:
					{
					setState(93);
					subgraphStatement();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(98); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__6 || _la==T__7 );
			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(100);
				attributesList();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NodeStatementContext extends ParserRuleContext {
		public NodeIdentifierContext nodeIdentifier() {
			return getRuleContext(NodeIdentifierContext.class,0);
		}
		public AttributesListContext attributesList() {
			return getRuleContext(AttributesListContext.class,0);
		}
		public NodeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterNodeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitNodeStatement(this);
		}
	}

	public final NodeStatementContext nodeStatement() throws RecognitionException {
		NodeStatementContext _localctx = new NodeStatementContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_nodeStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			nodeIdentifier();
			setState(105);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(104);
				attributesList();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NodeStatementNoAttributesContext extends ParserRuleContext {
		public NodeIdentifierContext nodeIdentifier() {
			return getRuleContext(NodeIdentifierContext.class,0);
		}
		public NodeStatementNoAttributesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeStatementNoAttributes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterNodeStatementNoAttributes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitNodeStatementNoAttributes(this);
		}
	}

	public final NodeStatementNoAttributesContext nodeStatementNoAttributes() throws RecognitionException {
		NodeStatementNoAttributesContext _localctx = new NodeStatementNoAttributesContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_nodeStatementNoAttributes);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			nodeIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NodeIdentifierContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public PortContext port() {
			return getRuleContext(PortContext.class,0);
		}
		public NodeIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterNodeIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitNodeIdentifier(this);
		}
	}

	public final NodeIdentifierContext nodeIdentifier() throws RecognitionException {
		NodeIdentifierContext _localctx = new NodeIdentifierContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_nodeIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			identifier();
			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(110);
				port();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PortContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public PortContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_port; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterPort(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitPort(this);
		}
	}

	public final PortContext port() throws RecognitionException {
		PortContext _localctx = new PortContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_port);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			match(T__8);
			setState(114);
			identifier();
			setState(117);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(115);
				match(T__8);
				setState(116);
				identifier();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SubgraphStatementContext extends ParserRuleContext {
		public CompoundStatementContext compoundStatement() {
			return getRuleContext(CompoundStatementContext.class,0);
		}
		public TerminalNode SUBGRAPH() { return getToken(DOTParser.SUBGRAPH, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SubgraphStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subgraphStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterSubgraphStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitSubgraphStatement(this);
		}
	}

	public final SubgraphStatementContext subgraphStatement() throws RecognitionException {
		SubgraphStatementContext _localctx = new SubgraphStatementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_subgraphStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUBGRAPH) {
				{
				setState(119);
				match(SUBGRAPH);
				setState(121);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1966080L) != 0)) {
					{
					setState(120);
					identifier();
					}
				}

				}
			}

			setState(125);
			compoundStatement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierPairContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public IdentifierPairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierPair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterIdentifierPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitIdentifierPair(this);
		}
	}

	public final IdentifierPairContext identifierPair() throws RecognitionException {
		IdentifierPairContext _localctx = new IdentifierPairContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_identifierPair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			identifier();
			setState(128);
			match(T__9);
			setState(129);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode Id() { return getToken(DOTParser.Id, 0); }
		public TerminalNode String() { return getToken(DOTParser.String, 0); }
		public TerminalNode HtmlString() { return getToken(DOTParser.HtmlString, 0); }
		public TerminalNode Numeral() { return getToken(DOTParser.Numeral, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTListener ) ((DOTListener)listener).exitIdentifier(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1966080L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0018\u0086\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001)\b\u0001\u0005\u0001+\b\u0001"+
		"\n\u0001\f\u0001.\t\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0003\u0002"+
		"3\b\u0002\u0001\u0002\u0001\u0002\u0003\u00027\b\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004@\b\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0003\u0007I\b\u0007\u0001\u0007\u0004"+
		"\u0007L\b\u0007\u000b\u0007\f\u0007M\u0001\b\u0001\b\u0003\bR\b\b\u0004"+
		"\bT\b\b\u000b\b\f\bU\u0001\t\u0001\t\u0003\tZ\b\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t_\b\t\u0004\ta\b\t\u000b\t\f\tb\u0001\t\u0003\tf\b\t\u0001\n"+
		"\u0001\n\u0003\nj\b\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0003\f"+
		"p\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0003\rv\b\r\u0001\u000e\u0001\u000e"+
		"\u0003\u000ez\b\u000e\u0003\u000e|\b\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0000\u0000\u0011\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \u0000\u0005\u0001\u0000\f\r\u0002"+
		"\u0000\f\f\u000e\u000f\u0002\u0000\u0002\u0002\u0006\u0006\u0001\u0000"+
		"\u0007\b\u0001\u0000\u0011\u0014\u0089\u0000\"\u0001\u0000\u0000\u0000"+
		"\u0002%\u0001\u0000\u0000\u0000\u00042\u0001\u0000\u0000\u0000\u00068"+
		"\u0001\u0000\u0000\u0000\b?\u0001\u0000\u0000\u0000\nA\u0001\u0000\u0000"+
		"\u0000\fC\u0001\u0000\u0000\u0000\u000eK\u0001\u0000\u0000\u0000\u0010"+
		"S\u0001\u0000\u0000\u0000\u0012Y\u0001\u0000\u0000\u0000\u0014g\u0001"+
		"\u0000\u0000\u0000\u0016k\u0001\u0000\u0000\u0000\u0018m\u0001\u0000\u0000"+
		"\u0000\u001aq\u0001\u0000\u0000\u0000\u001c{\u0001\u0000\u0000\u0000\u001e"+
		"\u007f\u0001\u0000\u0000\u0000 \u0083\u0001\u0000\u0000\u0000\"#\u0003"+
		"\u0004\u0002\u0000#$\u0003\u0002\u0001\u0000$\u0001\u0001\u0000\u0000"+
		"\u0000%,\u0005\u0001\u0000\u0000&(\u0003\b\u0004\u0000\')\u0005\u0002"+
		"\u0000\u0000(\'\u0001\u0000\u0000\u0000()\u0001\u0000\u0000\u0000)+\u0001"+
		"\u0000\u0000\u0000*&\u0001\u0000\u0000\u0000+.\u0001\u0000\u0000\u0000"+
		",*\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-/\u0001\u0000\u0000"+
		"\u0000.,\u0001\u0000\u0000\u0000/0\u0005\u0003\u0000\u00000\u0003\u0001"+
		"\u0000\u0000\u000013\u0005\u000b\u0000\u000021\u0001\u0000\u0000\u0000"+
		"23\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u000046\u0007\u0000\u0000"+
		"\u000057\u0003\u0006\u0003\u000065\u0001\u0000\u0000\u000067\u0001\u0000"+
		"\u0000\u00007\u0005\u0001\u0000\u0000\u000089\u0003 \u0010\u00009\u0007"+
		"\u0001\u0000\u0000\u0000:@\u0003\u0014\n\u0000;@\u0003\u0012\t\u0000<"+
		"@\u0003\f\u0006\u0000=@\u0003\n\u0005\u0000>@\u0003\u001c\u000e\u0000"+
		"?:\u0001\u0000\u0000\u0000?;\u0001\u0000\u0000\u0000?<\u0001\u0000\u0000"+
		"\u0000?=\u0001\u0000\u0000\u0000?>\u0001\u0000\u0000\u0000@\t\u0001\u0000"+
		"\u0000\u0000AB\u0003\u001e\u000f\u0000B\u000b\u0001\u0000\u0000\u0000"+
		"CD\u0007\u0001\u0000\u0000DE\u0003\u000e\u0007\u0000E\r\u0001\u0000\u0000"+
		"\u0000FH\u0005\u0004\u0000\u0000GI\u0003\u0010\b\u0000HG\u0001\u0000\u0000"+
		"\u0000HI\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JL\u0005\u0005"+
		"\u0000\u0000KF\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000\u0000MK\u0001"+
		"\u0000\u0000\u0000MN\u0001\u0000\u0000\u0000N\u000f\u0001\u0000\u0000"+
		"\u0000OQ\u0003\u001e\u000f\u0000PR\u0007\u0002\u0000\u0000QP\u0001\u0000"+
		"\u0000\u0000QR\u0001\u0000\u0000\u0000RT\u0001\u0000\u0000\u0000SO\u0001"+
		"\u0000\u0000\u0000TU\u0001\u0000\u0000\u0000US\u0001\u0000\u0000\u0000"+
		"UV\u0001\u0000\u0000\u0000V\u0011\u0001\u0000\u0000\u0000WZ\u0003\u0016"+
		"\u000b\u0000XZ\u0003\u001c\u000e\u0000YW\u0001\u0000\u0000\u0000YX\u0001"+
		"\u0000\u0000\u0000Z`\u0001\u0000\u0000\u0000[^\u0007\u0003\u0000\u0000"+
		"\\_\u0003\u0016\u000b\u0000]_\u0003\u001c\u000e\u0000^\\\u0001\u0000\u0000"+
		"\u0000^]\u0001\u0000\u0000\u0000_a\u0001\u0000\u0000\u0000`[\u0001\u0000"+
		"\u0000\u0000ab\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000bc\u0001"+
		"\u0000\u0000\u0000ce\u0001\u0000\u0000\u0000df\u0003\u000e\u0007\u0000"+
		"ed\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000f\u0013\u0001\u0000"+
		"\u0000\u0000gi\u0003\u0018\f\u0000hj\u0003\u000e\u0007\u0000ih\u0001\u0000"+
		"\u0000\u0000ij\u0001\u0000\u0000\u0000j\u0015\u0001\u0000\u0000\u0000"+
		"kl\u0003\u0018\f\u0000l\u0017\u0001\u0000\u0000\u0000mo\u0003 \u0010\u0000"+
		"np\u0003\u001a\r\u0000on\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000"+
		"p\u0019\u0001\u0000\u0000\u0000qr\u0005\t\u0000\u0000ru\u0003 \u0010\u0000"+
		"st\u0005\t\u0000\u0000tv\u0003 \u0010\u0000us\u0001\u0000\u0000\u0000"+
		"uv\u0001\u0000\u0000\u0000v\u001b\u0001\u0000\u0000\u0000wy\u0005\u0010"+
		"\u0000\u0000xz\u0003 \u0010\u0000yx\u0001\u0000\u0000\u0000yz\u0001\u0000"+
		"\u0000\u0000z|\u0001\u0000\u0000\u0000{w\u0001\u0000\u0000\u0000{|\u0001"+
		"\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000}~\u0003\u0002\u0001\u0000"+
		"~\u001d\u0001\u0000\u0000\u0000\u007f\u0080\u0003 \u0010\u0000\u0080\u0081"+
		"\u0005\n\u0000\u0000\u0081\u0082\u0003 \u0010\u0000\u0082\u001f\u0001"+
		"\u0000\u0000\u0000\u0083\u0084\u0007\u0004\u0000\u0000\u0084!\u0001\u0000"+
		"\u0000\u0000\u0012(,26?HMQUY^beiouy{";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}