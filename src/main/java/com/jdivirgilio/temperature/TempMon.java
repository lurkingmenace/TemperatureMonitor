package com.jdivirgilio.temperature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Master;
import com.pi4j.io.w1.W1Device;

public class TempMon
  implements Runnable
{
  public static final int MAX_TEMP = 67;
  public static final int MIN_TEMP = 66;
  public static final int ALERT_TEMP = 69;
  public static final int ON_TIME = 5000;
  public static final int OFF_TIME = 120000;
  public static final long REPORT_TIME_INTERVAL = 300000L; // 5 minutes
  public static final long PUSH_WATER_INTERVAL = 7200000L; // 2 hours
  private Thread t;
  private ArrayList<Jugs> jugList = new ArrayList<>();
  private Timer twoHourTimer = new Timer();
  private Timer tempCheckTimer = new Timer();
  private Timer reportTimer = new Timer();
  private GregorianCalendar lastTimePumpOn = new GregorianCalendar();
  private GregorianCalendar lastTimePumpOff = new GregorianCalendar();
  
  public TempMon()
  {
    t = new Thread(this, "TempMon");
  }
  
  private void initSystem()
  {
    try
    {
      Runtime.getRuntime().exec("modprobe w1-gpio");
      Runtime.getRuntime().exec("modprobe e1-therm");
    }
    catch (IOException e)
    {
      System.out.println("modprobe init failed");
      System.exit(1);
    }
    W1Master master = new W1Master();
    List<TemperatureSensor> devices = master.getDevices(TemperatureSensor.class);
    
    Path filePath = Paths.get("/home", new String[] { "pi", "tempSwitch", "jugs.txt" });
    boolean foundItInPersistedFile = false;
    if (Files.exists(filePath)) {
    	try (BufferedReader fin = Files.newBufferedReader(filePath)) {
    		ArrayList<String> parms = new ArrayList<>(4);
    		String line;
    		while ((line = fin.readLine()) != null)
    		{
    			StringTokenizer strTkn = new StringTokenizer(line, ":");
    			while (strTkn.hasMoreTokens()) {
    				parms.add(strTkn.nextToken());
    			}
    			int i = 0;
    			for (; i < devices.size(); i++) {
    				if ((parms.get(0)).equals(((W1Device)devices.get(i)).getId().trim()))
    				{
    					foundItInPersistedFile = true;
    					break;
    				}
    			}
    			if (!foundItInPersistedFile)
    			{
    				System.out.println("Mismatch in persisted file with devices on board");
    				break;
    			}
    			if (parms.size() == 3) {
    				jugList.add(new Jugs(devices.get(i), parms.get(1), Double.parseDouble(parms.get(2))));
    			} else {
    				jugList.add(new Jugs(devices.get(i), parms.get(1), Double.parseDouble(parms.get(2)), parms.get(3)));
    			}
    			parms.clear();
    		}
    	}
    	catch (IOException e ) {
    		System.out.println("Error opening the file");
    		System.exit(1);
    	}
    } else {
    	System.out.println("Ask for input");
    }
    try
    {
    	Files.copy(filePath, filePath.getParent().resolve("jugs.bak"), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    }
    catch (IOException e)
    {
    	System.out.println("Failed backing up " + filePath);

    	System.exit(1);
    }
    try (BufferedWriter fout = new BufferedWriter(Files.newBufferedWriter(filePath,
    		StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))) {
    	for (int i = 0; i < jugList.size(); i++) {
    		((Jugs) jugList.get(i)).persist(fout);
    	}
    } catch (IOException e) {
    	System.out.println("Error persisting Jugs");
    	System.exit(1);
    }
  }

  public void run()
  {
		initSystem();

	    Calendar calendar = Calendar.getInstance();
	    int hour = calendar.get(11) % 2 == 0 ? calendar.get(11) + 2 : calendar.get(11) + 1;
	    calendar.set(11, hour);
	    calendar.set(12, 0);
	    calendar.set(13, 0);
	    TwoHourTimerTask twoHourTimerTask = new TwoHourTimerTask();
	    /*   Calendar calendar = Calendar.getInstance();
	    int minute = calendar.get(12);
	    if (minute % 5 == 0) {
	      minute++;
	    }
	    if (minute < 15) {
	      minute = 15;
	    } else if (minute < 30) {
	      minute = 30;
	    } else if (minute < 45) {
	      minute = 45;
	    } else {
	      minute = 0;
	    }
	    calendar.set(12, minute);
	    calendar.set(13, 0);
	    */
	    ReportTemperatureTask reportTemperatureTask = new ReportTemperatureTask(jugList, new GregorianCalendar(), false, lastTimePumpOn, lastTimePumpOff);
	    
	    TempCheckTask tempCheckTask = new TempCheckTask(jugList, twoHourTimerTask, reportTemperatureTask);
		tempCheckTimer.schedule(tempCheckTask, 0, 5000);
	    twoHourTimer.scheduleAtFixedRate(twoHourTimerTask, calendar.getTime(), PUSH_WATER_INTERVAL);
	    reportTimer.schedule(reportTemperatureTask, 0, REPORT_TIME_INTERVAL);
	}

  
  public static void main(String[] args)
  {
    TempMon tm = new TempMon();
    tm.t.start();
    try
    {
      tm.t.join();
    }
    catch (InterruptedException e)
    {
      System.out.println("Main thread exception");
    }
  }
}
  
  