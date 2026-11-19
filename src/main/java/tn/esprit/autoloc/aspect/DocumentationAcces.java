package tn.esprit.autoloc.aspect;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@SecurityScheme(name = "manager", type = SecuritySchemeType.HTTP, scheme = "bearer")
public class DocumentationAcces {
}
