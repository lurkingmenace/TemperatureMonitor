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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Master;
import com.pi4j.io.w1.W1Device;

public class TempMon implements Runnable {
	@Parameter(names = "-f", description = "Hours to wait before cycling water through the pump. <= 0 = off")
	private Integer freezePreventionTime = 2;
	
	public static final Double MAX_TEMP = 68.0;
	public static final Double MIN_TEMP = 67.0;
	public static final Double ALERT_TEMP = 69.0;
	public static final Double MAX_TEMP_SINGLE_JUG = 69.0;
	public static final int ON_TIME = 5000;
	public static final int OFF_TIME = 120000;
	public static final long REPORT_TIME_INTERVAL = 300000L; // 5 minutes
	public static final long PUSH_WATER_INTERVAL = 7200000L; // 2 hours
	private Thread t;
	private ArrayList<Jugs> jugList = new ArrayList<>();
	private Timer tempCheckTimer = new Timer();

	public TempMon(String[] args) {
		t = new Thread(this, "TempMon");
		JCommander.newBuilder()
			.addObject(this)
			.build()
			.parse(args);
	}

	private void initSystem() {
		try {
			Runtime.getRuntime().exec("modprobe w1-gpio");
			Runtime.getRuntime().exec("modprobe e1-therm");
		} catch (IOException e) {
			e.printStackTrace();
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
				while ((line = fin.readLine()) != null) {
					StringTokenizer strTkn = new StringTokenizer(line, ":");
					while (strTkn.hasMoreTokens()) {
						parms.add(strTkn.nextToken());
					}
					int i = 0;
					for (; i < devices.size(); i++) {
						if ((parms.get(0)).equals(((W1Device) devices.get(i)).getId().trim())) {
							foundItInPersistedFile = true;
							break;
						}
					}
					if (!foundItInPersistedFile) {
						System.out.println("Mismatch in persisted file with devices on board");
						break;
					}
					if (parms.size() == 3) {
						System.out.println("Found Jug: " + parms.get(0) + " " + parms.get(1) + " " + parms.get(2));
						jugList.add(new Jugs(devices.get(i), parms.get(1), Double.parseDouble(parms.get(2))));
					} else {
						System.out.println("Found Jug: " + parms.get(0) + " " + parms.get(1) + " " + parms.get(2) + " " + parms.get(3));
						jugList.add(
								new Jugs(devices.get(i), parms.get(1), Double.parseDouble(parms.get(2)), parms.get(3)));
					}
					parms.clear();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			System.out.println("Ask for input");
		}
		try {
			Files.copy(filePath, filePath.getParent().resolve("jugs.bak"), StandardCopyOption.COPY_ATTRIBUTES,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try (BufferedWriter fout = new BufferedWriter(
				Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))) {
			for (int i = 0; i < jugList.size(); i++) {
				((Jugs) jugList.get(i)).persist(fout);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		initSystem();
		
//		for (Jugs jugs: jugList) {
//			System.out.println("Jug: " + jugs.getName() + " " + jugs.getOffset() + " " + jugs.getPlantName() + " is Empty? " + jugs.getPlantName().isEmpty());
//		}


		
		/*
		 * Calendar calendar = Calendar.getInstance(); int minute = calendar.get(12); if
		 * (minute % 5 == 0) { minute++; } if (minute < 15) { minute = 15; } else if
		 * (minute < 30) { minute = 30; } else if (minute < 45) { minute = 45; } else {
		 * minute = 0; } calendar.set(12, minute); calendar.set(13, 0);
		 */
		ReportTemperature reportTemperature = new ReportTemperature(jugList, new GregorianCalendar(), false);

		TempCheckTask tempCheckTask = new TempCheckTask(jugList, freezePreventionTime, reportTemperature);
		tempCheckTimer.schedule(tempCheckTask, 0, 5000);

	}

	public static void main(String[] args) {
		TempMon tm = new TempMon(args);
		tm.t.start();
		try {
			tm.t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
