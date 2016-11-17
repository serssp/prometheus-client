package com.outbrain.swinfra.metrics;

import static com.google.common.base.Preconditions.checkArgument;
import static com.outbrain.swinfra.metrics.StringUtils.isNotBlank;

public class LabelUtils {

  public static String labelsToCommaDelimitedString(final String... labels) {
    return String.join(",", (CharSequence[]) labels);
  }

  public static String[] commaDelimitedStringToLabels(final String commaDelimitedLabels) {
    checkArgument(isNotBlank(commaDelimitedLabels), "commaDelimitedLabels must not be empty");
    return commaDelimitedLabels.split(",");
  }

}
