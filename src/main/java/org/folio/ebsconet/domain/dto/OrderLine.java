package org.folio.ebsconet.domain.dto;

import java.math.BigDecimal;
import java.util.Date;

public class OrderLine   {
    private String vendor;

    private Boolean cancellationRestriction;

    private String cancellationRestrictionNote;

    private BigDecimal unitPrice;

    private String currency;

    private String vendorReferenceNumber;

    private String poNumber;

    private String poLineNumber;

    private Date subscriptionToDate;

    private Date subscriptionFromDate;

    private Integer quantity;

    private String fundCode;

    private String publisherName;

    private String vendorAccountNumber;

    private String workflowStatus;

}

