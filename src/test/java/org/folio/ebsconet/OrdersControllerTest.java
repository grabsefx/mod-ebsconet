package org.folio.ebsconet;

import org.folio.ebsconet.domain.dto.EbsconetOrderLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.folio.ebsconet.TestUtils.equalsToJSONMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdersControllerTest extends TestBase {
  private static final String PO_LINE_URL = "http://localhost:%s/ebsconet/orders/order-lines/";
  private static final String PO_LINE_NUMBER = "268758-03";
  private static final String UNKNOWN_PO_LINE_NUMBER = "268758-07";
  private static final String INVALID_PO_LINE_NUMBER = "13245";
  private String poLineUrl;

  @BeforeEach
  void prepareUrl() {
    poLineUrl = String.format(PO_LINE_URL, okapiPort);
  }

  @Test
  void canGetPoLineWithNumber() {
    ResponseEntity<String> response = get(poLineUrl + PO_LINE_NUMBER, String.class);
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), equalsToJSONMock("mockdata/ebsconet_order_line.json"));
  }

  @Test
  void shouldReturnNotFoundForUnknownPOLNumber() {
    String urlWithRandomUuid = poLineUrl + UNKNOWN_PO_LINE_NUMBER;
    HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
      () -> get(urlWithRandomUuid, EbsconetOrderLine.class));
    assertThat(exception.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void shouldReturnBadRequestForInvalidPOLNumber() {
    String urlWithInvalidUuid = poLineUrl + INVALID_PO_LINE_NUMBER;
    HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
      () -> get(urlWithInvalidUuid, EbsconetOrderLine.class));
    assertThat(exception.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
  }
}
