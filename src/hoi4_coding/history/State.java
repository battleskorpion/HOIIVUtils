package hoi4_coding.history;

import hoi4_coding.HOI4Fixes;
import hoi4_coding.buildings.Infrastructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

import static hoi4_coding.HOI4Fixes.states_dir;

public class State {
    private static final ArrayList<State> states = new ArrayList<>();

    private File stateFile;
    private String name;
    private Infrastructure stateData;

    public State(File stateFile) {
        this.stateFile = stateFile;
        this.name = stateFile.getName();

        int infrastructure = 0;
        int population = 0;
        int civilianFactories = 0;
        int militaryFactories = 0;
        int dockyards = 0;
//        int navalPorts = 0;       has a province location
//        int airfields = 0;        has a province location

        //todo fix constructor time not working issues
//        Scanner stateReader;
//        try {
//            stateReader = new Scanner(stateFile);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//
//        String[] data = stateReader.next().replaceAll("\s", "").split("=\\{|}|\n");
//
//        // parse buildings


//        String dataJoined = String.join(" ", data).;
//
//        int infrastructureIndex = dataJoined.indexOf("infrastructure");
//        int populationIndex = dataJoined.indexOf("manpower");
//        int civFactoriesIndex = dataJoined.indexOf("industrial_complex");
//        int milFactoriesIndex = dataJoined.indexOf("arms_factory");
//        int dockyardsIndex = dataJoined.indexOf("dockyard");
//
//        if(infrastructureIndex >= 0) {
//
//        }



        // data record
        stateData = new Infrastructure(population, infrastructure, civilianFactories, militaryFactories,
                dockyards, navalPorts, airfields);

        // add to states list
        states.add(this);
    }

    public static void readStates() {
        for (File stateFile : Objects.requireNonNull(states_dir.listFiles())) {
            new State(stateFile);
        }
    }

    public static ArrayList<State> list() {
        return states;
    }

    public Infrastructure getStateData() {
        return stateData;
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
