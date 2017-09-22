package com.nortal.xbrl.metamodel.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.nortal.xbrl.metamodel.meta.DimensionEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;

public class MetamodelUtil {

	public static OutputStream serialize(XbrlMetamodel model) throws JAXBException {
		Marshaller marshaller = getJaxbContext().createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		OutputStream buffer = new ByteArrayOutputStream();
		marshaller.marshal(model, buffer);

		return buffer;
	}

	public static XbrlMetamodel deserialize(InputStream inputStream, MetamodelUnmarshaller unmarshaller)
			throws JAXBException {
		Unmarshaller m = getJaxbContext().createUnmarshaller();
		m.setListener(unmarshaller);

		return (XbrlMetamodel) m.unmarshal(inputStream);
	}

	public static List<DimensionEntry> getAxisDomainMembers(DimensionEntry dimension) {
		List<DimensionEntry> domainMembers = new ArrayList<DimensionEntry>();

		if (dimension.getArcRole() == DimensionEntry.ArcRole.DOMAIN_MEMBER) {
			domainMembers.add(dimension);
		}

		for (DimensionEntry member : dimension.getChildren()) {
			if (member.getArcRole() == DimensionEntry.ArcRole.DOMAIN_MEMBER
					|| member.getArcRole() == DimensionEntry.ArcRole.DIMENSION_DOMAIN) {
				domainMembers.add(member);
			}

			for (DimensionEntry child : member.getChildren()) {
				domainMembers.addAll(getAxisDomainMembers(child));
			}
		}

		return domainMembers;
	}

	public static List<DimensionEntry> getDimensionDomains(DimensionEntry dimension) {
		List<DimensionEntry> dimensionDomains = new ArrayList<DimensionEntry>();

		if (dimension.getArcRole() == DimensionEntry.ArcRole.DIMENSION_DOMAIN) {
			dimensionDomains.add(dimension);
		}

		for (DimensionEntry member : dimension.getChildren()) {
			dimensionDomains.addAll(getDimensionDomains(member));
		}

		return dimensionDomains;
	}

	public static List<DimensionEntry> getLineItems(DimensionEntry dimension) {
		List<DimensionEntry> lineItems = new ArrayList<DimensionEntry>();

		for (DimensionEntry dimensionEntry : dimension.getChildren()) {
			if (!dimensionEntry.isAbstract()
					&& dimensionEntry.getArcRole() == DimensionEntry.ArcRole.DOMAIN_MEMBER) {
				lineItems.add(dimensionEntry);
			}

			// Do not process children of AXIS
			if (dimensionEntry.getArcRole() == DimensionEntry.ArcRole.DOMAIN_MEMBER
					&& !dimensionEntry.getChildren().isEmpty()) {
				lineItems.addAll(getLineItems(dimensionEntry));
			}
		}

		return lineItems;
	}

	public static List<PresentationEntry> getLineItems(PresentationEntry presentation) {
		List<PresentationEntry> lineItems = new ArrayList<PresentationEntry>();

		for (PresentationEntry presentationEntry : presentation.getChildren()) {
			if (presentationEntry.getType() == PresentationEntry.PresentationType.MONETARY
					|| presentationEntry.getType() == PresentationEntry.PresentationType.PER_SHARE) {
				lineItems.add(presentationEntry);
			}

			if (!presentationEntry.getChildren().isEmpty()) {
				lineItems.addAll(getLineItems(presentationEntry));
			}
		}

		return lineItems;
	}

	private static JAXBContext getJaxbContext() throws JAXBException {
		return JAXBContext.newInstance(XbrlMetamodel.class);
	}
}
