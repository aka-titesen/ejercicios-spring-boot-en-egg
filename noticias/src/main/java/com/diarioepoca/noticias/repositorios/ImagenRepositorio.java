package com.diarioepoca.noticias.repositorios;

import com.diarioepoca.noticias.entidades.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenRepositorio extends JpaRepository<Imagen, Integer> {

}
