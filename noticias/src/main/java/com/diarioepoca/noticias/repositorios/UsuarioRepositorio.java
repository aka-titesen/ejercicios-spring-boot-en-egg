package com.diarioepoca.noticias.repositorios;

import com.diarioepoca.noticias.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    public Usuario buscarUsuarioPorEmail(@Param("email") String email);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE Usuario SET dtype = :tipo WHERE id = :id", nativeQuery = true)
    public void cambiarRol(@Param("tipo") String tipo, @Param("id") Integer id);
}
