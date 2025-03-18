package com.devsuperior.cwcdev.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.devsuperior.cwcdev.model.Document;
import com.devsuperior.cwcdev.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	@Autowired
	private DocumentService documentService;

	@PostMapping("/upload")
	public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file,
	                                              @RequestParam("name") String name, 
	                                              @RequestParam("description") String description,
	                                              @RequestParam("shared") Boolean shared) throws IOException {
	    
	    byte[] fileBytes = file.getBytes();
	    
	    Document document = new Document();
	    document.setName(name);
	    document.setDescription(description);
	    document.setFileData(fileBytes);
	    document.setShared(shared);
	    document.setOriginalFileName(file.getOriginalFilename()); // Nome original
	    document.setFileType(file.getContentType()); // Tipo MIME do arquivo

	    Document savedDocument = documentService.saveDocument(document);
	    return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
	}
	
	
	
	@PutMapping("/{id}/update")
	public ResponseEntity<Document> updateDocument(
	        @PathVariable Long id,
	        @RequestParam(value = "file", required = false) MultipartFile file,
	        @RequestParam("name") String name,
	        @RequestParam("description") String description,
	        @RequestParam("shared") Boolean shared) throws IOException {

	    Document existingDocument = documentService.getDocument(id)
	            .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

	    // Atualiza os campos do documento
	    existingDocument.setName(name);
	    existingDocument.setDescription(description);
	    existingDocument.setShared(shared);

	    // Se um novo arquivo for enviado, atualiza os dados
	    if (file != null && !file.isEmpty()) {
	        existingDocument.setFileData(file.getBytes());
	        existingDocument.setOriginalFileName(file.getOriginalFilename());
	        existingDocument.setFileType(file.getContentType());
	    }

	    Document updatedDocument = documentService.saveDocument(existingDocument);
	    return ResponseEntity.ok(updatedDocument);
	}



	
	@GetMapping("/{id}/view")
	public ResponseEntity<byte[]> viewDocument(@PathVariable Long id) {
	    Document document = documentService.getDocument(id)
	        .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

	    // Determinar o tipo de conteúdo com base na extensão do arquivo
	    String contentType = determineContentType(document.getOriginalFileName());

	    return ResponseEntity.ok()
	        .header("Content-Type", contentType)
	        .header("Content-Disposition", "inline; filename=\"" + document.getOriginalFileName() + "\"")
	        .body(document.getFileData());
	}
	
	

	// Buscar documento por ID
	@GetMapping("/{id}")
	public ResponseEntity<Document> getDocument(@PathVariable Long id) {
	    Optional<Document> document = documentService.getDocument(id);
	    
	    if (document.isPresent()) {
	        return ResponseEntity.ok(document.get());
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}
	

	// Deletar documento
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
		documentService.deleteDocument(id);
		return ResponseEntity.noContent().build();
	}

	// Buscar todos os documentos com paginação
	@GetMapping("/all")
	public ResponseEntity<Page<Document>> getAllDocuments(@RequestParam("page") int page,
			@RequestParam("size") int size) {
		Page<Document> documents = documentService.getAllDocuments(page, size);
		return ResponseEntity.ok(documents);
	}
	
	// Método para determinar o Content-Type com base na extensão do arquivo
	private String determineContentType(String fileName) {
	    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	    
	    switch (extension) {
	        case "pdf": return "application/pdf";
	        case "jpg": 
	        case "jpeg": return "image/jpeg";
	        case "png": return "image/png";
	        case "gif": return "image/gif";
	        case "txt": return "text/plain";
	        case "csv": return "text/csv";
	        case "html": return "text/html";
	        case "json": return "application/json";
	        case "xml": return "application/xml";
	        case "mp4": return "video/mp4";
	        case "mp3": return "audio/mpeg";
	        case "wav": return "audio/wav";
	        case "doc": 
	        case "docx": return "application/msword";
	        case "xls": 
	        case "xlsx": return "application/vnd.ms-excel";
	        case "ppt": 
	        case "pptx": return "application/vnd.ms-powerpoint";
	        default: return "application/octet-stream"; // Tipo genérico para arquivos desconhecidos
	    }
	}
}
