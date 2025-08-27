package ch.denic0la.konfi;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

  private final ObjectMapper objectMapper;

  public WebConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600); // 1 hour
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    jsonConverter.setObjectMapper(objectMapper);
    converters.add(jsonConverter);
  }
}
