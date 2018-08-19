package com.jdivirgilio.temperature;

import java.util.Calendar;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

class TwoHourTimerTask
extends TimerTask
{
	private ReentrantLock lock = new ReentrantLock();
	private PumpPin pumpPin;

	public TwoHourTimerTask() {
		pumpPin = PumpPin.getInstance();
	}

	void lockIt()
	{
		this.lock.lock();
	}

	void unlockIt()
	{
		this.lock.unlock();
	}

	public void run()
	{
		lockIt();
		System.out.println("\n" + Calendar.getInstance().getTime() + "\nPushing water through to prevent freezing");
		if (this.pumpPin.isLow()) {
			this.pumpPin.high();
		}
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (this.pumpPin.isHigh()) {
			this.pumpPin.low();
		}
		try {
			Thread.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		unlockIt();
	}
}