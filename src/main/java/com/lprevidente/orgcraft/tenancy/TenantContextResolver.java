package com.lprevidente.orgcraft.tenancy;

import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
class TenantContextResolver
    implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

  static final String UNSET_TENANT = "__unset__";

  @Override
  public String resolveCurrentTenantIdentifier() {
    final var tenantId = TenantContext.get();
    return tenantId != null ? tenantId.value().toString() : UNSET_TENANT;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
  }
}
