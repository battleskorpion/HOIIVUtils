package hoi4utils.clausewitz_data.focus;

import clausewitz_parser_new.Node;
import clausewitz_parser_new.Parser;
import hoi4utils.clausewitz_data.Localizable;
import hoi4utils.clausewitz_data.country.CountryTag;
import hoi4utils.clausewitz_data.country.CountryTags;
import hoi4utils.clausewitz_data.localization.FocusLocalizationFile;
import hoi4utils.clausewitz_data.localization.Localization;
import hoi4utils.clausewitz_data.localization.LocalizationFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.*;
import ui.FXWindow;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ALL of the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
// todo extends file?
public class FocusTree implements Localizable {

	// private HashSet<Focus> focuses;
	private final HashMap<String, Focus> focuses;
	private ArrayList<String> focus_names;
	private File focus_file;
	private CountryTag country;
	private FocusLocalizationFile focusLocFile = null;
	private String id;
	// private Modifier countryModifier;
	// private boolean defaultFocus; // ! todo Do This
	// private Point continuousFocusPosition; // ! todo DO THIS
	private int minX; // min x of all focuses

	/**
	 * Instantiate focus tree from pre-existing focus tree (file).
	 * 
	 * @param focus_file pre-existing focus tree.
	 */
	public FocusTree(File focus_file) {
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
		//! defaultFocus = false;
		//! continuousFocusPosition = new Point(50, 1200);
		country = tag;
		focuses = new HashMap<>();

		FocusTree.add(country(), this);
	}

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
		Parser focusTreeParser = new Parser(this.focus_file);
		Node focusTreeNode;
		try {
			 focusTreeNode = focusTreeParser.parse();
			 focusTreeNode = focusTreeNode.filterName("focus_tree").toList().get(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		List<Focus> focuses = getFocuses(focusTreeNode);

//		Expression focusTreeExp = focusTreeParser.expression();
//		Expression[] focusesExps = focusTreeExp.getAll("focus={");
//		// System.out.println(focusesExps[focusesExps.length-1]);
//		// if (focuses == null) {
//		// return null;
//		// }
//		if (focusesExps == null) {
//			System.err.println("focusesExps null in " + this.getClass());
//			return null;
//		}
//		System.out.println("Num focuses detected: " + focusesExps.length);
//
//		/* focuses */
		focus_names = new ArrayList<>();
		focus_names.addAll(focuses.parallelStream()
				.map(Focus::id).toList());
		this.focuses.clear();
		this.focuses.putAll(focuses.parallelStream()
				.collect(Collectors.toMap(Focus::id, f -> f)));
//		minX = 0; // min x is 0 or less
//
//		for (Expression focusExp : focusesExps) {
//			Focus focus;
//
//			/* focus id */
//			{
//				Expression focusIDExp = focusExp.get("id=");
//				if (focusIDExp == null) {
//					continue; // id important
//				}
//				String focus_id = focusIDExp.getText(); // gets the ##### from "id = #####"
//				if (focus_id == null) {
//					continue; // id important
//				}
//				focus = new Focus(focus_id, this);
//				focus_names.add(focus_id);
//				focuses.put(focus_id, focus);
//
//				// todo THIS SHOULD RUN ONCE PER FOCUS TREE IDEALLY NOT FOR EACH LINE
//			}
//		}
//
//		for (Expression focusExp : focusesExps) {
//			Focus focus;
//
//			/* focus id */
//			{
//				Expression focusIDExp = focusExp.get("id=");
//				if (focusIDExp == null) {
//					continue; // id important
//				}
//				String focus_id = focusIDExp.getText(); // gets the ##### from "id = #####"
//				if (focus_id == null) {
//					continue; // id important
//				}
//				focus = getFocus(focus_id);
//
//				// todo THIS SHOULD RUN ONCE PER FOCUS TREE IDEALLY NOT FOR EACH LINE
//				focus.loadAttributes(focusExp);
//				if (focus.absoluteX() < minX) {
//					minX = focus.x();
//				}
//			}
//		}

		/* country */
		// todo should not be .findFirst("modifier"); need mofifier handling. but thats okay.
		Node countryModifierExp = focusTreeNode.findFirst("country").findFirst("modifier");
		if (countryModifierExp != null) {
			country = new CountryTag(countryModifierExp.getValue("tag").string());
		}

		return focus_names;
	}

	@NotNull
	private List<Focus> getFocuses(Node focusTreeNode) {
		if (focusTreeNode.value().list() == null) {
			System.err.println("Expected list of nodes, for focuses getter");
			return null;
		}

		List<Focus> focuses = new ArrayList<>();

		// todo woooo
		List<Node> focusTreeNodes =
				focusTreeNode.filterName("focus").toList();
		// todo call a get focus function on each node
		for (Node node : focusTreeNodes) {
			Focus focus;
			/* focus id */
			String focus_id = node.getValue("id").string();     // gets the ##### from "id = #####"
			focus = new Focus(focus_id, this, node);
//			focus_names.add(focus_id);
//			focuses.put(focus_id, focus);
			focuses.add(focus);
		}

		/* when all focuses init loaded */
		checkPendingFocusReferences();

		return focuses;
	}

	private void checkPendingFocusReferences() {
		List<Focus> resolvedReferences = new ArrayList<>();
//		private final HashMap<String, Consumer<List<Node>>> pendingFocusReferences = new HashMap<>();

		// todo more efficient algo
		List<PendingFocusReferenceList> pendingFocusReferenceLists = focuses().parallelStream().map(Focus::getPendingFocusReferences).toList();

		/* resolve references of focuses that exist */
		for (var pendingFocusReferenceList : pendingFocusReferenceLists) {
			var pendingFocusReferences = pendingFocusReferenceList.pendingFocusReferences;
//			pendingFocusReferenceList.pendingFocusReferences.stream()
//					.filter((pfr) -> focus_names.contains(pfr.id()))
//					.forEach(PendingFocusReference::resolve);
			List<String> referencesToRemove = pendingFocusReferences.stream()
					.map(PendingFocusReference::id)
					.filter(id -> focus_names.contains(id)).toList();
			referencesToRemove.forEach(pendingFocusReferenceList::resolve);
		}

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
		return focusLocFile;
	}

	public File focusFile() {
		return focus_file;
	}

	public File setLocalization(File locFile) {
		try {
			this.focusLocFile = new FocusLocalizationFile(locFile);
		} catch (IllegalArgumentException e) {
			FXWindow.openGlobalErrorWindow("Illegal argument: " + e.getLocalizedMessage());
			return null;
		} catch (NullPointerException e) {
			FXWindow.openGlobalErrorWindow("Focus loc. file does not exist/is not available or other error. Details: \n\t" + e.getLocalizedMessage());
		}
		return locFile;
	}

	public File setLocalization(FocusLocalizationFile file) {
		this.focusLocFile = file;
		return focusLocFile;
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

	public static FocusTree[] listFocusTrees() {
		return focusTrees.values().toArray(new FocusTree[0]);
	}

	/**
	 * Returns focus tree corresponding to the tag, if it exists
	 * @param tag
	 * @return The focus tree, or null if could not be found/not yet created.
	 */
	public static FocusTree get(CountryTag tag) { return focusTrees.get(tag); }
	public static FocusTree getdankwizardisfrench(CountryTag tag) {
		for (FocusTree tree : listFocusTrees()) {
			assert tree.country() != null;
			if (tree.country().equals(tag)) {
				return tree;
			}
		}

		return null;
	}

	public static ArrayList<FocusTree> unlocalizedFocusTrees() {
		ArrayList<FocusTree> focusTrees = new ArrayList<>();

		for (FocusTree tree : listFocusTrees()) {
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
		for (FocusTree tree : listFocusTrees()) {
			aa:
			if (tree.locFile() != null) {
				Scanner locReader = new Scanner(tree.locFile().getFile());
				ArrayList<String> focuses = tree.listFocusNames();
				if (focuses == null) {
					break aa;
				}

				//ArrayList<Boolean> localized; Commited out till Skorp fixes this
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

		for (FocusTree tree : listFocusTrees()) {
			aa:
			if (tree.locFile() != null) {
				Scanner locReader = new Scanner(tree.locFile().getFile());
				ArrayList<String> focuses = tree.listFocusNames();
				if (focuses == null) {
					break aa;

				}

				//ArrayList<Boolean> localized; Commited out till Skorp fixies this
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

	public ObservableList<Focus> listFocuses() {
		ObservableList<Focus> focusList = FXCollections.observableArrayList();
		focusList.addAll(focuses());
		return focusList;
	}

	public void updateLocalization(Localization nameLocalization) {
		focusLocFile.setLocalization(nameLocalization);
	}
}