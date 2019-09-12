package io.openems.edge.batteryinverter.api;

public interface BaseBatteryInverter {

	// === Measured values ======
	float getDCPower();
	float getDCCurrent();
	float getDCVoltage();
	
	float getACCurrent();
	float getFrequency();

	float getActivePower();
	// ==========================
	
	// --- Values coming from data sheet or static registers ---
	float getMaxApparentPower();
	float getMinDCVoltage();
	float getMaxDCVoltage();
	float getMaxChargeCurrrent();
	float getMaxDischargeCurrent();
	// -------------------------------------
		
}
