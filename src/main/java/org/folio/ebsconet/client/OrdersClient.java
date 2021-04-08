package org.folio.ebsconet.client;

import org.folio.ebsconet.domain.dto.CompositePoLine;
import org.folio.ebsconet.domain.dto.PoLineCollection;
import org.folio.ebsconet.domain.dto.PurchaseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("orders")
public interface OrdersClient {
  @GetMapping(value = "/composite-orders/{id}")
  PurchaseOrder getOrderById(@PathVariable("id") String id);

  @GetMapping(value = "/order-lines/{id}")
  CompositePoLine getOrderLineById(@PathVariable("id") String id);

  @GetMapping(value = "/order-lines")
  PoLineCollection getOrderLinesByQuery(@RequestParam("query") String query);

  @PutMapping(value = "/order-lines/{id}")
  void putOrderLine(@PathVariable("id") String id, CompositePoLine poLine);
}
