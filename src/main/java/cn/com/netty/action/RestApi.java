package cn.com.netty.action;

import cn.com.netty.mobile.ResponseMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.springframework.stereotype.Service;
import reactor.function.Consumer;
import reactor.net.NetChannel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author: xian jie
 * @date: 2016-5-11 11:23
 */
@Service
public class RestApi {

    /**
     * upload image
     *
     * @param channel
     * @return
     */
    public Consumer<FullHttpRequest> uploadImage(NetChannel<FullHttpRequest, FullHttpResponse> channel) {
        return req -> {

            // 判断请求是不是 POST 方法
            if (req.getMethod() != HttpMethod.POST) {
                channel.send(ApiHelper.badRequest(req.getMethod() + " not supported for this URI"));
                return;
            }

            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);

            // 获取请求中的参数
            Attribute fileName = (Attribute) decoder.getBodyHttpData("fileName");
            FileUpload fileContent = (FileUpload) decoder.getBodyHttpData("fileContent");


            try {
                // 转换文件二进制流
                byte[] bytes = ApiHelper.readUploadBytes(fileContent.content());

                FileOutputStream fos = new FileOutputStream("f:/java_tmp/aa.pdf");
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 处理逻辑

            // 构建返回 信息
            ResponseMessage message = new ResponseMessage(100000, "success");

            FullHttpResponse resp = ApiHelper.writeJson(message);

            channel.send(resp);
        };
    }



    // get image
    public Consumer<FullHttpRequest> getImage(NetChannel<FullHttpRequest, FullHttpResponse> channel) {
        return request -> {

            // 判断请求是不是 POST 方法
            if (request.getMethod() != HttpMethod.POST) {
                channel.send(ApiHelper.badRequest(request.getMethod() + " not supported for this URI"));
                return;
            }

            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);

            // 获取请求中的参数
            Attribute fileName = (Attribute) decoder.getBodyHttpData("fileName");

            byte[] data;
            try {
                Path path = Paths.get("F:/java_tmp/" + fileName.getValue());
                data = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            FullHttpResponse resp = ApiHelper.servePNGImage(data);

            channel.send(resp);
        };
    }
}
