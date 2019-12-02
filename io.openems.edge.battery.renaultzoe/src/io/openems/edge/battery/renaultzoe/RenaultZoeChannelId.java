package io.openems.edge.battery.renaultzoe;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;

public enum RenaultZoeChannelId implements io.openems.edge.common.channel.ChannelId {
	
	/*
	 * Battery String1
	 */

	// IntegerReadChannels
	END_OF_CHARGE_REQUEST(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	AVAILABLE_POWER(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT).accessMode(AccessMode.READ_ONLY)),
	HV_BATTERY_TEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS).accessMode(AccessMode.READ_ONLY)),
	HV_BAT_HEALTH(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT).accessMode(AccessMode.READ_ONLY)),
	LBCPRUN_ANSWER(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	HV_BATTERY_MAX_TEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS).accessMode(AccessMode.READ_ONLY)),
	HV_BAT_STATE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	LBC_REFUSE_TO_SLEEP(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	AVAILABLE_ENERGY(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT_HOURS).accessMode(AccessMode.READ_ONLY)),
	ISOL_DIAG_AUTHORISATION(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	SAFETY_MODE_1_FLAG(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	HV_ISOLATON_IMPEDANCE(Doc.of(OpenemsType.INTEGER).unit(Unit.OHM).accessMode(AccessMode.READ_ONLY)),
	CELL_HIGHEST_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT).accessMode(AccessMode.READ_ONLY)),
	CELL_LOWEST_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT).accessMode(AccessMode.READ_ONLY)),
	CHARGING_POWER(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT).accessMode(AccessMode.READ_ONLY)),
	HV_BAT_INSTANT_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE).accessMode(AccessMode.READ_ONLY)),
	HV_POWER_CONNECTION(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	HV_BAT_LEVEL2_FAILURE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	HV_BAT_LEVEL1_FAILURE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	USER_SOC(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT).accessMode(AccessMode.READ_ONLY)),
	HV_NETWORK_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	HV_BAT_SERIAL_NUMBER(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	CELL_LOWEST_VOLTAGE_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	CELL_HIGHEST_VOLTAGE_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	HV_BATTERY_MAX_TEMP_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	LBCRUN_ANSWER_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	HV_POWER_CONNECTION_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	HV_BAT_LEVEL2_FAILURE_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	SAFETY_MODE_1_FLAG_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	LBC2_REFUSE_TO_SLEEP(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	ELEC_MASCHINE_SPEED(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	ETS_SLEEP_MODE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	SCH_WAKE_UP_SLEEP_COMMAND(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	WAKE_UP_TYPE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	LBCPRUN_KEY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	LBCPRUNKEY_RCY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	OPERATING_TYPE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	POWER_RELAY_STATE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	DISTANCE_TOTALIZER_COPY(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	ABSOLUTE_TIME_SINCE_1RST_IGNITION(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	VEHICLE_ID(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),

	// EnumReadChannels
	STR_ST(Doc.of(StringStatus.values()).accessMode(AccessMode.READ_ONLY)), //

	// EnumWriteChannels
	EN_STRING(Doc.of(EnableString.values()).accessMode(AccessMode.READ_WRITE)), //
	CON_STRING(Doc.of(StartStopString.values()).accessMode(AccessMode.READ_WRITE)), //

	// StateChannels
	ALARM_STRING_1(Doc.of(Level.WARNING).text("Alarm Ids specific")), //
	FAULT_STRING_1(Doc.of(Level.FAULT).text("Fault Ids specific")), //

	/*
	 * Battery Stack
	 */

	// IntegerReadChannels
	SERIAL_NO(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	MOD_V(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	STATE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	WARR_DT(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	INST_DT(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	AH_RTG(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE_HOURS).accessMode(AccessMode.READ_ONLY)),
	WH_RTG(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT_HOURS).accessMode(AccessMode.READ_ONLY)),
	W_CHA_RTE_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT).accessMode(AccessMode.READ_ONLY)),
	W_DIS_CHA_RTE_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT).accessMode(AccessMode.READ_ONLY)),
	V_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	V_MIN(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	CELL_V_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT).accessMode(AccessMode.READ_ONLY)),
	CELL_V_MAX_STR(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	CELL_V_MIN(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT).accessMode(AccessMode.READ_ONLY)),
	CELL_V_MIN_STR(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	CHARGE_WH(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT_HOURS).accessMode(AccessMode.READ_ONLY)),
	DISCHARGE_WH(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT_HOURS).accessMode(AccessMode.READ_ONLY)),
	TOTAL_CAP(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT_HOURS).accessMode(AccessMode.READ_ONLY)),
	REST_CAP(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT_HOURS).accessMode(AccessMode.READ_ONLY)),
	A(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE).accessMode(AccessMode.READ_ONLY)),
	V(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	A_CHA_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE).accessMode(AccessMode.READ_ONLY)),
	A_DIS_CHA_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE).accessMode(AccessMode.READ_ONLY)),
	W(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT).accessMode(AccessMode.READ_ONLY)),
	N_STR(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	N_STR_CON(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	STR_V_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	STR_V_MAX_STR(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	STR_V_MIN(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	STR_V_MIN_STR(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	STR_V_AVG(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	STR_A_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE).accessMode(AccessMode.READ_ONLY)),
	STR_A_MAX_STR(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).accessMode(AccessMode.READ_ONLY)),
	STR_A_MIN(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE).accessMode(AccessMode.READ_ONLY)),
	STR_A_MIN_STR(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)),
	STR_A_AVG(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE).accessMode(AccessMode.READ_ONLY)),

	// IntegerWriteChannels
	REQ_W(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT).accessMode(AccessMode.WRITE_ONLY)),

	// EnumReadChannels
	TYP(Doc.of(BatteryTyp.values())), //
	BAT_MAN(Doc.of(BatteryManufacturer.values())), //
	BAT_MODEL(Doc.of(BatteryModel.values())), //
	REQ_INV_STATE(Doc.of(InverterStateRequest.values())), //
	CTL_MODE(Doc.of(ControlMode.values())), //

	// EnumWriteChannels
	REQ_MODE(Doc.of(BatteryChargeDischargeRequest.values())),
	ON_OFF(Doc.of(StartStopBatteryStack.values())),

	// StateChannels
	ALARM_STACK(Doc.of(Level.WARNING).text("Alarm Stack")), //
	FAULT_STACK(Doc.of(Level.FAULT).text("Fault Stack")); //

	
	
	private final Doc doc;

	private RenaultZoeChannelId(Doc doc) {
		this.doc = doc;
	}
	
	@Override
	public Doc doc() {
		return this.doc;		
	}

}
