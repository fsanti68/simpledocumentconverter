package com.logicalis.br.sdc.converters;

import java.io.File;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicalis.br.sdc.Converter;
import com.logicalis.br.sdc.ConverterService;
import com.logicalis.br.sdc.model.Configuration;

/**
 * Convert from csv to Html format, using open office command line tool.
 * 
 * @author Fabio De Santi
 *
 */
public class ConvertCSVToHTML implements Converter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String FROM = "csv";
	private final String TO = "html";

	private final File tempPath = new File("/tmp");

	@Override
	public byte[] convert(Configuration config, byte[] data) throws Exception {

		String commandConfig = FROM + "-to-" + TO;
		File temp = ConverterService.saveToTempFile(tempPath, FROM, data);
		logger.info("Wrote " + data.length + " bytes to " + temp.getAbsolutePath() + " to be converted to ." + TO);

		String cmdLine = config.getConverter().get(commandConfig);
		if (cmdLine == null || cmdLine.isEmpty()) {
			throw new Exception(
					"Unknown convertion: " + FROM + " to " + TO + " (no configuration entry '" + cmdLine + "')");
		}

		String cmd = MessageFormat.format(cmdLine, temp.getAbsolutePath());
		Process p = Runtime.getRuntime().exec(cmd);

		String convertInfo = ConverterService.getOutput(p.getInputStream());
		String convertError = ConverterService.getOutput(p.getErrorStream());
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info(convertInfo);
		if (convertError != null && !convertError.isEmpty())
			logger.error(convertError);
		if (p.exitValue() != 0)
			logger.warn("Exec.exitCode = " + p.exitValue());

		// get output file
		String outfile = tempPath.getAbsolutePath() + File.separator + temp.getName().replaceAll("." + FROM, "." + TO);
		File resultf = new File(outfile);
		logger.info("Search for " + outfile + ": " + (resultf.exists() ? "found" : "not found!"));
		return ConverterService.loadFromFile(resultf);
	}
}
