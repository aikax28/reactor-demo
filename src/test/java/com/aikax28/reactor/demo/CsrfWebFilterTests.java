package com.aikax28.reactor.demo;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 *
 * @author aikax28
 */
class CsrfWebFilterTests {
    
    @Test
    void allowMethod() {
        final AtomicBoolean isSuccess = new AtomicBoolean(true);
        
        Mono<String> result = Mono.just(isSuccess)
                .filter(m -> m.get())
                .flatMap(m -> Mono.just("match"))
                .switchIfEmpty(Mono.just("notMatch"));
        
        StepVerifier.create(result)
                .expectNext("match")
                .verifyComplete();
        
        isSuccess.set(false);
        StepVerifier.create(result)
                .expectNext("notMatch")
                .verifyComplete();
    }
    
    @Test
    void validateTokenTestTokenNotEmptyAndValid() {
        final Mono<String> token = Mono.just("token");
        final Mono<Boolean> isValid = Mono.just(true);
        final Mono<Void> result = validateToken(token, isValid);
        
        StepVerifier.create(result)
                .verifyComplete();
    }
    
    @Test
    void validateTokenTestTokenNotEmptyAndInValid() {
        final Mono<String> token = Mono.just("token");
        final Mono<Boolean> isValid = Mono.just(false);
        final Mono<Void> result = validateToken(token, isValid);
        
        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Invalid CSRF Token")
                ).verify();
    }
    
    @Test
    void validateTokenTestTokenAndValid() {
        final Mono<String> token = Mono.empty();
        final Mono<Boolean> isValid = Mono.just(true);
        final Mono<Void> result = validateToken(token, isValid);
        
        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("CSRF Token has been associated to this client")
                ).verify();
    }
    
    @Test
    void validateTokenTestTokenAndInValid() {
        final Mono<String> token = Mono.empty();
        final Mono<Boolean> isValid = Mono.just(false);
        final Mono<Void> result = validateToken(token, isValid);
        
        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("CSRF Token has been associated to this client")
                ).verify();
    }
    
    @Test
    void filterForEmpty(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<String> token = Mono.empty();
        final Mono<Void> result = filter(true, true, token);
        
        System.out.println("================ debug " + name + "================");
        StepVerifier.create(result).verifyComplete();
        System.out.println("================ debug " + name + "================");
    }

    @Test
    void filterForNotEmpty(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<String> token = Mono.fromCallable(() -> {
            System.out.println("call");
            return "success";
        });
        final Mono<Void> result = filter(true, true, token);
        
        System.out.println("================ debug " + name + "================");
        StepVerifier.create(result).verifyComplete();
        System.out.println("================ debug " + name + "================");
    }
    
    @Test
    void filterForNotEmptyFirstFalse(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<String> token = Mono.fromCallable(() -> {
            System.out.println("call");
            return "success";
        });
        final Mono<Void> result = filter(false, true, token);
        
        System.out.println("================ debug " + name + "================");
        StepVerifier.create(result).verifyComplete();
        System.out.println("================ debug " + name + "================");
    }

    
    @Test
    void filterForNotEmptySecondFalse(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<String> token = Mono.fromCallable(() -> {
            System.out.println("call");
            return "success";
        });
        final Mono<Void> result = filter(true, false, token);
        
        System.out.println("================ debug " + name + "================");
        StepVerifier.create(result).verifyComplete();
        System.out.println("================ debug " + name + "================");
    }

    private Mono<Void> validateToken(Mono<String> token, Mono<Boolean> isValid) {
        return token
                .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("CSRF Token has been associated to this client"))))
                .filterWhen(expected -> isValid)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Invalid CSRF Token"))))
                .then();
    }
    
    private Mono<Void> filter(boolean isFirst, boolean isSecond, Mono<String> successToken) {
        return Mono.just("start")
                .filter(m -> isFirst)
                .filter(m -> isSecond)
                .flatMap(m -> successToken)
                .switchIfEmpty(handle("switchIfEmpty"))
                .onErrorResume(IllegalArgumentException.class, e -> handle("onErrorResume"))
                .then();
    }
    
    private Mono<String> handle(String name) {
        return Mono.defer(() -> {
            System.out.println(name);
            return Mono.just(name);
        });
    }
}
