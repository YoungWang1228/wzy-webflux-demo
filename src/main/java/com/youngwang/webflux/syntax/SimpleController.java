package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RestController
@RequestMapping("/simple")
public class SimpleController {


    // 注意，在webflux 中，接口只需要定义 Mono 或 Flux，无需订阅

    @GetMapping("/mono")
    public Mono<String> simpleMono(@RequestParam("name") String name) {
        // 简单示例，直接包装值然后返回
        return Mono.just("hello " + name);
    }

    @GetMapping("/mono/order/just")
    public Mono<String> simpleMonoJustOrder(@RequestParam("name") String name) {
        // 直接包装值,注意打印顺序
        // 和 defer 比较，这里是直接执行
        Mono<String> result = Mono.just(getResponse(name));
        System.out.println("已获取到 Mono<String> result ");
        return result;
    }

    @GetMapping("/mono/order/defer")
    public Mono<String> simpleMonoDeferOrder(@RequestParam("name") String name) {
        // 直接包装值,注意打印顺序
        // 和 just 比较，这里是延迟执行
        Mono<String> result = Mono.defer(() -> Mono.just(getResponse(name)));
        System.out.println("已获取到 Mono<defer> result ");
        return result;
    }

    private String getResponse(String name) {
        String response = "hello " + name;
        System.out.println(MessageFormat.format("调用 getResponse({0}) 返回值为 \"{1}\"", name, response));
        return response;
    }


    @GetMapping("/flux/just")
    public Flux<String> simpleFluxJust(@RequestParam("name") String name) {
        // 直接包装值
        // ！！！
        // 特别注意： 返回 Flux<String> ，会被合并为一个 String，而不是 String 数组
        // @see https://stackoverflow.com/questions/48421597/returning-fluxstring-from-spring-webflux-returns-one-string-instead-of-array-o
        // ！！！
        return Flux.just("hello ", name);
    }

    @GetMapping("/flux/just/string/array")
    public Mono<List<String>> simpleFluxJustStringArray(@RequestParam("name") String name) {
        // 直接包装值
        // ！！！
        // 特别注意： 返回 Flux<String> ，会被合并为一个 String，而不是 String 数组
        // @see https://stackoverflow.com/questions/48421597/returning-fluxstring-from-spring-webflux-returns-one-string-instead-of-array-o
        // ！！！
        return Flux.just("hello ", name).collectList();
    }

    @GetMapping("/flux/from")
    public Flux<Integer> simpleFluxFrom(@RequestParam("name") String name) {
        // 包装Stream返回
        // 类似的还有 Flux.fromArray , Flux.fromIterable
        return Flux.fromStream(
                Stream.of(name.split(""))
        ).map(s -> (int) s.charAt(0));
    }

    @GetMapping("/flux/range")
    public Flux<Integer> simpleFluxRange(@RequestParam("size") Integer size) {
        // range 会从 start 累加指定的次数，形成的元素放入流中
        return Flux.range(0, size);
    }


}
