package de.cmt.cometportable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CometPortableApplication {

	@PostConstruct
	private void init() {

		Path path = Paths.get("jobs");

		if(!Files.exists(path) || !Files.isDirectory(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(CometPortableApplication.class, args);
	}
}
