package hoi4utils.clausewitz_coding.country;

import hoi4utils.clausewitz_coding.state.buildings.Infrastructure;
import hoi4utils.clausewitz_coding.state.buildings.Resources;

public class Country {
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
}
