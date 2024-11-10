package Package.PHARMACY_PROJECT;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String errorMessage = "Error de integridad de datos: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Response<>("400", errorMessage, null, "ERROR_INTEGRIDAD_DATOS"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleGeneralException(Exception e) {
        String errorMessage = "Error en el servidor: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response<>("500", errorMessage, null, "ERROR_GENERAL"));
    }
}

