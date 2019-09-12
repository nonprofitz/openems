package io.openems.edge.batteryinverter.api;

public interface AsymmetricInverterReactive extends ThreePhaseInverter {
	
	// === Measured values ======
	float getReactivePower(Phase phase);
	float getApparentPower(Phase phase);
	float getCosPhi(Phase phase);		
	// ==========================
	
	void setApparentPower(Phase phase, float apparentPower, float cosPhi);
	void setReactivePower(Phase phase, float reactivePower);
		
}
