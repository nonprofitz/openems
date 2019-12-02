package io.openems.edge.battery.renaultzoe;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.battery.renaultzoe.Config;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.common.channel.AccessMode;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.taskmanager.Priority;

import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component( //
		name = "Bms.RenaultZoe", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE //
)
public class BatteryRenaultZoe extends AbstractOpenemsModbusComponent
		implements Battery, OpenemsComponent, EventHandler, ModbusSlave {

	// Default values for the battery ranges
	public static final int DISCHARGE_MIN_V = 288;
	public static final int CHARGE_MAX_V = 400;
	public static final int DISCHARGE_MAX_A = 300;
	public static final int CHARGE_MAX_A = 300;
	
	@Reference
	protected ConfigurationAdmin cm;
	
	private final Logger log = LoggerFactory.getLogger(BatteryRenaultZoe.class);
	private String modbusBridgeId;
	private StringStatus state = StringStatus.UNDEFINED;

	// if configuring is needed this is used to go through the necessary steps
	private Config config;
	
	
	
	public BatteryRenaultZoe() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Battery.ChannelId.values(), //
				RenaultZoeChannelId.values() //
		);
	}

	private String name;

	@Activate
	void activate(ComponentContext context, Config config) {
		this.config = config;

		// adds dynamically created channels and save them into a map to access them
		// when modbus tasks are created
		super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus",
				config.modbus_id());
		this.modbusBridgeId = config.modbus_id();
	}
	
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}
	

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
			//handleBatteryState();
			break;
		}
	}

	
//	private void handleBatteryState() {
//		private void handleBatteryState() {
//			switch (config.batteryState()) {
//			case DEFAULT:
//				handleStateMachine();
//				break;
//			case OFF:
//				stopSystem();
//				break;
//			case ON:
//				startSystem();
//				break;
//			}
//		}
//	}

	private void stopSystem() {
		// TODO Currently not necessary, Battery starts itself?!
		this.log.debug("Stop system");
	}
	
	
	@Override
	protected ModbusProtocol defineModbusProtocol() {
		return new ModbusProtocol(this, //

				/*
				 * Battery String1
				 */
				new FC3ReadRegistersTask(0x100, Priority.HIGH, //
						m(RenaultZoeChannelId.END_OF_CHARGE_REQUEST, new UnsignedWordElement(0x100), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.AVAILABLE_POWER, new UnsignedWordElement(0x101), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.HV_BATTERY_TEMP, new UnsignedWordElement(0x102), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.HV_BAT_HEALTH, new UnsignedWordElement(0x103), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.LBCPRUN_ANSWER, new UnsignedWordElement(0x104), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.HV_BATTERY_MAX_TEMP, new UnsignedWordElement(0x105), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.HV_BAT_STATE, new UnsignedWordElement(0x106), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.LBC_REFUSE_TO_SLEEP, new UnsignedWordElement(0x107), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.AVAILABLE_ENERGY, new UnsignedWordElement(0x108), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.ISOL_DIAG_AUTHORISATION, new UnsignedWordElement(0x109), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.SAFETY_MODE_1_FLAG, new UnsignedWordElement(0x110), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_ISOLATON_IMPEDANCE, new UnsignedWordElement(0x111), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.CELL_HIGHEST_VOLTAGE, new UnsignedWordElement(0x112), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.CELL_LOWEST_VOLTAGE, new UnsignedWordElement(0x113), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.CHARGING_POWER, new UnsignedWordElement(0x114), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_BAT_INSTANT_CURRENT, new UnsignedWordElement(0x115), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_POWER_CONNECTION, new UnsignedWordElement(0x116), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_BAT_LEVEL2_FAILURE, new UnsignedWordElement(0x117), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_BAT_LEVEL1_FAILURE, new UnsignedWordElement(0x118), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.USER_SOC, new UnsignedWordElement(0x119), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_NETWORK_VOLTAGE, new UnsignedWordElement(0x120), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_BAT_SERIAL_NUMBER, new UnsignedWordElement(0x121), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.CELL_LOWEST_VOLTAGE_RCY, new UnsignedWordElement(0x122), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.CELL_HIGHEST_VOLTAGE_RCY, new UnsignedWordElement(0x123), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_BATTERY_MAX_TEMP_RCY, new UnsignedWordElement(0x124), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.LBCRUN_ANSWER_RCY, new UnsignedWordElement(0x125), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_POWER_CONNECTION_RCY, new UnsignedWordElement(0x126), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.HV_BAT_LEVEL2_FAILURE_RCY, new UnsignedWordElement(0x127), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.SAFETY_MODE_1_FLAG_RCY, new UnsignedWordElement(0x128), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.LBC2_REFUSE_TO_SLEEP, new UnsignedWordElement(0x129), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.ELEC_MASCHINE_SPEED, new UnsignedWordElement(0x130), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.ETS_SLEEP_MODE, new UnsignedWordElement(0x131), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.SCH_WAKE_UP_SLEEP_COMMAND, new UnsignedWordElement(0x132), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.WAKE_UP_TYPE, new UnsignedWordElement(0x133), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.LBCPRUN_KEY, new UnsignedWordElement(0x134), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.LBCPRUNKEY_RCY, new UnsignedWordElement(0x135), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.OPERATING_TYPE, new UnsignedWordElement(0x136), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.POWER_RELAY_STATE, new UnsignedWordElement(0x137), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.DISTANCE_TOTALIZER_COPY, new UnsignedWordElement(0x138), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.ABSOLUTE_TIME_SINCE_1RST_IGNITION, new UnsignedWordElement(0x139), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.VEHICLE_ID, new UnsignedWordElement(0x140), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),//
						new DummyRegisterElement(0x141, 0x159),
						m(RenaultZoeChannelId.STR_ST, new UnsignedWordElement(0x160), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1)), //
				
				new FC6WriteRegisterTask(0x161, //
						m(RenaultZoeChannelId.EN_STRING, new UnsignedWordElement(0x161)) //	
				),
				new FC6WriteRegisterTask(0x162, //
						m(RenaultZoeChannelId.CON_STRING, new UnsignedWordElement(0x162)) //	
				),
				
				
				/*
				 * Battery Stack
				 */

				new FC3ReadRegistersTask(0x500, Priority.HIGH, //
						m(RenaultZoeChannelId.SERIAL_NO, new UnsignedWordElement(0x500), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.MOD_V, new UnsignedWordElement(0x501), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.TYP, new UnsignedWordElement(0x502), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.BAT_MAN, new UnsignedWordElement(0x503), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(RenaultZoeChannelId.BAT_MODEL, new UnsignedWordElement(0x504), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STATE, new UnsignedWordElement(0x505), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.WARR_DT, new UnsignedWordElement(0x506), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.INST_DT, new UnsignedWordElement(0x507), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.AH_RTG, new UnsignedWordElement(0x508), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.WH_RTG, new UnsignedWordElement(0x509), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.W_CHA_RTE_MAX, new UnsignedWordElement(0x510), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.W_DIS_CHA_RTE_MAX, new UnsignedWordElement(0x511), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.V_MAX, new UnsignedWordElement(0x512), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.V_MIN, new UnsignedWordElement(0x513), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.CELL_V_MAX, new UnsignedWordElement(0x514), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.CELL_V_MAX_STR, new UnsignedWordElement(0x515), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.CELL_V_MIN, new UnsignedWordElement(0x516), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.CELL_V_MIN_STR, new UnsignedWordElement(0x517), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.CHARGE_WH, new UnsignedWordElement(0x518), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.DISCHARGE_WH, new UnsignedWordElement(0x519), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.TOTAL_CAP, new UnsignedWordElement(0x520), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.REST_CAP, new UnsignedWordElement(0x521), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.A, new UnsignedWordElement(0x522), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.V, new UnsignedWordElement(0x523), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.A_CHA_MAX, new UnsignedWordElement(0x524), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.A_DIS_CHA_MAX, new UnsignedWordElement(0x525), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.W, new UnsignedWordElement(0x526), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.REQ_INV_STATE, new UnsignedWordElement(0x527), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1)), //	
				
				new FC6WriteRegisterTask(0x528, //
						m(RenaultZoeChannelId.REQ_W, new UnsignedWordElement(0x528)) //
				),
				new FC6WriteRegisterTask(0x529, //
						m(RenaultZoeChannelId.REQ_MODE, new UnsignedWordElement(0x529)) //
				),
				new FC3ReadRegistersTask(0x530, Priority.HIGH, //
						m(RenaultZoeChannelId.CTL_MODE, new UnsignedWordElement(0x530), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1)), //
				new FC6WriteRegisterTask(0x531, //
						m(RenaultZoeChannelId.ON_OFF, new UnsignedWordElement(0x531)) //
				),		
				new FC3ReadRegistersTask(0x532, Priority.HIGH, //			
						m(RenaultZoeChannelId.N_STR, new UnsignedWordElement(0x532), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.N_STR_CON, new UnsignedWordElement(0x533), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_V_MAX, new UnsignedWordElement(0x534), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_V_MAX_STR, new UnsignedWordElement(0x535), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_V_MIN, new UnsignedWordElement(0x536), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_V_MIN_STR, new UnsignedWordElement(0x537), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_V_AVG, new UnsignedWordElement(0x538), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_A_MAX, new UnsignedWordElement(0x539), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_A_MAX_STR, new UnsignedWordElement(0x540), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_A_MIN, new UnsignedWordElement(0x541), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_A_MIN_STR, new UnsignedWordElement(0x542), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.STR_A_AVG, new UnsignedWordElement(0x543), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.ALARM_STACK, new UnsignedWordElement(0x544), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(RenaultZoeChannelId.FAULT_STACK, new UnsignedWordElement(0x545), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1)) //
				);//


	}



	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable( //
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				Battery.getModbusSlaveNatureTable(accessMode));
	}

}
