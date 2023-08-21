package hoi4utils.clausewitz_coding.country;

import hoi4utils.clausewitz_coding.state.State;
import hoi4utils.clausewitz_coding.state.buildings.Infrastructure;
import hoi4utils.clausewitz_coding.state.buildings.Resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Country {
	private static final List<Country> countryList = new ArrayList<>();
	CountryTag countryTag;
	Infrastructure infrastructure;          // infrastructure of all owned states
	Resources resources;                    // resources of all owned states

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
	 *
	 * @deprecated
	 * @return
	 */
	public static List<Country> getList() {
		if (countryList == null || countryList.isEmpty()) {
			loadCountries();
		}
		return countryList;
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
		return resources.aluminum();
	}

	public int chromium() {
		return resources.chromium();
	}

	public int oil() {
		return resources.oil();
	}

	public int rubber() {
		return resources.rubber();
	}

	public int steel() {
		return resources.steel();
	}

	public int tungsten() {
		return resources.tungsten();
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
		for (T item: list) {
			countryList.add(new Country(item));
		}

		return countryList;
	}

	public static List<Country> loadCountries(List<CountryTag> countryTags, List<Infrastructure> infrastructureList, List<Resources> resourcesList) {
		Iterator<CountryTag> countryTagsIterator = countryTags.iterator();
		Iterator<Infrastructure> infrastructureListIterator = infrastructureList.iterator();
		Iterator<Resources> resourcesListIterator = resourcesList.iterator();
		
		while(countryTagsIterator.hasNext()) {
			countryList.add(new Country(countryTagsIterator.next(), infrastructureListIterator.next(), resourcesListIterator.next()));
		}

		return countryList;
	}

	public static List<Country> loadCountries(List<Infrastructure> infrastructureList, List<Resources> resourcesList) {
		return loadCountries(CountryTags.getCountryTags(), infrastructureList, resourcesList);
	}

	public static List<Country> loadCountries() {
		return loadCountries(State.infrastructureOfCountries(), State.resourcesOfCountries());
	}
}
