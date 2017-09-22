package com.nortal.xbrl.metamodel.meta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "calculation")
public class CalculationEntry extends AbstractReportEntry implements LinkedEntry, Serializable {
	private static final long serialVersionUID = 6986943071881557774L;

	public enum Balance {
		DEBIT, CREDIT;

		public static Balance fromString(String balance) {
			return balance == null ? null : valueOf(balance.toUpperCase());
		}
	}

	private List<CalculationEntry> children;
	private Balance balance;
	private BigDecimal weight;

	public CalculationEntry() {
		super();
		children = new ArrayList<CalculationEntry>();
	}

	@XmlAttribute(name = "balance")
	public Balance getBalance() {
		return balance;
	}

	public void setBalance(Balance balance) {
		this.balance = balance;
	}

	@XmlAttribute(name = "weight")
	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	@XmlElementWrapper(name = "children")
	@XmlElement(name = "entry")
	public List<CalculationEntry> getChildren() {
		return children;
	}

	public void setChildren(List<CalculationEntry> children) {
		this.children = children;
	}

	public LinkedEntry getChild(LinkedEntry linkedEntry) {
		for (CalculationEntry calculationEntry : children) {
			if (calculationEntry.isLinked(linkedEntry)) {
				return calculationEntry;
			}
		}

		return null;
	}

	public boolean isLinked(LinkedEntry linkedEntry) {
		return getNamespace().equals(linkedEntry.getNamespace()) && getName().equals(linkedEntry.getName());
	}

	@Override
	public String toString() {
		return "CalculationEntry{" +
				"children=" + children +
				", balance=" + balance +
				", weight=" + weight +
				'}';
	}

	public static class Builder {

		private String name;
		private String namespace;
		private String namespacePrefix;
		private Double order;
		private Integer level;

		private List<CalculationEntry> children;
		private Balance balance;
		private BigDecimal weight;

		public Builder() {
			this.children = new ArrayList<CalculationEntry>();
		}

		public Builder setName(String name) {
			this.name = name;

			return this;
		}

		public Builder setNamespace(String namespace) {
			this.namespace = namespace;

			return this;
		}

		public Builder setNamespacePrefix(String namespacePrefix) {
			this.namespacePrefix = namespacePrefix;

			return this;
		}

		public Builder setOrder(Double order) {
			this.order = order;

			return this;
		}

		public Builder setLevel(Integer level) {
			this.level = level;

			return this;
		}

		public Builder addChild(CalculationEntry child) {
			this.children.add(child);

			return this;
		}

		public Builder setBalance(Balance balance) {
			this.balance = balance;

			return this;
		}

		public Builder setWeight(BigDecimal weight) {
			this.weight = weight;

			return this;
		}

		public CalculationEntry build() {
			CalculationEntry metamodel = new CalculationEntry();

			metamodel.setNamespace(namespace);
			metamodel.setNamespacePrefix(namespacePrefix);
			metamodel.setName(name);
			metamodel.setOrder(order);
			metamodel.setLevel(level);

			metamodel.setChildren(children);
			metamodel.setBalance(balance);
			metamodel.setWeight(weight);

			return metamodel;
		}

	}

}
