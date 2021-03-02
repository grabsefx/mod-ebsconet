package org.folio.ebsconet.controller;

import org.folio.tenant.rest.resource.TenantApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("folioTenantController")
@RequestMapping(value = "/_/")
public class TenantController implements TenantApi {
}
