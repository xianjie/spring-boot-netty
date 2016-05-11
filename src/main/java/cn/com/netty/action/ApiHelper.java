package cn.com.netty.action;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import reactor.function.Consumer;
import reactor.net.NetChannel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ApiHelper {

    public static FullHttpResponse writeJson(Object json) {

        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);

        try {
            resp.content().writeBytes(JSON.toJSONString(json).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        resp.headers().set(CONTENT_TYPE, "application/json;charset=UTF-8");
        resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
        return resp;
    }

    ////////////////////////// HELPER METHODS //////////////////////////
    /*
     * Read POST uploads and write them to a temp file, returning the Path to that file.
    */
    public static byte[] readUploadBytes(ByteBuf content) throws IOException {

        byte[] bytes = new byte[content.readableBytes()];

        content.readBytes(bytes);
        content.release();

        return bytes;
    }

    /*
     * Create an HTTP 400 bad request response.
     */
    public static FullHttpResponse badRequest(String msg) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
        resp.content().writeBytes(msg.getBytes());
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
        return resp;
    }

    /*
     * Create an HTTP 301 redirect response.
     */
    public static FullHttpResponse redirect() {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
        resp.headers().set(CONTENT_LENGTH, 0);
        resp.headers().set(LOCATION, "");
        return resp;
    }

    /*
     * 创建包含byte数据的HTTP200响应
     */
    public static FullHttpResponse serveImage(byte[] imageData) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);

        resp.headers().set(CONTENT_TYPE, "image/jpeg");
        resp.headers().set(CONTENT_LENGTH, imageData.length);

        resp.content().writeBytes(imageData);

        return resp;
    }

    public static FullHttpResponse servePNGImage(byte[] imageData) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);

        resp.headers().set(CONTENT_TYPE, "image/png");
        resp.headers().set(CONTENT_LENGTH, imageData.length);

        resp.content().writeBytes(imageData);

        return resp;
    }

    public static FullHttpResponse servePDF(byte[] data) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);

        resp.headers().set(CONTENT_TYPE, "application/pdf");
        resp.headers().set(CONTENT_LENGTH, data.length);

        resp.content().writeBytes(data);

        return resp;
    }

    /**
     * 构建一个 系统错误的返回
     * @param channel
     * @return
     */
    public static Consumer<Throwable> errorHandler(NetChannel<FullHttpRequest, FullHttpResponse> channel) {
        return ev -> {
            DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            resp.content().writeBytes(ev.getMessage().getBytes());
            resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, resp.content().readableBytes());
            channel.send(resp);
        };
    }

    /**
     * 构建一个正常的返回
     * @param channel
     * @return
     */
    public static Consumer<FullHttpRequest> okHandler(NetChannel<FullHttpRequest, FullHttpResponse> channel) {
        return request -> {
            DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            resp.content().writeBytes("ok".getBytes());
            resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, resp.content().readableBytes());
            channel.send(resp);
        };
    }

}
