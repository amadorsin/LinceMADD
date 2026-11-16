package com.example.apiLince.repository;

import com.example.apiLince.model.UsuarioLocal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioLocalRepository extends JpaRepository<UsuarioLocal, String> {
    // Fundamental para el login con Spring Security
    Optional<UsuarioLocal> findByCorreo(String correo);
}