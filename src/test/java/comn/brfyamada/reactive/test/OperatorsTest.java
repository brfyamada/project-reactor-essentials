package comn.brfyamada.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnSimple() {

        Flux<Integer> flux = Flux.range(1,5)
                .map( i -> {
                    log.info("Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.elastic())
                .map(i -> {
                    log.info("Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .verifyComplete();

    }

    @Test
    public void publishOnSimple() {

        Flux<Integer> flux = Flux.range(1,5)
                .map( i -> {
                    log.info("Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .verifyComplete();

    }

    @Test
    public void multipleSubscribeOnSimple() {
        // Aqui sempre o primeiro subscribe é utilizado para todos os operators
        Flux<Integer> flux = Flux.range(1,4)
                .subscribeOn(Schedulers.single())
                .map( i -> {
                    log.info("Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .verifyComplete();

    }

    @Test
    public void multiplePublishOnSimple() {
        // Aqui sempre o primeiro subscribe é utilizado para todos os operators
        Flux<Integer> flux = Flux.range(1,4)
                .publishOn(Schedulers.single())
                .map( i -> {
                    log.info("Map 1 - Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();

    }

    @Test
    public void publishAndSubscribeOnSimple() {
        // Aqui sempre o primeiro subscribe é utilizado para todos os operators
        Flux<Integer> flux = Flux.range(1,4)
                .publishOn(Schedulers.single())
                .map( i -> {
                    log.info("Map 1 - Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();

    }

    @Test
    public void subscribeAndPublishOnSimple() {
        // Aqui sempre o primeiro subscribe é utilizado para todos os operators
        Flux<Integer> flux = Flux.range(1,4)
                .subscribeOn(Schedulers.single())
                .map( i -> {
                    log.info("Map 1 - Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number: {}, Thread name: {}", i , Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void subscribeOnIO() throws Exception {
        Mono<List<String>> list = Mono.fromCallable(() -> Files.readAllLines(Path.of("text-file")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());

        list.subscribe(s -> log.info("{}", s));

        StepVerifier
                .create(list)
                .expectSubscription()
                .thenConsumeWhile(l -> {
                    Assertions.assertFalse(l.isEmpty());
                    log.info("Size {}", l.size());
                    return true;
                })
                .verifyComplete();

    }

    @Test
    public void switchIfEmptyOperator() {
        Flux<Object> flux = emptyFlux()
                .switchIfEmpty(Flux.just("Hello World"))
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("Hello World")
                .expectComplete()
                .verify();
    }

    private Flux<Object> emptyFlux() {
        return Flux.empty();
    }

    @Test
    public void deferOperator() throws InterruptedException {

        //Mono<Long> mono = Mono.just(System.currentTimeMillis());
        Mono<Long> mono = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

        mono.subscribe(i -> log.info("Valeu: {}", i));
        Thread.sleep(100);
        mono.subscribe(i -> log.info("Valeu: {}", i));
        Thread.sleep(100);
        mono.subscribe(i -> log.info("Valeu: {}", i));
        Thread.sleep(100);
        mono.subscribe(i -> log.info("Valeu: {}", i));

    }

    @Test
    public void concatOperator() {

        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concat(flux1, flux2)
                .log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","b","c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatWithOperator() {

        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatWithFlux = flux1.concatWith(flux2).log();


        StepVerifier.create(concatWithFlux)
                .expectSubscription()
                .expectNext("a","b","c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatOperatorError() {

        Flux<String> flux1 = Flux.just("a", "b")
                .map(s -> {
                    if(s.equals("b")){
                        throw new IllegalArgumentException();
                    }
                    return s;
                });

        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concatDelayError(flux1, flux2)
                .log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void combineLatestOperator() {

        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> combineLatestFlux = Flux.combineLatest(flux1,flux2, (s1,s2) -> s1.toUpperCase() + s2.toUpperCase())
                        .log();


        StepVerifier.create(combineLatestFlux)
                .expectSubscription()
                .expectNext("BC", "BD")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeOperator() throws InterruptedException {

        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.merge(flux1, flux2)
                .delayElements(Duration.ofMillis(200))
                .log();

        mergeFlux.subscribe(log::info);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWithOperator() throws InterruptedException {

        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = flux1.mergeWith(flux2)
                .delayElements(Duration.ofMillis(200))
                .log();

        mergeFlux.subscribe(log::info);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeSequentialOperator() throws InterruptedException {

        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.mergeSequential(flux1, flux2)
                .delayElements(Duration.ofMillis(200))
                .log();

        mergeFlux.subscribe(log::info);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("a","b","c","d")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeDelayErrorOperator() throws InterruptedException {

        Flux<String> flux1 = Flux.just("a", "b")
                .map(s -> {
                    if(s.equals("b")){
                        throw new IllegalArgumentException();
                    }
                    return s;
                });
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.mergeDelayError(1, flux1, flux2, flux1)
                .delayElements(Duration.ofMillis(200))
                .log();

        mergeFlux.subscribe(log::info);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("a","c","d","a")
                .expectError()
                .verify();

    }

    @Test
    public void flatMapOperator() throws InterruptedException {

        Flux<String> flux = Flux.just("a", "b")
                .map(String::toUpperCase)
                .flatMap(this::findByName)
                .log();

        flux.subscribe(log::info);

        Thread.sleep(500);

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("nameB1", "nameB2","nameA1", "nameA2")
                .verifyComplete();

    }

    @Test
    public void flatMapSequentialOperator() throws InterruptedException {

        Flux<String> flux = Flux.just("a", "b")
                .map(String::toUpperCase)
                .flatMapSequential(this::findByName)
                .log();

        flux.subscribe(log::info);

        Thread.sleep(500);

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("nameA1", "nameA2","nameB1", "nameB2")
                .verifyComplete();

    }

    private Flux<String> findByName(String name) {
        return name.equals("A") ? Flux.just("nameA1", "nameA2").delayElements(Duration.ofMillis(100)) : Flux.just("nameB1", "nameB2");
    }

}



