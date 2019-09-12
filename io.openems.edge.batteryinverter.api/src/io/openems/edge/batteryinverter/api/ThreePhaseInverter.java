package io.openems.edge.batteryinverter.api;

public interface ThreePhaseInverter {

	float getVoltage(Phase phaseA, Phase phaseB);

	float getVoltageToN(Phase phase);
	float getCurrent(Phase phase);
	float getPower(Phase phase);
	
}
