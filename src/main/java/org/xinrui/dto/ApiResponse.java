package org.xinrui.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应格式
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private Integer code;  // 0-成功 非0-失败
    private String msg;   // 返回信息
    private T data;         // 业务数据

    private static final long serialVersionUID = 1L;

    /**
     * 常用错误码
     */
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final int DB_ERROR = 1001;
    public static final int NOT_FOUND = 1002;
    public static final int PARAM_ERROR = 1003;



    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(SUCCESS);
        response.setMsg("请求成功");
        response.setData(data);
        return response;
    }

    public static ApiResponse success() {
        ApiResponse response = new ApiResponse<>();
        response.setCode(SUCCESS);
        response.setMsg("请求成功");
        // result 默认为 null，JSON 序列化时通常会被忽略
        return response;
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMsg(message);
        return response;
    }

    public static <T> ApiResponse<T> fail(String message) {
        return fail(ERROR, message);
    }


}