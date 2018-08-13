package com.jdivirgilio.temperature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ReportTemperatureTask extends TimerTask
{
  private ArrayList<Jugs> jugList;
  private GregorianCalendar time;
  private boolean isPumpOn;
  private Calendar lastTimePumpOn;
  private Calendar lastTimePumpOff;
  
  private class TimeBreakDown {
	private long days;
	private long hours;
	private long minutes;
	private long seconds;
	
	public TimeBreakDown(long deltaTime) {
	    days = TimeUnit.MILLISECONDS.toDays(deltaTime);
	    deltaTime -= TimeUnit.DAYS.convert(deltaTime, TimeUnit.MILLISECONDS);
	    if (deltaTime > 0L)
	    {
	      hours = TimeUnit.MILLISECONDS.toHours(deltaTime);
	      deltaTime -= TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
	      if (deltaTime > 0L)
	      {
	        minutes = TimeUnit.MILLISECONDS.toMinutes(deltaTime);
	        deltaTime -= TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
	        if (deltaTime > 0L) {
	          seconds = TimeUnit.MILLISECONDS.toSeconds(deltaTime);
	        }
	      }
	    }
	}

	public long getDays() {
		return days;
	}

	public long getHours() {
		return hours;
	}

	public long getMinutes() {
		return minutes;
	}

	public long getSeconds() {
		return seconds;
	}
  }
  
  public ReportTemperatureTask(ArrayList<Jugs> jugList, GregorianCalendar time, boolean isPumpOn, Calendar lastTimePumpOn, Calendar lastTimePumpOff)
  {
    this.jugList = jugList;
    this.time = time;
    this.isPumpOn = isPumpOn;
    this.lastTimePumpOff = lastTimePumpOff;
    this.lastTimePumpOn = lastTimePumpOn;
  }
  
  public void run()
  {
    Double averageInsideTemp = 0.0D;
    Double averageOutsideTemp = 0.0D;
    int numInside = 0;
    int numOutside = 0;
    StringBuilder sb = new StringBuilder();
    System.out.println("\n" + Calendar.getInstance().getTime());
    for (Jugs jug : this.jugList) {
      if (jug.getPlantName().isEmpty())
      {
        averageOutsideTemp = averageOutsideTemp + jug.getTemperature() + jug.getOffset();
        numOutside++;
      }
      else
      {
        averageInsideTemp = averageInsideTemp + jug.getTemperature() + jug.getOffset();
        numInside++;
        sb.append(jug.getPlantName() + " temp is %.2fF\n");
        System.out.printf(sb.toString(), jug.getTemperature() + jug.getOffset());
      }
    }
    averageInsideTemp = averageInsideTemp / numInside;
    averageOutsideTemp = averageOutsideTemp / numOutside;
    TimeBreakDown timeInUnits = new TimeBreakDown(System.currentTimeMillis() - this.time.getTimeInMillis());

    System.out.printf("The average tank temperature is %.2f\nThe average outside temperature is %.2f\n", averageInsideTemp, averageOutsideTemp);
   	timeInUnits = new TimeBreakDown(System.currentTimeMillis() - (isPumpOn ? lastTimePumpOff.getTimeInMillis() : lastTimePumpOn.getTimeInMillis()));
    System.out.printf("The pump was last " + (this.isPumpOn ? "off " : "on ") + "%d Days %02d:%02d:%02d\n", timeInUnits.getDays(), 
    		timeInUnits.getHours(), timeInUnits.getMinutes(), timeInUnits.getSeconds());
    System.out.printf("The pump has been " + (this.isPumpOn ? "on" : "off") + " for %d Days %02d:%02d:%02d\n",
    		timeInUnits.getDays(), timeInUnits.getHours(), timeInUnits.getMinutes(), timeInUnits.getSeconds());
  }
  
  public void setPumpTime(GregorianCalendar lastTime, boolean isPumpOn, Calendar lastTimePumpOn, Calendar lastTimePumpOff)
  {
    this.time = lastTime;
    this.isPumpOn = isPumpOn;
    this.lastTimePumpOn = lastTimePumpOn;
    this.lastTimePumpOff = lastTimePumpOff;
  }
}