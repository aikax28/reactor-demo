package com.aikax28.reactor.demo;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

/**
 * subscriberContext は後に設定されたものが呼ばれる
 *
 * @author aikax28
 */
class Lesson1Test {

    @Test
    void lesson1_1(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final int a = 5;
        final Mono<Integer> callable = Mono.fromCallable(() -> a);
        StepVerifier.create(callable).expectNext(5).verifyComplete();
    }

    int a = 5;

    /**
     *
     * @see https://stackoverflow.com/questions/55955567/what-does-mono-defer-do
     */
    @Test
    void lesson1_2(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<Integer> callable = Mono.fromCallable(() -> {
            System.out.println(a);
            return a;
        });

        final Mono<Integer> just = Mono.just(a);

        StepVerifier.create(callable).expectNext(5).verifyComplete();
        StepVerifier.create(just).expectNext(5).verifyComplete();

        a = 6;

        StepVerifier.create(callable).expectNext(6).verifyComplete();
        StepVerifier.create(just).expectNext(5).verifyComplete();

    }

    /**
     *
     */
    @Test
    void lesson1_3(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<Integer> callable = Mono.fromCallable(() -> {
            System.out.println(a);
            return a;
        });

        System.out.println("================ debug " + name + "================");
        log(callable.subscriberContext(c -> {
            System.out.println("subscriberContext : " + name);
            return c;
        }), name).subscribe();
        System.out.println("================ debug " + name + "================");
    }

    /**
     *
     */
    @Test
    void lesson1_4(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<Integer> callable = Mono.fromCallable(() -> {
            System.out.println(1);
            return 1;
        }).then(Mono.fromCallable(() -> {
            System.out.println(2);
            return 2;
        }));

        System.out.println("================ debug " + name + "================");
        log(callable.subscriberContext(c -> {
            System.out.println("subscriberContext : " + name);
            return c;
        }), name).subscribe();
        System.out.println("================ debug " + name + "================");
    }

    /**
     * subscriberContext は後に設定されたものが呼ばれる
     */
    @Test
    void lesson1_5(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        Mono<Integer> callable = log(Mono.fromCallable(() -> {
            System.out.println(1);
            return 1;
        }), name);
        Mono<Integer> callable2 =Mono.fromCallable(() -> {
            System.out.println(2);
            return 2;
        });
        
        callable = callable.then(callable2);

        System.out.println("================ debug " + name + "================");
        log(callable.subscriberContext(c -> {
            System.out.println("subscriberContext : " + name);
            return c;
        }), name).subscribe();
        System.out.println("================ debug " + name + "================");
    }

    @Test
    void lesson1_6_1(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<Integer> callable1 = log(Mono.fromCallable(() -> {
            System.out.println(1);
            return 1;
        }), name);
        final Mono<Integer> callable2 = log(Mono.fromCallable(() -> {
            System.out.println(2);
            return 2;
        }), name);

        System.out.println("================ debug " + name + "================");
        List<Mono<Integer>> callables = new ArrayList<>();
        callables.add(callable1);
        callables.add(callable2);

        Mono<Integer> result = Mono.<Integer>first(callables);

        log(result.subscriberContext(c -> {
            System.out.println("subscriberContext : " + name);
            return c;
        }), name).subscribe();
        System.out.println("================ debug " + name + "================");
    }

    @Test
    void lesson1_6_2(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<Integer> callable1 = log(Mono.fromCallable(() -> {
            System.out.println("null");
            return null;
        }), name);
        callable1.subscribeOn(Schedulers.parallel());
        
        final Mono<Integer> callable2 = log(Mono.fromCallable(() -> {
            System.out.println(2);
            return 2;
        }), name).subscribeOn(Schedulers.parallel());

        System.out.println("================ debug " + name + "================");
        List<Mono<Integer>> callables = new ArrayList<>();
        callables.add(callable1);
        callables.add(callable2);

        Mono<Integer> result = Mono.<Integer>first(callables);

        log(result.subscriberContext(c -> {
            System.out.println("subscriberContext : " + name);
            return c;
        }), name).subscribe();
        System.out.println("================ debug " + name + "================");
    }

    @Test
    void lesson1_7(TestInfo testInfo) {
        final String name = testInfo.getTestMethod().get().getName();
        final Mono<Integer> callable1 = log(Mono.fromCallable(() -> {
            System.out.println(name + " : " + 1);
            return 1;
        }), name).subscribeOn(Schedulers.parallel());

        final Mono<Integer> callable2 = log(Mono.fromCallable(() -> {
            System.out.println(name + " : " + 2);
            return 2;
        }), name).subscribeOn(Schedulers.parallel());

        System.out.println("================ debug " + name + "================");
        Mono<Tuple2<Integer, Integer>> result = Mono.zip(callable1, callable2);
        log(result.subscriberContext(c -> {
            System.out.println("subscriberContext : " + name);
            return c;
        }), name).subscribe();
        System.out.println("================ debug " + name + "================");
    }

    private <T> Mono<T> log(Mono<T> mono, String name) {
        return mono.subscriberContext(c -> {
            System.out.println("subscriberContext : log():" + name + "," + Thread.currentThread().getName());
            return c;
        })
                .doOnSubscribe(r -> System.out.println("doOnSubscribe :" + name + "," + Thread.currentThread().getName()))
                .doOnRequest(r -> System.out.println("doOnRequest :" + name + "," +  Thread.currentThread().getName()))
                // ↑上記までは処理が走る前にすべてのMonoの処理が実行される
                // ここで処理が走る
                .doOnCancel(() -> System.out.println("doOnCancel :" + name + "," +  Thread.currentThread().getName()))
                .doOnNext(r -> System.out.println("doOnNext :" + name + "," +  Thread.currentThread().getName()))
                .doOnSuccess(r -> System.out.println("doOnSuccess :" + name + "," +  Thread.currentThread().getName()))
                .doAfterTerminate(() -> System.out.println("doAfterTerminate :" + name + "," +  Thread.currentThread().getName()))
                .doOnTerminate(() -> System.out.println("doOnTerminate :" + name + "," +  Thread.currentThread().getName()))
                .doFinally(r -> System.out.println("doFinally :" + name + "," +  Thread.currentThread().getName()));
    }
}
