package main.java.com.hoi4utils.clauzewitz.map.state;

import main.java.com.hoi4utils.clauzewitz.HOIIVFile;
import main.java.com.hoi4utils.clauzewitz.localization.Localizable;
import main.java.com.hoi4utils.clauzewitz.code.ClausewitzDate;
import main.java.com.hoi4utils.clauzewitz.data.country.Country;
import main.java.com.hoi4utils.clauzewitz.data.country.CountryTag;
import main.java.com.hoi4utils.clauzewitz.data.country.CountryTagsManager;
import main.java.com.hoi4utils.clauzewitz.map.Owner;
import main.java.com.hoi4utils.clauzewitz.map.UndefinedStateIDException;
import main.java.com.hoi4utils.clauzewitz.map.buildings.Infrastructure;
import main.java.com.hoi4utils.clauzewitz.map.province.VictoryPoint;
import main.java.com.hoi4utils.clauzewitz.map.resources.Resources;
import main.kotlin.com.hoi4utils.clausewitz_parser.Node;
import main.kotlin.com.hoi4utils.clausewitz_parser.Parser;
import main.kotlin.com.hoi4utils.clausewitz_parser.ParserException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Function;

/**
 * Loads HOI4 State files, each instance represents a state as defined in "history/states"
 * Localizable: state name
 *
 * I apologize in advance.
 */
public class State implements InfrastructureData, Localizable, Iterable<State>, Comparable<State> {
	/* static */
	private static final ArrayList<State> states = new ArrayList<>();

	private File stateFile;
	private int stateID;
	private String name;
	private final Map<ClausewitzDate, Owner> owner;
	//! todo Finish state Category
	// private StateCategory stateCategory; 
	private Infrastructure stateInfrastructure;
	private Resources resourcesData;
	private List<VictoryPoint> victoryPoints;

	public State(File stateFile) {
		this(stateFile, true);
	}

	public State(File stateFile, boolean addToStatesList) {
		/* init */
		owner = new HashMap<>();

		this.stateFile = stateFile;
		this.name = stateFile.getName().replace(".txt", "");
		this.victoryPoints = new ArrayList<>();

		readStateFile(stateFile);

		// add to states list
		if (addToStatesList) {
			states.add(this);
		}
	}

	// todo simplify
	private void readStateFile(File stateFile) {
		int infrastructure = 0;
		int population = 0;
		int civilianFactories = 0;
		int militaryFactories = 0;
		int dockyards = 0;
		// ! todo something important
		// int navalPorts = 0; //has a province location
		int airfields = 0;

		/* parse state data */
		Parser stateParser = new Parser(stateFile);
		// Expression exp = stateParser.expressions();
		Node stateNode = null;
		try {
			stateNode = stateParser.parse().findFirst("state");
		} catch (ParserException e) {
			throw new RuntimeException(e);
		}

		// id
		if (stateNode.contains("id")) {
			stateID = stateNode.getValue("id").integer();
		} else {
			System.out.println(stateNode.value().asString());
			throw new UndefinedStateIDException(stateFile);
		}
		// population (manpower)
		if (stateNode.contains("manpower")) {
			population = stateNode.getValue("manpower").integer(); // todo after here etc.
		}
		// state category
		if (stateNode.contains("state_category")) {

		}

		/* buildings */
		if (stateNode.contains("history")) {
			Node historyNode = stateNode.findFirst("history");
			Node buildingsNode = null;
			if (historyNode.contains("buildings")) {
				buildingsNode = historyNode.findFirst("buildings");
			}
			// owner
			if (historyNode.contains("owner")) {
				// empty date constructor for default date
				owner.put(ClausewitzDate.of(), new Owner(new CountryTag(historyNode.getValue("owner").string())));
			} else {
				System.err.println("Warning: state owner not defined, " + stateFile.getName());
			}
			if (buildingsNode == null) {
				System.err.println("Warning: buildings (incl. infrastructure) not defined in state, " + stateFile.getName());
				stateInfrastructure = null;
			} else {
				// infrastructure
				if (buildingsNode.contains("infrastructure")) {
					infrastructure = buildingsNode.getValue("infrastructure").integer(); // todo after here etc.
				}
				// civilian factories
				if (buildingsNode.contains("industrial_complex")) {
					civilianFactories = buildingsNode.getValue("industrial_complex").integer(); // todo after here etc.
				}
				// military factories
				if (buildingsNode.contains("arms_factory")) {
					militaryFactories = buildingsNode.getValue("arms_factory").integer(); // todo after here etc.
				}
				// dockyards
				if (buildingsNode.contains("dockyard")) {
					dockyards = buildingsNode.getValue("dockyard").integer(); // todo after here etc.
				}
				// airfields
				if (buildingsNode.contains("air_base")) {
					airfields = buildingsNode.getValue("air_base").integer(); // todo after here etc.
				}
			}
			/* victory points */
			if (historyNode.contains("victory_points")) {
				Node victoryPointsNode = historyNode.findFirst("victory_points");
				for (Node vpNode : victoryPointsNode.toList()) {
					if (!vpNode.contains("province") || !vpNode.contains("value")) {
						System.out.println("Warning: invalid victory point node in state, " + stateFile.getName());
						continue;
					}
					var vp = VictoryPoint.of(vpNode.getValue("province").integer(),
							vpNode.getValue("value").integer());
					victoryPoints.add(vp);
				}
				//locHandler.addLocalization(victoryPoints, "VICTORY_POINTS_\\d+");
			}
		} else {
			System.out.println("Warning: history not defined in state, " + stateFile.getName());
			System.out.println(stateNode.value().asString());
		}

		// resources
		resourcesData = findStateResources(stateNode);

		// data record
		stateInfrastructure = new Infrastructure(population, infrastructure, civilianFactories, militaryFactories,
				dockyards, 0, airfields);
	}

	public static void read() {
		if (!HOIIVFile.mod_states_folder.exists() || !HOIIVFile.mod_states_folder.isDirectory()) {
			System.err.println("In State.java - " + HOIIVFile.mod_states_folder + " is not a directory, or etc.");
			return;
		}
		if (HOIIVFile.mod_states_folder.listFiles() == null || HOIIVFile.mod_states_folder.listFiles().length == 0) {
			System.out.println("No states found in " + HOIIVFile.mod_states_folder);
			return;
		}

		for (File stateFile : HOIIVFile.mod_states_folder.listFiles()) {
			if (stateFile.getName().endsWith(".txt"))
				new State(stateFile);
		}
	}

	public static ArrayList<State> list() {
		return states;
	}

	public static ArrayList<State> ownedStatesOfCountry(Country country) {
		return ownedStatesOfCountry(country.countryTag());
	}

	public static ArrayList<State> ownedStatesOfCountry(CountryTag tag) {
		ArrayList<State> countryStates = new ArrayList<>();

		for (State state : states) {
			Owner owner = state.owner.get(ClausewitzDate.defaulty());
			if (owner != null) {
				if (owner.isCountry(tag)) {
					countryStates.add(state);
				}
			} else {
				System.out.println("Undefined owner for state: " + state);
			}
		}

		return countryStates;
	}

	public static Infrastructure infrastructureOfStates(ArrayList<State> states) {
		int infrastructure = 0;
		int population = 0;
		int civilianFactories = 0;
		int militaryFactories = 0;
		int dockyards = 0;
		int airfields = 0;

		for (State state : states) {
			Infrastructure stateData = state.getStateInfrastructure();
			infrastructure += stateData.infrastructure();
			population += stateData.population();
			civilianFactories += stateData.civilianFactories();
			militaryFactories += stateData.militaryFactories();
			dockyards += stateData.navalDockyards();
			airfields += stateData.airfields();
		}

		return new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, dockyards, 0,
				airfields);
	}

	// todo this is called and ran lots of times, optimize?
	public static Resources resourcesOfStates(ArrayList<State> states) {
		Resources resourcesOfStates = new Resources();
//		int aluminum = 0;
//		int chromium = 0;
//		int oil = 0;
//		int rubber = 0;
//		int steel = 0;
//		int tungsten = 0;

		for (State state : states) {
			Resources resources = state.getResources();
			resourcesOfStates.add(resources);
		}

		//return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
		System.out.println(resourcesOfStates.get("aluminum").amt());
		return resourcesOfStates;
	}

	public static Resources resourcesOfStates() {
		return resourcesOfStates(states);
	}

	public static int numStates(CountryTag country) {
		return ownedStatesOfCountry(country).size();
	}

	public static State get(String state_name) {
		state_name = state_name.trim();
		for (State state : states) {
			if (state.name.equals(state_name)) {
				return state;
			}
		}
		return null;
	}

	public static State get(File file) {
		for (State state : states) {
			if (state.stateFile.equals(file)) {
				return state;
			}
		}

		return null;
	}

	/**
	 * If the state represented by file is not in the list of states, creates the
	 * new state.
	 * If the state already exists, overwrites the state.
	 * 
	 * @param file state file
	 */
	public static void readState(File file) {
		State tempState = new State(file, false);
		if (tempState.stateID < 1) {
			System.err.println("Error: Invalid state id for state " + tempState);
			return;
		}

		for (State state : states) {
			if (state.stateID == tempState.stateID) {
				states.remove(state);
				states.add(tempState);
				System.out.println("Modified state " + tempState);
				return;
			}
		}

		// if state did not exist already in states
		states.add(tempState);
		System.out.println("Added state " + tempState);
	}

	/**
	 * If the state represented by the file exists in states list, removes the state
	 * from the states list
	 * 
	 * @param file state file
	 */
	public static void deleteState(File file) {
		State tempState = new State(file, false);
		if (tempState.stateID < 1) {
			System.err.println("Error: Invalid state id for state " + tempState);
			return;
		}
		for (State state : states) {
			if (state.stateID == tempState.stateID) {
				states.remove(state);
				System.out.println("Removed state " + tempState);
				return;
			}
		}

		System.out.println("Tried to delete state represented by file: " + "\n\t" + file + "\n\t"
				+ "but state not found in states list");
	}

	public static State get(int id) {
		return states.stream().filter(state -> state.stateID == id).findFirst().orElse(null);
	}

	public static List<Function<State, ?>> getStateDataFunctions(boolean resourcePercentages) {
		List<Function<State, ?>> dataFunctions = new ArrayList<>(17);         // for optimization, limited number of data functions.

		dataFunctions.add(State::id);
		dataFunctions.add(c -> c.stateInfrastructure.population());
		dataFunctions.add(c -> c.stateInfrastructure.civMilRatio());
		dataFunctions.add(c -> c.stateInfrastructure.militaryFactories());
		dataFunctions.add(c -> c.stateInfrastructure.navalDockyards());
		dataFunctions.add(c -> c.stateInfrastructure.airfields());
		dataFunctions.add(c -> c.stateInfrastructure.civMilRatio());
		dataFunctions.add(c -> c.stateInfrastructure.popPerFactoryRatio());
		dataFunctions.add(c -> c.stateInfrastructure.popPerCivRatio());
		dataFunctions.add(c -> c.stateInfrastructure.popPerMilRatio());
		dataFunctions.add(c -> c.stateInfrastructure.popAirportCapacityRatio());
		/* todo better way to do this obv! plz fix :(
		with (wrapper function that returns either or depndent on resourcesPerfcentages boolean value ofc */
		// also if we're gonna have different resources able to load in down the line... it'll break this.
		if (resourcePercentages) {
			dataFunctions.add(s -> s.getResources().get("aluminum").amt());
			dataFunctions.add(s -> s.getResources().get("chromium").amt());
			dataFunctions.add(s -> s.getResources().get("oil").amt());
			dataFunctions.add(s -> s.getResources().get("rubber").amt());
			dataFunctions.add(s -> s.getResources().get("steel").amt());
			dataFunctions.add(s -> s.getResources().get("tungsten").amt());
		} else {
			dataFunctions.add(s -> s.getResources().get("aluminum").percentOfGlobal());
			dataFunctions.add(s -> s.getResources().get("chromium").percentOfGlobal());
			dataFunctions.add(s -> s.getResources().get("oil").percentOfGlobal());
			dataFunctions.add(s -> s.getResources().get("rubber").percentOfGlobal());
			dataFunctions.add(s -> s.getResources().get("steel").percentOfGlobal());
			dataFunctions.add(s -> s.getResources().get("tungsten").percentOfGlobal());
		}

		return dataFunctions;
	}


	public Resources findStateResources(Node stateNode) {
		int aluminum = 0;
		int chromium = 0;
		int oil = 0;
		int rubber = 0;
		int steel = 0;
		int tungsten = 0;

		if (!stateNode.contains("resources")) {
			return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
		}

		/* resources */
		Node resourcesNode = stateNode.findFirst("resources");
		// aluminum (aluminium bri'ish spelling)
		if (resourcesNode.contains("aluminium")) {        // ! todo always null
			aluminum = (int) resourcesNode.getValue("aluminium").rational();
		}
		// chromium
		if (resourcesNode.contains("chromium")) {
			chromium = (int) resourcesNode.getValue("chromium").rational();
		}
		// rubber
		if (resourcesNode.contains("rubber")) {
			rubber = (int) resourcesNode.getValue("rubber").rational();
		}
		// oil
		if (resourcesNode.contains("oil")) {
			oil = (int) resourcesNode.getValue("oil").rational();
		}
		// steel
		if (resourcesNode.contains("steel")) {
			steel = (int) resourcesNode.getValue("steel").rational();
		}
		// tungsten
		if (resourcesNode.contains("tungsten")) {
			tungsten = (int) resourcesNode.getValue("tungsten").rational();
		}

		return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
	}

	public static List<Infrastructure> infrastructureOfCountries() {
		List<CountryTag> countryList = CountryTagsManager.getCountryTags();
		List<Infrastructure> countriesInfrastructureList = new ArrayList<>();

		for (CountryTag tag : countryList) {
			countriesInfrastructureList.add(infrastructureOfCountry(tag));
		}

		return countriesInfrastructureList;
	}

	private static Infrastructure infrastructureOfCountry(CountryTag tag) {
		return infrastructureOfStates(ownedStatesOfCountry(tag));
	}

	// ! todo test if working
	public static List<Resources> resourcesOfCountries() {
		List<CountryTag> countryList = CountryTagsManager.getCountryTags();
		List<Resources> countriesResourcesList = new ArrayList<>();

		for (CountryTag tag : countryList) {
			countriesResourcesList.add(resourcesOfCountry(tag));
		}

		return countriesResourcesList;
	}

	public static Resources resourcesOfCountry(CountryTag tag) {
		return resourcesOfStates(ownedStatesOfCountry(tag));
	}

	public Infrastructure getStateInfrastructure() {
		return stateInfrastructure;
	}

	public Resources getResources() {
		return resourcesData;
	}

	public String toString() {
		return name;
	}

	protected static boolean usefulData(String data) {
		if (!data.isEmpty()) {
			if (data.trim().charAt(0) == '#') {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public File getFile() {
		return stateFile;
	}

	public Infrastructure getInfrastructureRecord() {
		return stateInfrastructure;
	}

	public int id() {
		return stateID;
	}

	@NotNull
	@Override
	public Iterator<State> iterator() {
		return states.iterator();
	}

	@Override
	public int compareTo(@NotNull State o) {
		return Integer.compare(stateID, o.stateID);
	}

	@Override
	public @NotNull Map<Property, String> getLocalizableProperties() {
		return Map.of(Property.NAME, name);
	}

	@Override
	public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
		return states;
	}
}
