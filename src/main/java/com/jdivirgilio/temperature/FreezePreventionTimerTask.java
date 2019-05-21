package com.jdivirgilio.temperature;

import java.util.Calendar;
import java.util.TimerTask;

class FreezePreventionTimerTask
extends TimerTask
{
	private PumpPin pumpPin;

	public FreezePreventionTimerTask() {
		pumpPin = PumpPin.getInstance();
	}

	public void run()
	{
		System.out.println("\n" + Calendar.getInstance().getTime() + "\nPushing water through to prevent freezing");
		if (this.pumpPin.isHigh()) {
			this.pumpPin.low();
		}
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (this.pumpPin.isLow()) {
			this.pumpPin.high();
		}
		try {
			Thread.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}