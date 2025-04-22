package qinghuan.common;

import lombok.Data;

/**
 * 通用响应类
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {
    /** 响应状态码，0 表示成功 */
    private int code;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private T data;

    /**
     * 创建成功的响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("Success");
        result.setData(data);
        return result;
    }

    /**
     * 创建无数据的成功响应
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}