package com.jdivirgilio.temperature;

public class PumpOnTask extends Thread {

	private final int TIME_PUMP_ON_MILLISECS = 5 * 1000; // 5 Seconds
	private final int TIME_PUMP_OFF_MILLISECS = 180 * 1000; // 180 Seconds
	private Boolean finished = false;
	private PumpPin pumpPin;

	public PumpOnTask(PumpPin pumpPin) {
		setName("PumpOnTask");
		this.pumpPin = pumpPin;
	}

	public void shutdown() {
		synchronized (finished) {
			finished = true;
			interrupt();
		}
	}

	@Override
	public void run() {
		while (!finished) {
			try {
				pumpPin.low();
				sleep(TIME_PUMP_ON_MILLISECS);
				pumpPin.high();
				sleep(TIME_PUMP_OFF_MILLISECS);
			} catch (InterruptedException e) {
				pumpPin.high();
			}
			synchronized (finished) {
				if (finished)
					break;
			}
		}
	}
}
