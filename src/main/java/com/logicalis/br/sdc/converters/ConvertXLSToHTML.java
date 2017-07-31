package com.logicalis.br.sdc.converters;

import java.io.ByteArrayInputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicalis.br.sdc.Converter;
import com.logicalis.br.sdc.model.Configuration;

/**
 * Convert from Excel spreadsheet to Html document, using Apache POI libraries.
 * 
 * @author Fabio De Santi
 *
 */
public class ConvertXLSToHTML implements Converter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public final String FROM = "xls";
	public final String TO = "html";

	public final String DEFAULT_HEADER = "<html><head></head><body>";

	@Override
	public byte[] convert(Configuration config, byte[] source) throws Exception {

		String htmlHeader = config.getConverter().get("html-header");
		if (htmlHeader == null || htmlHeader.isEmpty())
			htmlHeader = DEFAULT_HEADER;
		StringBuilder html = new StringBuilder(htmlHeader);

		HSSFDataFormatter formatter = new HSSFDataFormatter();
		ByteArrayInputStream bais = new ByteArrayInputStream(source);
		HSSFWorkbook workbook = new HSSFWorkbook(bais);
		HSSFSheet sheet = workbook.getSheetAt(workbook.getFirstVisibleTab());
		HSSFCell cell;
		String string;
		int rows = sheet.getPhysicalNumberOfRows();
		logger.info("Found " + rows + " rows on workbook");
		html.append("<table>");
		for (int rIndex = 0; rIndex < rows; rIndex++) {
			boolean isHead = rIndex == 0;
			HSSFRow row = sheet.getRow(rIndex);
			short cellCount = row.getLastCellNum();

			html.append(isHead ? "<thead>" : "").append("<tr>");
			for (short cIndex = 0; cIndex < cellCount; cIndex++) {
				cell = row.getCell(cIndex);
				string = formatter.formatCellValue(cell).replaceAll("\n", "<br/>");

				boolean isString = rIndex == 0 || CellType.STRING.equals(cell.getCellTypeEnum());

				html.append(isHead ? "<th" : "<td").append(isString ? "" : " nowrap").append('>').append(string)
						.append(isHead ? "</th>" : "</td>");
			}
			html.append("</tr>").append(isHead ? "</thead><tbody>" : "");
		}
		workbook.close();

		html.append("</tbody></table>");

		html.append("</body></html>");

		byte[] bytes = html.toString().getBytes();

		logger.info("Generated " + bytes.length + " bytes as HTML document");

		return bytes;
	}
}
