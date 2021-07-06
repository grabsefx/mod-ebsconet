package org.folio.ebsconet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ebsconet.domain.dto.EbsconetOrderLine;
import org.folio.ebsconet.rest.resource.OrdersApi;
import org.folio.ebsconet.service.OrdersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ebsconet")
@Log4j2
public class OrdersController implements OrdersApi {

  private final OrdersService ordersService;

  @Override
  public ResponseEntity<EbsconetOrderLine> getEbsconetOrderLine(String poLineNumber) {
    var orderLine = ordersService.getEbscoNetOrderLine(poLineNumber);
    return new ResponseEntity<>(orderLine, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> putEbsconetOrderLine(String poLineNumber, EbsconetOrderLine ebsconetOrderLine) {
    if (!ebsconetOrderLine.getPoLineNumber().equals(poLineNumber)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    ordersService.updateEbscoNetOrderLine(ebsconetOrderLine);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
