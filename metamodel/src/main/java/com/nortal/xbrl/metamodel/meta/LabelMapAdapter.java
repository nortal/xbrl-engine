package com.nortal.xbrl.metamodel.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LabelMapAdapter extends XmlAdapter<LabelMapAdapter.LabelMap, Map<String, LabelEntry>> {

	@Override
	public LabelMap marshal(Map<String, LabelEntry> arg0) throws Exception {
		LabelMap LabelMap = new LabelMap();
		for (Map.Entry<String, LabelEntry> entry : arg0.entrySet()) {
			LabelMapEntry labelMapEntry = new LabelMapEntry();
			labelMapEntry.key = entry.getKey();

			for (Map.Entry<LabelEntry.LabelType, String> label : entry.getValue().getLabels().entrySet()) {
				LabelEntryMapEntry labelEntryMapEntry = new LabelEntryMapEntry();
				labelEntryMapEntry.key = label.getKey();
				labelEntryMapEntry.value = label.getValue();
				labelMapEntry.entry.add(labelEntryMapEntry);
			}

			LabelMap.entry.add(labelMapEntry);
		}
		return LabelMap;
	}

	@Override
	public Map<String, LabelEntry> unmarshal(LabelMap arg0) throws Exception {
		HashMap<String, LabelEntry> hashMap = new HashMap<String, LabelEntry>();
		for (LabelMapEntry myEntryType : arg0.entry) {
			LabelEntry labelEntry = new LabelEntry();
			for (LabelEntryMapEntry mapEntry : myEntryType.entry) {
				labelEntry.put(mapEntry.key, mapEntry.value);
			}
			hashMap.put(myEntryType.key, labelEntry);
		}
		return hashMap;
	}

	public static final class LabelMap {

		@XmlElement(name = "language")
		public List<LabelMapEntry> entry = new ArrayList<LabelMapEntry>();

	}

	public static final class LabelMapEntry {

		@XmlAttribute
		public String key;

		@XmlElement(name = "label")
		public List<LabelEntryMapEntry> entry = new ArrayList<LabelEntryMapEntry>();

	}

	public static final class LabelEntryMapEntry {

		@XmlAttribute
		public LabelEntry.LabelType key;

		@XmlValue
		public String value;

	}

}