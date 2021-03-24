package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mono")
public class MonoController {

    @GetMapping("/and")
    public Mono<String> and(@RequestParam("name") String name) {
        // mono.and 丢弃前面的结果，执行and 里面的流，同样丢弃结果
        Mono<String> m = Mono.defer(() -> {
            System.out.println("1 hello " + name);
            return Mono.just("1 hello " + name);
        }).and(Mono.defer(() -> {
            System.out.println("2 hello " + name);
            return Mono.just("2 hello " + name);
        })).thenReturn("3 hello " + name);

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/when")
    public Mono<String> when(@RequestParam("name") String name) {
        // Mono.when 等待when 里面的所有流执行完成后，丢弃结果，并往下走
        Mono<String> m = Mono.when(
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
                })))
                .thenReturn("3 hello " + name);

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/just-or-empty")
    public Mono<String> justOrEmpty(@RequestParam(name = "name", required = false) String name) {
        // Mono.just() 不能接受 null 值
        // Mono.justOrEmpty() 可以接收 null 值，如果是 null 则相当于 Mono<Void>
        Mono<String> m = Mono.justOrEmpty(name).defaultIfEmpty("张三");
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/switch-if-empty")
    public Mono<String> switchIfEmpty(@RequestParam(name = "name", required = false) String name) {
        // Mono.just() 不能接受 null 值
        // Mono.justOrEmpty() 可以接收 null 值，如果是 null 则相当于 Mono<Void>

        // mono.defaultIfEmpty 如果流是 Mono<Void>， 直接给个默认值
        // mono.switchIfEmpty 如果流是 Mono<Void>， 用个默认流替换返回结果
        Mono<String> m = Mono.justOrEmpty(name).switchIfEmpty(Mono.defer(() -> Mono.just("张三")));
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/using")
    public Mono<String> using(@RequestParam(name = "name", required = false) String name) {
        // Mono.using(resourceSupplier, sourceSupplier, resourceCleanup)
        // 1 resourceSupplier 准备资源。例如打开网络流，打开文件流 等
        // 2 sourceSupplier 使用资源。资源准备好后，进行资源操作。如文件流准备好了后，开始读文件
        // 3 resourceCleanup 清理资源。如关闭流
        //
        // 这么做的好处在于，例如 网络IO 的建立需要耗费不少时间，分步处理可以等 网络IO 建立好了之后进行回调，避免CPU等待

        Mono<String> m = Mono.using(
                () -> {
                    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.yaml");
                    InputStreamReader reader = new InputStreamReader(is);
                    return new BufferedReader(reader);
                },
                br -> {
                    String s = br.lines().collect(Collectors.joining("\n"));
                    return Mono.just(s);
                },
                br -> {
                    try {
                        br.close();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });

        System.out.println("你好 " + name);
        return m;
    }

    // usingWhen
    // error
    // create
    // deferContextual
    // delay
    // empty
    // firstWithSignal
    // firstWithValue
    // ignoreElements
    // never
    // whenDelayError
    // zipDelayError

    // transform
    // cache
    // checkpoint
    // contextWrite
    // delayElement
    // delaySubscription
    // dematerialize
    // delayUntil
    // do....
    // filterWhen
    // cancelOn
    // cast
    // handle
    // hide
    // log
    // metrics
    // name
    // ofType
    // or
    // on...
    // publish
    // repeat
    // retry
    // share
    // single
    // subscribe
    // tag
    // take
    // timeout
    // as
    // block
    // concatWith
    // elapsed
    // expand
    // mergeWith
    // timed
    // toFuture


    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
