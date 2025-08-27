package ch.denic0la.konfi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@ActiveProfiles("test")
class KonfiApplicationTests {

  @Test
  void contextLoads() {}
}
