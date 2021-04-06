package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Mono from 的用法示例
 */
@RestController
@RequestMapping("/mono/from")
public class MonoFromController {

    @GetMapping("mono")
    public Mono<String> fromMono(@RequestParam("name") String name) {
        // Mono.from(mono) 相当于脱裤子放屁。
        // 这个 API 的作用应该是将其他发布者流转换为 Mono
        return Mono.from(Mono.defer(() -> Mono.just("hello " + name)));
    }

    @GetMapping("flux")
    public Mono<Integer> fromFlux(@RequestParam("name") String name) {
        // Mono.from(flux) , 只取第一个值， 忽略其他， 相当于 blockFirst 的效果
        return Mono.from(Flux.defer(() ->
                Flux.fromStream(
                        Stream.of(name.split(""))
                                .map(s -> (int) s.charAt(0))
                                .peek(System.out::println)
                )
        ));
    }

    @GetMapping("callable")
    public Mono<String> fromCallable(@RequestParam("name") String name) {
        // Mono.fromCallable 基于一个 Callable 来创建

        Mono<String> m = Mono.fromCallable(() -> {
            System.out.println("fromCallable : hello " + name);
            return "hello " + name;
        });
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("future")
    public Mono<String> fromFuture(@RequestParam("name") String name) {
        // Mono.fromFuture 基于一个 Future 来创建
        Mono<String> m = Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
            System.out.println("fromFuture : hello " + name);
            return "hello " + name;
        }));
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("runnable")
    public Mono<String> fromRunnable(@RequestParam("name") String name) {
        // Mono.fromRunnable 基于一个 Runnable 来创建，并返回 空流

        Mono<String> m = Mono.fromRunnable(() -> {
            System.out.println("fromRunnable : hello " + name);
        }).thenReturn("hello " + name);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("supplier")
    public Mono<String> fromSupplier(@RequestParam("name") String name) {
        // Mono.fromSupplier ，类似 defer，有异步效果

        Mono<String> m = Mono.fromSupplier(() -> {
            System.out.println("fromSupplier : hello " + name);
            return "hello " + name;
        });
        System.out.println("你好 " + name);
        return m;
    }


    @GetMapping("future/concurrent")
    public Mono<String> fromFutureConcurrent(@RequestParam("name") String name) {
        // 执行无返回值的并发任务，执行完成后，then 返回结果
        // 对于并发执行，一般来说，不要在 Mono 的处理步骤里使用阻塞操作
        // 会导致线程挂起，吞吐量下降，因为一般配置的线程池大小会是CPU 的核数
        //
        // 所以 CompletableFuture.allOf 之后，不要执行 then、join 等操作
        // 直接 返回，交由 Mono 去处理，执行 then，
        // 这样就不会阻塞，是回调操作
        //
        Mono<String> m = Mono.fromFuture(
                () -> CompletableFuture.allOf(
                        CompletableFuture.runAsync(() -> {
                            sleep(500);
                            System.out.println("Future 1");
                        }),
                        CompletableFuture.runAsync(() -> {
                            sleep(1500);
                            System.out.println("Future 2");
                        }),
                        CompletableFuture.runAsync(() -> {
                            sleep(300);
                            System.out.println("Future 3");
                        })
                )

                // thenApply 方法将造成阻塞，不能这么使用
                // Mono 的方法需要无阻塞快速返回 ，需要阻塞的，交由主线程去callback
//                .thenApply(v -> {
//                    System.out.println("thenApply : hello " + name);
//                    return "hello " + name;
//                })
        ).then(Mono.fromSupplier(() -> {
            System.out.println("thenApply : hello " + name);
            return "hello " + name;
        }));

        System.out.println("你好 " + name);
        return m;
    }


    @GetMapping("future/concurrent/result")
    public Mono<String> fromFutureConcurrentResult(@RequestParam("name") String name) {
        // Mono.zip 用于合并多个流，合并后的得到 Tuple 元素
        // Tuple 可以获得每个流的结果

        Mono<String> m = Mono.zip(
                Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
                    sleep(500);
                    System.out.println("Future 1");
                    return "Future 1";
                })),
                Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
                    sleep(1500);
                    System.out.println("Future 2");
                    return "Future 2";
                })),
                Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
                    sleep(300);
                    System.out.println("Future 3");
                    return "Future 3";
                }))
        ).map(t -> t.getT1() + ",   " + t.getT2() + ",   " + t.getT3());
        System.out.println("你好 " + name);
        return m;
    }


    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
