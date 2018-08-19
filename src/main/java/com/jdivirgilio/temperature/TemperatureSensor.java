/**
 * 
 */
package com.jdivirgilio.temperature;

/**
 * @author jdivirgilio
 *
 */
public enum TemperatureSensor {

	DEVICE5FF("28-0416735b95ff", 2.34, "White Widow"), DEVICE4FF("28-041672dbe4ff", 0.0, ""),
	DEVICEAFF("28-031673dddaff", 0.0, ""), DEVICEBFF("28-0316734c6bff", 0.72, "");

	private String deviceId;
	private double offset;
	private String plantName;

	TemperatureSensor(String id, double offset, String plantName) {
		this.deviceId = id;
		this.offset = offset;
		this.plantName = plantName;
	}

}
