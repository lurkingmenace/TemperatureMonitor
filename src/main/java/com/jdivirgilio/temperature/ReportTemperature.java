package com.jdivirgilio.temperature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ReportTemperature {
	private ArrayList<Jugs> jugList;
	private GregorianCalendar time;
	private boolean isPumpOn;

	public ReportTemperature(ArrayList<Jugs> jugList, GregorianCalendar time, boolean isPumpOn) {
		this.jugList = jugList;
		this.time = time;
		this.isPumpOn = isPumpOn;
//		for (Jugs jugs: jugList) {
//			System.out.println("Jug: " + jugs.getName() + " " + jugs.getOffset() + " " + jugs.getPlantName() + " is Empty? " + jugs.getPlantName().isEmpty());
//		}

	}

	public void publish() {
//		for (Jugs jugs: jugList) {
//			System.out.println("Jug: " + jugs.getName() + " " + jugs.getOffset() + " " + jugs.getPlantName() + " is Empty? " + jugs.getPlantName().isEmpty());
//		}
		Double averageInsideTemp = 0.0D;
		Double averageOutsideTemp = 0.0D;
		int numInside = 0;
		int numOutside = 0;
		System.out.println("\n" + Calendar.getInstance().getTime());
		for (Jugs jug : this.jugList) {
			StringBuilder sb = new StringBuilder();
			if (jug.getPlantName().isEmpty()) {
				averageOutsideTemp = averageOutsideTemp + jug.getTemperature();
				numOutside++;
			} else {
				averageInsideTemp = averageInsideTemp + jug.getTemperature();
				numInside++;
				sb.append(jug.getPlantName() + " temp is %.2fF\n");
				System.out.printf(sb.toString(), jug.getTemperature());
			}
		}
		averageInsideTemp = averageInsideTemp / numInside;
		averageOutsideTemp = averageOutsideTemp / numOutside;
		TimeBreakDown timeInUnits = new TimeBreakDown(System.currentTimeMillis() - this.time.getTimeInMillis());

		System.out.printf("The average tank temperature is %.2f\nThe average outside temperature is %.2f\n",
				averageInsideTemp, averageOutsideTemp);
		System.out.printf("The pump has been " + (this.isPumpOn ? "on" : "off") + " for %d Days %02d:%02d:%02d\n",
				timeInUnits.getDays(), timeInUnits.getHours(), timeInUnits.getMinutes(), timeInUnits.getSeconds());
	}

	public void setPumpTime(GregorianCalendar lastTime, boolean isPumpOn) {
		this.time = lastTime;
		this.isPumpOn = isPumpOn;
	}
}