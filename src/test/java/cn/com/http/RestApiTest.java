package cn.com.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.*;

/**
 * @author: xian jie
 * @date: 2016-5-11 15:15
 */
public class RestApiTest {

    @Test
    public void uploadImage() {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpPost post = new HttpPost("http://localhost:3000/uploadImage");

            FileBody fileBody = new FileBody(new File("f:/java_tmp/AsianTest.pdf"));
            StringBody fileName = new StringBody("AsianTest.pdf", ContentType.MULTIPART_FORM_DATA);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("fileName", fileName);
            builder.addPart("fileContent", fileBody);
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("服务器正常响应.....");
                HttpEntity resEntity = response.getEntity();
                System.out.println(EntityUtils.toString(resEntity));//httpclient自带的工具类读取返回数据
                System.out.println(resEntity.getContent());
                EntityUtils.consume(resEntity);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception ignore) {

            }
        }
    }

    @Test
    public void getImage() {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpPost post = new HttpPost("http://localhost:3000/getImage");
            StringBody fileName = new StringBody("AsianTest.pdf", ContentType.MULTIPART_FORM_DATA);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("fileName", fileName);
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("服务器正常响应.....");
                HttpEntity resEntity = response.getEntity();

                InputStream in = resEntity.getContent();
                toFile(in, new File("f:/java_tmp/jack.pdf"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception ignore) {

            }
        }
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private void closeHttpClient(CloseableHttpClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }

    public void toFile(InputStream ins, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
    }
}
