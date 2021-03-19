package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

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
        // Mono.fromCallable(mono) 基于一个 Callable 来创建
        // 也有延时的效果
        Mono<String> m = Mono.fromCallable(() -> {
            System.out.println("fromCallable : hello " + name);
            return "hello " + name;
        });
        System.out.println("你好 " + name);
        return m;
    }
}
