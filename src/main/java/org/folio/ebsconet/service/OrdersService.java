package org.folio.ebsconet.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ebsconet.client.FinanceClient;
import org.folio.ebsconet.client.OrdersClient;
import org.folio.ebsconet.client.OrganizationClient;
import org.folio.ebsconet.domain.dto.*;
import org.folio.ebsconet.error.ResourceNotFoundException;
import org.folio.ebsconet.mapper.OrdersMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrdersService {
  private final OrdersClient ordersClient;
  private final FinanceClient financeClient;
  private final OrdersMapper ordersMapper = Mappers.getMapper(OrdersMapper.class);
  private final OrganizationClient organizationClient;

  public EbsconetOrderLine getEbsconetOrderLine(String poLineNumber) {
    log.info("starting getEbsconetOrderLine poLineNumber={}", poLineNumber);
    PoLineCollection queryResult;
    try {
      queryResult = ordersClient.getOrderLinesByQuery("poLineNumber==" + poLineNumber);
    } catch (FeignException.NotFound e) {
      throw new ResourceNotFoundException("PO Line not found: " + poLineNumber);
    }
    if (queryResult.getTotalRecords() < 1)
      throw new ResourceNotFoundException("PO Line not found: " + poLineNumber);
    PoLine line = queryResult.getPoLines().get(0);
    log.debug("order line received for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    PurchaseOrder order = ordersClient.getOrderById(line.getPurchaseOrderId());
    log.debug("order received for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    String vendorId = order.getVendor();
    Organization vendor = organizationClient.getOrganizationById(vendorId);
    log.debug("Vendor organization received for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    EbsconetOrderLine eol = ordersMapper.folioToEbsconet(order, line, vendor);
    log.info("success for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    log.debug(eol);
    return eol;
  }

  public void updateEbsconetOrderLine(EbsconetOrderLine updateOrderLine) {

    PoLineCollection poLines;
    try {
      poLines = ordersClient.getOrderLinesByQuery("poLineNumber==" + updateOrderLine.getPoLineNumber());
    } catch (Exception e) {
      throw new ResourceNotFoundException("PO Line not found: " + updateOrderLine.getPoLineNumber());
    }
    if (poLines.getTotalRecords() < 1) {
      throw new ResourceNotFoundException("PO Line not found: " + updateOrderLine.getPoLineNumber());
    }

    PoLine poLine = poLines.getPoLines().get(0);

    CompositePoLine compositePoLine = ordersClient.getOrderLineById(poLine.getId());

    if (compositePoLine == null) {
      throw new ResourceNotFoundException("Composite PO Line not found: " + poLine.getPoLineNumber());
    }

    Fund fund = null;
    // Retrieve fund for update if need to change
    if (!compositePoLine.getFundDistribution().get(0).getCode().equals(updateOrderLine.getFundCode())) {
      FundCollection funds = financeClient.getFundsByQuery("code==" + updateOrderLine.getFundCode());

      if (funds.getTotalRecords() < 1) {
        throw new ResourceNotFoundException("Fund not found for: " + updateOrderLine.getFundCode());
      }
      fund = funds.getFunds().get(0);
    }

    // Convert ebsconet dto to poLine
    ordersMapper.ebsconetToFolio(compositePoLine, updateOrderLine, fund);

    ordersClient.putOrderLine(poLine.getId(), compositePoLine);
  }
}
