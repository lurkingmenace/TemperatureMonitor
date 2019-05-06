package com.jdivirgilio.temperature;

import java.util.concurrent.TimeUnit;

public class TimeBreakDown {
		private long days;
		private long hours;
		private long minutes;
		private long seconds;

	public TimeBreakDown(long deltaTime) {
		days = TimeUnit.MILLISECONDS.toDays(deltaTime);
		deltaTime -= TimeUnit.MILLISECONDS.convert(days, TimeUnit.MILLISECONDS);
		if (deltaTime > 0L) {
			hours = TimeUnit.MILLISECONDS.toHours(deltaTime);
			deltaTime -= TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
			if (deltaTime > 0L) {
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

