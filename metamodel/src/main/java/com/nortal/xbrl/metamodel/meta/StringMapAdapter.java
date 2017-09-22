package com.nortal.xbrl.metamodel.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringMapAdapter extends XmlAdapter<StringMapAdapter.StringMap, Map<String, String>> {

	@Override
	public StringMap marshal(Map<String, String> arg0) throws Exception {
		StringMap StringMap = new StringMap();
		for (Map.Entry<String, String> entry : arg0.entrySet()) {
			StringMapEntry StringMapEntry = new StringMapEntry();
			StringMapEntry.key = entry.getKey();
			StringMapEntry.value = entry.getValue();
			StringMap.entry.add(StringMapEntry);
		}
		return StringMap;
	}

	@Override
	public Map<String, String> unmarshal(StringMap arg0) throws Exception {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		for (StringMapEntry myEntryType : arg0.entry) {
			hashMap.put(myEntryType.key, myEntryType.value);
		}
		return hashMap;
	}

	public static final class StringMap {

		public List<StringMapEntry> entry = new ArrayList<StringMapEntry>();

	}

	public static final class StringMapEntry {

		@XmlAttribute
		public String key;

		@XmlValue
		public String value;

	}

}