package org.icann.rdapconformance.validator.utils.lang;

import org.apache.commons.lang3.StringUtils;

public final class StringUtil {

    public static boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }
}
