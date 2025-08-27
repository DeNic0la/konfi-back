package ch.denic0la.konfi;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class TestKonfiApplication {

  public static void main(String[] args) {
    SpringApplication.from(KonfiApplication::main).run(args);
  }
}
