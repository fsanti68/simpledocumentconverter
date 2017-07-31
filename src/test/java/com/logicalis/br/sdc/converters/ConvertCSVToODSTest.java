package com.logicalis.br.sdc.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Test;

import com.logicalis.br.sdc.Converter;
import com.logicalis.br.sdc.converters.ConvertCSVToODS;

public class ConvertCSVToODSTest extends ConverterTest {

	@Test
	public void testConvert() throws Exception {

		InputStream in = this.getClass().getResourceAsStream("/incident_csv.csv");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[4098];
		int len = 0;
		while ((len = in.read(b)) > 0) {
			baos.write(b, 0, len);
		}
		in.close();

		Converter converter = new ConvertCSVToODS();
		byte[] doc = converter.convert(getConfig(), baos.toByteArray());

		File f = new File("/tmp/generated_from_csv.ods");
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(doc);
		fos.close();
	}
}
