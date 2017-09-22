package com.nortal.xbrl.metamodel;

import java.io.Serializable;

public class XbrlUnit implements Serializable {

	private static final long serialVersionUID = -1601220248950791316L;
	protected String id;
	protected String measure;
	protected String unitNumeratorMeasure;
	protected String unitDenominatorMeasure;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	@Override
	public String toString() {
		return "UnitModel{" + "id='" + id + '\'' + ", measure='" + measure + '\'' + '}';
	}

	public String getUnitNumeratorMeasure() {
		return unitNumeratorMeasure;
	}

	public void setUnitNumeratorMeasure(String unitNumeratorMeasure) {
		this.unitNumeratorMeasure = unitNumeratorMeasure;
	}

	public String getUnitDenominatorMeasure() {
		return unitDenominatorMeasure;
	}

	public void setUnitDenominatorMeasure(String unitDenominatorMeasure) {
		this.unitDenominatorMeasure = unitDenominatorMeasure;
	}

	public static class Builder {

		private String id;
		private String measure;
		private String unitNumeratorMeasure;
		private String unitDenominatorMeasure;

		public Builder() {

		}

		public Builder(String id, String measure, String unitNumeratorMeasure, String unitDenominatorMeasure) {
			this();

			this.id = id;
			this.measure = measure;
			this.unitNumeratorMeasure = unitNumeratorMeasure;
			this.unitDenominatorMeasure = unitDenominatorMeasure;
		}

		public Builder setId(String id) {
			this.id = id;

			return this;
		}

		public Builder setMeasure(String measure) {
			this.measure = measure;

			return this;
		}

		public Builder setUnitNumeratorMeasure(String unitNumeratorMeasure) {
			this.unitNumeratorMeasure = unitNumeratorMeasure;

			return this;
		}


		public Builder setUnitDenominatorMeasure(String unitDenominatorMeasure) {
			this.unitDenominatorMeasure = unitDenominatorMeasure;

			return this;
		}

		public String buildId() {
			if(measure == null) {
				return unitNumeratorMeasure + "perShare";
			}
			return measure;
		}

		public XbrlUnit build() {
			// Check for required fields
			if (!(measure != null || (unitNumeratorMeasure != null && unitDenominatorMeasure != null))) {
				throw new IllegalStateException("Some mandatory fields are missing.");
			}

			XbrlUnit metamodel = new XbrlUnit();
			metamodel.setMeasure(measure);
			metamodel.setUnitNumeratorMeasure(unitNumeratorMeasure);
			metamodel.setUnitDenominatorMeasure(unitDenominatorMeasure);


			// If id is not set, then generate an id
			if (id == null) {
				id = buildId();
			}

			metamodel.setId(id);

			return metamodel;
		}
	}
}