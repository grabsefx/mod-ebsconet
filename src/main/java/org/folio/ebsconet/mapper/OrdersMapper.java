package org.folio.ebsconet.mapper;

import org.folio.ebsconet.domain.dto.CompositePoLine;
import org.folio.ebsconet.domain.dto.Cost;
import org.folio.ebsconet.domain.dto.EbsconetOrderLine;
import org.folio.ebsconet.domain.dto.Fund;
import org.folio.ebsconet.domain.dto.FundDistribution;
import org.folio.ebsconet.domain.dto.Organization;
import org.folio.ebsconet.domain.dto.PoLine;
import org.folio.ebsconet.domain.dto.PurchaseOrder;
import org.folio.ebsconet.domain.dto.Source;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

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
    poLine.setSource(Source.EBSCONET);
    poLine.setCancellationRestriction(ebsconetOrderLine.getCancellationRestriction());
    poLine.setCancellationRestrictionNote(ebsconetOrderLine.getCancellationRestrictionNote());
    poLine.getCost().setCurrency(ebsconetOrderLine.getCurrency());
    poLine.getVendorDetail().setReferenceNumbers(ebsconetOrderLine.getVendorReferenceNumbers());
    poLine.getDetails().setSubscriptionTo(ebsconetOrderLine.getSubscriptionToDate());
    poLine.getDetails().setSubscriptionFrom(ebsconetOrderLine.getSubscriptionFromDate());
    poLine.getVendorDetail().setVendorAccount(ebsconetOrderLine.getVendorAccountNumber());
    poLine.setPublisher(ebsconetOrderLine.getPublisherName());

    populateCostAndLocations(poLine, ebsconetOrderLine);

    if (fund != null) {
      poLine.getFundDistribution().get(0).setCode(fund.getCode());
      poLine.getFundDistribution().get(0).setFundId(fund.getId());
    }
  }

  private void populateCostAndLocations(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine) {
    switch (poLine.getOrderFormat()) {
    case ELECTRONIC_RESOURCE:
      populateElectronicCostAndLocation(poLine, ebsconetOrderLine);
      break;
    case OTHER:
    case PHYSICAL_RESOURCE:
      populatePhysicalCostAndLocation(poLine, ebsconetOrderLine);
      break;
    case P_E_MIX:
      populateCostAndLocationPEMix(poLine, ebsconetOrderLine);
      break;
    }
  }

  private void populatePhysicalCostAndLocation(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine) {
    poLine.getCost().setQuantityPhysical(ebsconetOrderLine.getQuantity());
    poLine.getCost().setListUnitPrice(ebsconetOrderLine.getUnitPrice());

    poLine.getLocations().get(0).setQuantityPhysical(ebsconetOrderLine.getQuantity());
  }

  private void populateElectronicCostAndLocation(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine) {
    poLine.getCost().setQuantityElectronic(ebsconetOrderLine.getQuantity());
    poLine.getCost().setListUnitPriceElectronic(ebsconetOrderLine.getUnitPrice());

    poLine.getLocations().get(0).setQuantityElectronic(ebsconetOrderLine.getQuantity());
  }

  private void populateCostAndLocationPEMix(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine) {
    processPEmixPriceUpdate(poLine, ebsconetOrderLine);
    processPEMixQuantityUpdate(poLine, ebsconetOrderLine);
  }

  private void processPEMixQuantityUpdate(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine) {
    // Q physical = 1 and electronic > 1
    if (poLine.getCost().getQuantityPhysical() == 1 && poLine.getCost().getQuantityElectronic() > 1) {
      poLine.getCost().setQuantityElectronic(ebsconetOrderLine.getQuantity() - 1);
      poLine.getLocations().get(0).setQuantityElectronic(ebsconetOrderLine.getQuantity() - 1);
      poLine.getCost().setQuantityPhysical(1);
      poLine.getLocations().get(0).setQuantityPhysical(1);
    }

    // Q physical > 1, Q electronic = 1
    else if (poLine.getCost().getQuantityPhysical() > 1 && poLine.getCost().getQuantityElectronic() == 1) {
      poLine.getCost().setQuantityPhysical(ebsconetOrderLine.getQuantity() - 1);
      poLine.getLocations().get(0).setQuantityPhysical(ebsconetOrderLine.getQuantity() - 1);
      poLine.getCost().setQuantityElectronic(1);
      poLine.getLocations().get(0).setQuantityElectronic(1);
    }

    // Q (physical > 1 and Q electronic > 1)  OR  (physical = 1 and electronic = 1)
    else if ((poLine.getCost().getQuantityElectronic() > 1 && poLine.getCost().getQuantityPhysical() > 1)
      || (poLine.getCost().getQuantityElectronic() == 1 && poLine.getCost().getQuantityPhysical() == 1)) {
      int newElectronicQuantity = ebsconetOrderLine.getQuantity() / 2;
      int newPhysicalQuantity = ebsconetOrderLine.getQuantity() - newElectronicQuantity;

      poLine.getCost().setQuantityElectronic(newElectronicQuantity);
      poLine.getCost().setQuantityPhysical(newPhysicalQuantity);
      poLine.getLocations().get(0).setQuantityElectronic(newElectronicQuantity);
      poLine.getLocations().get(0).setQuantityPhysical(newPhysicalQuantity);
    }
  }

  private void processPEmixPriceUpdate(CompositePoLine poLine, EbsconetOrderLine ebsconetOrderLine) {
    var fractionDigits = Currency.getInstance(ebsconetOrderLine.getCurrency()).getDefaultFractionDigits();
    var unitPrice = ebsconetOrderLine.getUnitPrice().setScale(fractionDigits, RoundingMode.HALF_EVEN);

    // Price physical = 0 and electronic > 0
    if (poLine.getCost().getListUnitPrice().signum() == 0 && poLine.getCost().getListUnitPriceElectronic().signum() > 0) {
      poLine.getCost().setListUnitPriceElectronic(unitPrice);
    }

    // Price physical > 0, electronic = 0
    else if (poLine.getCost().getListUnitPriceElectronic().signum() == 0 && poLine.getCost().getListUnitPrice().signum() > 0) {
      poLine.getCost().setListUnitPrice(unitPrice);
    }
    // Price physical > 0, electronic > 0
    else if (poLine.getCost().getListUnitPriceElectronic().signum() > 0 && poLine.getCost().getListUnitPrice().signum() > 0) {
      // divide unit price

      BigDecimal newElectronicPrice = unitPrice.divide(BigDecimal.valueOf(2), fractionDigits, RoundingMode.HALF_EVEN).setScale(fractionDigits, RoundingMode.HALF_EVEN);
      BigDecimal newPhysicalPrice = unitPrice.subtract(newElectronicPrice).setScale(fractionDigits, RoundingMode.HALF_EVEN);;

      poLine.getCost().setListUnitPriceElectronic(newElectronicPrice);
      poLine.getCost().setListUnitPrice(newPhysicalPrice);
    }
  }
}
