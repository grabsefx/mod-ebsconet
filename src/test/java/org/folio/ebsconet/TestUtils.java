package org.folio.ebsconet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TestUtils {

  public static String getMockData(String path) throws IOException {
    try (InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (resourceAsStream != null) {
        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
      } else {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
          lines.forEach(sb::append);
        }
        return sb.toString();
      }
    }
  }

  public static IsEqualToJSONMock equalsToJSONMock(String jsonMockFilePath) {
    return new IsEqualToJSONMock(jsonMockFilePath);
  }

  public static class IsEqualToJSONMock extends DiagnosingMatcher<Object> {
    final private String jsonMockFilePath;

    IsEqualToJSONMock(String jsonMockFilePath) {
      this.jsonMockFilePath = jsonMockFilePath;
    }

    @Override
    public void describeTo(final Description description) {
      description.appendText(jsonMockFilePath);
    }

    @Override
    protected boolean matches(final Object actual, final Description mismatchDescription) {
      try {
        String expectedJSON = getMockData(jsonMockFilePath);
        final String actualJSON = toJSONString(actual);
        final JSONCompareResult result = JSONCompare.compareJSON(expectedJSON, actualJSON, JSONCompareMode.STRICT);
        if (!result.passed()) {
          mismatchDescription.appendText(result.getMessage());
        }
        return result.passed();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private static String toJSONString(final Object o) throws JsonProcessingException {
      return o instanceof String ? (String) o : new ObjectMapper().writeValueAsString(o);
    }
  }
}
