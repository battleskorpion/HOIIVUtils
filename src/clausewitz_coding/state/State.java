package clausewitz_coding.state;

import clausewitz_coding.code.ClausewitzDate;
import clausewitz_parser.Expression;
import clausewitz_parser.Parser;
import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.state.buildings.Infrastructure;
import clausewitz_coding.state.buildings.Resources;
import clausewitz_coding.country.CountryTag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class State {
    /* static */
    private static final ArrayList<State> states = new ArrayList<>();

    private File stateFile;
    private int stateID;
    private String name;
    private final Map<ClausewitzDate, Owner> owner;
    private StateCategory stateCategory;
    private Infrastructure stateInfrastructure;
    private Resources resourcesData;


    public State(File stateFile) {
        /* init */
        owner = new HashMap<>();

        this.stateFile = stateFile;
        this.name = stateFile.getName().replace(".txt", "");

        int infrastructure = 0;
        int population = 0;
        int civilianFactories = 0;
        int militaryFactories = 0;
        int dockyards = 0;
//        int navalPorts = 0;       has a province location
        int airfields = 0;
        int aluminum = 0;
        int chromium = 0;
        int oil = 0;
        int rubber = 0;
        int steel = 0;
        int tungsten = 0;

        /* parse state data */
        Parser stateParser = new Parser(stateFile);
        //Expression exp = stateParser.expressions();

        // id
        if (stateParser.find("id") != null) {
            stateID = stateParser.find("id").getValue();
        }
        // owner
        if (stateParser.find("owner") != null) {
            // empty date constructor for default date
            owner.put(new ClausewitzDate(), new Owner(new CountryTag(stateParser.find("owner").getText())));
        }
        // population (manpower)
        if (stateParser.find("manpower") != null) {
            population = stateParser.find("manpower").getValue(); // TODO after here etc.
        }
        // state category
        if (stateParser.find("state_category") != null) {

        }

        /* buildings */
        Expression buildingsExp = stateParser.find("buildings = {");

        if (buildingsExp == null) {
            System.err.println("State error: buildings does not exist, " + stateFile.getName());
            stateInfrastructure = null;
        }
        else {
            // infrastructure
            if (stateParser.find("infrastructure") != null) {
                infrastructure = stateParser.find("infrastructure").getValue(); // TODO after here etc.
            }
            // civilian factories
            if (stateParser.find("industrial_complex") != null) {
                civilianFactories = stateParser.find("industrial_complex").getValue(); // TODO after here etc.
            }
            // military factories
            if (stateParser.find("arms_factory") != null) {
                militaryFactories = stateParser.find("arms_factory").getValue(); // TODO after here etc.
            }
            // dockyards
            if (stateParser.find("dockyard") != null) {
                dockyards = stateParser.find("dockyard").getValue(); // TODO after here etc.
            }
            // airfields
            if (stateParser.find("air_base") != null) {
                airfields = stateParser.find("air_base").getValue(); // TODO after here etc.
            }
        }

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

        // data record
        stateInfrastructure = new Infrastructure(population, infrastructure, civilianFactories, militaryFactories,
                dockyards, 0, airfields);
        resourcesData = new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
        // add to states list
        states.add(this);
    }

    public static void readStates() {
        File states_dir = new File(HOI4Fixes.hoi4_dir_name + HOI4Fixes.states_folder);

        if (!states_dir.exists() || !states_dir.isDirectory()) {
            System.err.println("In State.java - " + HOI4Fixes.states_folder + " is not a directory, or etc.");
            return;
        }
        if (states_dir.listFiles() == null || states_dir.listFiles().length == 0) {
            System.out.println("No states found in " + HOI4Fixes.states_folder);
            return;
        }

        for (File stateFile : states_dir.listFiles()) {
            new State(stateFile);
        }
    }

    public static ArrayList<State> list() {
        return states;
    }

    public static ArrayList<State> listFromCountry(CountryTag tag) {
        ArrayList<State> countryStates = new ArrayList<>();

        for(State state : states) {
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

        return new Infrastructure(population, infrastructure, civilianFactories, militaryFactories, dockyards, 0, airfields);
    }

    public static Resources resourcesOfStates(ArrayList<State> states) {
        int aluminum = 0;
        int chromium = 0;
        int oil = 0;
        int rubber = 0;
        int steel = 0;
        int tungsten = 0;

        for (State state : states) {
            Resources resources = state.getResources();
            aluminum += resources.aluminum();
            chromium += resources.chromium();
            oil += resources.oil();
            rubber += resources.rubber();
            steel += resources.steel();
            tungsten += resources.tungsten();
        }

        return new Resources(aluminum, chromium, oil, rubber, steel, tungsten);
    }

    public static Resources resourcesOfStates() {
        return resourcesOfStates(states);
    }

    public static double numStates(CountryTag country) {
        return listFromCountry(country).size();
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
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }
}
