package com.youngwang.webflux.syntax;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/zip")
public class ZipController {

    @GetMapping("/tuple2")
    public Mono<String> tuple2(@RequestParam("name") String name) {
        Mono<String> m1 = Mono.just("Mono.just");
        Mono<String> m2 = Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
            sleep(1500);
            System.out.println("Future 2");
            return "Future 2";
        }));

        // Mono.zip 对流进行合并，并返回 tuple 用于后续处理
        Mono<String> m = Mono.zip(m1, m2)
                .map(t -> t.getT1() + ", " + t.getT2());

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/tuple8")
    public Mono<String> tuple8(@RequestParam("name") String name) {
        // Mono.zip 对流进行合并，并返回 tuple 用于后续处理
        // 从 tuple2 到 tuple8，对应不同的参数数量
        Mono<String> m = Mono.zip(
                Mono.just("1 Mono.just"),
                Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
                    sleep(1500);
                    System.out.println("Future 2");
                    return "2 Mono.fromFuture";
                })),
                Mono.defer(() -> Mono.just("3 Mono.defer")),
                Mono.from(Mono.just("4 Mono.from")),
                Mono.fromDirect(Mono.just("5 Mono.fromDirect")),
                Mono.create(sink -> {
                    sink.success("6 Mono.create");
                }),
                Mono.fromSupplier(() -> {
                    System.out.println("Supplier 7");
                    return "7 Mono.fromSupplier";
                }),
                Mono.fromCallable(() -> {
                    sleep(1000);
                    System.out.println("Callable 8");
                    return "8 Mono.fromCallable";
                })
        )
                .map(t -> t.toList().stream()
                        .map(o -> (String) o)
                        .collect(Collectors.joining("\n")));

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/tuples")
    public Mono<String> tuples(@RequestParam("name") String name) {
        // Mono.zip(Function, Mono...) 后面参数的 Mono 的值，合并到第一个参数，形成数组，处理后，得到返回值
        Mono<String> m = Mono.zip(objs -> {
                    return Arrays.stream(objs).map(o -> (String) o).collect(Collectors.joining("\n"));
                },
                Mono.just("1 Mono.just"),
                Mono.just("2 Mono.just")
        );
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/tuples2")
    public Mono<String> tuples2(@RequestParam("name") String name) {
        // Mono.zip(Iterable, Function) 第一个参数是 Mono 集合，第二个参数是 值数组
        List<Mono<String>> monoList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            monoList.add(Mono.just("Mono.just" + i));
        }
        Mono<String> m = Mono.zip(monoList, objs -> {
            return Arrays.stream(objs).map(o -> (String) o).collect(Collectors.joining("\n"));
        });
        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/with")
    public Mono<String> with(@RequestParam("name") String name) {
        // mono.with ，合并两个流，得到 tuple2
        // 两个流会并发执行

        Mono<String> m1 = Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
            sleep(1500);
            System.out.println(Thread.currentThread().getName() + " fromFuture1 : hello " + name);
            return "fromFuture1 " + name;
        }));

        Mono<String> m2 = Mono.defer(() -> {
            return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
                sleep(500);
                System.out.println(Thread.currentThread().getName() + " fromFuture2 : hello " + name);
                return "fromFuture2 " + name;
            }));
        });

        Mono<String> m = m1.zipWith(m2)
                .map(t -> t.getT1() + ",   " + t.getT2());

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/with2")
    public Mono<String> with2(@RequestParam("name") String name) {
        // mono.with ，合并两个流，并返回两个流处理后的 结果
        Mono<String> m1 = Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
            System.out.println("fromFuture1 : hello " + name);
            return "fromFuture1 " + name;
        }));

        Mono<String> m2 = Mono.defer(() -> {
            return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
                sleep(500);
                System.out.println("fromFuture2 : hello " + name);
                return "fromFuture2 " + name;
            }));
        });

        Mono<String> m = m1.zipWith(m2, (t1, t2) -> t1 + ",   " + t2);

        System.out.println("你好 " + name);
        return m;
    }

    @GetMapping("/when")
    public Mono<String> when(@RequestParam("name") String name) {
        // mono.when ，合并两个流，得到 tuple2
        // 两个流会顺序执行
        // 第一个流完成后，再把结果给第二个流开始执行

        Mono<String> m1 = Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
            sleep(500);
            System.out.println("fromFuture1 : hello " + name);
            return "fromFuture1 " + name;
        }));
        Mono<String> m2 = Mono.defer(() -> {
            return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
                sleep(1500);
                System.out.println("fromFuture2 : hello " + name);
                return "fromFuture2 " + name;
            }));
        });


        Mono<String> m = m1.zipWhen(t -> {
            return m2;
        }).map(t -> t.getT1() + ",   " + t.getT2());

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
