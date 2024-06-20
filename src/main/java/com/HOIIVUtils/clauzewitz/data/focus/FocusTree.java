package com.HOIIVUtils.clauzewitz.data.focus;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.Parser;
import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clauzewitz.data.country.CountryTag;
import com.HOIIVUtils.clauzewitz.data.country.CountryTags;
import com.HOIIVUtils.clauzewitz.localization.FocusLocalizationFile;
import com.HOIIVUtils.clauzewitz.localization.Localization;
import com.HOIIVUtils.clauzewitz.exceptions.IllegalLocalizationFileTypeException;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.jetbrains.annotations.*;
import com.HOIIVUtils.ui.FXWindow;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.HOIIVUtils.clauzewitz.data.country.CountryTag.COUNTRY_TAG_LENGTH;

/**
 * ALL of the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
// todo extends file?
public class FocusTree implements Localizable, Comparable<FocusTree>, Iterable<Focus> {
	private static final ObservableMap<File, FocusTree> focusTrees = FXCollections.observableHashMap();
	private static final ObservableList<FocusTree> focusTreesList = FXCollections.observableArrayList();
	static {
		focusTrees.addListener((MapChangeListener<File, FocusTree>) change -> {
			updateObservableValues();
			focusTreesList.sort(Comparator.naturalOrder());
		});
	}

	// private HashSet<Focus> focuses;
	private final ObservableMap<String, Focus> focuses;

	private ArrayList<String> focusIDList;
	private File focus_file;
	private CountryTag country;
	private FocusLocalizationFile focusLocFile = null;
	private String id;
	// private Modifier countryModifier;
	// private boolean defaultFocus; // ! todo Do This
	// private Point continuousFocusPosition; // ! todo DO THIS
	private int minX; // min x of all focuses

	public static FocusTree get(File focus_file) {
		if (!focusTrees.containsKey(focus_file)) {
			new FocusTree(focus_file);
		}
		return focusTrees.get(focus_file);
	}

	/**
	 * Instantiate focus tree from pre-existing focus tree (file).
	 * 
	 * @param focus_file pre-existing focus tree.
	 */
	private FocusTree(File focus_file) {
		this.focus_file = focus_file;
		country = new CountryTag("###");
		focuses = FXCollections.observableHashMap();
		minX = 0;

		parse();
		FocusTree.add(this);
	}

	// /**
	// * Instantiate new focus tree.
	// *
	// * @param id Focus tree id (usually kept same as file/country name).
	// */
	// private FocusTree(String id, CountryTag tag) {
	// this.id = id;
	// // countryModifier = new CountryModifier();
	// //! defaultFocus = false;
	// //! continuousFocusPosition = new Point(50, 1200);
	// country = tag;
	// focuses = FXCollections.observableHashMap();
	//
	// FocusTree.add(country(), this);
	// }

	public static ObservableList<FocusTree> observeFocusTrees() {
		return focusTreesList;
	}

	public static void read() {
		if (!HOIIVFile.mod_focus_folder.exists() || !HOIIVFile.mod_focus_folder.isDirectory()) {
			System.err.println("Focus folder does not exist or is not a directory.");
			return;
		}
		if (HOIIVFile.mod_focus_folder.listFiles() == null || HOIIVFile.mod_focus_folder.listFiles().length == 0) {
			System.err.println("No focuses found in " + HOIIVFile.mod_focus_folder);
			return;
		}

		for (File f : HOIIVFile.mod_focus_folder.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				new FocusTree(f);
			}
		}
	}

	public static void loadLocalization() {
		// todo not just english :(
		if (!HOIIVFile.mod_localization_folder.exists() || !HOIIVFile.mod_localization_folder.isDirectory()) {
			System.err.println("Localization folder does not exist or is not a directory.");
			return;
		}
		if (HOIIVFile.mod_localization_folder.listFiles() == null
				|| HOIIVFile.mod_localization_folder.listFiles().length == 0) {
			System.err.println("No localization files found in " + HOIIVFile.mod_localization_folder);
			return;
		}
		aa: for (FocusTree focusTree : unlocalizedFocusTrees()) {
			for (File f : HOIIVFile.mod_localization_folder.listFiles()) {
				FocusLocalizationFile flf;
				try {
					flf = new FocusLocalizationFile(f);
				} catch (IllegalLocalizationFileTypeException exc) {
					continue;
				}
				flf.read();
				if (flf.containsLocalizationFor(focusTree)) {
					focusTree.setLocalization(flf);
					continue aa;
				}
			}
		}
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

		focusIDList = new ArrayList<>();
		this.focuses.clear();

		/* parser */
		Parser focusTreeParser = new Parser(this.focus_file);
		Node focusTreeNode;
		try {
			focusTreeNode = focusTreeParser.parse();
			var l = focusTreeNode.filterName("focus_tree").toList();
			if (l.isEmpty()) {
				System.out.println("focus_tree filter yielded no results for " + focus_file);
				return null;
			}
			focusTreeNode = l.get(0);
		} catch (Exception e) {
			System.err.println("Error parsing focus tree file: " + focus_file);
			return focusIDList; 
			//throw new RuntimeException(e);
		}

		/* focuses */
		List<Focus> focuses = getFocuses(focusTreeNode);
		minX = focuses.stream().mapToInt(Focus::absoluteX).min().orElse(0);

		// focus_names = new ArrayList<>();
		// focus_names.addAll(focuses.parallelStream()
		// .map(Focus::id).toList());
		// this.focuses.clear();
		// this.focuses.putAll(focuses.parallelStream()
		// .collect(Collectors.toMap(Focus::id, f -> f)));
		// minX = 0; // min x is 0 or less

		/* country */
		// todo should not be .findFirst("modifier"); need mofifier handling. but thats
		// okay.
		Node countryModifierExp = focusTreeNode.findFirst("country").findFirst("modifier");
		if (countryModifierExp != null) {
			try {
				if (countryModifierExp.getValue("tag") != null)
					country = new CountryTag(countryModifierExp.getValue("tag").string());
				else
					country = CountryTag.NULL_TAG;
			} catch (NullPointerException e) {
				System.out.println("Country modifier tag not found, focus file: " + focus_file);
				throw new RuntimeException(e);
			}
		}

		return focusIDList;
	}

	@NotNull
	private List<Focus> getFocuses(Node focusTreeNode) {
		if (focusTreeNode.value().list() == null) {
			System.err.println("Expected list of nodes, for focuses getter");
			return null;
		}

		List<Focus> focusList = new ArrayList<>();

		List<Node> focusTreeNodes = focusTreeNode.filterName("focus").toList();
		for (Node node : focusTreeNodes) {
			Focus focus;
			/* focus id */
			String focus_id = node.getValue("id").string(); // gets the ##### from "id = #####"
			try {
				focus = new Focus(focus_id, this, node);
			} catch (DuplicateFocusException e) {
				System.err.println(e.getMessage());
				continue;
			}
			focusIDList.add(focus_id);
			focuses.put(focus_id, focus);
			focusList.add(focus);
		}

		/* when all focuses init loaded */
		checkPendingFocusReferences();

		return focusList;
	}

	private void checkPendingFocusReferences() {
		List<Focus> resolvedReferences = new ArrayList<>();
		// private final HashMap<String, Consumer<List<Node>>> pendingFocusReferences =
		// new HashMap<>();

		List<PendingFocusReferenceList> pendingFocusReferenceLists = focuses().parallelStream()
				.map(Focus::getPendingFocusReferences).toList();

		/* resolve references of focuses that exist */
		for (var pendingFocusReferenceList : pendingFocusReferenceLists) {
			var pendingFocusReferences = pendingFocusReferenceList.pendingFocusReferences;
			List<String> referencesToRemove = pendingFocusReferences.stream()
					.map(PendingFocusReference::id)
					.filter(id -> focusIDList.contains(id)).toList();
			referencesToRemove.forEach(pendingFocusReferenceList::resolve);
			// todo temp want better warnings in future
			/* unresolved references */
			// pendingFocusReferences.forEach(pfr -> JOptionPane.showMessageDialog(null,
			// "Undefined Focus reference invalid: " + pfr.id() + ", " +
			// pfr.pendingActionMap().keySet()));
			pendingFocusReferences.forEach(pfr -> System.out.println("Warning: [Focus.java] " +
					"Undefined Focus reference invalid: " + pfr.id() + ", " + pfr.pendingActionMap().keySet()));
		}

	}

	/**
	 * Lists last set of focuses
	 * 
	 * @return
	 */
	public ArrayList<String> listFocusIDs() {
		if (focusIDList == null) {
			return null; // bad :(
		}

		return focusIDList;
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
			FXWindow.openGlobalErrorWindow(
					"Focus loc. file does not exist/is not available or other error. Details: \n\t"
							+ e.getLocalizedMessage());
		} catch (IllegalLocalizationFileTypeException e) {
			throw new RuntimeException(e);
		}
		for (Focus focus : focuses()) {
			focus.setLocalization(focusLocFile);
		}
		return locFile;
	}

	// todo redo entire localization how it works :(
	public File setLocalization(FocusLocalizationFile file) {
		this.focusLocFile = file;
		for (Focus focus : focuses()) {
			focus.setLocalization(focusLocFile);
		}
		return focusLocFile;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

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
		return focuses.get(focus_id);
	}

	public int minX() {
		return minX;
	}

	public static HashMap<File, FocusTree> add(FocusTree focusTree) {
		focusTrees.put(focusTree.focus_file, focusTree);
		return new HashMap<>(focusTrees);
	}

	public static FocusTree[] listFocusTrees() {
		return focusTrees.values().toArray(new FocusTree[0]);
	}

	/**
	 * Returns focus tree corresponding to the tag, if it exists
	 * 
	 * @param tag
	 * @return The focus tree, or null if could not be found/not yet created.
	 */
	public static FocusTree get(CountryTag tag) {
		return focusTrees.get(tag);
	}

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
			aa: if (tree.locFile() != null) {
				try (Scanner locReader = new Scanner(tree.locFile().getFile())) {
					ArrayList<String> focuses = tree.listFocusIDs();
					if (focuses == null) {
						break aa;
					}

					// ArrayList<Boolean> localized; Commited out till Skorp fixes this
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
		}
		return focusTrees;
	}

	public static ArrayList<FocusTree> localizedFocusTrees() throws IOException {
		ArrayList<FocusTree> focusTrees = new ArrayList<>();

		for (FocusTree tree : listFocusTrees()) {
			aa: if (tree.locFile() != null) {
				Scanner locReader = new Scanner(tree.locFile().getFile()); // ! TODO close this
				ArrayList<String> focuses = tree.listFocusIDs();
				if (focuses == null) {
					break aa;

				}

				// ArrayList<Boolean> localized; Commited out till Skorp fixies this
				while (locReader.hasNext()) {
					String locLine = locReader.nextLine();
					if (locLine.trim().length() >= COUNTRY_TAG_LENGTH) {
						String potentialTag = locLine.trim().substring(0, COUNTRY_TAG_LENGTH);

						if (CountryTags.exists(potentialTag)) {
							// TODO don't know but remove it if it's not needed
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

	@Override
	public int compareTo(@NotNull FocusTree o) {
		int c = this.country.compareTo(o.country);
		return c == 0 ? this.id.compareTo(o.id) : c;
	}

	@NotNull
	@Override
	public Iterator<Focus> iterator() {
		return focuses().iterator();
	}
}