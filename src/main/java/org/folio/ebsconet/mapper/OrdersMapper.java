package org.folio.ebsconet.mapper;

import java.math.BigDecimal;
import java.util.Optional;
import org.folio.ebsconet.domain.dto.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class OrdersMapper {
  @Mapping(target = "vendor", source = "vendor.code")
  @Mapping(target = "cancellationRestriction", source = "line.cancellationRestriction")
  @Mapping(target = "cancellationRestrictionNote", source = "line.cancellationRestrictionNote")
  @Mapping(target = "unitPrice", source = "line", qualifiedByName = "getUnitPrice")
  @Mapping(target = "currency", source = "line.cost.currency")
  @Mapping(target = "vendorReferenceNumbers", source = "line.vendorDetail.referenceNumbers")
  @Mapping(target = "poLineNumber", source = "line.poLineNumber")
  @Mapping(target = "subscriptionToDate", source = "line.details.subscriptionTo")
  @Mapping(target = "subscriptionFromDate", source = "line.details.subscriptionFrom")
  @Mapping(target = "quantity", source = "line", qualifiedByName = "getQuantity")
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

  @Named("getQuantity")
  public Integer getQuantity(PoLine line) {
    Integer physical = Optional.ofNullable(line.getCost()).map(Cost::getQuantityPhysical).orElse(0);
    Integer electronic = Optional.ofNullable(line.getCost()).map(Cost::getQuantityElectronic).orElse(0);
    return physical + electronic;
  }

  @Named("getUnitPrice")
  public BigDecimal getUnitPrice(PoLine line) {
    BigDecimal physical = Optional.ofNullable(line.getCost()).map(Cost::getListUnitPrice).orElse(BigDecimal.ZERO);
    BigDecimal electronic = Optional.ofNullable(line.getCost()).map(Cost::getListUnitPriceElectronic).orElse(BigDecimal.ZERO);
    return physical.add(electronic);
  }

  public void ebsconetToFolio(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine, Fund fund) {
    poLine.setCancellationRestriction(ebsconetOrderLine.getCancellationRestriction());
    poLine.setCancellationRestrictionNote(ebsconetOrderLine.getCancellationRestrictionNote());
    poLine.getCost().setCurrency(ebsconetOrderLine.getCurrency());
    poLine.getVendorDetail().setReferenceNumbers(ebsconetOrderLine.getVendorReferenceNumbers());
    poLine.getDetails().setSubscriptionTo(ebsconetOrderLine.getSubscriptionToDate());
    poLine.getDetails().setSubscriptionFrom(ebsconetOrderLine.getSubscriptionFromDate());
    poLine.getVendorDetail().setVendorAccount(ebsconetOrderLine.getVendorAccountNumber());
    poLine.setPublisher(ebsconetOrderLine.getPublisherName());

    if (poLine.getLocations().size() == 1 && !poLine.getOrderFormat().equals(OrderFormat.P_E_MIX)) {
      if (poLine.getOrderFormat() == OrderFormat.PHYSICAL_RESOURCE) {
        poLine.getCost().setQuantityPhysical(ebsconetOrderLine.getQuantity());
        poLine.getCost().setListUnitPrice(ebsconetOrderLine.getUnitPrice());
        poLine.getLocations().get(0).setQuantityPhysical(ebsconetOrderLine.getQuantity());
        poLine.getLocations().get(0).setQuantity(ebsconetOrderLine.getQuantity());
      }
      if (poLine.getOrderFormat() == OrderFormat.ELECTRONIC_RESOURCE) {
        poLine.getCost().setQuantityElectronic(ebsconetOrderLine.getQuantity());
        poLine.getCost().setListUnitPriceElectronic(ebsconetOrderLine.getUnitPrice());
        poLine.getLocations().get(0).setQuantityElectronic(ebsconetOrderLine.getQuantity());
        poLine.getLocations().get(0).setQuantity(ebsconetOrderLine.getQuantity());
      }
    }

    if(fund != null){
      poLine.getFundDistribution().get(0).setCode(fund.getCode());
      poLine.getFundDistribution().get(0).setFundId(fund.getId());
    }
  }
}
