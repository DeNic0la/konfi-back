package ch.denic0la.konfi.config;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestWebConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder.json().modules(new JsonNullableModule()).build();
  }

  @Bean
  public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder().modules(new JsonNullableModule());
  }
}
