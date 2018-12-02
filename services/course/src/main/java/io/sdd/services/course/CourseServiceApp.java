package io.sdd.services.course;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@EnableSwagger2WebMvc
@EnableDiscoveryClient
@Configuration
@ComponentScan("io.sdd.services")
public class CourseServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApp.class);
    }
}
