package cl.duoc.linceops.backend.dto.response;

public class AuthResponseDTO {
    private String token;
    private String usuarioId;
    private String nombre;
    private String rol;

    public AuthResponseDTO(String token, String usuarioId, String nombre, String rol) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.rol = rol;
    }

    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}