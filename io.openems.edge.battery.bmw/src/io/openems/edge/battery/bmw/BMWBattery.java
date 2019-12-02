package io.openems.edge.battery.bmw;

import java.time.LocalDateTime;
import java.util.Optional;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.BitsWordElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.channel.BooleanWriteChannel;
import io.openems.edge.common.channel.EnumReadChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.ess.api.SymmetricEss;

@Designate(ocd = Config.class, factory = true)
@Component( //
		name = "Bms.Bmw.Battery", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE //
)
public class BMWBattery extends AbstractOpenemsModbusComponent
		implements Battery, OpenemsComponent, EventHandler, ModbusSlave {

	// , // JsonApi // TODO

	private static final Integer OPEN_CONTACTORS = 0;
	private static final Integer CLOSE_CONTACTORS = 4;

	@Reference
	protected ConfigurationAdmin cm;

//	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
//	protected void setEss(SymmetricEss ess) {
//		this.ess = ess;
//	}

	private final Logger log = LoggerFactory.getLogger(BMWBattery.class);
	private String modbusBridgeId;
	private State state = State.UNDEFINED;
	// if configuring is needed this is used to go through the necessary steps
	private Config config;
	// If an error has occurred, this indicates the time when next action could be
	// done
	private LocalDateTime errorDelayIsOver = null;
	private int unsuccessfulStarts = 0;
	private LocalDateTime startAttemptTime = null;

	private LocalDateTime pendingTimestamp;

	public BMWBattery() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Battery.ChannelId.values(), //
				BMWChannelId.values() //
		);
	}

	@Reference
	private ComponentManager manager;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		this.config = config;

		// adds dynamically created channels and save them into a map to access them
		// when modbus tasks are created

		super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus",
				config.modbus_id());
		this.modbusBridgeId = config.modbus_id();
		initializeCallbacks();

	}

	private void handleStateMachine() {
		log.info("BMWBattery.handleStateMachine(): State: " + this.getStateMachineState());
		boolean readyForWorking = false;
		switch (this.getStateMachineState()) {
		case ERROR:
			this.clearError();

			// TODO Reset BMS? anything else?

			errorDelayIsOver = LocalDateTime.now().plusSeconds(config.errorDelay());
			setStateMachineState(State.ERRORDELAY);

			break;

		case ERRORDELAY:
			if (LocalDateTime.now().isAfter(errorDelayIsOver)) {
				errorDelayIsOver = null;
				if (this.isError()) {
					this.setStateMachineState(State.ERROR);
				} else {
					this.setStateMachineState(State.OFF);
				}
			}
			break;
		case INIT:
			if (this.isSystemRunning()) {
				this.setStateMachineState(State.RUNNING);
				unsuccessfulStarts = 0;
				startAttemptTime = null;
			} else {
				if (startAttemptTime.plusSeconds(config.maxStartTime()).isBefore(LocalDateTime.now())) {
					startAttemptTime = null;
					unsuccessfulStarts++;
					this.stopSystem();
					this.setStateMachineState(State.STOPPING);
					if (unsuccessfulStarts >= config.maxStartAttempts()) {
						errorDelayIsOver = LocalDateTime.now().plusSeconds(config.startUnsuccessfulDelay());
						this.setStateMachineState(State.ERRORDELAY);
						unsuccessfulStarts = 0;
					}
				}
			}
			break;
		case OFF:
			log.debug("in case 'OFF'; try to start the system");
			this.startSystem();
			log.debug("set state to 'INIT'");
			this.setStateMachineState(State.INIT);
			startAttemptTime = LocalDateTime.now();
			break;
		case RUNNING:
			if (this.isError()) {
				this.setStateMachineState(State.ERROR);
			} else if (!this.isSystemRunning()) {
				this.setStateMachineState(State.UNDEFINED);
			} else {
				this.setStateMachineState(State.RUNNING);
				readyForWorking = true;
			}
			break;
		case STOPPING:
			if (this.isError()) {
				this.setStateMachineState(State.ERROR);
			} else {
				if (this.isSystemStopped()) {
					this.setStateMachineState(State.OFF);
				}
			}
			break;
		case UNDEFINED:
			if (this.isError()) {
				this.setStateMachineState(State.ERROR);
			} else if (this.isSystemStopped()) {
				this.setStateMachineState(State.OFF);
			} else if (this.isSystemRunning()) {
				this.setStateMachineState(State.RUNNING);
			} else if (this.isSystemStatePending()) {
				this.setStateMachineState(State.PENDING);
			}
			break;
		case PENDING:
			if (this.pendingTimestamp == null) {
				this.pendingTimestamp = LocalDateTime.now();
			}
			if (this.pendingTimestamp.plusSeconds(this.config.pendingTolerance()).isBefore(LocalDateTime.now())) {
				// System state could not be determined, stop and start it
				this.pendingTimestamp = null;
				this.stopSystem();
				this.setStateMachineState(State.OFF);
			} else {
				if (this.isError()) {
					this.setStateMachineState(State.ERROR);
					this.pendingTimestamp = null;
				} else if (this.isSystemStopped()) {
					this.setStateMachineState(State.OFF);
					this.pendingTimestamp = null;
				} else if (this.isSystemRunning()) {
					this.setStateMachineState(State.RUNNING);
					this.pendingTimestamp = null;
				}
			}
			break;
		case STANDBY:
			break;
		}

		this.getReadyForWorking().setNextValue(readyForWorking);
	}

	private void clearError() {
		BooleanWriteChannel clearErrorChannel = this.channel(BMWChannelId.BMS_STATE_COMMAND_CLEAR_ERROR);
		try {
			clearErrorChannel.setNextWriteValue(true);
		} catch (OpenemsNamedException e) {
			System.out.println("Error while trying to reset the system!");
		}
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	private void initializeCallbacks() {

		// TODO Check what values to use... !!!
//		this.channel(BMWChannelId.CLUSTER_1_VOLTAGE).onChange(value -> {
//			@SuppressWarnings("unchecked")
//			Optional<Integer> vOpt = (Optional<Integer>) value.asOptional();
//			if (!vOpt.isPresent()) {
//				return;
//			}
//			int voltage_volt = (int) (vOpt.get() * 0.001);
//			log.debug("callback voltage, value: " + voltage_volt);
//			this.channel(Battery.ChannelId.VOLTAGE).setNextValue(voltage_volt);
//		});
//
//		this.channel(BMWChannelId.CLUSTER_1_MIN_CELL_VOLTAGE).onChange(value -> {
//			@SuppressWarnings("unchecked")
//			Optional<Integer> vOpt = (Optional<Integer>) value.asOptional();
//			if (!vOpt.isPresent()) {
//				return;
//			}
//			int voltage_millivolt = vOpt.get();
//			log.debug("callback min cell voltage, value: " + voltage_millivolt);
//			this.channel(Battery.ChannelId.MIN_CELL_VOLTAGE).setNextValue(voltage_millivolt);
//		});

		// write battery ranges to according channels in battery api
		// MAX_VOLTAGE ==> DcVolDynMax Register 1012

		// TODO 23:09:2019 COMMITTED
		

		
		this.channel(BMWChannelId.MAXIMUM_LIMIT_DYNAMIC_VOLTAGE).onChange( (oldValue, newValue) -> {
			@SuppressWarnings("unchecked")
			Optional<Integer> vOpt = (Optional<Integer>) newValue.asOptional();
			if (!vOpt.isPresent()) {
				return;
			}
			int max_charge_voltage = (int) (vOpt.get());
			log.debug("callback battery range, max charge voltage, value: " + max_charge_voltage);
			this.channel(Battery.ChannelId.CHARGE_MAX_VOLTAGE).setNextValue(max_charge_voltage);
		});

		// DISCHARGE_MIN_VOLTAGE ==> DcVolDynMin Registerc 1013
		this.channel(BMWChannelId.MINIMUM_LIMIT_DYNAMIC_VOLTAGE).onChange((oldValue, newValue) -> {
			@SuppressWarnings("unchecked")
			Optional<Integer> vOpt = (Optional<Integer>) newValue.asOptional();
			if (!vOpt.isPresent()) {
				return;
			}
			int min_discharge_voltage = (int) (vOpt.get());
			log.debug("callback battery range, min discharge voltage, value: " + min_discharge_voltage);
			this.channel(Battery.ChannelId.DISCHARGE_MIN_VOLTAGE).setNextValue(min_discharge_voltage);
		});

		// !!!!! TODO What values are needed !!!!! Is this correct??
		// CHARGE_MAX_CURRENT ==> DcAmpDynMax ==> 1010
		this.channel(BMWChannelId.MAXIMUM_LIMIT_DYNAMIC_CURRENT).onChange((oldValue, newValue) -> {
			@SuppressWarnings("unchecked")
			Optional<Integer> cOpt = (Optional<Integer>) newValue.asOptional();
			if (!cOpt.isPresent()) {
				return;
			}
			int max_current = (int) (cOpt.get() * 1);
			log.debug("callback battery range, max charge current, value: " + max_current);
			this.channel(Battery.ChannelId.DISCHARGE_MAX_CURRENT).setNextValue(max_current);
		});

		// !!!!! TODO What values are needed !!!!! Is this correct??
		// DISCHARGE_MAX_CURRENT ==> DcAmpDynMin ==> 1011
//		this.channel(BMWChannelId.MINIMUM_LIMIT_DYNAMIC_CURRENT).onChange(value -> {
//			@SuppressWarnings("unchecked")
//			Optional<Integer> cOpt = (Optional<Integer>) value.asOptional();
//			if (!cOpt.isPresent()) {
//				return;
//			}
//			int max_current = (int) (cOpt.get() * -1);
//			log.debug("callback battery range, max discharge current, value: " + max_current);
//			this.channel(Battery.ChannelId.CHARGE_MAX_CURRENT).setNextValue(max_current);
//		});

	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:

			if (config.batteryOff()) {
				SymmetricEss ess;
				try {
					ess = this.manager.getComponent(this.config.Inverter_id());
					// Just temporarily for REFU: do not switch off if inverter is not in
					// standby("8")
					EnumReadChannel c = ess.channel("St");
					int inverterState = c.value().orElse(0);
					if (inverterState == 8) {
						this.shutDownBattery();
					} else {
						return;
					}
				} catch (OpenemsNamedException e1) {
					// TODO Auto-generated catch block

					e1.printStackTrace();
					return;
				}
			} else {
				this.handleBatteryState();
			}
			break;
		}
	}

	private void handleBatteryState() {
		switch (config.batteryState()) {
		case DEFAULT:
			handleStateMachine();
			break;
		case OFF:
			stopSystem();
			break;
		case ON:
			startSystem();
			break;
		}
	}

	public void shutDownBattery() {
		SymmetricEss ess;
		try {
			ess = this.manager.getComponent(this.config.Inverter_id());
		} catch (OpenemsNamedException e1) {
			// TODO Auto-generated catch block

			e1.printStackTrace();
			return;
		}
		int activePowerInverter = ess.getActivePower().value().orElse(0);
		int reactivePowerInverter = ess.getReactivePower().value().orElse(0);

		if (activePowerInverter == 0 && reactivePowerInverter == 0) {
			IntegerWriteChannel commandChannel = this.channel(BMWChannelId.BMS_STATE_COMMAND);
			try {
				commandChannel.setNextWriteValue(OPEN_CONTACTORS);
			} catch (OpenemsNamedException e) {
				// TODO Auto-generated catch block
				log.error("Problem occurred during send start command");
			}
		}

	}

	private boolean isSystemRunning() {
		EnumReadChannel bmsStateChannel = this.channel(BMWChannelId.BMS_STATE);
		BmsState bmsState = bmsStateChannel.value().asEnum();
		return bmsState == BmsState.OPERATION;
	}

	private boolean isSystemStopped() {
		EnumReadChannel bmsStateChannel = this.channel(BMWChannelId.BMS_STATE);
		BmsState bmsState = bmsStateChannel.value().asEnum();
		return bmsState == BmsState.OFF;
	}

	/**
	 * Checks whether system has an undefined state
	 */
	private boolean isSystemStatePending() {
		return !isSystemRunning() && !isSystemStopped();
	}

	private boolean isError() {
		EnumReadChannel bmsStateChannel = this.channel(BMWChannelId.BMS_STATE);
		BmsState bmsState = bmsStateChannel.value().asEnum();
		return bmsState == BmsState.ERROR;
	}

	public String getModbusBridgeId() {
		return modbusBridgeId;
	}

	@Override
	public String debugLog() {
		return "State:" + this.getStateMachineState() + " | SoC:" + this.getSoc().value() //
				+ " | Voltage:" + this.getVoltage().value() + " | Max Operating Current:"
				+ this.channel(BMWChannelId.MAXIMUM_OPERATING_CURRENT).value().asString() //
				+ " | Min Operating Current:" + this.channel(BMWChannelId.MINIMUM_OPERATING_CURRENT).value().asString() //

//				+ "|Discharge:" + this.getDischargeMinVoltage().value() + ";" + this.getDischargeMaxCurrent().value() //
//				+ "|Charge:" + this.getChargeMaxVoltage().value() + ";" + this.getChargeMaxCurrent().value() + "|State:"
//				+ this.channel(BMWChannelId.BMS_STATE)
		;
	}

	private void startSystem() {
		// TODO Currently not necessary, Battery starts itself?!
		this.log.debug("Start system");
		IntegerWriteChannel commandChannel = this.channel(BMWChannelId.BMS_STATE_COMMAND);
		try {
			commandChannel.setNextWriteValue(CLOSE_CONTACTORS);
		} catch (OpenemsNamedException e) {
			// TODO Auto-generated catch block
			log.error("Problem occurred during send start command");
		}
	}

	private void stopSystem() {
		// TODO Currently not necessary, Battery starts itself?!
		this.log.debug("Stop system");
	}

	public State getStateMachineState() {
		return state;
	}

	public void setStateMachineState(State state) {
		this.state = state;
		this.channel(BMWChannelId.STATE_MACHINE).setNextValue(this.state);
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {

		ModbusProtocol protocol = new ModbusProtocol(this, //

				new FC16WriteRegistersTask(1399, m(BMWChannelId.HEART_BEAT, new UnsignedWordElement(1399)), //
						m(BMWChannelId.BMS_STATE_COMMAND, new UnsignedWordElement(1400)), //
//						m(new BitsWordElement(1400, this) //
//								.bit(15, BMWChannelId.BMS_STATE_COMMAND_RESET) //
//								.bit(14, BMWChannelId.BMS_STATE_COMMAND_CLEAR_ERROR) //
//								.bit(3, BMWChannelId.BMS_STATE_COMMAND_CLOSE_PRECHARGE) //
//								.bit(2, BMWChannelId.BMS_STATE_COMMAND_CLOSE_CONTACTOR) //
//								.bit(1, BMWChannelId.BMS_STATE_COMMAND_WAKE_UP_FROM_STOP) //
//								.bit(0, BMWChannelId.BMS_STATE_COMMAND_ENABLE_BATTERY) //
//						), //
						m(BMWChannelId.OPERATING_STATE_INVERTER, new UnsignedWordElement(1401)), //
						m(BMWChannelId.DC_LINK_VOLTAGE, new UnsignedWordElement(1402),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.DC_LINK_CURRENT, new UnsignedWordElement(1403)), //
						m(BMWChannelId.OPERATION_MODE_REQUEST_GRANTED, new UnsignedWordElement(1404)), //
						m(BMWChannelId.OPERATION_MODE_REQUEST_CANCELED, new UnsignedWordElement(1405)), //
						m(new BitsWordElement(1406, this) //
								.bit(1, BMWChannelId.CONNECTION_STRATEGY_HIGH_SOC_FIRST) //
								.bit(0, BMWChannelId.CONNECTION_STRATEGY_LOW_SOC_FIRST) //
						), //
						m(BMWChannelId.SYSTEM_TIME, new UnsignedDoublewordElement(1407)) //
				),

				new FC3ReadRegistersTask(999, Priority.HIGH, m(BMWChannelId.LIFE_SIGN, new UnsignedWordElement(999)), //
						m(BMWChannelId.BMS_STATE, new UnsignedWordElement(1000)), //
						m(BMWChannelId.ERROR_BITS_1, new UnsignedWordElement(1001)), //
						m(BMWChannelId.ERROR_BITS_2, new UnsignedWordElement(1002)), //
						m(BMWChannelId.WARNING_BITS_1, new UnsignedWordElement(1003)), //
						m(BMWChannelId.WARNING_BITS_2, new UnsignedWordElement(1004)), //
						m(BMWChannelId.INFO_BITS, new UnsignedWordElement(1005)), //
						m(BMWChannelId.MAXIMUM_OPERATING_CURRENT, new UnsignedWordElement(1006)), //
						m(BMWChannelId.MINIMUM_OPERATING_CURRENT, new SignedWordElement(1007)), //
						m(Battery.ChannelId.CHARGE_MAX_VOLTAGE, new UnsignedWordElement(1008),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(Battery.ChannelId.DISCHARGE_MIN_VOLTAGE, new UnsignedWordElement(1009),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(Battery.ChannelId.DISCHARGE_MAX_CURRENT, new UnsignedWordElement(1010)), //
						m(Battery.ChannelId.CHARGE_MAX_CURRENT, new SignedWordElement(1011)), //
						m(BMWChannelId.MAXIMUM_LIMIT_DYNAMIC_VOLTAGE, new UnsignedWordElement(1012),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.MINIMUM_LIMIT_DYNAMIC_VOLTAGE, new UnsignedWordElement(1013),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.NUMBER_OF_STRINGS_CONNECTED, new UnsignedWordElement(1014)), //
						m(BMWChannelId.NUMBER_OF_STRINGS_INSTALLED, new UnsignedWordElement(1015)), //
						m(BMWChannelId.SOC_ALL_STRINGS, new UnsignedWordElement(1016),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_2), //
						m(Battery.ChannelId.SOC, new UnsignedWordElement(1017),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_2), //
						m(BMWChannelId.REMAINING_CHARGE_CAPACITY, new UnsignedWordElement(1018)), //
						m(BMWChannelId.REMAINING_DISCHARGE_CAPACITY, new UnsignedWordElement(1019)), //
						m(BMWChannelId.REMAINING_CHARGE_ENERGY, new UnsignedWordElement(1020)), //
						m(BMWChannelId.REMAINING_DISCHARGE_ENERGY, new UnsignedWordElement(1021)), //
						m(BMWChannelId.NOMINAL_ENERGY, new UnsignedWordElement(1022)), //
						m(BMWChannelId.TOTAL_ENERGY, new UnsignedWordElement(1023)), //
						m(BMWChannelId.NOMINAL_CAPACITY, new UnsignedWordElement(1024)), //
						m(BMWChannelId.TOTAL_CAPACITY, new UnsignedWordElement(1025)), //
						m(Battery.ChannelId.SOH, new UnsignedWordElement(1026),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_2), //
						m(Battery.ChannelId.VOLTAGE, new UnsignedWordElement(1027),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.DC_VOLTAGE_AVERAGE, new UnsignedWordElement(1028),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.DC_CURRENT, new UnsignedWordElement(1029),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.AVERAGE_TEMPERATURE, new UnsignedWordElement(1030)), //
						m(BMWChannelId.MINIMUM_TEMPERATURE, new UnsignedWordElement(1031)), //
						m(BMWChannelId.MAXIMUM_TEMPERATURE, new UnsignedWordElement(1032)), //
						m(Battery.ChannelId.MIN_CELL_VOLTAGE, new UnsignedWordElement(1033)), //
						m(Battery.ChannelId.MAX_CELL_VOLTAGE, new UnsignedWordElement(1034)), //
						m(BMWChannelId.AVERAGE_CELL_VOLTAGE, new UnsignedWordElement(1035)), //
						m(BMWChannelId.INTERNAL_RESISTANCE, new UnsignedWordElement(1036)), //
						m(BMWChannelId.INSULATION_RESISTANCE, new UnsignedWordElement(1037),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.CONTAINER_TEMPERATURE, new UnsignedWordElement(1038),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.AMBIENT_TEMPERATURE, new UnsignedWordElement(1039),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.HUMIDITY_CONTAINER, new UnsignedWordElement(1040),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1), //
						m(BMWChannelId.MAXIMUM_LIMIT_DYNAMIC_CURRENT_HIGH_RES, new UnsignedWordElement(1041),
								ElementToChannelConverter.SCALE_FACTOR_2), //
						m(BMWChannelId.MINIMUM_LIMIT_DYNAMIC_CURRENT_HIGH_RES, new UnsignedWordElement(1042),
								ElementToChannelConverter.SCALE_FACTOR_2), //
						m(BMWChannelId.FULL_CYCLE_COUNT, new UnsignedWordElement(1043)), //
						m(BMWChannelId.OPERATING_TIME_COUNT, new UnsignedDoublewordElement(1044)), //
						m(BMWChannelId.COM_PRO_VERSION, new UnsignedDoublewordElement(1046)), //
						m(BMWChannelId.SERIAL_NUMBER, new UnsignedDoublewordElement(1048)), //
						m(BMWChannelId.SERIAL_NUMBER, new UnsignedDoublewordElement(1050)), //
						m(BMWChannelId.SOFTWARE_VERSION, new UnsignedDoublewordElement(1052)) //
				)

		); //

		return protocol;
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable( //
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				Battery.getModbusSlaveNatureTable(accessMode) //
		);
	}
}
