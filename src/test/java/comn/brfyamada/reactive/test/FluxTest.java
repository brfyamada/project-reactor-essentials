package comn.brfyamada.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriber(){

        Flux<String>  fluxString = Flux.just("Bruno", "Thiago", "Felipe", "João")
                .log();

        StepVerifier.create(fluxString)
                .expectNext("Bruno", "Thiago", "Felipe", "João")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers(){

        Flux<Integer> flux = Flux.range(1, 5)
                .log();

        flux.subscribe(s -> log.info("Number {}", s));

        log.info("----------------Test-----------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersList(){

        Flux<Integer> flux = Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .log();

        flux.subscribe(s -> log.info("Number {}", s));

        log.info("----------------Test-----------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersError(){

        Flux<Integer> flux = Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .log()
                .map( i -> {

                    if(i == 4){
                        throw new IndexOutOfBoundsException("Index out of bound error");
                    }
                    return i;
                });

        flux.subscribe(s -> log.info("Number {}", s),
                Throwable::printStackTrace,
                () -> log.info("DONE"),
                subscription -> subscription.request(3)
        );

        log.info("----------------Test-----------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackpressure(){

        Flux<Integer> flux = Flux.range(1,10)
                .log();

        flux.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            private int requestCount = 2;
            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(2);

            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    subscription.request(requestCount);
                }

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        log.info("----------------Test-----------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }
    @Test
    public void fluxSubscriberNumbersNotSoUglyBackpressure(){

        Flux<Integer> flux = Flux.range(1,10)
                .log();

        flux.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(2);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if(count >= requestCount){
                    count =0;
                    request(2);
                }
            }
        });

        log.info("----------------Test-----------------");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void FLuxSubscriberIntervalOne() throws Exception{

        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .log();

        interval.subscribe(l -> log.info("Value {}", l));

        Thread.sleep(3000);

    }

    @Test
    public void FLuxSubscriberIntervalTwo() throws Exception{

        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofDays(1))
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();

    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1))
                .log();
    }

    @Test
    public void fluxSubscriberPrettyBackPressure() {

        Flux<Integer> flux = Flux.range(1,10)
                .log()
                .limitRate(3);

        flux.subscribe(s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("COMPLETED"));

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void connectableFlux() throws Exception {

        ConnectableFlux<Integer> connectableFlux = Flux.range(1,10)
                .delayElements(Duration.ofMillis(100))
                .log()
                .publish();

//        connectableFlux.connect();
//
//        log.info("Thread sleeping for 300ms");
//
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i -> log.info("Sub 1 number {}", i));
//
//        log.info("Thread sleeping for 200ms");
//
//        Thread.sleep(200);
//
//        connectableFlux.subscribe(i -> log.info("Sub 2 number {}", i));

        StepVerifier
                .create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(i -> i <= 5)
                .expectNext(6,7,8,9,10)
                .expectComplete()
                .verify();

    }

    @Test
    public void connectableFluxAutoConnect() throws Exception {

        Flux<Integer> fluxAutoConnect = Flux.range(1,5)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(2);

        StepVerifier
                .create(fluxAutoConnect)
                .then(fluxAutoConnect::subscribe)
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();

    }



}

