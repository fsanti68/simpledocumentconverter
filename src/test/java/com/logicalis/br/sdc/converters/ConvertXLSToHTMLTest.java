package com.logicalis.br.sdc.converters;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import com.logicalis.br.sdc.Converter;
import com.logicalis.br.sdc.converters.ConvertXLSToHTML;

public class ConvertXLSToHTMLTest extends ConverterTest {

	@Test
	public void testConvert() throws Exception {

		InputStream in = this.getClass().getResourceAsStream("/incident_xls.xls");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[4098];
		int len = 0;
		while ((len = in.read(b)) > 0) {
			baos.write(b, 0, len);
		}
		in.close();

		Converter converter = new ConvertXLSToHTML();
		byte[] doc = converter.convert(getConfig(), baos.toByteArray());

		File f = new File("/tmp/generated_from_xls.html");
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(doc);
		fos.close();

		Document html = Jsoup.parse(f, "UTF-8");
		Element element = html.select("th").first();
		assertEquals(element.text(), "Número");

		f.deleteOnExit();
	}
}
