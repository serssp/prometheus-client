package com.outbrain.swinfra.metrics.utils;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

public class LabelUtils {

  /**
   * Convert an array of strings into a single comma-separated string
   */
  public static String labelsToCommaDelimitedString(final String... labels) {
    return String.join(",", (CharSequence[]) labels);
  }

  /**
   * Convert a comma-separated string into an array of strings
   */
  public static String[] commaDelimitedStringToLabels(final String commaDelimitedLabels) {
    Validate.notBlank(commaDelimitedLabels, "commaDelimitedLabels must not be empty");
    return commaDelimitedLabels.split(",");
  }

  /**
   * Returns a new list that contains all the elements in the original list combined with the new element
   */
  public static List<String> addLabelToList(final List<String> source, final String element) {
    final List<String> result = new ArrayList<>(source);
    result.add(element);
    return result;
  }

}
