package com.logicalis.br.sdc.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicalis.br.sdc.Converter;
import com.logicalis.br.sdc.model.Configuration;

/**
 * Convert from Excel spreadsheet to Word document, using Apache POI libraries.
 * 
 * @author Fabio De Santi
 *
 */
public class ConvertXLSToDOC implements Converter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String[] headers = null;

	public final String FROM = "xls";
	public final String TO = "doc";

	@Override
	public byte[] convert(Configuration config, byte[] source) throws Exception {

		XWPFDocument doc = new XWPFDocument();
		doc.createParagraph();
		XWPFTable docTable = null;
		XWPFTableRow docRow;
		XWPFTableCell docCell;
		XWPFParagraph paragraph;
		XWPFRun run;

		HSSFDataFormatter formatter = new HSSFDataFormatter();
		ByteArrayInputStream bais = new ByteArrayInputStream(source);
		HSSFWorkbook workbook = new HSSFWorkbook(bais);
		HSSFSheet sheet = workbook.getSheetAt(workbook.getFirstVisibleTab());
		HSSFCell cell;
		String string;
		int rows = sheet.getPhysicalNumberOfRows();
		logger.info("Found " + rows + " rows on workbook");
		for (int rIndex = 0; rIndex < rows; rIndex++) {
			HSSFRow row = sheet.getRow(rIndex);
			short cellCount = row.getLastCellNum();
			if (rIndex == 0) {
				docTable = doc.createTable(1, 2);
				docTable.setWidth(1440 * 6);
				docTable.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf((int) (1440 * 1.3)));
				docTable.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf((int) (1440 * 4.7)));
			}
			for (short cIndex = 0; cIndex < cellCount; cIndex++) {
				cell = row.getCell(cIndex);
				string = formatter.formatCellValue(cell);

				checkHeaders(cIndex, cellCount, string);

				// ignore header row
				if (rIndex > 0) {
					// populate word
					docRow = rIndex == 1 && cIndex == 0 ? docTable.getRow(0) : docTable.createRow();
					docCell = docRow.getCell(0);
					docCell.setColor("C0C0C0");
					docCell.setText(headers[cIndex]);

					String[] tokens = string.split("\n");
					docCell = docRow.getCell(1);
					if (tokens.length <= 1) {
						docCell.setText(string);
					} else {
						paragraph = docCell.addParagraph();

						run = paragraph.createRun();
						int i = 0;
						for (String s : tokens) {
							run.setText(s);
							if (++i < tokens.length)
								run.addBreak();
						}
						docCell.removeParagraph(0);
					}
				}
			}
			// add an empty row
			docRow = docTable.createRow();
			docRow.getCell(0).getCTTc().addNewTcPr().addNewGridSpan().setVal(BigInteger.valueOf(2));
		}
		workbook.close();

		doc.createParagraph();

		// get document's byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.write(baos);
		baos.flush();
		baos.close();
		doc.close();

		logger.info("Generated " + baos.size() + " bytes as Word document");

		return baos.toByteArray();
	}

	private void checkHeaders(short index, short count, String value) {
		if (headers == null)
			headers = new String[count];

		if (headers.length <= index) {
			String[] newHeaders = new String[index + 1];
			for (int i = 0; i < headers.length; i++)
				newHeaders[i] = headers[i];
			headers = newHeaders;
		}
		if (headers[index] == null)
			headers[index] = value;
	}
}
