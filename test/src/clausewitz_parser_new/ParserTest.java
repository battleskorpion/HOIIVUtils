package src.clausewitz_parser_new;

import clausewitz_parser_new.Node;
import clausewitz_parser_new.Parser;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class ParserTest {
    private static String input_test = "test = test2\ntest3 = test4\ntest5 = test6";
    private static String input_test2 = """
			focus_tree = {
			id = SMI_michigan
            
			country = {
			factor = 0
			modifier = {
			add = 10
			tag = SMI
			}
			}
			default = no
            
			focus = {
			id = "exp_focus"
			completion_reward = {
			}
			}

			focus = {
			id = "exp_focus2"
			completion_reward = {
			}
			}
			
			}
			""";

    @Test
    public void mainTest() {
        Parser parser;
        Node n;
        try {
            parser = new Parser(input_test);
            n = parser.parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

		// input test 2
        try {
            parser = new Parser(input_test2);
            n = parser.parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ArrayList<Node> nodes;
        try {
            nodes = (ArrayList<Node>) n.valueObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//		System.out.println(n.value());
        ArrayList<Node> subnodes = (ArrayList<Node>) nodes.get(0).valueObject();
        for (Node node : subnodes) {
            System.out.println(node.name);
        }
    }

	@Test
	public void miniMichiganTest1 () {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File("test\\src\\clausewitz_parser_new\\minimichigantest.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void miniMichiganTest2 () {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File("test\\src\\clausewitz_parser_new\\minimichigantest2.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void miniMichiganTest3 () {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File("test\\src\\clausewitz_parser_new\\minimichigantest3.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void focusWithSearchFilterTest1 () {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File("test\\src\\clausewitz_parser_new\\focus_with_search_filter_test1.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void focusWithSearchFilterTest2 () {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File("test\\src\\clausewitz_parser_new\\focus_with_search_filter_test2.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
