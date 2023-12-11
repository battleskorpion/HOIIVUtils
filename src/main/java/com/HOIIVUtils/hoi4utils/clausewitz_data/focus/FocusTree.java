package com.HOIIVUtils.hoi4utils.clausewitz_data.focus;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.Parser;
import com.HOIIVUtils.hoi4utils.clausewitz_data.Localizable;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTag;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTags;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.FocusLocalizationFile;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.Localization;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.jetbrains.annotations.*;
import com.HOIIVUtils.ui.FXWindow;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTag.COUNTRY_TAG_LENGTH;

/**
 * ALL of the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
// todo extends file?
public class FocusTree implements Localizable {
	private static final ObservableMap<CountryTag, FocusTree> focusTrees
			= FXCollections.observableHashMap();
	private static final ObservableList<FocusTree> focusTreesList = FXCollections.observableArrayList();
	static {
		focusTrees.addListener((MapChangeListener<CountryTag, FocusTree>) change -> {
			updateObservableValues();
		});
	}


	// private HashSet<Focus> focuses;
	private final ObservableMap<String, Focus> focuses;

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
		focuses = FXCollections.observableHashMap();
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
		focuses = FXCollections.observableHashMap();

		FocusTree.add(country(), this);
	}

	public static ObservableList<FocusTree> observeFocusTrees() {
		return focusTreesList;
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

		focus_names = new ArrayList<>();
		this.focuses.clear();

		/* parser */
		Parser focusTreeParser = new Parser(this.focus_file);
		Node focusTreeNode;
		try {
			 focusTreeNode = focusTreeParser.parse();
			 focusTreeNode = focusTreeNode.filterName("focus_tree").toList().get(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		/* focuses */
		List<Focus> focuses = getFocuses(focusTreeNode);
		minX = focuses.stream().mapToInt(Focus::absoluteX).min().orElse(0);

//		focus_names = new ArrayList<>();
//		focus_names.addAll(focuses.parallelStream()
//				.map(Focus::id).toList());
//		this.focuses.clear();
//		this.focuses.putAll(focuses.parallelStream()
//				.collect(Collectors.toMap(Focus::id, f -> f)));
//		minX = 0; // min x is 0 or less

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

		List<Focus> focusList = new ArrayList<>();

		List<Node> focusTreeNodes =
				focusTreeNode.filterName("focus").toList();
		for (Node node : focusTreeNodes) {
			Focus focus;
			/* focus id */
			String focus_id = node.getValue("id").string();     // gets the ##### from "id = #####"
			focus = new Focus(focus_id, this, node);
			focus_names.add(focus_id);
			focuses.put(focus_id, focus);
			focusList.add(focus);
		}

		/* when all focuses init loaded */
		checkPendingFocusReferences();

		return focusList;
	}

	private void checkPendingFocusReferences() {
		List<Focus> resolvedReferences = new ArrayList<>();
//		private final HashMap<String, Consumer<List<Node>>> pendingFocusReferences = new HashMap<>();

		List<PendingFocusReferenceList> pendingFocusReferenceLists = focuses().parallelStream().map(Focus::getPendingFocusReferences).toList();

		/* resolve references of focuses that exist */
		for (var pendingFocusReferenceList : pendingFocusReferenceLists) {
			var pendingFocusReferences = pendingFocusReferenceList.pendingFocusReferences;
			List<String> referencesToRemove = pendingFocusReferences.stream()
					.map(PendingFocusReference::id)
					.filter(id -> focus_names.contains(id)).toList();
			referencesToRemove.forEach(pendingFocusReferenceList::resolve);
			// todo temp want better warnings in future
			/* unresolved references */
			pendingFocusReferences.forEach(pfr -> JOptionPane.showMessageDialog(null,
					"Undefined Focus reference invalid: " + pfr.id() + ", " + pfr.pendingActionMap().keySet()));
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
	 * @return Localization file for this focus tree, or null.
	 */
	public FocusLocalizationFile locFile() {
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
		if (id != null && !id.isEmpty()) {
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

	public static HashMap<CountryTag, FocusTree> add(CountryTag tag, FocusTree focusTree) {
		focusTrees.put(tag, focusTree);
		return new HashMap<>(focusTrees);
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
					if (locLine.trim().length() >= COUNTRY_TAG_LENGTH) {
						String potentialTag = locLine.trim().substring(0, COUNTRY_TAG_LENGTH);

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

	private static void updateObservableValues() {
		focusTreesList.setAll(focusTrees.values());
	}
}