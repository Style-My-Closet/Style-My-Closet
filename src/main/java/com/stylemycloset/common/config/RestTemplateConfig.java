package com.stylemycloset.common.config;

import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        // JDK 기반 팩토리 + 타임아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);

        RestTemplate rt = new RestTemplate(factory);
        // 기본 헤더(UA/언어) 부착 인터셉터
        ClientHttpRequestInterceptor ua = (req, body, ex) -> {
            req.getHeaders().set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            req.getHeaders().set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            req.getHeaders().set("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            return ex.execute(req, body);
        };
        rt.setInterceptors(List.of(ua));
        return rt;
    }
}
