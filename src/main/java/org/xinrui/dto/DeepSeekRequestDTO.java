package org.xinrui.dto;

import lombok.Data;

import java.util.List;

/**
 * DeepSeek API请求DTO（完全匹配前端传入格式）
 */
@Data
public class DeepSeekRequestDTO {
    private String model;
    private String system;
    private List<Message> messages;
    private Double temperature;
    private Boolean stream;

    @Data
    public static class Message {
        private String role;
        private List<ContentItem> content;

        @Data
        public static class ContentItem {
            private String type;
            private String text;
        }
    }
}