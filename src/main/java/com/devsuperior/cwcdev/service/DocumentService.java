package com.devsuperior.cwcdev.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.cwcdev.model.Document;
import com.devsuperior.cwcdev.repository.DocumentRepository;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Transactional
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    public Optional<Document> getDocument(Long id) {
        return documentRepository.findById(id);
    }

    @Transactional
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    @Transactional
    public Document updateDocument(Long id, Document documentDetails) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        document.setName(documentDetails.getName());
        document.setDescription(documentDetails.getDescription());
        document.setShared(documentDetails.getShared());

        // Se houver um novo arquivo, atualiza os dados do arquivo
        if (documentDetails.getFileData() != null && documentDetails.getFileData().length > 0) {
            document.setFileData(documentDetails.getFileData());
            document.setOriginalFileName(documentDetails.getOriginalFileName());
            document.setFileType(documentDetails.getFileType());
        }

        return documentRepository.save(document);
    }

    public Page<Document> getAllDocuments(int page, int size) {
        return documentRepository.findAll(PageRequest.of(page, size));
    }
}
