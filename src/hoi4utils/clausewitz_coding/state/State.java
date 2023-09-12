package hoi4utils.clausewitz_coding.state;

import hoi4utils.HOIIVFile;
import hoi4utils.clausewitz_coding.code.ClausewitzDate;
import hoi4utils.clausewitz_coding.country.CountryTags;
import hoi4utils.clausewitz_parser.Expression;
import hoi4utils.clausewitz_parser.Parser;
import hoi4utils.clausewitz_coding.state.buildings.Infrastructure;
import hoi4utils.clausewitz_coding.state.resources.Resources;
import hoi4utils.clausewitz_coding.country.CountryTag;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads HOI4 State files, each instance represents a state as defined in "history/states"
 */
public class State {
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

	public State(File stateFile) {
		this(stateFile, true);
	}

	public State(File stateFile, boolean addToStatesList) {
		/* init */
		owner = new HashMap<>();

		this.stateFile = stateFile;
		this.name = stateFile.getName().replace(".txt", "");

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

		// id
		if (stateParser.find("id") != null) {
			stateID = stateParser.find("id").getValue();
		}
		// population (manpower)
		if (stateParser.find("manpower") != null) {
			population = stateParser.find("manpower").getValue(); // todo after here etc.
		}
		// state category
		if (stateParser.find("state_category") != null) {

		}

		/* buildings */
		Expression historyExp = stateParser.find("history");
		System.out.println(historyExp.getSubexpressions());
		Expression buildingsExp = null;
		// todo fix purposless code if statemnt nevered mattered
		if (historyExp != null) {
			buildingsExp = historyExp.getImmediate("buildings"); // default buildings
			// owner
			if (historyExp.getImmediate("owner") != null) {
				// empty date constructor for default date
				owner.put(new ClausewitzDate(), new Owner(new CountryTag(historyExp.getImmediate("owner").getText())));
			}
		} /*
		   * else {
		   * System.err.println("State error: history does not exist, " +
		   * stateFile.getName());
		   * }
		   */
		if (buildingsExp == null) {
			System.err.println("Warning: buildings does not exist, " + stateFile.getName());
			stateInfrastructure = null;
		} else {
			// infrastructure
			if (buildingsExp.get("infrastructure") != null) {
				infrastructure = buildingsExp.get("infrastructure").getValue(); // todo after here etc.
			}
			// civilian factories
			if (buildingsExp.get("industrial_complex") != null) {
				civilianFactories = buildingsExp.get("industrial_complex").getValue(); // todo after here etc.
			}
			// military factories
			if (buildingsExp.get("arms_factory") != null) {
				militaryFactories = buildingsExp.get("arms_factory").getValue(); // todo after here etc.
			}
			// dockyards
			if (buildingsExp.get("dockyard") != null) {
				dockyards = buildingsExp.get("dockyard").getValue(); // todo after here etc.
			}
			// airfields
			if (buildingsExp.get("air_base") != null) {
				airfields = buildingsExp.get("air_base").getValue(); // todo after here etc.
			}
		}

		resourcesData = findStateResources(stateParser);

		// data record
		stateInfrastructure = new Infrastructure(population, infrastructure, civilianFactories, militaryFactories,
				dockyards, 0, airfields);

		// add to states list
		if (addToStatesList) {
			states.add(this);
		}
	}

	public static void readStates() {
		if (!HOIIVFile.states_folder.exists() || !HOIIVFile.states_folder.isDirectory()) {
			System.err.println("In State.java - " + HOIIVFile.states_folder + " is not a directory, or etc.");
			return;
		}
		if (HOIIVFile.states_folder.listFiles() == null || HOIIVFile.states_folder.listFiles().length == 0) {
			System.out.println("No states found in " + HOIIVFile.states_folder);
			return;
		}

		for (File stateFile : HOIIVFile.states_folder.listFiles()) {
			new State(stateFile);
		}
	}

	public static ArrayList<State> list() {
		return states;
	}

	public static ArrayList<State> ownedStatesOfCountry(CountryTag tag) {
		ArrayList<State> countryStates = new ArrayList<>();

		for (State state : states) {
			Owner owner = state.owner.get(ClausewitzDate.current());
			if (owner != null) {
				if (owner.isCountry(tag)) {
					countryStates.add(state);
				}
			} else {
				System.out.println("No owner for state: " + state);
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

	public Resources findStateResources(Parser stateParser) {
		int aluminum = 0;
		int chromium = 0;
		int oil = 0;
		int rubber = 0;
		int steel = 0;
		int tungsten = 0;

		/* resources */
		// aluminum (aluminium bri'ish spelling)
		if (stateParser.find("aluminium") != null) {
			aluminum = (int) stateParser.find("aluminium").getDoubleValue();
		}
		// chromium
		if (stateParser.find("chromium") != null) {
			chromium = (int) stateParser.find("chromium").getDoubleValue();
		}
		// rubber
		if (stateParser.find("rubber") != null) {
			rubber = (int) stateParser.find("rubber").getDoubleValue();
		}
		// oil
		if (stateParser.find("oil") != null) {
			oil = (int) stateParser.find("oil").getDoubleValue();
		}
		// steel
		if (stateParser.find("steel") != null) {
			steel = (int) stateParser.find("steel").getDoubleValue();
		}
		// tungsten
		if (stateParser.find("tungsten") != null) {
			tungsten = (int) stateParser.find("tungsten").getDoubleValue();
		}

		return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
	}

	public static List<Infrastructure> infrastructureOfCountries() {
		List<CountryTag> countryList = CountryTags.getCountryTags();
		List<Infrastructure> countriesInfrastructureList = new ArrayList<>();

		for (CountryTag tag : countryList) {
			countriesInfrastructureList.add(infrastructureOfCountry(tag));
		}

		return countriesInfrastructureList;
	}

	private static Infrastructure infrastructureOfCountry(CountryTag tag) {
		return infrastructureOfStates(ownedStatesOfCountry(tag));
	}

	public static List<Resources> resourcesOfCountries() {
		List<CountryTag> countryList = CountryTags.getCountryTags();
		List<Resources> resourcesInfrastructureList = new ArrayList<>();

		for (CountryTag tag : countryList) {
			resourcesInfrastructureList.add(resourcesOfCountry(tag));
		}

		return resourcesInfrastructureList;
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

}
