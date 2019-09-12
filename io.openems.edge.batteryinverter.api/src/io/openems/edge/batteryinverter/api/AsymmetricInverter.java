package io.openems.edge.batteryinverter.api;

public interface AsymmetricInverter extends ThreePhaseInverter {
	
	void setActivePower(Phase phase, float activePower);
		
}
