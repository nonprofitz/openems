package io.openems.edge.batteryinverter.api;

public interface SymmetricInverterReactive extends BaseBatteryInverter {

	// === Measured values ======
	float getReactivePower();
	float getApparentPower();
	float getCosPhi();		
	// ==========================
	
	void setApparentPower(float apparentPower, float cosPhi);
	void setReactivePower(float reactivePower);
	
}
