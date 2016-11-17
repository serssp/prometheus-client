package com.outbrain.swinfra.metrics;

import static com.google.common.base.Strings.isNullOrEmpty;

class StringUtils {

  static boolean isNotBlank(final String s) {
    return s != null && !isNullOrEmpty(s.trim());
  }

}
