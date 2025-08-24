package com.mrokga.carrot_server.config;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer; // <- 여기 z!
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer addGlobal500() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    Schema<?> schema = new Schema<>().$ref("#/components/schemas/ErrorResponse");
                    Content content = new Content().addMediaType("application/json",
                            new MediaType().schema(schema));
                    operation.getResponses().addApiResponse("500",
                            new ApiResponse().description("서버 내부 오류").content(content));
                })
        );
    }


}
