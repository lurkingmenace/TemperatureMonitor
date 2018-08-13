package com.jdivirgilio.temperature;

import java.util.concurrent.Semaphore;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class PumpPin {
	
	Semaphore lock = new Semaphore(1);
	private static PumpPin instance = null;
	
	private final GpioPinDigitalOutput pumpPin = GpioFactory.getInstance().provisionDigitalOutputPin(RaspiPin.GPIO_01, "PumpPin");
	
	private PumpPin() {
	}
	
	public static PumpPin getInstance() {
		if (instance == null) {
			instance = new PumpPin();
		}
		return instance;
	}
	
	public void low() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pumpPin.low();
		lock.release();
	}
	
	public void high() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pumpPin.high();
		lock.release();
	}
	
	public boolean isLow() {
		return pumpPin.isLow();
	}

	public boolean isHigh() {
		return pumpPin.isHigh();
	}
}
