package com.nortal.xbrl.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.nortal.xbrl.constants.Constants;

/**
 * Utility class for Date problems (formatting and truncating). This class is mainly an wrapper between libs with
 * similar functionality.
 */
public class DateUtils {

	/**
	 * Returns the current day without seconds, minutes and hours.
	 * 
	 * @return truncated date
	 */
	public static Date getCurrentDate() {
		return org.apache.commons.lang3.time.DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
	}

	/**
	 * Truncates the hour, minutes and seconds so the comparison is done by day, month and year only.
	 * 
	 * @param date needed date
	 * @return truncated date
	 */
	public static Date trimDate(Date date) {
		if (date == null) {
			return null;
		}
		return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
	}

	/**
	 * Increments the given date with one day.
	 */
	public static Date incrementDayInDate(Date date) {
		if (date == null) {
			return null;
		}

		Calendar cal = new GregorianCalendar();

		cal.setTime(date);
		cal.add(Calendar.DATE, 1);

		return trimDate(cal.getTime());
	}

	/**
	 * Increments the given date with given number of days.
	 */
	public static Date incrementDaysInDate(Date date, int number) {
		if (date == null) {
			return null;
		}

		Calendar cal = new GregorianCalendar();

		cal.setTime(date);
		cal.add(Calendar.DATE, number);

		return trimDate(cal.getTime());
	}

    /**
     * Increments the given date with given number of seconds.
     */
    public static Date incrementSecondsInDate(Date date, int number) {
        if (date == null) {
            return null;
        }

        Calendar cal = new GregorianCalendar();

        cal.setTime(date);
        cal.add(Calendar.SECOND, number);

        return cal.getTime();
    }

	/**
	 * Increments the given date with one month
	 */
	public static Date incrementMonthInDate(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.MONTH, 1);

		return trimDate(cal.getTime());
	}

	/**
	 * Increments the given date with xxx months
	 */
	public static Date incrementMonthsInDate(Date date, int number) {
		if (date == null) {
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.MONTH, number);

		return trimDate(cal.getTime());
	}

	public static Date incrementYearsInDate(Date date, int number) {
		final Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.YEAR, number);
		return cal.getTime();
	}

	/**
	 * Decrements the given date by one day.
	 */
	public static Date decrementDayInDate(Date date) {
		if (date == null) {
			return null;
		}

		Calendar cal = new GregorianCalendar();

		cal.setTime(date);
		cal.add(Calendar.DATE, -1);

		return trimDate(cal.getTime());
	}

	/**
	 * Decrements the given date by given number of days.
	 */
	public static Date decrementDaysInDate(Date date, int number) {
		if (date == null) {
			return null;
		}

		Calendar cal = new GregorianCalendar();

		cal.setTime(date);
		cal.add(Calendar.DATE, -1 * number);

		return trimDate(cal.getTime());
	}

	/**
	 * Returns iterator on the week before the given date.
	 */
	public static Iterator<Calendar> getLastWeek(Date date) {
		if (date == null) {
			return null;
		}
		// decrement one week (7 days)
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, -7);

		return org.apache.commons.lang3.time.DateUtils.iterator(cal,
				org.apache.commons.lang3.time.DateUtils.RANGE_WEEK_SUNDAY);
	}

	/**
	 * Returns iterator on the current week (week starting on sunday).
	 */
	public static Iterator<Calendar> getCurrentWeek(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);

		return org.apache.commons.lang3.time.DateUtils.iterator(cal,
				org.apache.commons.lang3.time.DateUtils.RANGE_WEEK_SUNDAY);
	}

	/**
	 * Returns iterator on the next week.
	 */
	public static Iterator<Calendar> getNextWeek(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);

		cal.add(Calendar.DATE, +7);

		return org.apache.commons.lang3.time.DateUtils.iterator(cal,
				org.apache.commons.lang3.time.DateUtils.RANGE_WEEK_SUNDAY);
	}

	/**
	 * Returns last week before the present day.
	 */
	public static Iterator<Calendar> getLastWeek() {
		return getLastWeek(new Date());
	}

	/**
	 * Returns current week including the present day.
	 */
	public static Iterator<Calendar> getCurrentWeek() {
		return getCurrentWeek(new Date());
	}

	/**
	 * Returns next week after the present day.
	 */
	public static Iterator<Calendar> getNextWeek() {
		return getNextWeek(new Date());
	}

	/**
	 * Gets the year in a certain day.
	 */
	public static int getYearInDate(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	/**
	 * Gets the month in a certain day Attention: value returned is in interval [0..11]
	 */
	public static int getMonthInDate(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.MONTH);
	}

	/**
	 * Gets the month in a certain day
	 */
	public static int getDayInDate(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public static Date getFirstDayInWeek(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		Iterator<Calendar> it = getCurrentWeek(date);
		cal = it.next();

		return cal.getTime();
	}

	public static Date getFirstDayInLastWeek(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		Iterator<Calendar> it = getLastWeek(date);
		cal = it.next();

		return cal.getTime();
	}

	public static Date getFirstDayInMonth(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.MONTH);

		return cal.getTime();
	}

	public static Date getFirstDayInYear(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.YEAR);

		return cal.getTime();
	}

	/**
	 * returns the first day from last year
	 */
	public static Date getFirstDayInLastYear(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -1);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.YEAR);
		return cal.getTime();
	}

	/**
	 * returns the last day from last year
	 */
	public static Date getLastDayInLastYear(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.YEAR);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
		return cal.getTime();
	}

	/**
	 * returns the last day of the year
	 */
	public static Date getLastDayInYear(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.YEAR);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		return cal.getTime();
	}

	public static Date getLastDayInMonth(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.MONTH);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

		return cal.getTime();
	}

	public static Date getLastDayInWeek(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
		while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
			cal.add(Calendar.DATE, 1);
		}
		return cal.getTime();
	}

	public static Date getLastDayInLastWeek(Date date) {
		// decrement one week
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.WEEK_OF_MONTH, -1);

		return getLastDayInWeek(cal.getTime());
	}

	public static boolean areDatesInSameMonth(Date fromDate, Date toDate) {
		Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(fromDate);
		Calendar toCal = Calendar.getInstance();
		toCal.setTime(toDate);

		return fromCal.get(Calendar.MONTH) == toCal.get(Calendar.MONTH);
	}

	/**
	 * return the number of days between 2 dates
	 */
	public static long getDaysBetweenDates(Date date1, Date date2) {
		return (date1.getTime() - date2.getTime()) / org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;
	}

	/**
	 * returns a vector with the Calendar months
	 */
	public static Object[] getMonths() {
		return new Object[] {
				Calendar.JANUARY,
				Calendar.FEBRUARY,
				Calendar.MARCH,
				Calendar.APRIL,
				Calendar.MAY,
				Calendar.JUNE,
				Calendar.JULY,
				Calendar.AUGUST,
				Calendar.SEPTEMBER,
				Calendar.OCTOBER,
				Calendar.NOVEMBER,
				Calendar.DECEMBER
		};
	}

	/**
	 * Get the number of days in the month of a given date
	 */
	public static int getDaysInMonth(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Get the number of days in a month
	 */
	public static int getDaysInMonth(int monthIndex, int year) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, monthIndex);
		cal.set(Calendar.YEAR, year);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Returns a date representing the first date in a month and year. Month index is from 0 to 11
	 */
	public static Date getFirstDayInMonth(int monthIndex, int year) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, monthIndex);
		cal.set(Calendar.YEAR, year);
		cal = org.apache.commons.lang3.time.DateUtils.truncate(cal, Calendar.MONTH);
		return cal.getTime();
	}

	/**
	 * Returns a date representing a certain day in a month (from the date param)
	 */
	public static Date getDayInMonth(int dayIndex, Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.DATE, dayIndex);
		return cal.getTime();
	}

	/**
	 * compares 2 dates by day, month and year hours, secs, milisecs arent considered
	 */
	public static int compareDatesByDay(Date date1, Date date2) {
		date1 = trimDate(date1);
		date2 = trimDate(date2);
		return date1.compareTo(date2);
	}

	/**
	 * @return The list of days between the specified dates, including them
	 */
	public static List<Date> getDays(Date fromDate, Date toDate) {
		fromDate = DateUtils.trimDate(fromDate);
		toDate = DateUtils.trimDate(toDate);
		final Calendar cal = Calendar.getInstance();
		cal.setTime(fromDate);
		final long limit = toDate.getTime();

		List<Date> dates = new ArrayList<Date>();
		do {
			dates.add(cal.getTime());
			cal.add(Calendar.DATE, 1);
		} while (cal.getTimeInMillis() <= limit);
		return dates;
	}

	/**
	 * Returns the last day of the given year.
	 */
	public static Date getLastDayInYear(int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, Calendar.DECEMBER);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
		return calendar.getTime();
	}

	/**
	 * returns true if the given date is in the date interval specified
	 */
	public static boolean isInInterval(Date date, Date startIntervalDate, Date endIntervalDate) {
		if (date == null) {
			return false;
		}
		if (startIntervalDate == null && endIntervalDate == null) {
			return true;
		}
		if (startIntervalDate == null && endIntervalDate != null && date.compareTo(endIntervalDate) <= 0) {
			return true;
		}
		if (startIntervalDate != null && date.compareTo(startIntervalDate) >= 0 && endIntervalDate == null) {
			return true;
		}
		if (date.compareTo(startIntervalDate) >= 0 && date.compareTo(endIntervalDate) <= 0) {
			return true;
		}
		return false;
	}

	public static Date getFirstDayInLastMonth(Date date) {
		if (date == null) {
			return null;
		}
		// decrement one month
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.MONTH, -1);

		return getFirstDayInMonth(cal.getTime());
	}

	public static Date getLastDayInLastMonth(Date date) {
		if (date == null) {
			return null;
		}
		// decrement one month
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.MONTH, -1);

		return getLastDayInMonth(cal.getTime());
	}

	/**
	 * Returns the number of years between 2 dates.
	 */
	public static int calculateAge(Date dateOfBirth) {
		if (dateOfBirth != null) {
			Calendar dateOfBirthCal = new GregorianCalendar();
			dateOfBirthCal.setTime(dateOfBirth);

			Calendar crtDateCal = new GregorianCalendar();
			crtDateCal.setTime(DateUtils.getCurrentDate());

			int age = crtDateCal.get(Calendar.YEAR) - dateOfBirthCal.get(Calendar.YEAR);
			if (dateOfBirthCal.get(Calendar.MONTH) > crtDateCal.get(Calendar.MONTH)
					|| dateOfBirthCal.get(Calendar.MONTH) == crtDateCal.get(Calendar.MONTH)
					&& dateOfBirthCal.get(Calendar.DAY_OF_MONTH) > crtDateCal.get(Calendar.DAY_OF_MONTH)) {
				age -= 1;
			}

			return age;
		}

		return 0;
	}

	/**
	 * @return Formatted date.
	 */
	public static String formatDate(Date date, String format) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(StringUtils.isNotBlank(format) ? format
					: Constants.DEFAULT_DATE_PATTERN);
			return sdf.format(date);
		}
		return null;
	}

	/**
	 * @return true if provided date is in future
	 */
	public static boolean isFuture(Date date) {
		if(date==null){
			return false;
		}
		return new Date().compareTo(date) < 0;
	}

	public static boolean isFutureTrimmed(Date date) {
		return (date != null) && trimDate(new Date()).compareTo(date) < 0;
	}

}