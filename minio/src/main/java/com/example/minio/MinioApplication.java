package com.example.minio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// minio test
// docker run -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001
// minioadmin / minioadmin

@SpringBootApplication
public class MinioApplication {

	public static void main(String[] args) {

		SpringApplication.run(MinioApplication.class, args);
	}
}