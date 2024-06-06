package com.HOIIVUtils.clausewitz_parser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

public class ParserTest {
	private final String test_path = "src\\test\\java\\com\\HOIIVUtils\\clausewitz_parser\\";
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
	public void miniMichiganTest1() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "minimichigantest.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void miniMichiganTest2() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "minimichigantest2.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void miniMichiganTest3() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "minimichigantest3.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void focusWithSearchFilterTest1() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "focus_with_search_filter_test1.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void focusWithSearchFilterTest2() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "focus_with_search_filter_test2.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void commentTest() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "carriage_return.txt"));
			n = parser.parse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void commentTest2() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "carriage_return.txt"));
			n = parser.parse();
			// check presence
			n.toList().forEach(node -> {
				assert !node.name().contains("\r");
				assert !node.value().asString().contains("\r");
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void commentTest3() {
		Parser parser;
		Node n;
		try {
			parser = new Parser(new File(test_path + "specialinfantry.txt"));
			n = parser.parse();
			assert n.contains("sub_units");
			// mobenforcer should be a sub unit
			assert n.findFirst("sub_units").contains("mobenforcer");
			// mobenforcer should have sprite
			assert n.findFirst("sub_units").findFirst("mobenforcer").contains("sprite");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
