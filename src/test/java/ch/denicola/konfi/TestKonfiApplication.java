package ch.denicola.konfi;

import org.springframework.boot.SpringApplication;

public class TestKonfiApplication {

    public static void main(String[] args) {
        SpringApplication.from(KonfiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
