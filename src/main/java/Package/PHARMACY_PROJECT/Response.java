package Package.PHARMACY_PROJECT;

public class Response<T> {
    private String code;
    private String message;
    private T data;
    private String status;

    public Response() {
    }

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

    // Métodos estáticos para facilitar la creación de respuestas
    public static <T> Response<T> success(T data) {
        return new Response<>("200", "Success", data, "OK");
    }

    public static <T> Response<T> error(String message) {
        return new Response<>("400", message, null, "ERROR");
    }

    public static <T> Response<T> notFound(String message) {
        return new Response<>("404", message, null, "NOT_FOUND");
    }

    public static <T> Response<T> internalServerError(String message) {
        return new Response<>("500", message, null, "INTERNAL_SERVER_ERROR");
    }
    // Getters y setters
}
