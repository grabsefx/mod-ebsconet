package org.folio.ebsconet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ebsconet.domain.dto.EbsconetOrderLine;
import org.folio.ebsconet.rest.resource.OrdersApi;
import org.folio.ebsconet.service.OrdersService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ebsconet")
@Log4j2
public class OrdersController implements OrdersApi {

  private final OrdersService ordersService;

  public static final String PO_LINE_NUMBER = "268758-03";
  public static final String STUB_EBSCONET_ORDER_LINE = new JSONObject()
    .put("vendor", "AMAZ")
    .put("cancellationRestriction", false)
    .put("cancellationRestrictionNote", "Any note")
    .put("unitPrice", 1)
    .put("currency", "USD")
    .put("vendorReferenceNumbers", new JSONArray(List.of(new JSONObject()
      .put("refNumber", "2019-184")
      .put("refNumberType", "Vendor title number")
    )))
    .put("poLineNumber", PO_LINE_NUMBER)
    .put("subscriptionToDate", "2018-10-05T00:00:00.000Z")
    .put("subscriptionFromDate", "2019-10-05T00:00:00.000Z")
    .put("quantity", 1)
    .put("fundCode", "HIST")
    .put("publisherName", "MIT Press")
    .put("vendorAccountNumber", "1121112569489")
    .put("workflowStatus", "Pending")
    .toString();

  EbsconetOrderLine orderLine;
  ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public ResponseEntity<EbsconetOrderLine> getEbsconetOrderLine(String poLineNumber) {
    orderLine = ordersService.getEbsconetOrderLine(poLineNumber);
    return new ResponseEntity<>(orderLine, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> putEbsconetOrderLine(String poLineNumber, EbsconetOrderLine ebsconetOrderLine) {
    try {
      orderLine = objectMapper.readValue(STUB_EBSCONET_ORDER_LINE, EbsconetOrderLine.class);

      if (!orderLine.getPoLineNumber().equals(poLineNumber)) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    }
    catch (JsonProcessingException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
