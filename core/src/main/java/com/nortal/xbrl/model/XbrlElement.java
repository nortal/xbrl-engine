package com.nortal.xbrl.model;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class XbrlElement {

	private String namespace;
	private String name;
	private String description;
	private Class<?> returnType;
	private Class<?> actualReturnType;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	public Class<?> getActualReturnType() {
		return actualReturnType;
	}

	public void setActualReturnType(Class<?> actualReturnType) {
		this.actualReturnType = actualReturnType;
	}

	public QName getQName() {
		return new QName(namespace, name);
	}

	public boolean isJAXBElement() {
		return returnType.isAssignableFrom(JAXBElement.class);
	}

}
