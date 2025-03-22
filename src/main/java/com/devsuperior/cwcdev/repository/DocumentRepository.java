package com.devsuperior.cwcdev.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devsuperior.cwcdev.model.Document;
import com.devsuperior.cwcdev.model.Usuario;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Encontrar um documento pelo id e pelo usuário
    Optional<Document> findByIdAndUsuario(Long id, Usuario usuario);

    // Encontrar documentos de um usuário com paginação
    Page<Document> findByUsuario(Usuario usuario, Pageable pageable);
    
 // Método para buscar por linguagem ou descrição com paginação
    Page<Document> findByUsuarioAndNameContainingIgnoreCaseOrUsuarioAndDescriptionContainingIgnoreCase(
            Usuario usuario, String nameKeyword, Usuario usuario2, String descriptionKeyword, Pageable pageable);

}
