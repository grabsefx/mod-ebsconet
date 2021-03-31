package org.folio.ebsconet.client;

import org.folio.ebsconet.domain.dto.Organization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("organizations")
public interface OrganizationClient {
  @GetMapping(value = "/organizations/{id}")
  Organization getOrganizationById(@PathVariable("id") String id);
}
