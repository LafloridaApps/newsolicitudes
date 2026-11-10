package com.newsolicitudes.newsolicitudes.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("select u from Usuario u where lower(u.login) like lower(concat('%', :busqueda, '%')) "
            + "or u.rut like concat('%', :busqueda, '%') or u.rut in :ruts")
    Page<Usuario> buscarPagina(@Param("busqueda") String busqueda, @Param("ruts") java.util.Collection<String> ruts, Pageable pageable);

    Optional<Usuario> findByRut(String rut);
    
}
