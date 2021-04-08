package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
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
        // mono#ofType 用于过滤，只保留目标类型，并将流做强制类型转换。相当于 filter + cast
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

        m.subscribe(s -> {
            System.out.println(s);
        });

        // 同个流，第二次订阅，直接返回结果，不再执行过程步骤
        m.subscribe(s -> {
            System.out.println(s);
        });

        return Mono.just(name);
    }

    @GetMapping("/handle")
    public Mono<String> handle(@RequestParam("name") String name) {
        // mono#handle，用 sink 来转换元素，重新发射信号。类似 Flux.generate
        Mono<String> m = Mono.just(name)
                .handle((s, sink) -> {
                    if ("张三".equals(s)) {
                        sink.next("hello " + s);
                    }
                });
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/to-future")
    public Mono<String> toFuture(@RequestParam("name") String name) {
        // mono#toFuture，字面意思
        CompletableFuture<String> f = Mono.defer(() -> {
            System.out.println("1 你好 " + name);
            return Mono.just("hello " + name).delayElement(Duration.ofSeconds(5));
        }).toFuture();
        System.out.println("你好 " + name);

        return Mono.fromFuture(() -> f);
    }

    @GetMapping("/retry")
    public Mono<String> retry(@RequestParam("name") String name) {
        // mono#retry，发生错误后，重试指定次数
        Mono<String> m = Mono.just(name)
                .map(s -> {
                    System.out.println("1 你好 " + s);
                    return s.toUpperCase();
                })
                .map(s -> {
                    System.out.println("2 你好 " + s);
                    if ("张三".equals(s)) {
                        return "hello " + s;
                    } else {
                        throw new RuntimeException("我只要张三");
                    }
                })
                .retry(1);
        System.out.println("你好 " + name);

        return m;
    }

    @GetMapping("/retry-when")
    public Mono<String> retryWhen(@RequestParam("name") String name) {
        // mono#retryWhen，自定义重试策略
        // 重试策略是一个 Flux，给入的是 Flux<RetrySignal> ，将元素转换成正常的元素，就会进行重试
        // 每重试一次，消费一个元素，流结束则重试结束，或者重试执行成功，没有发生异常则重试结束
        // 重试结束最好将异常抛出或记录日志，否则将返回空流，吞掉异常

        Retry customStrategy = Retry.from(companion -> companion.zipWith(
                Flux.range(1, 4).delayElements(Duration.ofSeconds(2)),
                (error, index) -> {
                    if (index < 4) return index;
                    else throw Exceptions.propagate(error.failure());
                })
        );

        AtomicInteger i = new AtomicInteger(2);
        Mono<String> m = Mono.just(name)
                .map(s -> {
                    System.out.println("1 你好 " + s);
                    return s.toUpperCase();
                })
                .map(s -> {
                    System.out.println("2 你好 " + s);
                    if (i.get() != 0) {
                        i.getAndDecrement();
                        throw new RuntimeException("我只要张三");
                    }
                    return s;
                })
                .retryWhen(customStrategy);
        System.out.println("你好 " + name);

        return m;
    }

    @GetMapping("/repeat")
    public Mono<List<String>> repeat(@RequestParam("name") String name) {
        // mono#repeat，指定重试次数，得到 Flux 流
        Mono<List<String>> m = Mono.just(name)
                .repeat(3)
                .collectList();
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/expand")
    public Mono<List<String>> expand(@RequestParam("name") String name) {
        // mono#expand，用于树的展开，广度优先，得到 Flux 流
        Mono<List<String>> m = Mono.just(buildTree())
                .expand(n -> {
                    return Flux.fromIterable(n.children);
                })
                .map(n -> n.id)
                .collectList();
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/expand-deep")
    public Mono<List<String>> expandDeep(@RequestParam("name") String name) {
        // mono#expandDeep，用于树的展开，深度优先，得到 Flux 流
        Mono<List<String>> m = Mono.just(buildTree())
                .expandDeep(n -> {
                    return Flux.fromIterable(n.children);
                })
                .map(n -> n.id)
                .collectList();
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/elapsed")
    public Mono<String> elapsed(@RequestParam("name") String name) {
        // mono#elapsed，将元素和流开始到当前步骤的耗时 合并为一个 Tuple2 对象
        Mono<String> m = Mono.defer(() -> {
            return Mono.just("hello " + name).delayElement(Duration.ofSeconds(5));
        })
                .elapsed()
                .map(t -> {
                    return t.getT2() + "， 用时 " + t.getT1() + " ms";
                });
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/share")
    public Mono<String> share(@RequestParam("name") String name) {
        // mono#share 缓存最终结果。share()方法(针对Flux)，cache()方法(针对Mono)
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
                .share();

        m.subscribe(s -> {
            System.out.println(s);
        });

        // 同个流，第二次订阅，直接返回结果，不再执行过程步骤
        m.subscribe(s -> {
            System.out.println(s);
        });

        return m;
    }

    @GetMapping("/take")
    public Mono<String> take(@RequestParam("name") String name) {
        // mono#take，只获取指定时间内产生的元素，超时的元素将被丢弃
        Mono<String> m = Mono.defer(() -> {
            return Mono.just("hello " + name).delayElement(Duration.ofSeconds(3));
        })
                .take(Duration.ofSeconds(1));
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/take-until-other")
    public Mono<String> takeUntilOther(@RequestParam("name") String name) {
        // mono#takeUntilOther，只获取指定时间内产生的元素，超时的元素将被丢弃
        // 这个指定的时间，就是 m2 发出完成信号的时间

        Mono<String> m1 = Mono.defer(() -> {
            return Mono.just("1 hello " + name).delayElement(Duration.ofSeconds(2));
        });

        Mono<String> m2 = Mono.defer(() -> {
            return Mono.just("2 hello " + name).delayElement(Duration.ofSeconds(3));
        });

        Mono<String> m = m1.takeUntilOther(m2);
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/delay-subscription")
    public Mono<String> delaySubscription(@RequestParam("name") String name) {
        // mono#delaySubscription，延时订阅

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
                .delaySubscription(Duration.ofSeconds(2));

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/as")
    public Mono<String> as(@RequestParam("name") String name) {
        // mono#as，做类型转换，类似 flatMap
        Mono<String> m = Mono.just("hello " + name)
                .flatMapMany(s -> Flux.fromArray(s.split("")))
                .as(f -> f.collect(Collectors.joining()));
        System.out.println("你好 " + name);
        return m;
    }


    @GetMapping("/metrics")
    public Mono<String> metrics(@RequestParam("name") String name) {
        // mono#metrics，监控指标，需要配合 Micrometer 使用，做应用的指标收集与监控
        Mono<String> m = Mono.just("hello " + name).metrics().name("test").tag("key", "hello");
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/subscribe")
    public Mono<String> subscribe(@RequestParam("name") String name) {
        // mono#subscribe，订阅
        // 一般情况下，在 webflux 环境中，不需要手动执行订阅操作
        // 方法返回后，框架会帮我们执行订阅操作

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
                });

        m.subscribe(s -> {
            System.out.println(s);
        });

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/subscribe-on")
    public Mono<String> subscribeOn(@RequestParam("name") String name) {
        // mono#subscribeOn，不会订阅流，只是指定调度器
        // subscribeOn 和 publishOn 的区别：
        //   subscribeOn 会影响上游操作，不管出现的位置在哪里，都会影响到整个流的调度器
        //   publishOn 用于切换后续步骤的调度器，不会影响上游操作，所以出现的位置决定了影响范围

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
                .subscribeOn(Schedulers.boundedElastic());

        m.subscribe(s -> {
            System.out.println(s);
        });

        System.out.println("你好 " + name);
        return m;
    }


    @GetMapping("/publish-on")
    public Mono<String> publishOn(@RequestParam("name") String name) {
        // mono#publishOn，切换调度器
        // subscribeOn 和 publishOn 的区别：
        //   subscribeOn 会影响上游操作，不管出现的位置在哪里，都会影响到整个流的调度器
        //   publishOn 用于切换后续步骤的调度器，不会影响上游操作，所以出现的位置决定了影响范围

        Mono<String> m = Mono.just(name)
                .filter(s -> {
                    System.out.println("1 你好 " + s);
                    return "张三".equals(s);
                })
                .publishOn(Schedulers.boundedElastic())
                .map(s -> {
                    System.out.println("2 你好 " + s);
                    return "hello " + s;
                })
                .delayElement(Duration.ofSeconds(3))
                .map(s -> {
                    System.out.println("3 你好 " + s);
                    return s.toUpperCase();
                });

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/do-on")
    public Mono<String> doOn(@RequestParam("name") String name) {
        // mono#doOn，doOn 操作用于窥视流，在不改变流的情况下，可以得到通知（只读）

        Mono<String> m = Mono.just(name)
                .map(s -> {
                    System.out.println("1 你好 " + s);
                    return "hello " + s;
                })

                // 每产生一个元素，就触发一次
                .doOnNext(s -> System.out.println("doOnNext " + s))

                .delayElement(Duration.ofSeconds(3))

                // 每一步操作就触发一次，包括完成操作
                .doOnEach(s -> System.out.println("doOnEach " + s.get() + ", " + s.toString()))
                .map(s -> {
                    System.out.println("2 你好 " + s);
                    return s.toUpperCase();
                })

                // 订阅时触发
                .doOnSubscribe(s -> System.out.println("doOnSubscribe"))

                // 成功完成时触发
                .doOnSuccess(s -> System.out.println("doOnSuccess " + s))

                // 结束时触发
                .doOnTerminate(() -> System.out.println("doOnTerminate"));
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/do-something")
    public Mono<String> doSomething(@RequestParam("name") String name) {
        // mono#doSomething，一次性事件触发

        Mono<String> m = Mono.just(name)
                .map(s -> {
                    System.out.println("1 你好 " + s);
                    return "hello " + s;
                })

                .delayElement(Duration.ofSeconds(3))

                // doFirst，订阅时触发。多个 doFirst 的执行顺序和声明顺序是反转的
                .doFirst(() -> System.out.println("doFirst three"))
                .doFirst(() -> System.out.println("doFirst two"))
                .doFirst(() -> System.out.println("doFirst one"))

                .map(s -> {
                    System.out.println("2 你好 " + s);
                    return s.toUpperCase();
                })

                // doAfterTerminate， 流结束后触发
                .doAfterTerminate(() -> System.out.println("doAfterTerminate"))

                // doFinally， 流结束后触发，这里可以拿到流是正常结束，还是异常或者取消，然后作出不同的逻辑
                .doFinally(st -> System.out.println("doFinally " + st.name()));

        System.out.println("你好 " + name);
        return m;
    }


    @GetMapping("/on-error-map")
    public Mono<String> onErrorMap(@RequestParam("name") String name) {
        // mono#on，on操作用于处理异常

        Mono<String> m = Mono.just(name)
                .map(s -> {
                    if ("张三".equals(s)) {
                        return "hello " + s;
                    } else {
                        throw new RuntimeException("我只要张三");
                    }
                })

                // onErrorMap 用于异常转换
                .onErrorMap(e -> new IllegalArgumentException(e.getMessage(), e))

                // 发生错误后，处理后返回新的值。类似 catch 操作
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just("fallback");
                })
                ;

        System.out.println("你好 " + name);
        return m;
    }


    @GetMapping("/on-error-continue")
    public Mono<String> onErrorContinue(@RequestParam("name") String name) {
        // mono#on，on操作用于处理异常

        Mono<String> m = Mono.just(name)
                .map(s -> {
                    if ("张三".equals(s)) {
                        return "hello " + s;
                    } else {
                        throw new RuntimeException("我只要张三");
                    }
                })

                // 将发生错误的元素移除，以便恢复流。 传入的参数为 (异常，发生错误的元素)
                // 在mono中，移除了元素，就变空流了
                .onErrorContinue((e, o) -> {
                    e.printStackTrace();
                    System.out.println("onErrorContinue " + o);
                })
                ;

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/on-error-return")
    public Mono<String> onErrorReturn(@RequestParam("name") String name) {
        // mono#on，on操作用于处理异常

        Mono<String> m = Mono.just(name)
                .map(s -> {
                    if ("张三".equals(s)) {
                        return "hello " + s;
                    } else {
                        throw new RuntimeException("我只要张三");
                    }
                })

                // 发生错误后，返回默认值
                .onErrorReturn("fallback")
                ;

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

    /**
     * 构建树结构
     *
     * @return 根节点
     */
    public Node buildTree() {
        Node root = new Node("1");

        Node n11 = new Node("1-1");
        n11.addChild(new Node("1-1-1"))
                .addChild(new Node("1-1-2"))
                .addChild(new Node("1-1-3"))
                .addChild(new Node("1-1-4"));

        Node n12 = new Node("1-2");
        n12.addChild(new Node("1-2-1"))
                .addChild(new Node("1-2-2"))
                .addChild(new Node("1-2-3"))
                .addChild(new Node("1-2-4"));


        Node n13 = new Node("1-3");
        n13.addChild(new Node("1-3-1"))
                .addChild(new Node("1-3-2"))
                .addChild(new Node("1-3-3"))
                .addChild(new Node("1-3-4"));

        root.addChild(n11).addChild(n12).addChild(n13);
        return root;
    }

    /**
     * 树节点
     */
    public static class Node {
        String id;
        List<Node> children = new ArrayList<>();

        public Node(String id) {
            this.id = id;
        }

        public Node addChild(Node n) {
            children.add(n);
            return this;
        }
    }
}
