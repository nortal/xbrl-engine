package com.nortal.xblr.metamodel.postprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class InsertTranslationsFromExcelToXBRL implements Constants {

	final static String labelPrefix = "<link:label ";

	public static void main(String[] args) throws Exception {
		HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(excelFilename));

		Sheet sheet = book.getSheetAt(0);

		List<ExcelEntry> excelEntries = getExcelEntries(sheet);

		Map<String, DocumentationEntry> documentationEntries = getDocumentationEntries();

		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("doc_full_ifrs-om_2014-03-05.xml"),
				"UTF-8"));
		try {
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<link:linkbase xml:lang=\"om\" xmlns:link=\"http://www.xbrl.org/2003/linkbase\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.xbrl.org/2003/linkbase http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd\">\n"
					+ "  <link:labelLink xlink:role=\"http://www.xbrl.org/2003/role/link\" xlink:type=\"extended\">\n");

			writeTranslatedDocumentationEntries(excelEntries, documentationEntries, out);

			out.write("  </link:labelLink>\n" + "</link:linkbase>\n");
		}
		finally {
			out.close();
		}

	}

	private static List<ExcelEntry> getExcelEntries(Sheet sheet) {
		List<ExcelEntry> excelEntries = new ArrayList<>();
		int i = 1;
		while (sheet.getRow(++i) != null) {
			Row row = sheet.getRow(i);

			// took "translation is missing" part away, should fail if some translation is missing
			// row.getCell(4) != null ? row.getCell(4).getStringCellValue() :
			// "Arabic translation in Oman language is missing"

			excelEntries.add(new ExcelEntry(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(),
					row.getCell(2).getStringCellValue(), row.getCell(3).getStringCellValue(), row.getCell(4)
							.getStringCellValue()));
		}
		return excelEntries;
	}

	private static Map<String, DocumentationEntry> getDocumentationEntries() throws IOException {
		Map<String, DocumentationEntry> documentationEntries = new HashMap<>();

		for (String documentationFilename : documentationFilenames) {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(documentationFilename),
					"UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().startsWith(labelPrefix)) {
					DocumentationEntry entry = new DocumentationEntry();
					entry.label = line.substring(0, line.indexOf('>') + 1);
					entry.locator = br.readLine();
					entry.arc = br.readLine();

					String name = entry.locator.split("#")[1].split("\"")[0].split("_")[1];
					documentationEntries.put(name, entry);
				}
			}
			br.close();
		}
		return documentationEntries;
	}

	private static void writeTranslatedDocumentationEntries(Collection<ExcelEntry> excelEntries,
			Map<String, DocumentationEntry> documentationEntries, Writer out) throws Exception {
		for (ExcelEntry excelEntry : excelEntries) {
			DocumentationEntry entry = documentationEntries.get(excelEntry.name);
			if (entry == null) {
				System.out.println(excelEntry.name + " English documentation is missing, skipped this entry");
				continue;
			}
			documentationEntries.remove(excelEntry.name);

			out.write(entry.locator + '\n');
			out.write(entry.label.replaceAll("lang=\"en\"", "lang=\"om\"") + excelEntry.documentationArabic
					+ "</link:label>\n");
			out.write(entry.arc + '\n');
		}
	}

}
