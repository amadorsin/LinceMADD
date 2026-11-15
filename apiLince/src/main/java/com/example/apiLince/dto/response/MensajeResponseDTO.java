package cl.duoc.linceops.backend.dto.response;

public class MensajeResponseDTO {
    private String mensaje;

    public MensajeResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    // Getter y Setter
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}