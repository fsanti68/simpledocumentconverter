package com.logicalis.br.sdc.converters;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicalis.br.sdc.Converter;
import com.logicalis.br.sdc.model.Configuration;

/**
 * Convert from Excel spreadsheet to a text document, using Apache POI
 * libraries.
 * 
 * @author Fabio De Santi
 *
 */
public class ConvertXLSToTXT implements Converter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public final String FROM = "xls";
	public final String TO = "html";

	@Override
	public byte[] convert(Configuration config, byte[] source) throws Exception {

		CharArrayWriter writer = new CharArrayWriter();
		int maxLineSize = 0;
		int[] colSize = null;
		int[] rowSize = null;

		HSSFDataFormatter formatter = new HSSFDataFormatter();
		ByteArrayInputStream bais = new ByteArrayInputStream(source);
		HSSFWorkbook workbook = new HSSFWorkbook(bais);
		HSSFSheet sheet = workbook.getSheetAt(workbook.getFirstVisibleTab());
		HSSFCell cell;
		int rows = sheet.getPhysicalNumberOfRows();
		logger.info("Found " + rows + " rows on workbook");

		// verifica o tamanho máximo de cada coluna
		short cellCount = 0;
		char[] cellData;
		for (int rIndex = 0; rIndex < rows; rIndex++) {
			if (rowSize == null) {
				rowSize = new int[rows];
				Arrays.fill(rowSize, 0);
			}
			HSSFRow row = sheet.getRow(rIndex);
			cellCount = row.getLastCellNum();
			if (colSize == null) {
				colSize = new int[cellCount];
				Arrays.fill(colSize, 0);
			}
			for (short cIndex = 0; cIndex < cellCount; cIndex++) {
				cell = row.getCell(cIndex);
				String[] data = formatter.formatCellValue(cell).split("\n");
				for (String s : data) {
					cellData = s.toCharArray();
					if (cellData.length > colSize[cIndex])
						colSize[cIndex] = cellData.length;
				}

				if (rowSize[rIndex] < data.length)
					rowSize[rIndex] = data.length;

				int lineSize = Arrays.stream(colSize).sum();
				if (lineSize > maxLineSize)
					maxLineSize = lineSize;
			}
		}
		maxLineSize += (cellCount + 1) * 2;

		char[][] line;
		int colPos;
		for (int rIndex = 0; rIndex < rows; rIndex++) {
			HSSFRow row = sheet.getRow(rIndex);
			cellCount = row.getLastCellNum();

			line = new char[rowSize[rIndex]][maxLineSize];
			for (char[] a : line)
				Arrays.fill(a, ' ');

			colPos = 2;
			for (short cIndex = 0; cIndex < cellCount; cIndex++) {
				cell = row.getCell(cIndex);
				String[] data = formatter.formatCellValue(cell).split("\n");
				for (int overflow = 0; overflow < data.length; overflow++) {
					cellData = data[overflow].toCharArray();
					System.arraycopy(cellData, 0, line[overflow], colPos, cellData.length);
				}

				colPos += colSize[cIndex] + 2;
			}

			for (char[] b : line) {
				writer.write(b);
				writer.write('\n');
			}
		}
		workbook.close();

		byte[] bytes = writer.toString().getBytes();
		writer.close();

		logger.info("Generated " + bytes.length + " bytes as a text document");

		return bytes;
	}
}
