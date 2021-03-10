package org.folio.ebsconet.controller;

import org.folio.ebsconet.rest.resource.ValidateApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping(value = "/ebsconet")
public class ValidationsController implements ValidateApi {

  @Override
  @RequestMapping(value = "/validate", method = RequestMethod.GET)
  public ResponseEntity<Void> getValidation() {

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
