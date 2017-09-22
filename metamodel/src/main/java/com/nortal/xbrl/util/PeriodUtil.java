package com.nortal.xbrl.util;

import com.nortal.xbrl.metamodel.meta.PresentationEntry.PeriodType;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class PeriodUtil {

	private static Calendar getCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public static boolean isDateEqual(Date firstDate, Date secondDate) {
		return firstDate == secondDate || (firstDate != null && firstDate.equals(secondDate));
	}

	public static PeriodType getPeriodType(Date periodStartDate, Date periodEndDate) {
		if (periodStartDate == null && periodEndDate != null) {
			return PeriodType.INSTANT;
		}
		if (periodStartDate != null && periodEndDate != null) {
			return PeriodType.DURATION;
		}
		else if (periodStartDate == null && periodEndDate == null) {
			return PeriodType.FOREVER;
		}
		else {
			throw new IllegalStateException("Unable to detect period type");
		}
	}

	public static Integer getAccountingYear(Date periodEndDate) {
		int periodEndMonth = getCalendar(periodEndDate).get(Calendar.MONTH);

		if (periodEndMonth >= Calendar.JANUARY && periodEndMonth <= Calendar.JUNE) {
			return getCalendar(periodEndDate).get(Calendar.YEAR) - 1;
		}
		else {
			return getCalendar(periodEndDate).get(Calendar.YEAR);
		}
	}

	public static boolean isYearDuration(Date startDate, Date endDate) {
		Calendar startCalendar = new GregorianCalendar();
		startCalendar.setTime(startDate);
		startCalendar.add(Calendar.DATE, -1);
		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);

		int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
		int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
		return diffMonth == 12;
	}

	public static boolean isWithinRange(Date testDate, Date startDate, Date endDate) {
		return !(testDate.before(startDate) || testDate.after(endDate));
	}

	public static Calendar getTodayDateCalendar() {
		Calendar calendar = Calendar.getInstance();
		removeTimeComponent(calendar);
		return calendar;
	}

	public static Calendar getTimelessDateCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		removeTimeComponent(calendar);
		return calendar;
	}

	public static void removeTimeComponent(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	public static Date removeTimeComponent(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		removeTimeComponent(cal);

		return cal.getTime();
	}

	private static Date validateEstablishmentDate(Date establishmentDate) {
		if (establishmentDate == null || removeTimeComponent(establishmentDate).after(getTodayDateCalendar().getTime())) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, 1975);

			return cal.getTime();
		}

		return establishmentDate;
	}

	public static Date getDefaultPeriodStart(Date periodEnd, Date establishmentDate) {
		establishmentDate = validateEstablishmentDate(establishmentDate);
		Calendar assumedPeriodStart = Calendar.getInstance();
		assumedPeriodStart.setTime(periodEnd);
		assumedPeriodStart.add(Calendar.YEAR, -1);
		assumedPeriodStart.add(Calendar.DATE, 1);
		removeTimeComponent(assumedPeriodStart);

		return establishmentDate.after(assumedPeriodStart.getTime()) ? removeTimeComponent(establishmentDate) : assumedPeriodStart.getTime();
	}

	public static Date getDefaultPeriodEnd(Integer fiscalYearEndDay, Integer fiscalYearEndMonth) {
		Calendar periodEnd = Calendar.getInstance();
		periodEnd.set(Calendar.MONTH, fiscalYearEndMonth - 1);
		periodEnd.set(Calendar.DAY_OF_MONTH, fiscalYearEndDay);
		removeTimeComponent(periodEnd);

		Calendar today = Calendar.getInstance();
		removeTimeComponent(today);

		if (periodEnd.before(today)) {
			return periodEnd.getTime();
		}

		periodEnd.add(Calendar.YEAR, -1);
		return periodEnd.getTime();
	}

	public static Date getDueDateForSubmission(Integer fiscalYearEndDay, Integer fiscalYearEndMonth, Integer fiscalYear, Integer dueMonths) {
		if (fiscalYearEndMonth <= 6) {
			fiscalYear++;
		}

		Calendar dueDate = Calendar.getInstance();

		dueDate.set(Calendar.YEAR, fiscalYear);
		dueDate.set(Calendar.MONTH, fiscalYearEndMonth - 1);
		dueDate.set(Calendar.DAY_OF_MONTH, fiscalYearEndDay);
		dueDate.add(Calendar.MONTH, dueMonths);
		dueDate.add(Calendar.DATE, 1);
		removeTimeComponent(dueDate);

		return dueDate.getTime();
	}

	public static Date getPrecedingPeriodStart(Date periodStart, Date establishmentDate) {
		establishmentDate = validateEstablishmentDate(establishmentDate);

		if (periodStart.compareTo(removeTimeComponent(establishmentDate)) == 0) {
			return removeTimeComponent(establishmentDate);
		}

		Calendar precedingPeriodStart = Calendar.getInstance();
		precedingPeriodStart.setTime(periodStart);
		precedingPeriodStart.add(Calendar.YEAR, -1);
		removeTimeComponent(precedingPeriodStart);

		return precedingPeriodStart.getTime().before(establishmentDate) ? removeTimeComponent(establishmentDate) : precedingPeriodStart.getTime();
	}

	public static Date getPrecedingPeriodEnd(Date periodStart, Date establishmentDate) {
		establishmentDate = validateEstablishmentDate(establishmentDate);

		if (periodStart.compareTo(removeTimeComponent(establishmentDate)) == 0) {
			return removeTimeComponent(establishmentDate);
		}

		Calendar precedingPeriodEnd = Calendar.getInstance();
		precedingPeriodEnd.setTime(periodStart);
		precedingPeriodEnd.add(Calendar.DATE, -1);
		removeTimeComponent(precedingPeriodEnd);

		return precedingPeriodEnd.getTime();
	}

	public static boolean isInspectionLockActive(Date inspectionStart, Integer inspectionLockPeriod) {
		if (inspectionStart == null) {
			return false;
		}

		Calendar inspectionLockEnd = getCalendar(inspectionStart);
		Calendar now = Calendar.getInstance();

		inspectionLockEnd.add(Calendar.MINUTE, inspectionLockPeriod);
		return inspectionLockEnd.after(now);
	}
}
