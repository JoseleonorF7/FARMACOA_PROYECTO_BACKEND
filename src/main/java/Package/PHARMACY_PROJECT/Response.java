package Package.PHARMACY_PROJECT;

public class Response<T> {
    private String code;
    private String message;
    private T data;
    private String status;

    public Response(String code, String message, T data, String status) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters y setters
}
