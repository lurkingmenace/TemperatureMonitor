package com.jdivirgilio.temperature;

import java.io.BufferedWriter;
import java.io.IOException;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;
import com.pi4j.temperature.TemperatureScale;

class Jugs {

	// Constants
	public final static String DEVICE5FF = "28-0416735b95ff";
	public final static String DEVICE4FF = "28-041672dbe4ff";
	public final static String DEVICEAFF = "28-031673dddaff";
	public final static String DEVICEBFF = "28-0316734c6bff";

	private TemperatureSensor device;
	private String name;
	private double offset;
	private String plantName;

	public Jugs(TemperatureSensor device, String name, double tempOffset, String plantName) {
		this.device = device;
		this.name = name;
		this.offset = tempOffset;
		this.plantName = plantName;
	}

	public Jugs(TemperatureSensor device, String name, double tempOffset) {
		this(device, name, tempOffset, "");
	}

	public void persist(BufferedWriter fout) throws IOException {
		fout.write(((W1Device) device).getId().trim() + ":" + getName() + ":" + Double.toString(getOffset()));
		if (!getPlantName().isEmpty()) {
			fout.write(":" + getPlantName());
		}
		fout.write("\n");
	}

	public Double getTemperature() {
		return device.getTemperature(TemperatureScale.FARENHEIT) + this.offset;
	}

	public TemperatureSensor getDevice() {
		return device;
	}

	public String getName() {
		return name;
	}

	private Double getOffset() {
		return offset;
	}

	public String getPlantName() {
		return plantName;
	}
}
