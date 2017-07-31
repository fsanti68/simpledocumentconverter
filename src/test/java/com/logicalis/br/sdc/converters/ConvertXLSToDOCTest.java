package com.logicalis.br.sdc.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Test;

import com.logicalis.br.sdc.Converter;
import com.logicalis.br.sdc.converters.ConvertXLSToDOC;

public class ConvertXLSToDOCTest extends ConverterTest {

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

		Converter converter = new ConvertXLSToDOC();
		byte[] doc = converter.convert(null, baos.toByteArray());

		File f = new File("/tmp/generated_from_xls.doc");
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(doc);
		fos.close();
	}
}
