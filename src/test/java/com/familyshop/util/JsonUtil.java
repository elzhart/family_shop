package com.familyshop.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonUtil {
  private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
  private JsonUtil() {}
  public static String toJson(Object o) {
    try { return MAPPER.writeValueAsString(o); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
}
