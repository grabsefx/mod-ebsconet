package org.folio.ebsconet.controller;

import lombok.extern.log4j.Log4j2;
import org.folio.ebsconet.domain.dto.ValidationResponse;
import org.folio.ebsconet.rest.resource.ValidateApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "/ebsconet")
public class ValidationsController implements ValidateApi {

  private static ValidationResponse getSuccessResponse() {
    var result = new ValidationResponse();
    result.setStatus(ValidationResponse.StatusEnum.SUCCESS);
    return result;
  }

  @Override
  @GetMapping(value = "/validate")
  public ResponseEntity<ValidationResponse> getValidation() {
    return new ResponseEntity<>(getSuccessResponse(), HttpStatus.OK);
  }
}
