package com.logicalis.br.sdc;

import com.logicalis.br.sdc.model.Configuration;

/**
 * Interface to define a converter's contract.
 * 
 * @author Fabio De Santi
 */
public interface Converter {

	byte[] convert(Configuration config, byte[] source) throws Exception;
}
