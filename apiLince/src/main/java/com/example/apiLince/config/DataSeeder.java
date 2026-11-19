package com.example.apiLince.config;

import com.example.apiLince.model.Rol;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.UsuarioLocalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioLocalRepository usuarioLocalRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioLocalRepository usuarioLocalRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioLocalRepository = usuarioLocalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste(
                "20",
                "Matías Soto Herrera",
                "driver@linceops.cl",
                "123456",
                Rol.CONDUCTOR
        );

        crearSiNoExiste(
                "12",
                "Camila Rojas Fuentes",
                "guia@linceops.cl",
                "123456",
                Rol.GUIA
        );

        crearSiNoExiste(
                "A301",
                "Ana Administradora",
                "admin@linceops.cl",
                "123456",
                Rol.ADMINISTRADOR
        );
    }

    private void crearSiNoExiste(String id, String nombre, String correo, String passwordPlana, Rol rol) {
        if (usuarioLocalRepository.findByCorreo(correo).isEmpty()) {
            UsuarioLocal usuario = new UsuarioLocal();
            usuario.setId(id);
            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setPassword(passwordEncoder.encode(passwordPlana));
            usuario.setRol(rol);
            usuarioLocalRepository.save(usuario);
            System.out.println("Usuario inicial creado: " + correo + " (" + rol + ")");
        }
    }
}