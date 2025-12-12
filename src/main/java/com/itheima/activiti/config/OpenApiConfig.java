package com.itheima.activiti.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 配置SpringDoc OpenAPI，提供API文档生成和Swagger UI访问
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置OpenAPI基本信息
     * @return OpenAPI对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 添加安全要求，使用Bearer认证
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                // 配置安全方案
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // 配置API基本信息
                .info(new Info()
                        .title("零售门店业务管理平台API")
                        .version("1.0")
                        .description("零售门店业务管理平台的API文档，包含流程部署、流程定义管理、流程实例管理、任务管理和用户角色管理等功能")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@example.com")
                                .url("http://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
