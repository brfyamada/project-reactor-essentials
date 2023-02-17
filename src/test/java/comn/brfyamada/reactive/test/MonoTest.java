package comn.brfyamada.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@Slf4j
/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * until:
 * 1. Publisher sends all the objects requested
 * 2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be canceled
 * 3. There is an error. (onError) -> subscriber and subbscription will be canceled
 *
 */
public class MonoTest {

    @Test
    public void monoSubscriber(){
        log.info("Everything working as intended");

        String name = "Bruno";

        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

    }


    @Test
    public void monoSubscriberConsumer() {
        String name = "Bruno";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Name: {}", s ));
        log.info("------------------------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Bruno";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing mono with error");});

        mono.subscribe(s -> log.info("Name: {}", s ), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name: {}", s ), Throwable::printStackTrace);
        log.info("------------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();

    }

    @Test
    public void monoSubscriberConsumerComplete(){
        String name = "Bruno";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase());

        mono.subscribe(s -> log.info("Name: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED")
                );

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        String name = "Bruno";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase());

        mono.subscribe(
                s -> log.info("Name: {}", s), // onNext
                Throwable::printStackTrace, // onError
                () -> log.info("FINISHED"), //  onComplete
                subscription -> subscription.request(5) // onSubscription
        );

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }


    @Test
    public void monoDoOnMethods() {

        String name = "Bruno";

        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something...."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s -> log.info("doOnSuccess successfully"));

        mono.subscribe(s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));

    }

    @Test
    public void monoDoOnError() {

        String name = "Bruno";

        Mono<Object> error = Mono.error(new IllegalArgumentException())

                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void monoDoOnErrorReturn() {

        String name = "Bruno";

        Mono<Object> error = Mono.error(new IllegalArgumentException())
                .onErrorReturn("Empty")
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("Empty")
                .verifyComplete();

    }


}

