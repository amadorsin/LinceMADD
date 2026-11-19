package com.example.apiLince.config;

import com.example.apiLince.model.Geocerca;
import com.example.apiLince.model.Rol;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.GeocercaRepository;
import com.example.apiLince.repository.UsuarioLocalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioLocalRepository usuarioLocalRepository;
    private final GeocercaRepository geocercaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioLocalRepository usuarioLocalRepository,
                      GeocercaRepository geocercaRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioLocalRepository = usuarioLocalRepository;
        this.geocercaRepository = geocercaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuarioSiNoExiste("20", "Matías Soto Herrera", "driver@linceops.cl", "123456", Rol.CONDUCTOR);
        crearUsuarioSiNoExiste("12", "Camila Rojas Fuentes", "guia@linceops.cl", "123456", Rol.GUIA);
        crearUsuarioSiNoExiste("A301", "Ana Administradora", "admin@linceops.cl", "123456", Rol.ADMINISTRADOR);

        // Geocercas base por oficina Southbound (SCL, PUX, PUQ) y ejemplo de hotel
        crearGeocercaSiNoExiste("SCL", -33.4372, -70.6506, 150);
        crearGeocercaSiNoExiste("PUX", -41.3178, -72.9854, 150);
        crearGeocercaSiNoExiste("PUQ", -53.1638, -70.9171, 150);
        crearGeocercaSiNoExiste("Hotel Barrio Lastarria", -33.4389, -70.6403, 150);
    }

    private void crearUsuarioSiNoExiste(String id, String nombre, String correo, String passwordPlana, Rol rol) {
        if (usuarioLocalRepository.findByCorreo(correo).isEmpty()) {
            UsuarioLocal usuario = new UsuarioLocal();
            usuario.setId(id);
            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setPassword(passwordEncoder.encode(passwordPlana));
            usuario.setRol(rol);
            usuarioLocalRepository.save(usuario);
        }
    }

    private void crearGeocercaSiNoExiste(String puntoReferencia, double lat, double lon, int radioMetros) {
        if (geocercaRepository.findByPuntoReferencia(puntoReferencia).isEmpty()) {
            Geocerca g = new Geocerca();
            g.setPuntoReferencia(puntoReferencia);
            g.setLatitudCentro(lat);
            g.setLongitudCentro(lon);
            g.setRadioMetros(radioMetros);
            geocercaRepository.save(g);
        }
    }
}