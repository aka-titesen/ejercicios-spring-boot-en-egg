package com.diarioepoca.noticias.repositorios;

import com.diarioepoca.noticias.entidades.Periodista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodistaRepositorio extends JpaRepository<Periodista, Integer> {

    @Query("SELECT p FROM Periodista p WHERE p.email = :email")
    public Periodista buscarPeriodistaPorEmail(@Param("email") String email);

}
