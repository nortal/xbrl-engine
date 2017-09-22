package com.nortal.xbrl.metamodel.meta;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Reference implements Serializable {

	private static final long serialVersionUID = 78295891085981135L;

	@XmlAttribute(name = "label")
	private String label;

	@XmlElement
	@XmlJavaTypeAdapter(StringMapAdapter.class)
	private Map<String, String> data;

	public Reference() {

	}

	public Reference(String label, Map<String, String> data) {
		this.label = label;
		this.data = data;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return data.get("name");
	}

	public String getNumber() {
		return data.get("number");
	}

	public Date getIssueDate() {
		try {
			return XbrlMetamodel.DATE_FORMAT.get().parse(data.get("issuedate"));
		}
		catch (ParseException e) {
		}
		return null;
	}

	public String getPage() {
		return data.get("page");
	}

	public String getParagraph() {
		return data.get("paragraph");
	}

	public String getSubParagraph() {
		return data.get("subparagraph");
	}

	public String getSection() {
		return data.get("section");
	}

	public String getSubSection() {
		return data.get("subsection");
	}

	public String getClause() {
		return data.get("clause");
	}

	public String getSubClause() {
		return data.get("subclause");
	}

	public String getArticle() {
		return data.get("article");
	}

	public String getChapter() {
		return data.get("chapter");
	}

	public String getSentence() {
		return data.get("sentence");
	}

	public String getAppendix() {
		return data.get("appendix");
	}

	public String getFootnote() {
		return data.get("footnote");
	}

	public String getExhibit() {
		return data.get("exhibit");
	}

	public String getExample() {
		return data.get("example");
	}

	public String getNote() {
		return data.get("note");
	}

	public String getUri() {
		return data.get("uri");
	}

	public Date getUriDate() {
		try {
			return XbrlMetamodel.DATE_FORMAT.get().parse(data.get("uridate"));
		}
		catch (ParseException e) {
		}
		return null;
	}

}
