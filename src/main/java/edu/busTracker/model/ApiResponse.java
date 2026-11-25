package edu.busTracker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic REST API response wrapper.
 * Demonstrates: generic class (Unit 5 Generics).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;
    private String message;
    private String timestamp;

    public ApiResponse() {}

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        r.timestamp = java.time.Instant.now().toString();
        return r;
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.error = errorCode;
        r.message = message;
        r.timestamp = java.time.Instant.now().toString();
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
