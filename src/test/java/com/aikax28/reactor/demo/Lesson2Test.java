
package com.aikax28.reactor.demo;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 *
 * @author aikax28
 */
class Lesson2Test {
    
    @Test
    void lesson2_1() {
        final Flux<Integer> range = Flux.range(1, 3);
        
        StepVerifier.create(range)
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    void lesson2_2() {
        final Flux<Integer> range = Flux.range(1, 3);
        
        StepVerifier.create(range)
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .verifyComplete();
    }
}
