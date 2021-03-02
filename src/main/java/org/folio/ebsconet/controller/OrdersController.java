package org.folio.ebsconet.controller;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.folio.ebsconet.domain.dto.PoLine;
import org.folio.ebsconet.rest.resource.OrdersApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = "/ebsconet")
public class OrdersController  implements OrdersApi {

  @Override
  public ResponseEntity<PoLine> getPoLine(@Pattern(regexp="^[a-zA-Z0-9]{1,22}-[0-9]{1,3}$") @ApiParam(value = "product order line number",required=true) @PathVariable("poLineNumber") String poLineNumber) {
    PoLine poLine = new PoLine();
    poLine.setPoLineNumber(poLineNumber);

    return new ResponseEntity<PoLine>(poLine, HttpStatus.OK);
  }

  public ResponseEntity<Void> putPoLine(@Pattern(regexp="^[a-zA-Z0-9]{1,22}-[0-9]{1,3}$") @ApiParam(value = "product order line number",required=true) @PathVariable("poLineNumber") String poLineNumber, @ApiParam(value = "" ,required=true )  @Valid @RequestBody PoLine poLine) {
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
