package com.youngwang.wenflux.test;

import com.youngwang.webflux.WebfluxApplication;
import com.youngwang.webflux.syntax.SimpleController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
// 需要指定程序入口所在类
@ContextConfiguration(classes = WebfluxApplication.class)
@WebFluxTest(controllers = SimpleController.class)
public class SimpleControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void testSimpleMono() {
        webClient.get()
                .uri("/simple/mono?name={name}", Map.of("name", "张三"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .isEqualTo("hello 张三")
                .value(res -> System.out.println("testSimpleMono: " + res));
    }

    @Test
    public void testSimpleMonoJustOrder() {
        webClient.get()
                .uri("/simple/mono/order/just?name={name}", Map.of("name", "张三"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .isEqualTo("hello 张三")
                .value(res -> System.out.println("testSimpleMonoJustOrder: " + res));
    }

    @Test
    public void testSimpleMonoDeferOrder() {
        webClient.get()
                .uri("/simple/mono/order/defer?name={name}", Map.of("name", "张三"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .isEqualTo("hello 张三")
                .value(res -> System.out.println("testSimpleMonoDeferOrder: " + res));
    }

    @Test
    public void testSimpleFluxJust() {
        webClient.get()
                .uri("/simple/flux/just?name={name}", Map.of("name", "张三"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .isEqualTo("hello 张三")
                .value(v -> System.out.println("testSimpleFluxJust: " + v));
    }

    @Test
    public void testSimpleFluxJustStringArray() {
        webClient.get()
                .uri("/simple/flux/just/string/array?name={name}", Map.of("name", "张三"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .value(s -> System.out.println("testSimpleFluxJustStringArray: " + s));
    }

    @Test
    public void testSimpleFluxFrom() {
        webClient.get()
                .uri("/simple/flux/from?name={name}", Map.of("name", "zhangsan"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(Integer.class)
                .hasSize(8)
                .value(list -> System.out.println("testSimpleFluxFrom: " + list.stream().map(String::valueOf).collect(Collectors.joining(","))));
    }

    @Test
    public void testSimpleFluxRange() {
        webClient.get()
                .uri("/simple/flux/range?size={size}", Map.of("size", "3"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(Integer.class)
                .hasSize(3)
                .contains(0, 1, 2)
                .value(l -> System.out.println("testSimpleFluxRange: " + l.stream().map(String::valueOf).collect(Collectors.joining(","))));
    }

}
