package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Stream;

@RestController
@RequestMapping("/then")
public class ThenController {

    @GetMapping
    public Mono<Void> then(@RequestParam("name") String name) {
        // then() 忽略前面的流，返回 void
        // 但 步骤依然会执行
        return Mono.just("hello " + name)
                .map(s -> {
                    System.out.println("then map : " + s);
                    return s;
                })
                .then();
    }

    @GetMapping("/mono")
    public Mono<String> thenMono(@RequestParam("name") String name) {
        // then(Mono) 忽略前面的流，返回 指定的 流
        // 但 步骤依然会执行
        return Mono.just("hello " + name)
                .map(s -> {
                    System.out.println("then map : " + s);
                    return s;
                })
                // defer 用于延时加载
                .then(Mono.defer(() -> Mono.just("你好" + name)));
    }

    @GetMapping("/return")
    public Mono<String> thenReturn(@RequestParam("name") String name) {
        // thenReturn() 忽略前面的流，返回指定的值
        // 但 步骤依然会执行
        return Mono.just("hello " + name)
                .map(s -> {
                    System.out.println("then map : " + s);
                    return s;
                })
                .thenReturn("你好" + name);
    }

    @GetMapping("/many")
    public Flux<Integer> thenMany(@RequestParam("name") String name) {
        // thenMany() 忽略前面的流，返回 指定的 Flux
        // 但 步骤依然会执行
        return Mono.just("hello " + name)
                .map(s -> {
                    System.out.println("then map : " + s);
                    return s;
                })
                .thenMany(Flux.defer(() -> Flux.fromStream(
                        Stream.of(name.split(""))
                                .map(s -> (int) s.charAt(0))
                )));
    }

    @GetMapping("/empty")
    public Mono<Void> thenEmpty(@RequestParam("name") String name) {
        // thenEmpty() 忽略前面的流，然后执行一个任务后，忽略
        // 但 步骤依然会执行
        // 常用来执行不需要获取结果的任务
        return Mono.just("hello " + name)
                .map(s -> {
                    System.out.println("then map : " + s);
                    return s;
                })
                .thenEmpty(Mono.defer(() ->
                        Flux.range(0, 10)
                                // 每个元素间隔 1秒
                                .delayElements(Duration.ofSeconds(1))
                                .then()
                ));
    }

}
