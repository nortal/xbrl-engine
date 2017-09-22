package com.nortal.xblr.metamodel.postprocess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExtractDocumentationLabelsFromMetamodelToExcel implements Constants {

	final static Set<ExcelEntry> metamodelEntries = new HashSet<>();

	final static String documentationLabelPrefix = "<label key=\"DOCUMENTATION\">";
	final static String suffix = "</label>";
	final static String defaultLabelPrefix = "<label key=\"DEFAULT\">";

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(metamodelFilename), "UTF-8"));
		String line, lastEntry = "";
		while ((line = br.readLine()) != null) {
			if (line.trim().startsWith(defaultLabelPrefix)) {
				if (lastEntry.contains("abstract=\"true\""))
					continue;

				String name = lastEntry.substring(lastEntry.indexOf("name=\"") + "name=\"".length());
				name = name.substring(0, name.indexOf("\""));

				String labelEnglish = line.trim().substring(defaultLabelPrefix.length(), line.trim().indexOf(suffix));

				line = br.readLine();
				String documentation = line.trim().startsWith(documentationLabelPrefix) ? line.trim().substring(
						documentationLabelPrefix.length(), line.trim().indexOf(suffix)) : "";

				while (!(line = br.readLine().trim()).contains(defaultLabelPrefix));
				String labelArabic = line.substring(defaultLabelPrefix.length(), line.indexOf(suffix));

				metamodelEntries.add(new ExcelEntry(name, labelEnglish, labelArabic, documentation));
			}
			else if (line.contains("<entry")) {
				lastEntry = line.trim();
			}
		}
		br.close();

		HSSFWorkbook book = new HSSFWorkbook();
		Sheet sheet = book.createSheet("Translations");

		int i = 0;
		Row header = sheet.createRow(i++);
		header.createCell(0).setCellValue("XBRL tag");
		header.createCell(1).setCellValue("English Label");
		header.createCell(2).setCellValue("Arabic Label");
		header.createCell(3).setCellValue("English Documentation");
		header.createCell(4).setCellValue("Arabic Documentation");

		for (ExcelEntry entry : metamodelEntries) {
			Row row = sheet.createRow(i++);
			row.createCell(0).setCellValue(entry.name);
			row.createCell(1).setCellValue(entry.labelEnglish);
			row.createCell(2).setCellValue(entry.labelArabic);
			row.createCell(3).setCellValue(entry.documentationEnglish);
		}
		book.close();

		book.write(new FileOutputStream(excelFilename));
		System.out.println("Success");
	}
}
