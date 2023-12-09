package com.HOIIVUtils.hoi4utils.clausewitz_data.state.buildings;
/*
 * infanstructure File
 */
// can be used for either states or countries
public record Infrastructure(int population, int infrastructure, int civilianFactories, int militaryFactories, int navalDockyards,
							 int navalPorts, int airfields) {
	private static final int AIRFIELD_CAPACITY_PER_LEVEL = 200;

	public Infrastructure() {
		this(0, 0, 0, 0, 0, 0, 0);
	}

	public Infrastructure updateInfrastructure(int value) {
		return new Infrastructure(population(), value, civilianFactories(), militaryFactories(), navalDockyards(), navalPorts(), airfields());
	}

	public Infrastructure updatePopulation(int value) {
		return new Infrastructure(value, infrastructure(), civilianFactories(), militaryFactories(), navalDockyards(), navalPorts(), airfields());
	}

	public Infrastructure updateCivilianFactories(int value) {
		return new Infrastructure(population(), infrastructure(), value, militaryFactories(), navalDockyards(), navalPorts(), airfields());
	}

	public Infrastructure updateMilitaryFactories(int value) {
		return new Infrastructure(population(), infrastructure(), civilianFactories(), value, navalDockyards(), navalPorts(), airfields());
	}

	public Infrastructure updateNavalDockyards(int value) {
		return new Infrastructure(population(), infrastructure(), civilianFactories(), militaryFactories(), value, navalPorts(), airfields());
	}

	public Infrastructure updateNavalPorts(int value) {
		return new Infrastructure(population(), infrastructure(), civilianFactories(), militaryFactories(), navalDockyards(), value, airfields());
	}

	public Infrastructure updateAirfields(int value) {
		return new Infrastructure(population(), infrastructure(), civilianFactories(), militaryFactories(), navalDockyards(), navalPorts(), value);
	}

//	public Infrastructure {
//
//	}

	/* util methods */
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

	public double popPerStateRatio(int numStates) {
		return (double) population() / numStates;
	}

	private double factories() {
		return militaryFactories() + civilianFactories();
	}

	private double airfieldsCapacity() {
		return airfields() * AIRFIELD_CAPACITY_PER_LEVEL;
	}

}
