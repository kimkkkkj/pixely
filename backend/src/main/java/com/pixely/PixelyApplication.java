package com.pixely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.imageio.ImageIO;

@EnableAsync
@ConfigurationPropertiesScan
@SpringBootApplication
public class PixelyApplication {

    public static void main(String[] args) {
        // WebP 등 classpath 의 ImageIO 플러그인을 확실하게 등록
        ImageIO.scanForPlugins();
        SpringApplication.run(PixelyApplication.class, args);
    }
}
