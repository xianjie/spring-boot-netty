package cn.com.netty;

import cn.com.netty.action.ApiHelper;
import cn.com.netty.action.RestApi;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.composable.Stream;
import reactor.net.NetServer;
import reactor.net.config.ServerSocketOptions;
import reactor.net.netty.NettyServerSocketOptions;
import reactor.net.netty.tcp.NettyTcpServer;
import reactor.net.tcp.spec.TcpServerSpec;
import reactor.spring.context.config.EnableReactor;

import java.util.concurrent.CountDownLatch;

/**
 * @author: xian jie
 * @date: 2016-5-11 11:23
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan("cn.com.netty")
@EnableReactor
public class Application {

    @Bean
    public ServerSocketOptions serverSocketOptions() {
        return new NettyServerSocketOptions()
                .pipelineConfigurer(pipeline -> pipeline.addLast(new HttpServerCodec())
                        .addLast(new HttpObjectAggregator(16 * 1024 * 1024)));
    }

    @Bean
    public NetServer<FullHttpRequest, FullHttpResponse> serverRestApi(Environment env,
                                                                ServerSocketOptions opts,
                                                                RestApi restApi,
                                                                CountDownLatch closeLatch) throws InterruptedException {

        NetServer<FullHttpRequest, FullHttpResponse> server = new TcpServerSpec<FullHttpRequest, FullHttpResponse>(
                NettyTcpServer.class)
                .env(env)
                .dispatcher("sync")
                .options(opts)
                .consume(ch -> {
                    // filter requests by URI via the input Stream
                    Stream<FullHttpRequest> in = ch.in();

                    // 定义error
                    in.when(Throwable.class, ApiHelper.errorHandler(ch));

                    // 测试的URL
                    in.filter((FullHttpRequest req) -> "/".equals(req.getUri()))
                            .when(Throwable.class, ApiHelper.errorHandler(ch))
                            .consume(ApiHelper.okHandler(ch));

                    // upload image
                    in.filter((FullHttpRequest req) -> "/uploadImage".equals(req.getUri()))
                            .when(Throwable.class, ApiHelper.errorHandler(ch))
                            .consume(restApi.uploadImage(ch));

                    // get image
                    in.filter((FullHttpRequest req) -> "/getImage".equals(req.getUri()))
                            .when(Throwable.class, ApiHelper.errorHandler(ch))
                            .consume(restApi.getImage(ch));

                    // 关闭程序的URL
                    in.filter((FullHttpRequest req) -> "/shutdown".equals(req.getUri()))
                            .consume(req -> closeLatch.countDown());

                })
                .get();

        server.start().await();

        return server;
    }

    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    public static void main(String... args) throws InterruptedException {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        // Reactor's TCP servers are non-blocking so we have to do something to keep from exiting the main thread
        CountDownLatch closeLatch = ctx.getBean(CountDownLatch.class);
        closeLatch.await();
    }
}
