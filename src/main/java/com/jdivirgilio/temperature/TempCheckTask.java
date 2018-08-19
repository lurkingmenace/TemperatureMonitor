package com.jdivirgilio.temperature;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

class TempCheckTask extends TimerTask {

	private static final int NUMBER_OF_SECS_WAITING = 30;
	static private int secondsWaiting = 1;

	static private Double averageInside = 0.0;
	static private long lastAverageInside = 0;
	private Double averageOutside = 0.0;
	private ArrayList<Jugs> jugList;
	private Semaphore lock = new Semaphore(1);
	static private PumpOnTask pumpOnTask = null;
	private TwoHourTimerTask twoHourTimerTask = null;
	private ReportTemperature reportTemperature = null;
	private PumpPin pumpPin;

	public TempCheckTask(ArrayList<Jugs> jugList, TwoHourTimerTask twoHourTimerTask,
			ReportTemperature reportTemperatureTask) {
		this.jugList = jugList;
		this.twoHourTimerTask = twoHourTimerTask;
		this.reportTemperature = reportTemperatureTask;
		pumpPin = PumpPin.getInstance();
		pumpPin.low();
	}

	public double getAverageInside() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
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
			e.printStackTrace();
		}
		averageOutside = 0.0;
		int numOutside = 0;
		averageInside = 0.0;
		int numInside = 0;
		for (Jugs jug : jugList) {
			// System.out.printf("%f %f\n", jug.getTemperature(), jug.getOffset());
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
//		System.out.println("in: " + averageInside + "   out: " + averageOutside);
		if ((averageInside > 67.0D) && (pumpOnTask == null)) {
			twoHourTimerTask.lockIt();
//			System.out.println("creating a pumpon task");
			pumpOnTask = new PumpOnTask(pumpPin);
			pumpOnTask.start();
			reportTemperature.setPumpTime(new GregorianCalendar(), true);
		} else if ((averageInside < 66.0D) && (pumpOnTask != null)) {
//			System.out.println("shutting down pumpon task");
			pumpOnTask.shutdown();
			pumpOnTask = null;
			twoHourTimerTask.unlockIt();
			reportTemperature.setPumpTime(new GregorianCalendar(), false);
		}
		if (lastAverageInside != (long) (averageInside * 100)) {
			if (secondsWaiting > NUMBER_OF_SECS_WAITING) {
				lastAverageInside = (long) (averageInside * 100);
				reportTemperature.publish();
				secondsWaiting = 1;
			} else {
				secondsWaiting++;
			}
		}
	}
}
