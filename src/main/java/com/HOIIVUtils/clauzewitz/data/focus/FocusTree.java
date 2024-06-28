package com.HOIIVUtils.clauzewitz.data.focus;

import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.data.country.CountryTagsManager;
import com.HOIIVUtils.clauzewitz.script.*;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clauzewitz.data.country.CountryTag;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.jetbrains.annotations.*;

import java.io.File;
import java.util.*;

/**
 * ALL of the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
// todo extends file?
public class FocusTree extends StructuredPDX implements Localizable, Comparable<FocusTree>, Iterable<Focus> {
	private static final ObservableMap<File, FocusTree> focusTrees = FXCollections.observableHashMap();
	private static final ObservableList<FocusTree> focusTreesList = FXCollections.observableArrayList();
	static {
		focusTrees.addListener((MapChangeListener<File, FocusTree>) change -> {
			updateObservableValues();
			focusTreesList.sort(Comparator.naturalOrder());
		});
	}

	//private final ObservableMap<String, Focus> focuses;
	@NotNull public final ReferencePDXScript<CountryTag> country;
	@NotNull public final MultiPDXScript<Focus> focuses;
	@NotNull public final AbstractPDX<String> id;

	private ArrayList<String> focusIDList;
	@NotNull private File focus_file;
	// private Modifier countryModifier;
	// private boolean defaultFocus; // ! todo Do This
	// private Point continuousFocusPosition; // ! todo DO THIS

	public static FocusTree get(File focus_file) {
		if (!focusTrees.containsKey(focus_file)) {
			new FocusTree(focus_file);
		}
		return focusTrees.get(focus_file);
	}

	/**
	 * Instantiate focus tree from pre-existing focus tree file.
	 * 
	 * @param focus_file pre-existing focus tree.
	 */
	private FocusTree(@NotNull File focus_file) {
		super("focus_tree");
		this.focus_file = focus_file;

		/* pdxscript */
		id = new StringPDX("id");
		country = new ReferencePDXScript<>(CountryTagsManager::getCountryTags, CountryTag::get, "country");
		focuses = new MultiPDXScript<>(() -> new Focus(this), "focus");
		obj.addAll(childScripts());
		loadPDX(focus_file);

		FocusTree.add(this);
	}

	@Override
	protected Collection<? extends AbstractPDX<?>> childScripts() {
		return List.of(id, country, focuses);
	}

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

//	private ArrayList<String> parse() {
//		if (this.focus_file == null) {
//			System.err.println(this + "File of focus tree not set.");
//			return null;
//		}
//		if (!this.focus_file.exists()) {
//			System.err.println(this + "Focus tree file does not exist.");
//			return null;
//		}
//
//		focusIDList = new ArrayList<>();
//		this.focuses.clear();
//
//		/* parser */
//		Parser focusTreeParser = new Parser(this.focus_file);
//		Node focusTreeNode;
//		try {
//			focusTreeNode = focusTreeParser.parse();
//			var l = focusTreeNode.filterName("focus_tree").toList();
//			if (l.isEmpty()) {
//				System.out.println("focus_tree filter yielded no results for " + focus_file);
//				return null;
//			}
//			focusTreeNode = l.get(0);
//		} catch (Exception e) {
//			System.err.println("Error parsing focus tree file: " + focus_file);
//			return focusIDList;
//			//throw new RuntimeException(e);
//		}
//
//		/* focus tree id */
//		try {
//			id = focusTreeNode.getValue("id").string();
//		} catch (NullPointerException e) {
//			System.err.println("Focus tree id not found, focus file: " + focus_file);
//			// todo throw exception?
//		}
//
//		/* country */
//		// todo should not be .findFirst("modifier"); need mofifier handling. but thats
//		// okay.
//		Node countryModifierExp = focusTreeNode.findFirst("country").findFirst("modifier");
//		if (countryModifierExp != null) {
//			try {
//				if (countryModifierExp.getValue("tag") != null)
//					country = new CountryTag(countryModifierExp.getValue("tag").string());
//				else
//					country = CountryTag.NULL_TAG;
//			} catch (NullPointerException e) {
//				System.out.println("Country modifier tag not found, focus file: " + focus_file);
//				throw new RuntimeException(e);
//			}
//		} else {
//			System.out.println("Country modifier not found, focus file: " + focus_file);
//		}
//
//		/* focuses */
//		List<Focus> focuses = getFocuses(focusTreeNode);
//		minX = focuses.stream().mapToInt(Focus::absoluteX).min().orElse(0);
//
//		return focusIDList;
//	}

//	@NotNull
//	private List<Focus> getFocuses(Node focusTreeNode) {
//		if (focusTreeNode.value().list() == null) {
//			System.err.println("Expected list of nodes, for focuses getter");
//			return new ArrayList<>();
//		}
//
//		List<Focus> focusList = new ArrayList<>();
//
//		List<Node> focusTreeNodes = focusTreeNode.filterName("focus").toList();
//		for (Node node : focusTreeNodes) {
//			Focus focus;
//			/* focus id */
//			try {
//				focus = new Focus(this, node);
//			} catch (DuplicateFocusException e) {
//				System.err.println(e.getMessage());
//				continue;
//			}
//			focusIDList.add(focus.id.get());
//			focuses.put(focus);
//			focusList.add(focus);
//		}
//
//		/* when all focuses init loaded */
//		checkPendingFocusReferences();
//
//		return focusList;
//	}

	private void checkPendingFocusReferences() {
//		List<Focus> resolvedReferences = new ArrayList<>();
//		// private final HashMap<String, Consumer<List<Node>>> pendingFocusReferences =
//		// new HashMap<>();
//
//		List<PendingFocusReferenceList> pendingFocusReferenceLists = focuses().parallelStream()
//				.map(Focus::getPendingFocusReferences).toList();
//
//		/* resolve references of focuses that exist */
//		for (var pendingFocusReferenceList : pendingFocusReferenceLists) {
//			var pendingFocusReferences = pendingFocusReferenceList.pendingFocusReferences;
//			List<String> referencesToRemove = pendingFocusReferences.stream()
//					.map(PendingFocusReference::id)
//					.filter(id -> focusIDList.contains(id)).toList();
//			referencesToRemove.forEach(pendingFocusReferenceList::resolve);
//			// todo temp want better warnings in future
//			/* unresolved references */
//			// pendingFocusReferences.forEach(pfr -> JOptionPane.showMessageDialog(null,
//			// "Undefined Focus reference invalid: " + pfr.id() + ", " +
//			// pfr.pendingActionMap().keySet()));
//			pendingFocusReferences.forEach(pfr -> System.out.println("Warning: [Focus.java] " +
//					"Undefined Focus reference invalid: " + pfr.id() + ", " + pfr.pendingActionMap().keySet()));
//		}

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

	public CountryTag countryTag() {
		if (country.get() != null) {
			return country.get();
		} else {
			// idk :(
			return null;
		}
	}

	public File focusFile() {
		return focus_file;
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
		if (id.get() != null) {
			return id.get();
		}
		if (country.get() != null) {
			return country.get().toString();
		}
		return super.toString();
	}

	public @NotNull HashSet<Focus> focuses() {
		var list = this.focuses.get();
		if (list == null) return new HashSet<>();
		return new HashSet<>(list);
	}

	public int minX() {
		return focuses().stream().map(Focus::absoluteX).min(Integer::compareTo).orElse(0);
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
//		return focusTrees.get(tag);
		return focusTrees.values().stream().filter(focusTree -> focusTree.country.objEquals(tag)).findFirst().orElse(null);
	}

	public static FocusTree getdankwizardisfrench(CountryTag tag) {
		for (FocusTree tree : listFocusTrees()) {
			assert tree.country.get() != null;
			if (tree.country.objEquals(tag)) {
				return tree;
			}
		}

		return null;
	}

	public ObservableList<Focus> listFocuses() {
		ObservableList<Focus> focusList = FXCollections.observableArrayList();
		focusList.addAll(focuses());
		return focusList;
	}

	private static void updateObservableValues() {
		focusTreesList.setAll(focusTrees.values());
	}

	@Override
	public int compareTo(@NotNull FocusTree o) {
		int c = 0;
		if (this.country.get() != null && o.country.get() != null) {
			c = this.country.get().compareTo(o.country.get());
		}
		int d = 0;
		if (this.id.get() != null && o.id.get() != null) {
			d = this.id.get().compareTo(o.id.get());
		}
		return c == 0 ? d : c;
	}

	@NotNull
	@Override
	public Iterator<Focus> iterator() {
		return focuses().iterator();
	}

	@Override
	public @NotNull Map<Property, String> getLocalizableProperties() {
		// lets us map null if we use hashmap instead of generic of() method
		HashMap<Property, String> properties = new HashMap<>();
		properties.put(Property.NAME, id.get());
		return properties;
	}

	/**
	 * Get the localizable group of this focus tree, which is the list of focuses.
	 * @return
	 */
	@Override
	public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
		return focuses();
	}

	@Override
	public boolean objEquals(PDXScript<?> other) {
		if (other instanceof FocusTree) {
			return this.equals(other);
		}
		return false;
	}
}