package com.devsuperior.cwcdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.cwcdev.model.Usuario;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("select u from Usuario u where u.login = ?1")
    Usuario findUserByLogin(String login);

    @Transactional
    @Modifying
    @Query(value = "update usuario set senha =?1 where id = ?2", nativeQuery = true)
    void updateSenha(String senha, Long codUser);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO usuarios_role (usuario_id, role_id) VALUES (:usuarioId, :roleId)", nativeQuery = true)
    void addRoleToUsuario(@Param("usuarioId") Long usuarioId, @Param("roleId") Long roleId);

    @Query("select u from Usuario u where u.login = ?1")
    Optional<Usuario> findByUsername(String username);

    // Método para remover a constraint
    @Transactional
    @Modifying
    @Query(value = "ALTER TABLE usuarios_role DROP CONSTRAINT uk_krvk2qx218dxa3ogdyplk0wxw", nativeQuery = true)
    void dropConstraint();

    
    @Query(value = "DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_krvk2qx218dxa3ogdyplk0wxw') THEN ALTER TABLE public.usuarios_role DROP CONSTRAINT uk_krvk2qx218dxa3ogdyplk0wxw; END IF; END $$;", nativeQuery = true)
    void dropConstraintIfExists();
    
}