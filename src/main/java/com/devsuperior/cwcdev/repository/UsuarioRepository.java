package com.devsuperior.cwcdev.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.cwcdev.model.Usuario;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    @Query("select u from Usuario u where u.login = ?1")
    Usuario findUserByLogin(String login);

    @Transactional
    @Modifying
    @Query(value = "update usuario set senha =?1 where id = ?2", nativeQuery = true)
    void updateSenha(String senha, Long codUser);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO public.usuarios_role (usuario_id, role_id) VALUES (:usuarioId, :roleId)", nativeQuery = true)
    void addRoleToUsuario(@Param("usuarioId") Long usuarioId, @Param("roleId") Long roleId);

    // Considerar a remoção da constraint em uma migração separada
    // @Transactional
    // @Modifying
    // @Query(value = "ALTER TABLE public.usuarios_role DROP CONSTRAINT uk_krvk2qx218dxa3ogdyplk0wxw", nativeQuery = true)
    // void dropConstraint();
}
