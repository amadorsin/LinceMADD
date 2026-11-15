package cl.duoc.linceops.backend.exception;

import cl.duoc.linceops.backend.dto.response.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo para cuando el conductor está a más de 150 metros
    @ExceptionHandler(GeocercaValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleGeocercaValidationException(GeocercaValidationException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "FUERA_DE_GEOCERCA",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 2. Manejo para cuando el conductor llega atrasado (menos de 15 min de holgura)
    @ExceptionHandler(HorarioInvalidoException.class)
    public ResponseEntity<ErrorResponseDTO> handleHorarioInvalidoException(HorarioInvalidoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "HORARIO_INVALIDO",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Manejo genérico para reglas de negocio (Ej: Credenciales inválidas, usuario no existe)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "REGLA_NEGOCIO_INVALIDA",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 4. Manejo de las validaciones de Jakarta (Los @NotBlank, @NotNull de tus DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Extraemos solo el primer mensaje de error para no saturar la pantalla del móvil
        String mensajeError = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Datos incompletos o inválidos en la petición");

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "DATOS_INVALIDOS",
                mensajeError
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 5. Escudo final (Cualquier otra excepción no controlada, error 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERROR_INTERNO_SERVIDOR",
                "Ha ocurrido un error inesperado. Contacta a coordinación."
        );
        // Aquí idealmente también pondrías un log.error(ex.getMessage(), ex); para ver el problema en tu consola
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}