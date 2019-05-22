package com.jdivirgilio.temperature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.Timer;

class TempCheckTask extends TimerTask {

	private static final int NUMBER_OF_SECS_WAITING = 30;
	static private int secondsWaiting = 1;

	static private Double averageInside = 0.0;
	static private long lastAverageInside = 0;
	private Double averageOutside = 0.0;
	private ArrayList<Jugs> jugList;
	private Semaphore lock = new Semaphore(1);
	static private PumpOnTask pumpOnTask = null;
	private FreezePreventionTimerTask freezePreventionTimerTask = null;
	private ReportTemperature reportTemperature = null;
	private TimePumpIsOnTask timePumpIsOnTask = null;
	private Timer freezePreventionTimer = null;
	private Integer freezePreventionTime = 2;
	private PumpPin pumpPin;

	public TempCheckTask(ArrayList<Jugs> jugList, Integer freezePreventionTime,
			ReportTemperature reportTemperatureTask) {
		this.jugList = jugList;
		this.reportTemperature = reportTemperatureTask;
		this.freezePreventionTime = freezePreventionTime;
		pumpPin = PumpPin.getInstance();
		pumpPin.high();
		startFreezePreventionTimer();
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
		boolean isAJugHot = false;
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
			// System.out.printf("%f %f\n", jug.getTemperature());
			if (jug.getPlantName().isEmpty()) {
				averageOutside += jug.getTemperature();
				numOutside++;
			} else {
				Double temp = jug.getTemperature();
				//System.out.println("jug: " + jug.getName() + " temp: " + temp);
				averageInside += temp;
				if (temp > TempMon.MAX_TEMP_SINGLE_JUG) {
					isAJugHot = true;
				}
				numInside++;
			}
		}
		averageInside /= numInside;
		averageOutside /= numOutside;
		lock.release();
		//System.out.println("pumpOnTask: " +  (pumpOnTask != null) + " isAJugHot: " + isAJugHot + " avgIn: "  + averageInside);
		if (pumpOnTask == null && (isAJugHot || averageInside > TempMon.MAX_TEMP)) {
			startPump();
		} else if (!isAJugHot && (averageInside < TempMon.MIN_TEMP) && (pumpOnTask != null)) {
			// System.out.println("shutting down pumpon task");
			pumpOnTask.shutdown();
			pumpOnTask = null;
			reportTemperature.setPumpTime(new GregorianCalendar(), false);
			if (timePumpIsOnTask != null) {
				timePumpIsOnTask.shutdown();
				timePumpIsOnTask = null;
			}
			startFreezePreventionTimer();
		}
		if (lastAverageInside != (long) (averageInside * 100)) { // This is a long to remove notificaiton on precision
																	// changes < 100th's pos.
			if (secondsWaiting > NUMBER_OF_SECS_WAITING) {
				lastAverageInside = (long) (averageInside * 100);
				reportTemperature.publish();
				secondsWaiting = 1;
			} else {
				secondsWaiting++;
			}
		}
	}

	private void startPump() {
		if (freezePreventionTimerTask != null) freezePreventionTimerTask.cancel();
		// System.out.println("creating a pumpon task");
		pumpOnTask = new PumpOnTask(pumpPin);
		pumpOnTask.start();
		reportTemperature.setPumpTime(new GregorianCalendar(), true);
		timePumpIsOnTask = new TimePumpIsOnTask();
	}
	
	private final void startFreezePreventionTimer() {
		// Set up freeze timer
		if (freezePreventionTime > 0) {
			Calendar freezeTimerTime = Calendar.getInstance();
			int hour = freezeTimerTime.get(11) % 2 == 0 ? freezeTimerTime.get(11) + 2 : freezeTimerTime.get(11) + 1;
			freezeTimerTime.set(11, hour);
			freezeTimerTime.set(12, 0);
			freezeTimerTime.set(13, 0);
			freezePreventionTimerTask = new FreezePreventionTimerTask();
			freezePreventionTimer = new Timer();
			System.out.println("Setting freeze prevention timer to " + freezePreventionTime.toString() + " hours.");
			freezePreventionTimer.scheduleAtFixedRate(freezePreventionTimerTask, freezeTimerTime.getTime(), freezePreventionTime * 60 * 60 * 1000);
		}
		else {
			System.out.println("No Freeze prevention task running");
		}
	}
}
