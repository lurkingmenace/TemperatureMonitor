package com.jdivirgilio.temperature;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

class TempCheckTask extends TimerTask {

	static private Double averageInside = 0.0;
	static private Double lastAverageInside = 0.0;
	private Double averageOutside = 0.0;
	private ArrayList<Jugs> jugList;
	private Semaphore lock = new Semaphore(1);
	static private PumpOnTask pumpOnTask = null;
	private TwoHourTimerTask twoHourTimerTask = null;
	private ReportTemperatureTask reportTemperatureTask = null;
	private PumpPin pumpPin;
	static private GregorianCalendar lastTimePumpOff = new GregorianCalendar();
	static private GregorianCalendar lastTimePumpOn = new GregorianCalendar();
	
	public TempCheckTask(ArrayList<Jugs> jugList, TwoHourTimerTask twoHourTimerTask, ReportTemperatureTask reportTemperatureTask) {
		this.jugList = jugList;
		this.twoHourTimerTask = twoHourTimerTask;
		this.reportTemperatureTask = reportTemperatureTask;
		pumpPin = PumpPin.getInstance();
	    pumpPin.low();
	}

	public double getAverageInside() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double retVal = averageInside;
		lock.release();
		return retVal;
	}

	public double getAverageOutside() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double retVal = averageOutside;
		lock.release();
		return retVal;
	}
	
	@Override
	public void run() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		averageOutside = 0.0;
		int numOutside = 0;
		averageInside = 0.0;
		int numInside = 0;
		for (Jugs jug : jugList) {
			//System.out.printf("%f %f\n", jug.getTemperature(), jug.getOffset());
			if (jug.getPlantName().isEmpty()) {
				averageOutside += jug.getTemperature() + jug.getOffset();
				numOutside++;
			} else {
				averageInside += jug.getTemperature() + jug.getOffset();
				numInside++;
			}
		}
		averageInside /= numInside;
		averageOutside /= numOutside;
		lock.release();
		if ((averageInside > 67.0D) && (pumpOnTask == null)) {
			twoHourTimerTask.lockIt();
//			System.out.println("creating a pumpon task");
			pumpOnTask = new PumpOnTask(pumpPin);
			lastTimePumpOn = new GregorianCalendar();
			pumpOnTask.start();
			reportTemperatureTask.setPumpTime(new GregorianCalendar(), true, lastTimePumpOn, lastTimePumpOff);
		} else if ((averageInside < 66.0D) && (pumpOnTask != null)) {
//			System.out.println("shutting down pumpon task");
			pumpOnTask.shutdown();
			pumpOnTask = null;
			lastTimePumpOff = new GregorianCalendar();
			twoHourTimerTask.unlockIt();
			reportTemperatureTask.setPumpTime(new GregorianCalendar(), false, lastTimePumpOn, lastTimePumpOff);
		}
	}
}