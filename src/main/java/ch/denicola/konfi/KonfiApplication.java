package ch.denicola.konfi;

import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denicola.konfi.brunch.data.Question;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.net.MalformedURLException;

@SpringBootApplication(
    scanBasePackages = {
        "ch.denicola.konfi",
        "ch.denicola.konfi.brunch",
        "ch.denicola.konfi.brunch.data"
    },
        exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories
public class KonfiApplication {

  @Bean
  public ModelMapper modelMapper() {
    var customModelMapper = new ModelMapper();
    customModelMapper.typeMap(BrunchQuestionDTO.class, Question.class).addMappings(mapper->{
      mapper.map(src->{
        if (src.getLink().isPresent()){
          try {
            return src.getLink().get().toURL();
          } catch (MalformedURLException e) {
            return null;
          }
        }
        return null;
      },Question::setLink);
      mapper.map(src->{
        if (src.getRecommended().isPresent()) {
          return src.getRecommended().get();
        }
        else {
          return src.getMin();
        }
      }, Question::setRecommended);

    });

    customModelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    return customModelMapper;
  }

  @Bean
  @Primary
  public Jackson2ObjectMapperBuilder customObjectMapper() {
    return new Jackson2ObjectMapperBuilder()
            // other configs are possible
            .modules(new JsonNullableModule());
  }

  public static void main(String[] args) {
    SpringApplication.run(KonfiApplication.class, args);
  }
}
