package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Mono 类的各种方法示例
 */
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

    @GetMapping("/empty")
    public Mono<String> empty(@RequestParam(name = "name", required = false) String name) {
        // Mono.empty 直接终止的流
        System.out.println("你好 " + name);
        return Mono.empty();
    }

    @GetMapping("/never")
    public Mono<String> never(@RequestParam(name = "name", required = false) String name) {
        // Mono.never 没内容的流，没有取消，没有终止。
        // 具体使用场景不明

        System.out.println("你好 " + name);
        // 直接这样返回的话，会一直处于等待状态
        return Mono.never();
    }

    @GetMapping("/create")
    public Mono<String> create(@RequestParam(name = "name", required = false) String name) {
        // Mono.create 通过 MonoSink 来创建流
        // 可实现灵活的事件监听设计
        Mono<String> m = Mono.create(sink -> {
            sink.success("hello " + name);
        });
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/delay")
    public Mono<String> delay(@RequestParam(name = "name", required = false) String name) {
        // Mono.delay 延迟指定的时间，之后发出元素
        Mono<String> m = Mono.delay(Duration.ofSeconds(2)).map(l -> "hello " + name);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/ignore-elements")
    public Mono<String> ignoreElements(@RequestParam(name = "name", required = false) String name) {
        // Mono.ignoreElements 忽略所有的元素，例如执行完毕后丢弃值
        // 用 then 可以达到同样的目的
        Mono<String> m = Mono.ignoreElements(Flux.just(1, 2, 3, 4, 5)).thenReturn("hello " + name);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/timeout")
    public Mono<String> timeout(@RequestParam(name = "name", required = false) String name) {
        // mono#timeout 指定等待时间，超时还未发出元素，将抛出 TimeoutException
        Mono<String> m = Mono.defer(() -> {
            // 延时2秒发出元素
            return Mono.delay(Duration.ofSeconds(2)).map(l -> "hello " + name);
        })
                // 1秒后还未得到元素，将抛出 TimeoutException
                .timeout(Duration.ofSeconds(1));
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/single")
    public Mono<String> single(@RequestParam(name = "name", required = false) String name) {
        // mono#single 期望流有一个元素，如果是空流，则抛出 NoSuchElementException
        Mono<String> m = Mono.justOrEmpty(name).single();
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/filter-when")
    public Mono<String> filterWhen(@RequestParam(name = "name", required = false) String name) {
        // mono#filterWhen 和 filter 的区别在于，一个返回 boolean ，一个返回 Mono<Boolean>
        Mono<String> m = Mono.justOrEmpty(name).filterWhen(str -> {
            return Mono.just("李四".equals(str));
        })
                .map(str -> "hello " + str);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/cast")
    public Mono<String> cast(@RequestParam(name = "name", required = false) String name) {
        // mono#cast 用于强转，cast(String.class) 等价于 map(o -> (String)o )
        Object obj = "hello " + name;
        Mono<String> m = Mono.just(obj).cast(String.class);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/of-type")
    public Mono<String> ofType(@RequestParam(name = "name", required = false) String name) {
        // mono#ofType 用户过滤，只保留目标类型，并将流做强制类型转换。相当于 filter + cast
        Object obj = "hello " + name;
        Mono<String> m = Mono.just(obj).ofType(String.class);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/or")
    public Mono<String> or(@RequestParam(name = "name", required = false) String name) {
        // mono#or 两个流，谁先发出信号就用哪个

        Mono<String> m1 = Mono.defer(() -> {
            return Mono.delay(Duration.ofSeconds(2)).then(Mono.justOrEmpty(name));
        });
        Mono<String> m2 = Mono.defer(() -> {
            return Mono.delay(Duration.ofSeconds(1)).then(Mono.just("李四"));
        });

        Mono<String> m = m1.or(m2).map(str -> "hello " + str);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/merge-with")
    public Mono<List<String>> mergeWith(@RequestParam(name = "name", required = false) String name) {
        // mono#mergeWith 连接 Mono，形成 flux。会有交错的情况，先放置的元素先进入流
        Mono<String> m1 = Mono.defer(() -> {
            return Mono.delay(Duration.ofSeconds(2)).then(Mono.justOrEmpty(name));
        });
        Mono<String> m2 = Mono.defer(() -> {
            return Mono.just("这是新的编程方式");
        });

        Mono<List<String>> m = Mono.just("hello ")
                .mergeWith(m1)
                .mergeWith(m2)
                .collectList();
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/concat-with")
    public Mono<List<String>> concatWith(@RequestParam(name = "name", required = false) String name) {
        // mono#concatWith 连接 Mono，形成 flux。不会有交错的情况，将严格保证顺序
        Mono<String> m1 = Mono.defer(() -> {
            return Mono.delay(Duration.ofSeconds(2)).then(Mono.justOrEmpty(name));
        });
        Mono<String> m2 = Mono.defer(() -> {
            return Mono.just("这是新的编程方式");
        });

        Mono<List<String>> m = Mono.just("hello ")
                .concatWith(m1)
                .concatWith(m2)
                .collectList();
        System.out.println("你好 " + name);

        return m;
    }

    @GetMapping("/log")
    public Mono<String> log(@RequestParam(name = "name", required = false) String name) {
        // mono#log 日志打印。将打印处理的详细信息
        Mono<String> m = Mono.justOrEmpty(name).log();
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/delay-element")
    public Mono<String> delayElement(@RequestParam(name = "name", required = false) String name) {
        // mono#delayElement 延迟指定的时间
        return Mono.defer(() -> {
            System.out.println("你好 " + name);
            return Mono.just("hello " + name);
        }).delayElement(Duration.ofSeconds(3));
    }

    @GetMapping("/delay-until")
    public Mono<String> delayUntil(@RequestParam(name = "name", required = false) String name) {
        // mono#delayUntil 顺序执行，丢弃第二个流的结果
        return Mono.defer(() -> {
            System.out.println("1 你好 " + name);
            return Mono.just("hello " + name).delayElement(Duration.ofSeconds(5));
        }).delayUntil(s -> {
            System.out.println("2 你好 " + name);
            return Mono.just(1).delayElement(Duration.ofSeconds(3));
        });
    }

    @GetMapping("/transform")
    public Mono<String> transform(@RequestParam("name") String name) {
        // mono#transform 将链式操作方到 function 里面去做。可用于抽象公共代码
        // 和 transformDeferred 对比，注意打印的顺序。

        Mono<String> m = Mono.just(name).transform(m1 -> {
            System.out.println("1 你好 " + name);
            return m1.filter("张三"::equals)
                    .map(s -> "hello " + s)
                    .map(String::toUpperCase);
        });

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/transform-deferred")
    public Mono<String> transformDeferred(@RequestParam("name") String name) {
        // mono#transformDeferred 将链式操作方到 function 里面去做，有延迟执行的效果。可用于抽象公共代码
        // 和 transform 对比，注意打印的顺序。

        Mono<String> m = Mono.just(name).transformDeferred(m1 -> {
            System.out.println("1 你好 " + name);
            return m1.filter("张三"::equals)
                    .map(s -> "hello " + s)
                    .map(String::toUpperCase);
        });

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/cache")
    public Mono<String> cache(@RequestParam("name") String name) {
        // mono#cache 缓存最终结果
        Mono<String> m = Mono.just(name)
                .filter(s -> {
                    System.out.println("1 你好 " + s);
                    return "张三".equals(s);
                })
                .map(s -> {
                    System.out.println("2 你好 " + s);
                    return "hello " + s;
                })
                .delayElement(Duration.ofSeconds(3))
                .map(s -> {
                    System.out.println("3 你好 " + s);
                    return s.toUpperCase();
                })
                .cache();

        m.subscribe(s->{
            System.out.println(s);
        });

        // 同个流，第二次订阅，直接返回结果，不再执行过程步骤
        m.subscribe(s->{
            System.out.println(s);
        });

        return Mono.just(name);
    }


    // checkpoint
    // delaySubscription
    // dematerialize
    // do....
    // cancelOn
    // handle
    // hide
    // metrics
    // name
    // tag
    // on...
    // publish
    // repeat
    // retry
    // share
    // subscribe
    // take
    // as
    // block
    // elapsed
    // expand
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
