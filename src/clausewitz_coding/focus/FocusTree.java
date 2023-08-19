package clausewitz_coding.focus;

import hoi4utils.HOIIVUtils;
import clausewitz_coding.localization.LocalizationFile;
import clausewitz_coding.country.CountryTag;
import clausewitz_coding.country.CountryTags;
import clausewitz_parser.Expression;
import clausewitz_parser.Parser;
//import settings.LocalizerSettings;
//import ui.menu.MenuSettings;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class FocusTree extends HOIIVUtils {

	// private HashSet<Focus> focuses;
	private final HashMap<String, Focus> focuses;
	private ArrayList<String> focus_names;
	private File focus_file;
	private CountryTag country;
	private LocalizationFile locFile = null;
	private String id;
	// private Modifier countryModifier;
	private boolean default_focus;
	private Point continuous_focus_position;
	private int minX; // min x of all focuses

	/**
	 * Instantiate focus tree from pre-existing focus tree (file).
	 * 
	 * @param focus_file pre-existing focus tree.
	 * @throws IOException file not found or otherwise unable to be accessed.
	 */
	public FocusTree(File focus_file) throws IOException {
		this.focus_file = focus_file;
		country = new CountryTag("###");
		focuses = new HashMap<>();
		minX = 0;

		parse();
		FocusTree.add(country(), this);
	}

	/**
	 * Instantiate new focus tree.
	 * 
	 * @param id Focus tree id (usually kept same as file/country name).
	 */
	public FocusTree(String id, CountryTag tag) {
		this.id = id;
		// countryModifier = new CountryModifier();
		default_focus = false;
		continuous_focus_position = new Point(50, 1200);
		country = tag;
		focuses = new HashMap<>();

		FocusTree.add(country(), this);
	}

	// public ArrayList<String> find(File focus_file) throws IOException {
	// Scanner focusReader = new Scanner(focus_file);
	private ArrayList<String> parse() {
		if (this.focus_file == null) {
			System.err.println(this + "File of focus tree not set.");
			return null;
		}
		if (!this.focus_file.exists()) {
			System.err.println(this + "Focus tree file does not exist.");
			return null;
		}

		/* parser */
		Parser focusParser = new Parser(this.focus_file);
		Expression focusTreeExp = focusParser.expression();
		Expression[] focusesExps = focusTreeExp.getAll("focus={");
		// System.out.println(focusesExps[focusesExps.length-1]);
		// if (focuses == null) {
		// return null;
		// }
		if (focusesExps == null) {
			System.err.println("focusesExps null in " + this.getClass());
			return null;
		}
		System.out.println(Arrays.toString(focusesExps));
		System.out.println("Num focuses detected: " + focusesExps.length);

		/* focuses */
		focus_names = new ArrayList<String>(); // todo needed?
		minX = 0; // min x is 0 or less

		for (Expression focusExp : focusesExps) {
			Focus focus;

			/* focus id */
			{
				Expression focusIDExp = focusExp.get("id=");
				if (focusIDExp == null) {
					continue; // id important
				}
				String focus_id = focusIDExp.getText(); // gets the ##### from "id = #####"
				if (focus_id == null) {
					continue; // id important
				}
				focus = new Focus(focus_id, this);
				focus_names.add(focus_id);
				focuses.put(focus_id, focus);

				// todo THIS SHOULD RUN ONCE PER FOCUS TREE IDEALLY NOT FOR EACH LINE
			}
		}

		for (Expression focusExp : focusesExps) {
			Focus focus;

			/* focus id */
			{
				Expression focusIDExp = focusExp.get("id=");
				if (focusIDExp == null) {
					continue; // id important
				}
				String focus_id = focusIDExp.getText(); // gets the ##### from "id = #####"
				if (focus_id == null) {
					continue; // id important
				}
				focus = getFocus(focus_id);

				// todo THIS SHOULD RUN ONCE PER FOCUS TREE IDEALLY NOT FOR EACH LINE
				focus.loadAttributes(focusExp);
				if (focus.absoluteX() < minX) {
					minX = focus.x();
				}
			}
		}

		/* country */
		Expression countryModifierExp = focusTreeExp.get("country").get("modifier");
		if (countryModifierExp != null && countryModifierExp.get("tag") != null) {
			country = new CountryTag(countryModifierExp.get("tag").getText());
		}

		// // make a list of all focus names
		// boolean findFocusName = false;
		// int focus_name_index; // index of focus name in string
		// while (focusReader.hasNextLine()) {
		// String data = focusReader.nextLine().replaceAll("\\s", "");
		// if (usefulData(data)) {
		// if (!findFocusName) {
		// if ((data.trim().length() >= 6) && (data.trim().substring(0,
		// 6).equals("focus="))) {
		// findFocusName = true;
		// }
		// } else {
		// if (data.trim().length() >= 3 && data.trim().substring(0, 3).equals(("id=")))
		// {
		// focus_name_index = data.indexOf("id=") + 3;
		// String focus_id = data.substring(focus_name_index, data.length()).trim();
		// focus_names.add(focus_id);
		// focuses.add(new Focus(focus_id));
		//
		// /* get country */
		// country = new CountryTag(data.trim().substring(focus_name_index,
		// focus_name_index + 3));
		//
		// findFocusName = false;
		// }
		// }
		// }
		// }
		return focus_names;
	}

	/**
	 * Lists last set of focuses
	 * 
	 * @return
	 */
	public ArrayList<String> listFocusNames() throws IOException {
		if (focus_names == null) {
			return null; // bad :(
		}

		return focus_names;
	}

	// public static ArrayList<String> list(File focus_file) throws IOException {
	// if(NationalFocuses.get(focus_file) focus_names == null) {
	// return FocusTree.find(focus_file);
	// }
	//
	// return focus_names;
	// }

	public CountryTag country() {
		if (country != null) {
			return country;
		} else {
			// idk :(
			return null;
		}
	}

	/**
	 *
	 * @return Localization file for this focus tree, or null.
	 */
	public LocalizationFile locFile() {
		return locFile;
	}

	public File focusFile() {
		return focus_file;
	}

	public File setLocalization(File locFile) {
		this.locFile = new LocalizationFile(locFile);
		return locFile;
	}

	@Override
	public boolean equals(Object other) {
		if (other.getClass() == this.getClass()) {
			return this.focus_file == ((FocusTree) other).focus_file;
		}

		return false;
	}

	@Override
	public String toString() {
		if (id != null && !id.equals("")) {
			return id;
		}
		return country.toString();
	}

	public HashSet<Focus> focuses() {
		return new HashSet<>(focuses.values());
	}

	public Focus getFocus(String focus_id) {
		if (focuses.get(focus_id) == null) {
			System.err.println(focuses.keySet());
		}

		return focuses.get(focus_id);
	}

	public int minX() {
		return minX;
	}

    private static final HashMap<CountryTag, FocusTree> focusTrees = new HashMap<>();

    public static HashMap<CountryTag, FocusTree> add(CountryTag tag, FocusTree focusTree) {
        focusTrees.put(tag, focusTree);
        return focusTrees;
    }

    public static FocusTree[] list() {
        return focusTrees.values().toArray(new FocusTree[0]);
    }

    /**
     * Returns focus tree corresponding to the tag, if it exists
     * @param tag
     * @return The focus tree, or null if could not be found/not yet created.
     */
    public static FocusTree get(CountryTag tag) { return focusTrees.get(tag); }
    public static FocusTree getdankwizardisfrench(CountryTag tag) {
        for (FocusTree tree : list()) {
            assert tree.country() != null;
            if (tree.country().equals(tag)) {
                return tree;
            }
        }

        return null;
    }

    public static ArrayList<FocusTree> unlocalizedFocusTrees() {
        ArrayList<FocusTree> focusTrees = new ArrayList<>();

        for (FocusTree tree : list()) {
            if (tree.locFile() == null) {
                focusTrees.add(tree);
            }
        }

        return focusTrees;
    }

    public static ArrayList<FocusTree> partiallyLocalizedFocusTrees() throws IOException {
        ArrayList<FocusTree> focusTrees = new ArrayList<>();

        // todo may be able to do something else in this function -
        // todo all focus trees - localized focus trees - unlocalized focus trees
        for (FocusTree tree : list()) {
            aa:
            if (tree.locFile() != null) {
                Scanner locReader = new Scanner(tree.locFile().getFile());
                ArrayList<String> focuses = tree.listFocusNames();
                if (focuses == null) {
                    break aa;
                }

                ArrayList<Boolean> localized;
                while (locReader.hasNext()) {
                    String locLine = locReader.nextLine();
                    if (locLine.trim().length() >= 3) {
                        String potentialTag = locLine.trim().substring(0, 3);

                        if (CountryTags.exists(potentialTag)) {

                        }
                    }
                }
            }
        }
        return focusTrees;
    }

    public static ArrayList<FocusTree> localizedFocusTrees() throws IOException {
        ArrayList<FocusTree> focusTrees = new ArrayList<>();

        for (FocusTree tree : list()) {
            aa:
            if (tree.locFile() != null) {
                Scanner locReader = new Scanner(tree.locFile().getFile());
                ArrayList<String> focuses = tree.listFocusNames();
                if (focuses == null) {
                    break aa;

                }

                ArrayList<Boolean> localized;
                while (locReader.hasNext()) {
                    String locLine = locReader.nextLine();
                    if (locLine.trim().length() >= 3) {
                        String potentialTag = locLine.trim().substring(0, 3);

                        if (CountryTags.exists(potentialTag)) {

                        }
                    }
                }
            }
        }
        return focusTrees;
    }
}