package clausewitz_parser_new;

public class SymbolNode {
	public String name;

	public SymbolNode(NodeValue parsedValue) {
		this.name = parsedValue.valueObject().toString();   // todo?
	}
}
