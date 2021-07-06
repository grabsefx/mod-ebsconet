package org.folio.ebsconet.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ebsconet.client.FinanceClient;
import org.folio.ebsconet.client.OrdersClient;
import org.folio.ebsconet.client.OrganizationClient;
import org.folio.ebsconet.domain.dto.EbsconetOrderLine;
import org.folio.ebsconet.domain.dto.Fund;
import org.folio.ebsconet.domain.dto.FundCollection;
import org.folio.ebsconet.domain.dto.PoLine;
import org.folio.ebsconet.domain.dto.PoLineCollection;
import org.folio.ebsconet.domain.dto.PurchaseOrder;
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
  private static final String PO_LINE_NOT_FOUND_MESSAGE = "PO Line not found: ";

  public EbsconetOrderLine getEbscoNetOrderLine(String poLineNumber) {
    log.info("starting getEbscoNetOrderLine poLineNumber={}", poLineNumber);
    PoLineCollection queryResult;
    try {
      queryResult = ordersClient.getOrderLinesByQuery("poLineNumber==" + poLineNumber);
    } catch (FeignException.NotFound e) {
      throw new ResourceNotFoundException(PO_LINE_NOT_FOUND_MESSAGE + poLineNumber);
    }
    if (queryResult.getTotalRecords() < 1)
      throw new ResourceNotFoundException(PO_LINE_NOT_FOUND_MESSAGE + poLineNumber);
    PoLine line = queryResult.getPoLines().get(0);
    log.debug("order line received for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    PurchaseOrder order = ordersClient.getOrderById(line.getPurchaseOrderId());
    log.debug("order received for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    String vendorId = order.getVendor();
    var vendor = organizationClient.getOrganizationById(vendorId);
    log.debug("Vendor organization received for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    EbsconetOrderLine eol = ordersMapper.folioToEbsconet(order, line, vendor);
    log.info("success for getEbsconetOrderLine poLineNumber={}", poLineNumber);
    log.debug(eol);
    return eol;
  }

  public void updateEbscoNetOrderLine(EbsconetOrderLine updateOrderLine) {

    PoLineCollection poLines;
    try {
      poLines = ordersClient.getOrderLinesByQuery("poLineNumber==" + updateOrderLine.getPoLineNumber());
    } catch (Exception e) {
      throw new ResourceNotFoundException(PO_LINE_NOT_FOUND_MESSAGE + updateOrderLine.getPoLineNumber());
    }
    if (poLines.getTotalRecords() < 1) {
      throw new ResourceNotFoundException(PO_LINE_NOT_FOUND_MESSAGE + updateOrderLine.getPoLineNumber());
    }

    var poLine = poLines.getPoLines().get(0);

    var compositePoLine = ordersClient.getOrderLineById(poLine.getId());

    if (compositePoLine == null) {
      throw new ResourceNotFoundException(PO_LINE_NOT_FOUND_MESSAGE + poLine.getPoLineNumber());
    }

    Fund fund = null;
    // Retrieve fund for update if need to change
    if (!compositePoLine.getFundDistribution().isEmpty()
        && !compositePoLine.getFundDistribution().get(0).getCode().equals(updateOrderLine.getFundCode())) {

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
