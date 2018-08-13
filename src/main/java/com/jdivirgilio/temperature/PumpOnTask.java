package com.jdivirgilio.temperature;

public class PumpOnTask extends Thread {
	
	private Boolean finished = false;
	private PumpPin pumpPin;

	
	public PumpOnTask(PumpPin pumpPin) {
		setName("PumpOnTask");
		this.pumpPin = pumpPin;
	}

	public void shutdown() {
		synchronized(finished) {
			finished = true;
			interrupt();
		}
	}

	@Override
	public void run() {
		while (!finished) {
			try {
				pumpPin.high();
				sleep(5000);
				pumpPin.low();
				sleep(120000);
			} catch (InterruptedException e) {
				pumpPin.low();
			}
			synchronized(finished) {
				if (finished)
					break;
			}
		}
	}
}
