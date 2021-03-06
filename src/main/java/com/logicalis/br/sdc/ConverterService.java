package com.logicalis.br.sdc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import com.logicalis.br.sdc.model.Configuration;

/**
 * Implements the converter API.
 * 
 * @author Fabio De Santi
 */
@RestController
public class ConverterService implements ApplicationContextAware {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final long CONFIG_EXPIRATION_MILLIS = 60000L;

	private static final Map<String, Converter> converters = new HashMap<>();

	private Configuration _config;
	private long _configExpiresAt = 0L;

	private ApplicationContext ctx;

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx;
	}

	/**
	 * Get configuration data from (in this order):
	 * <ul>
	 * <li>config.yml (located at application running path)</li>
	 * <li>packaged converter-config.yml (see src/main/resources)</li>
	 * </ul>
	 * 
	 * @return {@link Configuration}
	 */
	private Configuration getConfig() throws RuntimeException {

		final String[] sources = new String[] { "file:config.yml", "classpath:converter-config.yml" };

		if (_config != null && System.currentTimeMillis() < _configExpiresAt)
			return _config;

		if (ctx != null) {
			Yaml yaml = new Yaml();
			Resource res = null;
			for (String source : sources) {
				try {
					res = ctx.getResource(source);
					_config = yaml.loadAs(res.getInputStream(), Configuration.class);

					logger.info("Got configuration (" + source + "): " + _config.toString());
					_configExpiresAt = System.currentTimeMillis() + CONFIG_EXPIRATION_MILLIS;

					return _config;

				} catch (Exception e) {
					logger.warn("\"" + source + "\" unaccessible (" + e.getMessage() + ")");
				}
			}
			if (_config == null)
				throw new RuntimeException("Failed to get service configuration");
		}
		return null;
	}

	/**
	 * Executes one of the following convertions:
	 * <ul>
	 * <li>CSV -&gt; HTML</li>
	 * <li>CSV -&gt; ODS (csv to Calc8)</li>
	 * <li>CSV -&gt; XLS (csv to Excel)</li>
	 * <li>XLS -&gt; DOC (Excel to Word)</li>
	 * <li>XLS -&gt; HTML</li>
	 * <li>XLS -&gt; ODS (Excel to Calc8)</li>
	 * <li>XLS -&gt; TXT (Excel to text file)</li>
	 * <li>DOC -&gt; ODT (Word to Write8)</li>
	 * </ul>
	 * 
	 * @param from
	 *            a valid source format
	 * @param to
	 *            a valid destination format
	 * @param body
	 *            the BASE64 encoded source
	 * 
	 * @return a {@link ResponseBody} object containing the base64 response and
	 *         http code 200 (or any other http code if failed)
	 */
	@RequestMapping(value = "/api/convert/{from}/{to}", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<byte[]> convert(@PathVariable String from, @PathVariable String to,
			@RequestBody String body) {

		Configuration config = getConfig();
		byte[] data = Base64.getDecoder().decode(body.replace("\n", "").replace("\r", "").getBytes());

		try {
			String converterClassname = config.getConverter().get("package") + ".Convert" + from.toUpperCase() + "To"
					+ to.toUpperCase();
			Converter converter = converters.get(converterClassname);
			if (converter == null) {
				converter = (Converter) Class.forName(converterClassname).newInstance();
				converters.put(converterClassname, converter);
			}

			byte[] base64Response = Base64.getEncoder().encode(converter.convert(config, data));
			return ResponseEntity.ok(base64Response);

		} catch (Exception e) {
			logger.error("Failed to convert " + from + " to " + to, e);
			byte[] errorMessage = getErrorMessage("Error converting from " + from + " to " + to, e);
			return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
		}
	}

	private byte[] getErrorMessage(String msg, Exception e) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(baos);
		writer.println(msg);
		e.printStackTrace(writer);
		writer.close();
		return baos.toByteArray();
	}

	/**
	 * Save a byte array data to a temporary file.
	 * 
	 * @param path
	 *            Temporary file path
	 * @param from
	 *            Source data format (csv, xls, etc): used as file name
	 *            extension.
	 * @param data
	 *            binary content
	 * @return
	 * @throws IOException
	 */
	public static File saveToTempFile(File path, String from, byte[] data) throws IOException {

		File temp = File.createTempFile("req-", "." + from, path);
		FileOutputStream out = new FileOutputStream(temp);
		BufferedOutputStream bos = new BufferedOutputStream(out);
		bos.write(data, 0, data.length);
		bos.close();
		out.close();

		return temp;
	}

	/**
	 * Load a file data.
	 * 
	 * @param file
	 *            {@link File} object
	 * @return binary data (byte array)
	 * @throws IOException
	 */
	public static byte[] loadFromFile(File file) throws IOException {

		FileInputStream in = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int siz = 0;
			byte[] b = new byte[2048];
			while ((siz = bis.read(b)) > 0) {
				baos.write(b, 0, siz);
			}
			return baos.toByteArray();

		} finally {
			baos.close();
			bis.close();
			in.close();
			file.delete();
		}
	}

	/**
	 * Retrieves {@link InputStream} printed messages.
	 * 
	 * @param stream
	 *            a {@link InputStream}
	 * @return printed messages
	 * @throws IOException
	 */
	public static String getOutput(InputStream stream) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String s = null;
		StringBuilder sb = new StringBuilder(4096);
		while ((s = reader.readLine()) != null) {
			sb.append(s);
		}
		stream.close();
		return sb.toString();
	}
}
