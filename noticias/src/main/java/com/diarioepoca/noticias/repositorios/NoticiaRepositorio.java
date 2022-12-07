package com.diarioepoca.noticias.repositorios;

import com.diarioepoca.noticias.entidades.Noticia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticiaRepositorio extends JpaRepository<Noticia, Integer> {

    @Query("SELECT n FROM Noticia n WHERE n.titulo = :titulo")
    public Noticia buscarNoticiaPorTitulo(@Param("titulo") String titulo);

    @Query("SELECT n FROM Noticia n WHERE n.periodista.id = :id")
    public List<Noticia> buscarNoticiasPorIdPeriodista(@Param("id") Integer id);

}
