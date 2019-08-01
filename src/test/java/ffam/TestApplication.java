package ffam;

import ffam.configuration.DatabaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication extends DatabaseConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
