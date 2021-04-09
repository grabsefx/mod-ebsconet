package org.folio.ebsconet.mapper;

import org.folio.ebsconet.domain.dto.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class OrdersMapper {
  @Mapping(target = "vendor", source = "vendor.code")
  @Mapping(target = "cancellationRestriction", source = "line.cancellationRestriction")
  @Mapping(target = "cancellationRestrictionNote", source = "line.cancellationRestrictionNote")
  @Mapping(target = "unitPrice", source = "line.cost.listUnitPrice")
  @Mapping(target = "currency", source = "line.cost.currency")
  @Mapping(target = "vendorReferenceNumbers", source = "line.vendorDetail.referenceNumbers")
  @Mapping(target = "poLineNumber", source = "line.poLineNumber")
  @Mapping(target = "subscriptionToDate", source = "line.details.subscriptionTo", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
  @Mapping(target = "subscriptionFromDate", source = "line.details.subscriptionFrom", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
  @Mapping(target = "quantity", source = "line.cost.quantityPhysical")
  @Mapping(target = "fundCode", source = "line", qualifiedByName = "getFundCode")
  @Mapping(target = "publisherName", source = "line.publisher")
  @Mapping(target = "vendorAccountNumber", source = "line.vendorDetail.vendorAccount")
  @Mapping(target = "workflowStatus", source = "order.workflowStatus")
  public abstract EbsconetOrderLine folioToEbsconet(PurchaseOrder order, PoLine line, Organization vendor);

  @Named("getFundCode")
  public String getFundCode(PoLine line) {
    List<FundDistribution> distributions = line.getFundDistribution();
    if (distributions == null || distributions.isEmpty())
      return null;
    return distributions.get(0).getCode();
  }

  public void ebsconetToFolio(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine, Fund fund) {
    poLine.setCancellationRestriction(ebsconetOrderLine.getCancellationRestriction());
    poLine.setCancellationRestrictionNote(ebsconetOrderLine.getCancellationRestrictionNote());
    poLine.getCost().setListUnitPrice(ebsconetOrderLine.getUnitPrice());
    poLine.getCost().setCurrency(ebsconetOrderLine.getCurrency());
    poLine.getVendorDetail().setReferenceNumbers(ebsconetOrderLine.getVendorReferenceNumbers());
    poLine.getDetails().setSubscriptionTo(ebsconetOrderLine.getSubscriptionToDate());
    poLine.getDetails().setSubscriptionFrom(ebsconetOrderLine.getSubscriptionFromDate());
    poLine.getVendorDetail().setVendorAccount(ebsconetOrderLine.getVendorAccountNumber());
    poLine.setPublisher(ebsconetOrderLine.getPublisherName());

    if (poLine.getLocations().size() == 1) {
      poLine.getCost().setQuantityPhysical(ebsconetOrderLine.getQuantity());
      poLine.getLocations().get(0).setQuantityPhysical(ebsconetOrderLine.getQuantity());
    }

    if(fund != null){
      poLine.getFundDistribution().get(0).setCode(fund.getCode());
      poLine.getFundDistribution().get(0).setFundId(fund.getId());
    }
  }
}
