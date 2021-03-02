package org.folio.ebsconet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ebsconet.rest.resource.OrdersApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders/order-lines")
public class OrdersController implements OrdersApi {
}
