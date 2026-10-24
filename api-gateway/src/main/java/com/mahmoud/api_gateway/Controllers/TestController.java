package com.mahmoud.api_gateway.Controllers;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/api/test")
    public Mono<String> test(@AuthenticationPrincipal Jwt jwt) {
        return Mono.just(jwt.getClaims().toString());
    }

}