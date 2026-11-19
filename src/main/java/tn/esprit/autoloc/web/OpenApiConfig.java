package tn.esprit.autoloc.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public OpenAPI documentationAutoLoc() {
        return new OpenAPI().info(new Info()
                .title("AutoLoc API")
                .version("1.0")
                .description("Gestion des locations de véhicules pour plusieurs agences."));
    }
}
