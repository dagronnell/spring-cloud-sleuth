package dag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class FrontEnd {
    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) {
        SpringApplication.run(FrontEnd.class,
                "--spring.application.name=Frontend",
                "--server.port=8081");
    }

    @RequestMapping("/frontend")
    public String getFrontEnd() {
        return "From backend: " + restTemplate
                .getForObject("http://localhost:8080/api", String.class);
    }

    @Bean
    public Sampler sampler() {
        return new AlwaysSampler();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
