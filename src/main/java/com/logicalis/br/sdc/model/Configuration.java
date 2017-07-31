package com.logicalis.br.sdc.model;

import java.util.Map;

/**
 * Data transfer object for the application's configurations.
 * 
 * @author Fabio De Santi
 *
 */
public final class Configuration {

	private Map<String, String> converter;

	/**
	 * 
	 * @return
	 */
	public Map<String, String> getConverter() {
		return converter;
	}

	/**
	 * 
	 * @param converter
	 */
	public void setConverter(Map<String, String> converter) {
		this.converter = converter;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
		sb.append(" [converter=");
		if (converter != null) {
			converter.entrySet().stream()
					.forEach(e -> sb.append("\n\t\t\t").append(e.getKey()).append('=').append(e.getValue()));
			sb.append("\n");
		} else
			sb.append("\t\t\tnull");

		return sb.append(']').toString();
	}
}
