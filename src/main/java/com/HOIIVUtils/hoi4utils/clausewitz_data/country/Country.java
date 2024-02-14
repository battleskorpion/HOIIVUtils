package com.HOIIVUtils.hoi4utils.clausewitz_data.country;

import com.HOIIVUtils.hoi4utils.clausewitz_map.state.InfrastructureData;
import com.HOIIVUtils.hoi4utils.clausewitz_map.state.State;
import com.HOIIVUtils.hoi4utils.clausewitz_map.buildings.Infrastructure;
import com.HOIIVUtils.hoi4utils.clausewitz_map.resources.Resources;
import com.HOIIVUtils.hoi4utils.clausewitz_data.technology.Technology;
import com.HOIIVUtils.hoi4utils.clausewitz_data.units.OrdersOfBattle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

// todo make country extend countrytag???? ehhhhh
// todo consider... implements infrastructure, resources?????
// todo localizable data?
public class Country implements InfrastructureData, Comparable<Country> {
	private static final ObservableList<Country> countryList = FXCollections.observableArrayList();
	private CountryTag countryTag;
	private Infrastructure infrastructure;          // infrastructure of all owned states
	private Resources resources;                    // resources of all owned states
	private Set<OrdersOfBattle> oob_list;           // set of potential orders of battles defined in history/countries file (oob)
	private int defaultResearchSlots;               // default research slots as defined in history/countries file or similar

	private Set<CountryFlag> countryFlags;
	private int capital;                            // country capital as defined by applicable oob or unknown
	private double stability;                       // stability percentage defined from 0.0-1.0
	private double warSupport;                      // war support percentage defined from 0.0-1.0
	private Set<Technology> startingTech;           // starting technology defined in history/countries file


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

	// todo if country extends country tag this could be much better
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
	@Deprecated
	public static List<Country> getList() {
		// TODO: Remove this deprecated code someday.
		if (countryList == null || countryList.isEmpty()) {
			loadCountries();
		}
		return countryList;
	}

	public static List<Function<Country, ?>> getCountryDataFunctions(boolean resourcePercentages) {
		List<Function<Country, ?>> dataFunctions = new ArrayList<>(18);         // 18 for optimization, limited number of data functions.

		dataFunctions.add(Country::name);
		dataFunctions.add(c -> c.infrastructure.population());
		dataFunctions.add(c -> c.infrastructure.civMilRatio());
		dataFunctions.add(c -> c.infrastructure.militaryFactories());
		dataFunctions.add(c -> c.infrastructure.navalDockyards());
		dataFunctions.add(c -> c.infrastructure.airfields());
		dataFunctions.add(c -> c.infrastructure.civMilRatio());
		dataFunctions.add(c -> c.infrastructure.popPerFactoryRatio());
		dataFunctions.add(c -> c.infrastructure.popPerCivRatio());
		dataFunctions.add(c -> c.infrastructure.popPerMilRatio());
		dataFunctions.add(c -> c.infrastructure.popAirportCapacityRatio());
		dataFunctions.add(c -> c.infrastructure.popPerStateRatio(c.numOwnedStates()));
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

	@Override
	public Infrastructure getInfrastructureRecord() {
		return infrastructure;
	}

//	@Override
//	public int infrastructure() {
//		return infrastructure.infrastructure();
//	}
//
//	@Override
//	public int civilianFactories() {
//		return infrastructure.civilianFactories();
//	}
//
//	@Override
//	public int militaryFactories() {
//		return infrastructure.militaryFactories();
//	}
//
//	@Override
//	public int navalDockyards() {
//		return infrastructure.navalDockyards();
//	}
//
//	@Override
//	public int navalPorts() {
//		return infrastructure.navalPorts();
//	}
//
//	@Override
//	public int airfields() {
//		return infrastructure.airfields();
//	}

	public void setInfrastructure(Infrastructure infrastructure) {
		this.infrastructure = infrastructure;
	}

	public Resources resources() {
		return resources;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

//	@Override
//	public int population() {
//		return infrastructure.population();
//	}

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

	public static <T> List<Country> loadCountries(List<T> list) {
		countryList.clear();

		for (T item : list) {
			countryList.add(new Country(item));
		}

		return countryList;
	}

	public static ObservableList<Country> loadCountries(List<CountryTag> countryTags, List<Infrastructure> infrastructureList, List<Resources> resourcesList) {
		countryList.clear();

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

	private int numOwnedStates() {
		return 1;   // todo;
	}

	@Override
	public int compareTo(@NotNull Country o) {
		return countryTag.compareTo(o.countryTag);
	}
}
