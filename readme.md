## webflux 基础 API 使用 Demo
主要是 Mono 类的常用API，Flux 类和 Mono 类比较相似，触类旁通基本可以理解用法。   
有部分API 暂时确实不知道该怎么使用，所以选择性忽略，希望后续可以完善。   
基本上每个API 都写了个小 Demo，也写了注释，如果有错误，还请指正！   
共同学习，共同进步！

----------

### 项目启动
直接运行 `WebfluxApplication` 下的 `main` 方法，即可启动项目，默认监听端口： `8080`   
`SimpleController` 还写了单元测试，可以直接执行用例。
   
> 未接触过 webflux 的同学建议先看下 `SimpleController` ，有个直观了解

### API

#### SimpleController   
简单的创建 Mono 和 Flux 的示例，webflux 的 controller 接口都返回  Mono 或 Flux   

- Mono.just 直接将一个对象包装成 Mono
- Mono.defer 包装一个Mono，有延迟的效果，避免声明时直接执行具体的操作。
- Flux.just 将多个对象包装成 Flux
- Flux.fromStream 将 Stream 包装成 Flux
- Flux.range 按照范围生成连续 Flux

> 订阅之前，什么都不会发生。方法中只是一序列的声明，只有订阅了才会开始执行，而订阅操作是由框架帮我们完成的，我们只需要返回 Mono 或 Flux  

#### MonoFromController
Mono 的 from 操作示例，各种创建 Mono 的方式

- Mono.from(mono) 包装一层
- Mono.from(flux) 只取第一个值， 忽略其他
- Mono.fromCallable 基于一个 Callable 来创建
- Mono.fromFuture 基于一个 Future 来创建
- Mono.fromRunnable 基于一个 Runnable 来创建，并返回 空流
- Mono.fromSupplier 类似 defer，有延迟效果

还示例了等待多个任务完成后，继续执行的操作

#### ThenController
then 的用法示例   

- Mono#then() 忽略前面的流，返回 void
- Mono#then(Mono) 忽略前面的流，返回 指定的 流
- Mono#thenReturn 忽略前面的流，返回指定的值
- Mono#thenMany 忽略前面的流，返回 指定的 Flux
- Mono#thenEmpty 忽略前面的流，然后执行一个任务后，忽略

#### ThenController
zip 的用法示例   

- Mono.zip 对流进行合并，并返回 tuple 用于后续处理
- Mono#zipWith 合并两个流，得到 tuple2，两个流会并发执行
- Mono#zipWhen 合并两个流，得到 tuple2，两个流会顺序执行，第一个流完成后，再把结果给第二个流开始执行

#### MonoController
Mono API 的用法示例   

- Mono#and 丢弃前面的结果，执行and 里面的流，同样丢弃结果
- Mono.when 等待when 里面的所有流执行完成后，丢弃结果，并往下走
- Mono.justOrEmpty 可以接收 null 值，如果是 null 则相当于 Mono<Void>
- Mono#switchIfEmpty 如果流是 Mono<Void>， 用个默认流替换返回结果
- Mono#defaultIfEmpty 如果流是 Mono<Void>， 直接给个默认值
- Mono.using 类似 `try-with-resources` 操作
- Mono.empty 直接终止的流
- Mono.never 没内容的流，没有取消，没有终止
- Mono.create 通过 MonoSink 来创建流
- Mono.delay 延迟指定的时间，之后发出元素
- Mono.ignoreElements 忽略所有的元素，例如执行完毕后丢弃值
- Mono#timeout 指定等待时间，超时还未发出元素，将抛出 TimeoutException
- Mono#single 期望流有一个元素，如果是空流，则抛出 NoSuchElementException
- Mono#filterWhen 和 filter 的区别在于，一个返回 boolean ，一个返回 Mono<Boolean>
- Mono#cast 用于强转，cast(String.class) 等价于 map(o -> (String)o )
- Mono#ofType 用于过滤，只保留目标类型，并将流做强制类型转换。相当于 filter + cast
- Mono#or 两个流，谁先发出信号就用哪个
- Mono#mergeWith 连接 Mono，形成 flux。会有交错的情况，先放置的元素先进入流
- Mono#concatWith 连接 Mono，形成 flux。不会有交错的情况，将严格保证顺序
- Mono#log 日志打印。将打印处理的详细信息
- Mono#delayElement 延迟指定的时间
- Mono#delayUntil 顺序执行，丢弃第二个流的结果
- Mono#transform 将链式操作方到 function 里面去做。可用于抽象公共代码
- Mono#transformDeferred 将链式操作方到 function 里面去做，有延迟执行的效果。可用于抽象公共代码
- Mono#cache 缓存最终结果
- Mono#handle，用 sink 来转换元素，重新发射信号。类似 Flux.generate
- Mono#toFuture，字面意思
- Mono#retry，发生错误后，重试指定次数
- Mono#retryWhen，自定义重试策略
- Mono#repeat，指定重试次数，得到 Flux 流
- Mono#expand，用于树的展开，广度优先，得到 Flux 流
- Mono#expandDeep，用于树的展开，深度优先，得到 Flux 流
- Mono#elapsed，将元素和流开始到当前步骤的耗时 合并为一个 Tuple2 对象
- Mono#share 缓存最终结果。share()方法(针对Flux)，cache()方法(针对Mono)
- Mono#take，只获取指定时间内产生的元素，超时的元素将被丢弃
- Mono#takeUntilOther，只获取指定时间内产生的元素，超时的元素将被丢弃
- Mono#delaySubscription，延时订阅
- Mono#as，做类型转换，类似 flatMap
- Mono#metrics，监控指标，需要配合 Micrometer 使用，做应用的指标收集与监控
- Mono#subscribe，订阅
- Mono#subscribeOn，不会订阅流，只是指定调度器
- Mono#publishOn，切换调度器
- Mono#doOn，doOn 操作用于窥视流，在不改变流的情况下，可以得到通知（只读）
- Mono#doSomething，一次性事件触发
- Mono#onErrorMap 用于异常转换
- Mono#onErrorResume，发生错误后，处理后返回新的值。类似 catch 操作
- Mono#onErrorContinue，将发生错误的元素移除，以便恢复流
- Mono#onErrorReturn，发生错误后，返回默认值