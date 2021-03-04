package org.folio.ebsconet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import org.folio.ebsconet.domain.dto.EbsconetOrderLine;
import org.folio.ebsconet.rest.resource.OrdersApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

@RestController
@RequestMapping(value = "/ebsconet")
public class OrdersController  implements OrdersApi {
  public static final String PO_LINE_NUMBER = "268758-03";
  public static final String STUB_LINE = "{\n" +
    "  \"vendor\": \"AMAZ\",\n" +
    "  \"cancellationRestriction\": false,\n" +
    "  \"cancellationRestrictionNote\": \"Any note\",\n" +
    "  \"unitPrice\": 1,\n" +
    "  \"currency\": \"USD\",\n" +
    "  \"vendorReferenceNumber\": \"2019-184\",\n" +
    "  \"poNumber\": \"268758\",\n" +
    "  \"poLineNumber\": \"" + PO_LINE_NUMBER + "\",\n" +
    "  \"subscriptionToDate\": \"2018-10-05T00:00:00.000Z\",\n" +
    "  \"subscriptionFromDate\": \"2019-10-05T00:00:00.000Z\",\n" +
    "  \"quantity\": 1,\n" +
    "  \"fundCode\": \"HIST\",\n" +
    "  \"publisherName\": \"MIT Press\",\n" +
    "  \"vendorAccountNumber\": \"1121112569489\",\n" +
    "  \"workflowStatus\": \"Pending\"\n" +
    "}";

  @Override
  public ResponseEntity<EbsconetOrderLine> getPoLine(@Pattern(regexp="^[a-zA-Z0-9]{1,22}-[0-9]{1,3}$")
                                            @ApiParam(value = "product order line number",required=true) @PathVariable("poLineNumber") String poLineNumber) {

    EbsconetOrderLine ebsconetOrderLine;
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      ebsconetOrderLine = objectMapper.readValue(STUB_LINE, EbsconetOrderLine.class);
      if (!ebsconetOrderLine.getPoLineNumber().equals(poLineNumber)) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
      if (!PO_LINE_NUMBER.equals(poLineNumber)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (JsonProcessingException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(ebsconetOrderLine, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> putPoLine(@Pattern(regexp="^[a-zA-Z0-9]{1,22}-[0-9]{1,3}$") @ApiParam(value = "product order line number",required=true) @PathVariable("poLineNumber") String poLineNumber,
                                        @ApiParam(value = "" ,required=true )  @Valid @RequestBody EbsconetOrderLine ebsconetOrderLine) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      ebsconetOrderLine = objectMapper.readValue(STUB_LINE, EbsconetOrderLine.class);
      if (!ebsconetOrderLine.getPoLineNumber().equals(poLineNumber)) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } catch (JsonProcessingException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
