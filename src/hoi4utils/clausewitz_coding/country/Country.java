package hoi4utils.clausewitz_coding.country;

import hoi4utils.clausewitz_coding.state.State;
import hoi4utils.clausewitz_coding.state.buildings.Infrastructure;
import hoi4utils.clausewitz_coding.state.resources.Resource;
import hoi4utils.clausewitz_coding.state.resources.Resources;
import hoi4utils.clausewitz_coding.technology.Technology;
import hoi4utils.clausewitz_coding.units.OrdersOfBattle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jdk.jfr.Percentage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class Country {
	private static final ObservableList<Country> countryList = FXCollections.observableArrayList();
	private static final int AIRFIELD_CAPACITY_PER_LEVEL = 200;
	private CountryTag countryTag;
	private Infrastructure infrastructure;          // infrastructure of all owned states
	private Resources resources;                    // resources of all owned states
	private Set<OrdersOfBattle> oob;                // set of potential orders of battles defined in history/countries file (oob)
	private int defaultResearchSlots;               // default research slots as defined in history/countries file or similar

	//private Set<CountryFlag> countryFlags;
	private double stability;                       // stability percentage defined from 0.0-1.0
	private double warSupport;                      // war support percentage defined from 0.0-1.0
	private Set<Technology> startingTech;      // starting technology defined in history/countries file


	public Country() {
		this(CountryTag.NULL_TAG);
	}

	public Country(CountryTag countryTag) {
		this(countryTag, new Infrastructure(), new Resources());
	}

	public Country(CountryTag countrytag, Infrastructure infrastructure, Resources resources) {
		this.countryTag = countrytag;
		this.infrastructure = infrastructure;
		this.resources = resources;
	}

	public <T> Country(T item) {
		if (item instanceof CountryTag) {
			this.countryTag = (CountryTag) item;
			this.infrastructure = new Infrastructure();
			this.resources = new Resources();

		} else if (item instanceof Country country) {
			this.countryTag = country.countryTag;
			this.infrastructure = country.infrastructure;
			this.resources = country.resources;
		} else {
			this.countryTag = CountryTag.NULL_TAG;
			this.infrastructure = new Infrastructure();
			this.resources = new Resources();
		}
	}

	/**
	 * @return
	 * @deprecated
	 */
	public static List<Country> getList() {
		if (countryList == null || countryList.isEmpty()) {
			loadCountries();
		}
		return countryList;
	}

	public static List<Function<Country, ?>> getCountryDataFunctions(boolean resourcePercentages) {
		List<Function<Country, ?>> dataFunctions = new ArrayList<>(18);         // 18 for optimization, limited number of data functions.

		dataFunctions.add(Country::name);
		dataFunctions.add(Country::population);
		dataFunctions.add(Country::civilianFactories);
		dataFunctions.add(Country::militaryFactories);
		dataFunctions.add(Country::navalDockyards);
		dataFunctions.add(Country::airfields);
		dataFunctions.add(Country::civMilRatio);
		dataFunctions.add(Country::popPerFactoryRatio);
		dataFunctions.add(Country::popPerCivRatio);
		dataFunctions.add(Country::popPerMilRatio);
		dataFunctions.add(Country::popAirportCapacityRatio);
		dataFunctions.add(Country::popPerStateRatio);
		if (resourcePercentages) {
			dataFunctions.add(Country::aluminum);
			dataFunctions.add(Country::chromium);
			dataFunctions.add(Country::oil);
			dataFunctions.add(Country::rubber);
			dataFunctions.add(Country::steel);
			dataFunctions.add(Country::tungsten);
		} else {
			dataFunctions.add(Country::aluminumPercentOfGlobal);
			dataFunctions.add(Country::chromiumPercentOfGlobal);
			dataFunctions.add(Country::oilPercentOfGlobal);
			dataFunctions.add(Country::rubberPercentOfGlobal);
			dataFunctions.add(Country::steelPercentOfGlobal);
			dataFunctions.add(Country::tungstenPercentOfGlobal);
		}

		return dataFunctions;
	}

	public CountryTag countryTag() {
		return countryTag;
	}

	public void setCountryTag(CountryTag countryTag) {
		this.countryTag = countryTag;
	}

	public Infrastructure getInfrastructureRecord() {
		return infrastructure;
	}

	public int infrastructure() {
		return infrastructure.infrastructure();
	}

	public int civilianFactories() {
		return infrastructure.civilianFactories();
	}

	public int militaryFactories() {
		return infrastructure.militaryFactories();
	}

	public int navalDockyards() {
		return infrastructure.navalDockyards();
	}

	public int navalPorts() {
		return infrastructure.navalPorts();
	}

	public int airfields() {
		return infrastructure.airfields();
	}

	public void setInfrastructure(Infrastructure infrastructure) {
		this.infrastructure = infrastructure;
	}

	public Resources resources() {
		return resources;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

	public int population() {
		return infrastructure.population();
	}

	public int aluminum() {
		return resources.get("aluminum").amt();
	}

	public int chromium() {
		return resources.get("chromium").amt();
	}

	public int oil() {
		return resources.get("oil").amt();
	}

	public int rubber() {
		return resources.get("rubber").amt();
	}

	public int steel() {
		return resources.get("steel").amt();
	}

	public int tungsten() {
		return resources.get("tungsten").amt();
	}

	private double tungstenPercentOfGlobal() {
		return (double) tungsten() / State.resourcesOfStates().get("tungsten").amt();       // todo this should really be optimized and some other things later on.
		// todo this all (getting the resources) should be done a lil differently (more generically still.)
	}

	private double steelPercentOfGlobal() {
		// ! states resources and everything are correct, steel() etc must probably be returning 0.
		return (double) steel() / State.resourcesOfStates().get("steel").amt();
	}

	private double rubberPercentOfGlobal() {
		return (double) rubber() / State.resourcesOfStates().get("rubber").amt();
	}

	private double oilPercentOfGlobal() {
		return (double) oil() / State.resourcesOfStates().get("oil").amt();
	}

	private double chromiumPercentOfGlobal() {
		return (double) chromium() / State.resourcesOfStates().get("chromium").amt();
	}

	private double aluminumPercentOfGlobal() {
		return (double) aluminum() / State.resourcesOfStates().get("aluminum").amt();
	}


	public void updateInfrastructure(int value) {
		this.infrastructure = new Infrastructure(population(), value, civilianFactories(), militaryFactories(), navalDockyards(), navalPorts(), airfields());
	}

	public void updatePopulation(int value) {
		this.infrastructure = new Infrastructure(value, infrastructure(), civilianFactories(), militaryFactories(), navalDockyards(), navalPorts(), airfields());
	}

	public void updateCivilianFactories(int value) {
		this.infrastructure = new Infrastructure(population(), infrastructure(), value, militaryFactories(), navalDockyards(), navalPorts(), airfields());
	}

	public void updateMilitaryFactories(int value) {
		this.infrastructure = new Infrastructure(population(), infrastructure(), civilianFactories(), value, navalDockyards(), navalPorts(), airfields());
	}

	public void updateNavalDockyards(int value) {
		this.infrastructure = new Infrastructure(population(), infrastructure(), civilianFactories(), militaryFactories(), value, navalPorts(), airfields());
	}

	public void updateNavalPorts(int value) {
		this.infrastructure = new Infrastructure(population(), infrastructure(), civilianFactories(), militaryFactories(), navalDockyards(), value, airfields());
	}

	public void updateAirfields(int value) {
		this.infrastructure = new Infrastructure(population(), infrastructure(), civilianFactories(), militaryFactories(), navalDockyards(), navalPorts(), value);
	}

	public static <T> List<Country> loadCountries(List<T> list) {
		for (T item : list) {
			countryList.add(new Country(item));
		}

		return countryList;
	}

	public static ObservableList<Country> loadCountries(List<CountryTag> countryTags, List<Infrastructure> infrastructureList, List<Resources> resourcesList) {
		Iterator<CountryTag> countryTagsIterator = countryTags.iterator();
		Iterator<Infrastructure> infrastructureListIterator = infrastructureList.iterator();
		Iterator<Resources> resourcesListIterator = resourcesList.iterator();

		while (countryTagsIterator.hasNext()) {
			countryList.add(new Country(countryTagsIterator.next(), infrastructureListIterator.next(), resourcesListIterator.next()));
		}

		return countryList;
	}

	public static ObservableList<Country> loadCountries(List<Infrastructure> infrastructureList, List<Resources> resourcesList) {
		// ! todo resourcesList.get(0).get([resource]).amt() is 0.
		return loadCountries(CountryTags.getCountryTags(), infrastructureList, resourcesList);
	}

	public static ObservableList<Country> loadCountries() {
		return loadCountries(State.infrastructureOfCountries(), State.resourcesOfCountries());
	}

	public String name() {
		return countryTag.toString();
	}


	public double civMilRatio() {
		return (double) civilianFactories() / militaryFactories();
	}

	public double popPerFactoryRatio() {
		return (double) population() / factories();
	}

	public double popPerCivRatio() {
		return (double) population() / civilianFactories();
	}

	public double popPerMilRatio() {
		return (double) population() / militaryFactories();
	}

	public double popAirportCapacityRatio() {
		return (double) population() / airfieldsCapacity();
	}

	public double popPerStateRatio() {
		return (double) population() / numOwnedStates();
	}

	private double factories() {
		return militaryFactories() + civilianFactories();
	}

	private double airfieldsCapacity() {
		return airfields() * AIRFIELD_CAPACITY_PER_LEVEL;
	}

	private double numOwnedStates() {
		return 1;   // todo;
	}
}
