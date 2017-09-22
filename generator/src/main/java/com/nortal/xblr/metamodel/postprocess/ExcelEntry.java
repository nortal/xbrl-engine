package com.nortal.xblr.metamodel.postprocess;

public class ExcelEntry {

	String name;
	String labelEnglish;
	String labelArabic;
	String documentationEnglish;
	String documentationArabic;

	public ExcelEntry(String name, String labelEnglish, String labelArabic, String documentationEnglish) {
		this.name = name;
		this.labelEnglish = labelEnglish;
		this.labelArabic = labelArabic;
		this.documentationEnglish = documentationEnglish;
	}

	public ExcelEntry(String name, String labelEnglish, String labelArabic, String documentationEnglish,
			String documentationArabic) {
		this.name = name;
		this.labelEnglish = labelEnglish;
		this.labelArabic = labelArabic;
		this.documentationEnglish = documentationEnglish;
		this.documentationArabic = documentationArabic;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ExcelEntry entry = (ExcelEntry) o;

		if (name != null ? !name.equals(entry.name) : entry.name != null)
			return false;
		if (labelEnglish != null ? !labelEnglish.equals(entry.labelEnglish) : entry.labelEnglish != null)
			return false;
		if (labelArabic != null ? !labelArabic.equals(entry.labelArabic) : entry.labelArabic != null)
			return false;
		return !(documentationEnglish != null ? !documentationEnglish.equals(entry.documentationEnglish)
				: entry.documentationEnglish != null);

	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (labelEnglish != null ? labelEnglish.hashCode() : 0);
		result = 31 * result + (labelArabic != null ? labelArabic.hashCode() : 0);
		result = 31 * result + (documentationEnglish != null ? documentationEnglish.hashCode() : 0);
		return result;
	}
}
