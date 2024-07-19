package com.HOIIVUtils.clauzewitz.map.state;

import com.HOIIVUtils.clauzewitz.map.buildings.Infrastructure;

public interface InfrastructureData {
	Infrastructure getInfrastructureRecord();

	default int infrastructure(Infrastructure inf) {
		return inf.infrastructure();
	}

	default int civilianFactories(Infrastructure inf) {
		return inf.civilianFactories();
	}

	default int militaryFactories(Infrastructure inf) {
		return inf.militaryFactories();
	}

	default int navalDockyards(Infrastructure inf) {
		return inf.navalDockyards();
	}

	default int navalPorts(Infrastructure inf) {
		return inf.navalPorts();
	}

	default int airfields(Infrastructure inf) {
		return inf.airfields();
	}

}
