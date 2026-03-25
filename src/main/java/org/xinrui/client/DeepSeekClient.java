package org.xinrui.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinrui.config.DeepSeekConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DeepSeekClient {
    private final DeepSeekConfig config;
    private final CloseableHttpClient httpClient;

    @Autowired
    public DeepSeekClient(DeepSeekConfig config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();
    }

    public String callModel(String prompt, String model, Double temperature) throws IOException {

        log.info("抵达callModel方法");

        HttpPost httpPost = new HttpPost(config.getApiEndpoint());
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());

        // 关键：使用前端传入的model和temperature
        String jsonBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"temperature\": %f}",
                model,
                prompt.replace("\"", "\\\"")
                        .replace("\n", " ")
                        .replace("\r", " "),
                temperature
        );

        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("DeepSeek API response status: {}", statusCode);

            if (statusCode != 200) {
                String errorResponse = EntityUtils.toString(response.getEntity());
                log.error("DeepSeek API error: Status {}, Response: {}", statusCode, errorResponse);
                throw new IOException("DeepSeek API error: " + response.getStatusLine() + ", Response: " + errorResponse);
            }
            log.info("结束callModel方法");
            return EntityUtils.toString(response.getEntity());
        }
    }
}