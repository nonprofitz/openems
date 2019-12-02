package io.openems.edge.ess.refu88k;

import io.openems.common.types.OptionsEnum;

public enum PCSSetOperation implements OptionsEnum {
		
	UNDEFINED(-1, "Undefined"),
	CONNECT_TO_GRID(1, "Connect to grid"),
	ENTER_STARTED_MODE(2, "Stop system"),
	ENTER_STANDBY_MODE(3, "Enter Standby Mode"),
	EXIT_STANDBY_MODE(4, "Exit Standby Mode"),
	;

	private final int value;
	private final String name;

	private PCSSetOperation(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}

}
