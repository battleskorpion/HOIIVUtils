package clausewitz_parser;

public class SymbolNode {
	public String name;

	public SymbolNode(NodeValue parsedValue) {
		this.name = parsedValue.valueObject().toString();   // todo?
	}
}
