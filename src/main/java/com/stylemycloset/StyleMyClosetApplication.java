package com.stylemycloset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class StyleMyClosetApplication {

  public static void main(String[] args) {
    SpringApplication.run(StyleMyClosetApplication.class, args);
  }

}
