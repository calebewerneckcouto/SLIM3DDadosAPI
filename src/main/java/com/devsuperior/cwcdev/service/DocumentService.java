package com.devsuperior.cwcdev.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.cwcdev.model.Document;
import com.devsuperior.cwcdev.model.Usuario;
import com.devsuperior.cwcdev.repository.DocumentRepository;
import com.devsuperior.cwcdev.repository.UsuarioRepository;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para obter o usuário logado
    private Usuario getLoggedInUser() {
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Transactional
    public Document saveDocument(Document document) {
        document.setUsuario(getLoggedInUser());  // Associar o documento ao usuário logado
        return documentRepository.save(document);
    }

    

    @Transactional
    public void deleteDocument(Long id) {
        Usuario loggedInUser = getLoggedInUser();
        Document document = documentRepository.findByIdAndUsuario(id, loggedInUser)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado ou não pertence ao usuário"));

        documentRepository.deleteById(id);
    }
    
    @Transactional
    public Page<Document> getDocumentsByUser(Usuario usuario, int page, int size) {
        return documentRepository.findByUsuario(usuario, PageRequest.of(page, size));
    }

    @Transactional
    public Document updateDocument(Long id, Document documentDetails) {
        Usuario loggedInUser = getLoggedInUser();
        Document document = documentRepository.findByIdAndUsuario(id, loggedInUser)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado ou não pertence ao usuário"));

        document.setName(documentDetails.getName());
        document.setDescription(documentDetails.getDescription());
       

        // Se houver um novo arquivo, atualiza os dados do arquivo
        if (documentDetails.getFileData() != null && documentDetails.getFileData().length > 0) {
            document.setFileData(documentDetails.getFileData());
            document.setOriginalFileName(documentDetails.getOriginalFileName());
            document.setFileType(documentDetails.getFileType());
        }

        return documentRepository.save(document);
    }

    public Page<Document> getDocumentsByUserAndSharedWithUser(Usuario usuario, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.findByUsuarioOrSharedWithUserIdsContaining(usuario, usuario.getId(), pageable);
    }
    
    public Optional<Document> getDocument(Long id) {
        return documentRepository.findById(id);
    }

    public Page<Document> getAllDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.findAll(pageable);
    }
}
