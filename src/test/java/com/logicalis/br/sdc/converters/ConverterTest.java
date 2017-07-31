package com.logicalis.br.sdc.converters;

import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import com.logicalis.br.sdc.model.Configuration;

public abstract class ConverterTest {

	Configuration getConfig() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/converter-config.yml");
		try {
			Yaml yaml = new Yaml();
			return yaml.loadAs(in, Configuration.class);

		} finally {
			in.close();
		}
	}
}
