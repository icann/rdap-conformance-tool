package org.icann.rdapconformance.validator.utils.lang;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class AsciiValidator {

    public boolean isAscii(String value){

        if(StringUtils.isBlank(value)){
            return true;
        }

        CharsetEncoder asciiEncoder = StandardCharsets.US_ASCII.newEncoder();
        return asciiEncoder.canEncode(value);
    }
}
