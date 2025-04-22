package qinghuan.common;

import lombok.Getter;

/**
 * 自定义 API 异常类
 */
@Getter
public class ApiException extends RuntimeException {
    private final int code;

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   异常原因
     */
    public ApiException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}