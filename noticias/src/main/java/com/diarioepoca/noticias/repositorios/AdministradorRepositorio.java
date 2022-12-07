package com.diarioepoca.noticias.repositorios;

import com.diarioepoca.noticias.entidades.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdministradorRepositorio extends JpaRepository<Administrador, Integer> {

    @Query("SELECT a FROM Administrador a WHERE a.email = :email")
    public Administrador buscarAdministradorPorEmail(@Param("email") String email);

}
