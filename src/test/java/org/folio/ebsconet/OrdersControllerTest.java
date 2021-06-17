package org.folio.ebsconet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException.BadRequest;
import feign.FeignException.InternalServerError;
import feign.FeignException.UnprocessableEntity;
import feign.Request;
import feign.Request.Body;
import feign.Request.HttpMethod;
import feign.RequestTemplate;
import java.util.HashMap;
import org.folio.ebsconet.domain.dto.EbsconetOrderLine;
import org.folio.ebsconet.error.ResourceNotFoundException;
import org.folio.ebsconet.service.OrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

class OrdersControllerTest extends TestBase {
  private static final String PO_LINE_URL = "http://localhost:%s/ebsconet/orders/order-lines/";
  private static final String PO_LINE_NUMBER = "268758-03";
  private static final String UNKNOWN_PO_LINE_NUMBER = "268758-07";
  private static final String INVALID_PO_LINE_NUMBER = "13245";
  private String poLineUrl;

  @MockBean
  private OrdersService ordersService;

  @BeforeEach
  void prepareUrl() {
    poLineUrl = String.format(PO_LINE_URL, okapiPort);
  }

  @Test
  void canGetPoLineWithNumber() {
    var line = new EbsconetOrderLine();
    line.setPoLineNumber(PO_LINE_NUMBER);

    when(ordersService.getEbscoNetOrderLine(any())).thenReturn(line);
    EbsconetOrderLine ebsconetOrderLine = get(poLineUrl + PO_LINE_NUMBER)
      .then()
      .statusCode(200)
      .extract()
      .as(EbsconetOrderLine.class);

    verify(ordersService,times(1)).getEbscoNetOrderLine(any());
    assertThat(ebsconetOrderLine.getPoLineNumber(),is(PO_LINE_NUMBER));
  }

  @Test
  void shouldReturnNotFoundForUnknownPOLNumber() {
    String urlWithRandomUuid = poLineUrl + UNKNOWN_PO_LINE_NUMBER;
    when(ordersService.getEbscoNetOrderLine(anyString())).thenThrow(new ResourceNotFoundException(""));
    get(urlWithRandomUuid)
      .then()
      .statusCode(404);
    verify(ordersService,times(1)).getEbscoNetOrderLine(any());
  }

  @Test
  void shouldReturnBadRequestForInvalidPOLNumber() {
    String urlWithInvalidUuid = poLineUrl + INVALID_PO_LINE_NUMBER;
    get(urlWithInvalidUuid)
      .then()
      .statusCode(400);
    verify(ordersService,never()).getEbscoNetOrderLine(any());
  }

  @Test
  void shouldReturnInternalErrorForInternalDateParsingIssue() {
    String urlWithRandomUuid = poLineUrl + PO_LINE_NUMBER;
    Request request = Request.create(HttpMethod.GET, "", new HashMap<>(), Body.empty(), new RequestTemplate());
    when(ordersService.getEbscoNetOrderLine(anyString())).thenThrow(new InternalServerError("error", request,"".getBytes()));

    get(urlWithRandomUuid)
      .then()
      .statusCode(500);
    verify(ordersService,times(1)).getEbscoNetOrderLine(any());
  }

  @Test
  void shouldReturnUnprocessableEntityIfGetSuchResponse() {
    String urlWithRandomUuid = poLineUrl + PO_LINE_NUMBER;
    Request request = Request.create(HttpMethod.GET, "", new HashMap<>(), Body.empty(), new RequestTemplate());
    when(ordersService.getEbscoNetOrderLine(anyString())).thenThrow(new UnprocessableEntity("error", request,"".getBytes()));

    get(urlWithRandomUuid)
      .then()
      .statusCode(422);
    verify(ordersService,times(1)).getEbscoNetOrderLine(any());
  }

  @Test
  void shouldReturnBadRequestIfGetSuchResponse() {
    String urlWithRandomUuid = poLineUrl + PO_LINE_NUMBER;
    Request request = Request.create(HttpMethod.GET, "", new HashMap<>(), Body.empty(), new RequestTemplate());
    when(ordersService.getEbscoNetOrderLine(anyString())).thenThrow(new BadRequest("error", request,"".getBytes()));

    get(urlWithRandomUuid)
      .then()
      .statusCode(400);
    verify(ordersService,times(1)).getEbscoNetOrderLine(any());
  }

  @Test
  void shouldReturnBadRequestForRequestWithDifferentPoLineNumbers() {
    String urlWithInvalidUuid = poLineUrl + "10000-1";
    EbsconetOrderLine ebsconetOrderLine = new EbsconetOrderLine();
    ebsconetOrderLine.setPoLineNumber("10000-2");
    ebsconetOrderLine.setSubscriptionFromDate(null);
    ebsconetOrderLine.setSubscriptionToDate(null);
    put(urlWithInvalidUuid, ebsconetOrderLine)
      .then()
      .statusCode(400);
    verify(ordersService,never()).updateEbscoNetOrderLine(any());
  }

  @Test
  void shouldReturnNoContentIfUpdatedSuccessfully() {
    String url = poLineUrl + PO_LINE_NUMBER;
    EbsconetOrderLine ebsconetOrderLine = new EbsconetOrderLine();
    ebsconetOrderLine.setPoLineNumber(PO_LINE_NUMBER);
    ebsconetOrderLine.setSubscriptionFromDate(null);
    ebsconetOrderLine.setSubscriptionToDate(null);
    ebsconetOrderLine.setFundCode("AFRICAHIST");
    ebsconetOrderLine.setCurrency("USD");

    put(url, ebsconetOrderLine)
      .then()
      .statusCode(204);
    verify(ordersService,times(1)).updateEbscoNetOrderLine(any());
  }
}
