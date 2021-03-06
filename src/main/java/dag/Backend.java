package dag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Backend {
    public static void main(String[] args) {
        SpringApplication.run(Backend.class,
                "--spring.application.name=Backend",
                "--server.port=8080");
    }

    @RequestMapping("/api")
    public String getApi() {
        return "Some text";
    }

    @Bean
    public Sampler sampler() {
        return new AlwaysSampler();
    }
}
