package com.nortal.xbrl.metamodel.meta;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "dimension")
public class DimensionEntry extends AbstractReportEntry implements LinkedEntry, Serializable {
	private static final long serialVersionUID = 6437112698073065543L;

	public enum ArcRole {
		DEFAULT_DIMENSION("http://xbrl.org/int/dim/arcrole/dimension-default"), ALL(
				"http://xbrl.org/int/dim/arcrole/all"), NOT_ALL("http://xbrl.org/int/dim/arcrole/notAll"), DOMAIN_MEMBER(
				"http://xbrl.org/int/dim/arcrole/domain-member"), DIMENSION_DOMAIN(
				"http://xbrl.org/int/dim/arcrole/dimension-domain"), HYPERCUBE_DIMENSION(
				"http://xbrl.org/int/dim/arcrole/hypercube-dimension");

		String name;

		ArcRole(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static ArcRole fromString(String type) {
			for (ArcRole enumType : values()) {
				if (enumType.getName().equals(type)) {
					return enumType;
				}
			}
			throw new IllegalArgumentException("Unable to resolve arc role for: " + type);
		}
	}

	private ArcRole arcRole;
	private boolean isClosed = false;
	private boolean isUsable = true;
	private boolean isAbstract;
	private String contextElement;

	private DimensionEntry parent;
	private List<DimensionEntry> children;

	public DimensionEntry() {
		super();
		children = new ArrayList<DimensionEntry>() {

            private static final long serialVersionUID = -5045320431977493283L;

            @Override
			public boolean add(DimensionEntry element) {
				element.setParent(DimensionEntry.this);
				return super.add(element);
			}
		};
	}

	@XmlAttribute(name = "arcRole")
	public ArcRole getArcRole() {
		return arcRole;
	}

	public void setArcRole(ArcRole arcRole) {
		this.arcRole = arcRole;
	}

	@XmlAttribute(name = "closed")
	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	@XmlAttribute(name = "usable")
	public boolean isUsable() {
		return isUsable;
	}

	public void setUsable(boolean isUsable) {
		this.isUsable = isUsable;
	}

	@XmlAttribute(name = "abstract")
	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	@XmlAttribute(name = "contextElement")
	public String getContextElement() {
		return contextElement;
	}

	public void setContextElement(String contextElement) {
		this.contextElement = contextElement;
	}

	@XmlTransient
	public DimensionEntry getParent() {
		return parent;
	}

	public void setParent(DimensionEntry parent) {
		this.parent = parent;
	}

	@XmlElementWrapper(name = "children")
	@XmlElement(name = "entry")
	public List<DimensionEntry> getChildren() {
		return children;
	}

	public void setChildren(List<DimensionEntry> children) {
		this.children = children;
	}

	public LinkedEntry getChild(LinkedEntry linkedEntry) {
		for (DimensionEntry dimensionEntry : children) {
			if (dimensionEntry.isLinked(linkedEntry)) {
				return dimensionEntry;
			}
		}

		return null;
	}

	public boolean isLinked(LinkedEntry linkedEntry) {
		return getNamespace().equals(linkedEntry.getNamespace()) && getName().equals(linkedEntry.getName());
	}

	public int getElementCount() {
		int count = 1;
		for (DimensionEntry child : children) {
			count += child.getElementCount();
		}
		return count;
	}

	public int getDomainElementCount() {
		int count = (arcRole == ArcRole.DIMENSION_DOMAIN || arcRole == ArcRole.DOMAIN_MEMBER) ? 1 : 0;
		for (DimensionEntry child : children) {
			count += child.getDomainElementCount();
		}
		return count;
	}

	public int getMaxLevel() {
		return getMaxLevel(children);
	}

	private int getMaxLevel(List<DimensionEntry> children) {
		int maxDepth = Integer.MIN_VALUE;

		for (DimensionEntry child : children) {
			if (!child.getChildren().isEmpty()) {
				maxDepth = getMaxLevel(child.getChildren());
			}
			else if (maxDepth < child.getLevel()) {
				maxDepth = child.getLevel();
			}
		}

		return maxDepth;
	}

	public DimensionEntry getAxis() {
		if (getArcRole().equals(ArcRole.HYPERCUBE_DIMENSION)) {
			return this;
		}

		return (getParent() == null ? null : getParent().getAxis());
	}

}