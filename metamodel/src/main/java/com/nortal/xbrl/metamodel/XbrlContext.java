package com.nortal.xbrl.metamodel;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nortal.xbrl.constants.Constants;
import com.nortal.xbrl.util.DateUtils;
import com.nortal.xbrl.util.PeriodUtil;
import com.nortal.xbrl.metamodel.meta.LabelEntry;
import com.nortal.xbrl.metamodel.meta.LinkedEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry.PeriodType;
import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;

public class XbrlContext implements Serializable {

	private static final long serialVersionUID = 5499436644965981620L;

	private String id;

	private String entityScheme;
	private String entityName;

	private Date periodStartDate;
	private Date periodEndDate;

	private List<XbrlExplicitMember> segments;
	private List<XbrlExplicitMember> scenarios;

	public XbrlContext() {
		super();
		segments = new ArrayList<XbrlExplicitMember>();
		scenarios = new ArrayList<XbrlExplicitMember>();
	}

	public String getId() {
		return id;
	}

	public String getEntityScheme() {
		return entityScheme;
	}

	public String getEntityName() {
		return entityName;
	}

	public Date getPeriodStartDate() {
		return periodStartDate;
	}

	public Date getPeriodEndDate() {
		return periodEndDate;
	}

	public List<XbrlExplicitMember> getSegments() {
		return segments;
	}

	public void setSegments(List<XbrlExplicitMember> segments) {
		this.segments = segments;
	}

	public List<XbrlExplicitMember> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<XbrlExplicitMember> scenarios) {
		this.scenarios = scenarios;
	}

	public boolean hasExplicitMember() {
		return !segments.isEmpty() || !scenarios.isEmpty();
	}

	public boolean hasExplicitMember(String dimension, String value) {
		if (!hasExplicitMember()) {
			return false;
		}

		for (XbrlExplicitMember segment : segments) {
			if (segment.getDimensionName().equals(dimension) && segment.getValueName().equals(value)) {
				return true;
			}
		}

		for (XbrlExplicitMember scenario : scenarios) {
			if (scenario.getDimensionName().equals(dimension) && scenario.getValueName().equals(value)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasEqualPeriod(XbrlContext model) {
		return PeriodUtil.isDateEqual(periodStartDate, model.getPeriodStartDate())
				&& PeriodUtil.isDateEqual(periodEndDate, model.getPeriodEndDate());
	}

	public PeriodType getPeriodType() {
		return PeriodUtil.getPeriodType(periodStartDate, periodEndDate);
	}

	public boolean isDuration() {
		return getPeriodType() == PeriodType.DURATION;
	}

	public boolean isInstant() {
		return getPeriodType() == PeriodType.INSTANT;
	}

	public boolean isForever() {
		return getPeriodType() == PeriodType.FOREVER;
	}

	public String getPeriodLabel() {
		if (isDuration()) {
			return String.valueOf(PeriodUtil.getAccountingYear(periodEndDate));
		}
		else if (isInstant()) {
			return DateUtils.formatDate(periodEndDate, Constants.DEFAULT_DATE_PATTERN);
		}
		else {
			return null; // Forever
		}
	}

	@Override
	public String toString() {
		return "ContextModel{" + "id='" + id + '\'' + ", entityScheme='" + entityScheme + '\'' + ", entityName='"
				+ entityName + '\'' + ", periodStartDate=" + periodStartDate + ", periodEndDate=" + periodEndDate + '}';
	}

	public static class Builder {

		private String id;

		private String entityScheme;
		private String entityName;

		private Date startDate;
		private Date endDate;

		private List<XbrlExplicitMember> segments;
		private List<XbrlExplicitMember> scenarios;

		public Builder() {
			segments = new ArrayList<XbrlExplicitMember>();
			scenarios = new ArrayList<XbrlExplicitMember>();
		}

		public Builder(String scheme, String name) {
			this();

			this.entityScheme = scheme;
			this.entityName = name;
		}

		public Builder(String id, String scheme, String name) {
			this();

			this.id = id;
			this.entityScheme = scheme;
			this.entityName = name;
		}

		public Builder setId(String id) {
			this.id = id;

			return this;
		}

		public Builder setEntitySchema(String schema) {
			this.entityScheme = schema;

			return this;
		}

		public Builder setEntityName(String entityName) {
			this.entityName = entityName;

			return this;
		}

		public Builder setInstant(Date instantDate) {
			this.startDate = null;
			this.endDate = instantDate;

			return this;
		}

		public Builder setInstant(String instant) {
			this.startDate = null;
			this.endDate = parseDate(instant);

			return this;
		}

		public Builder setDateRange(String startDate, String endDate) throws ParseException {
			this.startDate = parseDate(startDate);
			this.endDate = parseDate(endDate);

			return this;
		}

		public Builder setDateRange(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;

			return this;
		}

		public Builder setDateRange(PresentationEntry presentation, Date startDate, Date endDate) {
			if (startDate == null && endDate != null) {
				setInstant(endDate);
			}
			else if (presentation.getPeriod() == PeriodType.DURATION) {
				setDateRange(startDate, endDate);
			}
			else if (presentation.getPeriod() == PeriodType.INSTANT) {
				if (presentation.getPreferredLabelType() == LabelEntry.LabelType.PERIOD_START) {
					setInstant(startDate);
				}
				else {
					setInstant(endDate);
				}
			}

			return this;
		}

		public Builder setSegment(XbrlExplicitMember xbrlExplicitMember) {
			this.segments.add(xbrlExplicitMember);

			return this;
		}

		public Builder setSegment(LinkedEntry dimension, LinkedEntry segment) {
			this.segments.add(new XbrlExplicitMember.Builder()
					.setDimension(dimension.getNamespace(), dimension.getNamespacePrefix(), dimension.getName())
					.setValue(segment.getNamespace(), segment.getNamespacePrefix(), segment.getName()).build());

			return this;
		}

		public Builder setScenario(XbrlExplicitMember xbrlExplicitMember) {
			this.scenarios.add(xbrlExplicitMember);

			return this;
		}

		public Builder setScenario(LinkedEntry dimension, LinkedEntry scenario) {
			this.scenarios.add(new XbrlExplicitMember.Builder()
					.setDimension(dimension.getNamespace(), dimension.getNamespacePrefix(), dimension.getName())
					.setValue(scenario.getNamespace(), scenario.getNamespacePrefix(), scenario.getName()).build());

			return this;
		}

		public Builder merge(XbrlContext metamodel) {
			if (metamodel.getEntityScheme() != null) {
				this.entityScheme = metamodel.getEntityScheme();
			}

			if (metamodel.getEntityName() != null) {
				this.entityName = metamodel.getEntityName();
			}

			if (metamodel.getPeriodStartDate() != null) {
				this.startDate = metamodel.getPeriodStartDate();
			}

			if (metamodel.getPeriodEndDate() != null) {
				this.endDate = metamodel.getPeriodEndDate();
			}

			if (metamodel.getSegments() != null) {
				this.segments.addAll(metamodel.getSegments());
			}

			if (metamodel.getScenarios() != null) {
				this.scenarios.addAll(metamodel.getScenarios());
			}

			return this;
		}

		private Date parseDate(String dateString) {
			try {
				return XbrlMetamodel.DATE_FORMAT.get().parse(dateString);
			}
			catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}

		public String buildId() {
			StringBuilder id = new StringBuilder();

			// Append period information
			if (startDate != null && endDate != null) {
				id.append("d");
				id.append(XbrlMetamodel.PARTS_DELIMITER);
				id.append(XbrlMetamodel.DATE_FORMAT.get().format(startDate));
				id.append(XbrlMetamodel.PARTS_DELIMITER);
				id.append(XbrlMetamodel.DATE_FORMAT.get().format(endDate));
			}
			else if (endDate != null) {
				id.append("i");
				id.append(XbrlMetamodel.PARTS_DELIMITER);
				id.append(XbrlMetamodel.DATE_FORMAT.get().format(endDate));
			}
			else {
				id.append("f");
			}

			for (XbrlExplicitMember explicitMember : segments) {
				id.append(XbrlMetamodel.PARTS_DELIMITER);
				id.append(explicitMember.getId());
			}

			for (XbrlExplicitMember explicitMember : scenarios) {
				id.append(XbrlMetamodel.PARTS_DELIMITER);
				id.append(explicitMember.getId());
			}

			return id.toString();
		}

		public XbrlContext build() {
			// Check for required fields
			if (entityScheme == null || entityName == null) {
				throw new IllegalStateException("Some mandatory fields are missing.");
			}

			XbrlContext metamodel = new XbrlContext();

			metamodel.entityScheme = entityScheme;
			metamodel.entityName = entityName;
			metamodel.periodStartDate = startDate;
			metamodel.periodEndDate = endDate;
			metamodel.scenarios = scenarios;
			metamodel.segments = segments;

			// If id is not set, then generate an id
			if (id == null) {
				id = buildId();
			}

			metamodel.id = id;

			return metamodel;
		}

	}
}
