package org.folio.ebsconet;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
@EnableTransactionManagement
@Log4j2
public class TestBase {
  private static HttpHeaders headers;
  private static RestTemplate restTemplate;
  public static WireMockServer wireMockServer;
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

  @AfterEach
  void eachTearDown() {
    wireMockServer.resetAll();
  }

  @BeforeAll
  static void testSetup() {
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(XOkapiHeaders.TENANT, TEST_TENANT);
    headers.add(XOkapiHeaders.URL, getOkapiUrl());
    restTemplate = new RestTemplate();

    wireMockServer = new WireMockServer(options().port(WIRE_MOCK_PORT).notifier(new ConsoleNotifier(true)));
    wireMockServer.start();
  }

  @AfterAll
  static void tearDown() {
    wireMockServer.stop();
  }

  public <T> ResponseEntity<T> get(String url, Class<T> clazz) {
    return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), clazz);
  }

  public ResponseEntity<String> put(String url, Object entity) {
    return restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(entity, headers), String.class);
  }
}
