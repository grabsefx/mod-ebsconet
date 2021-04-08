package org.folio.ebsconet;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.SocketUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
@EnableTransactionManagement
@Log4j2
public class TestBase {

  private static Header header;
  public static String TEST_TENANT = "test_tenant";

  @LocalServerPort
  protected int okapiPort;

  public final static int WIRE_MOCK_PORT = SocketUtils.findAvailableTcpPort();

  @Autowired
  private FolioModuleMetadata moduleMetadata;

  @BeforeEach
  void setUp() {
    FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(
      AsyncFolioExecutionContext.builder()
        .tenantId(TEST_TENANT)
        .moduleMetadata(moduleMetadata)
        .okapiUrl(getOkapiUrl()).build());
  }

  public static String getOkapiUrl() {
    return String.format("http://localhost:%s", WIRE_MOCK_PORT);
  }

  @BeforeAll
  static void testSetup() {
    header = new Header(XOkapiHeaders.TENANT, TEST_TENANT);
  }

  public <T> Response get(String url) {
    return given()
      .header(header)
      .contentType(ContentType.JSON)
      .get(url);
  }

  public Response put(String url, Object entity) {
    return given()
      .header(header)
      .contentType(ContentType.JSON)
      .body(entity)
      .put(url);
  }
}
