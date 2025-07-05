package ch.denicola.konfi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("test")
@Import(TestcontainersConfiguration.class)
class KonfiApplicationTests {

    @Test
    void contextLoads() {
    }

}
