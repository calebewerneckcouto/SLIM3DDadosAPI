package com.devsuperior.cwcdev.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.devsuperior.cwcdev.dto.CompartilharDocumentoDTO;
import com.devsuperior.cwcdev.dto.ExcluirCompartilhamentoDTO;
import com.devsuperior.cwcdev.model.Document;
import com.devsuperior.cwcdev.model.Usuario;
import com.devsuperior.cwcdev.repository.DocumentRepository;
import com.devsuperior.cwcdev.repository.UsuarioRepository;
import com.devsuperior.cwcdev.service.DocumentService;
import com.devsuperior.cwcdev.service.UsuarioService;



@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	@Autowired
	private DocumentService documentService;
	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private UsuarioService usuarioService;
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	 private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

	// Método para obter o usuário logado
	private Usuario getLoggedInUser() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication == null || !authentication.isAuthenticated()) {
	        throw new RuntimeException("Usuário não autenticado");
	    }
	    String loggedInUsername = authentication.getName();
	    return usuarioRepository.findByUsername(loggedInUsername)
	            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
	}
	
	
	@PostMapping("/compartilhar")
public ResponseEntity<String> compartilharDocumento(@RequestBody CompartilharDocumentoDTO dto) {
    Long documentoId = dto.getDocumentoId();
    Long usuarioId = dto.getUsuarioId();

    logger.info("CompartilharDocumento: Documento ID: {}, Usuario ID: {}", documentoId, usuarioId);

    Optional<Document> documentOpt = documentRepository.findById(documentoId);
    Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

    if (!documentOpt.isPresent() || !usuarioOpt.isPresent()) {
        logger.warn("Documento ou usuário não encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento ou usuário não encontrado");
    }

    Document document = documentOpt.get();
    Usuario loggedInUser = getLoggedInUser();

    logger.info("Documento Owner ID: {}, LoggedInUser ID: {}", document.getUsuario().getId(), loggedInUser.getId());

    if (!document.getUsuario().getId().equals(loggedInUser.getId())) {
        logger.warn("Usuário não tem permissão para compartilhar este documento.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para compartilhar este documento");
    }

    List<Long> sharedWith = document.getSharedWithUserIds();
    if (sharedWith == null) {
        document.setSharedWithUserIds(new ArrayList<>());
        sharedWith = document.getSharedWithUserIds();
    }

    if (!sharedWith.contains(usuarioId)) {
        sharedWith.add(usuarioId);
        documentRepository.save(document);
        logger.info("Documento ID: {} compartilhado com o usuário ID: {}", documentoId, usuarioId);
        return ResponseEntity.ok("Documento ID: " + documentoId + " compartilhado com o usuário ID: " + usuarioId);
    } else {
        logger.warn("Documento já compartilhado com este usuário.");
        return ResponseEntity.badRequest().body("Documento já compartilhado com este usuário");
    }
}

	@PostMapping("/compartilhar-todos")
	public ResponseEntity<String> compartilharDocumentoComTodos(@RequestBody CompartilharDocumentoDTO dto) {
	    logger.info("CompartilharDocumentoComTodos: Documento ID: {}", dto.getDocumentoId());

	    Long documentoId = dto.getDocumentoId();
	    Optional<Document> documentOpt = documentRepository.findById(documentoId);
	    if (!documentOpt.isPresent()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento não encontrado");
	    }

	    Document document = documentOpt.get();
	    Usuario loggedInUser = getLoggedInUser();

	    if (!document.getUsuario().getId().equals(loggedInUser.getId())) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para compartilhar este documento");
	    }

	    List<Usuario> allUsers = usuarioRepository.findAll();
	    List<Long> userIdsToShare = allUsers.stream()
	        .map(Usuario::getId)
	        .filter(id -> !id.equals(loggedInUser.getId()))
	        .collect(Collectors.toList());

	    if (userIdsToShare.isEmpty()) {
	        return ResponseEntity.badRequest().body("Nenhum usuário disponível para compartilhar");
	    }

	    List<Long> sharedWith = document.getSharedWithUserIds();
	    if (sharedWith == null) {
	        document.setSharedWithUserIds(new ArrayList<>());
	        sharedWith = document.getSharedWithUserIds();
	    }

	    int novosCompartilhamentos = 0;
	    for (Long userId : userIdsToShare) {
	        if (!sharedWith.contains(userId)) {
	            sharedWith.add(userId);
	            novosCompartilhamentos++;
	        }
	    }

	    documentRepository.save(document);
	    return ResponseEntity.ok("Documento ID: " + documentoId + " compartilhado com " + novosCompartilhamentos + " usuários.");
	}

@DeleteMapping("/excluir")
public ResponseEntity<String> excluirCompartilhamento(@RequestBody ExcluirCompartilhamentoDTO dto) {
    Long documentoId = dto.getDocumentoId();
    Long usuarioId = dto.getUsuarioId();

    Optional<Document> documentOpt = documentRepository.findById(documentoId);

    if (!documentOpt.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento não encontrado");
    }

    Document document = documentOpt.get();
    Usuario loggedInUser = getLoggedInUser();

    if (!document.getUsuario().getId().equals(loggedInUser.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para excluir compartilhamentos deste documento");
    }

    List<Long> sharedWith = document.getSharedWithUserIds();
    if (sharedWith != null && sharedWith.contains(usuarioId)) {
        sharedWith.remove(usuarioId);
        documentRepository.save(document);
        return ResponseEntity.ok("Compartilhamento do Documento ID: " + documentoId + " com o usuário ID: " + usuarioId + " excluído.");
    } else {
        return ResponseEntity.badRequest().body("Compartilhamento não encontrado");
    }
}

@DeleteMapping("/excluir-todos")
public ResponseEntity<String> excluirCompartilhamentoDeTodos(@RequestBody CompartilharDocumentoDTO dto) {
    logger.info("ExcluirCompartilhamentoDeTodos: Documento ID: {}", dto.getDocumentoId());

    Optional<Document> documentOpt = documentRepository.findById(dto.getDocumentoId());
    if (!documentOpt.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento não encontrado");
    }

    Document document = documentOpt.get();
    Usuario loggedInUser = getLoggedInUser();

    if (!document.getUsuario().getId().equals(loggedInUser.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para excluir compartilhamentos deste documento");
    }

    document.setSharedWithUserIds(new ArrayList<>());
    documentRepository.save(document);

    return ResponseEntity.ok("Todos os compartilhamentos do Documento ID: " + dto.getDocumentoId() + " foram removidos.");
}


	@PostMapping("/upload")
	public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file,
			@RequestParam("name") String name, @RequestParam("description") String description) throws IOException {

		byte[] fileBytes = file.getBytes();

		Document document = new Document();
		document.setName(name);
		document.setDescription(description);
		document.setFileData(fileBytes);

		document.setOriginalFileName(file.getOriginalFilename());
		document.setFileType(file.getContentType());

		// Atribui o usuário logado ao documento
		document.setUsuario(getLoggedInUser());

		Document savedDocument = documentService.saveDocument(document);
		return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
	}

	@PutMapping("/{id}/update")
	public ResponseEntity<Document> updateDocument(@PathVariable Long id,
			@RequestParam(value = "file", required = false) MultipartFile file, @RequestParam("name") String name,
			@RequestParam("description") String description) throws IOException {

		Optional<Document> existingDocumentOpt = documentService.getDocument(id);
		if (!existingDocumentOpt.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		Document existingDocument = existingDocumentOpt.get();

		// Verifica se o usuário logado é o dono do documento
		if (!existingDocument.getUsuario().getUsername().equals(getLoggedInUser().getUsername())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		existingDocument.setName(name);
		existingDocument.setDescription(description);

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

        Usuario loggedInUser = getLoggedInUser();

        // Verifica se o usuário é o dono ou se o documento foi compartilhado com ele
        if (!document.getUsuario().getId().equals(loggedInUser.getId()) &&
            (document.getSharedWithUserIds() == null || !document.getSharedWithUserIds().contains(loggedInUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String contentType = determineContentType(document.getOriginalFileName());

        return ResponseEntity.ok().header("Content-Type", contentType)
                .header("Content-Disposition", "inline; filename=\"" + document.getOriginalFileName() + "\"")
                .body(document.getFileData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocument(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        Usuario loggedInUser = getLoggedInUser();

        // Verifica se o usuário é o dono ou se o documento foi compartilhado com ele
        if (!document.getUsuario().getId().equals(loggedInUser.getId()) &&
            (document.getSharedWithUserIds() == null || !document.getSharedWithUserIds().contains(loggedInUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(document);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Document>> getAllDocuments(@RequestParam("page") int page,
            @RequestParam("size") int size) {
        // Busca os documentos apenas do usuário logado e compartilhados com ele
        Usuario loggedInUser = getLoggedInUser();
        Page<Document> documents = documentService.getDocumentsByUserAndSharedWithUser(loggedInUser, page, size);
        return ResponseEntity.ok(documents);
    }
    


	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
		Document document = documentService.getDocument(id)
				.orElseThrow(() -> new RuntimeException("Documento não encontrado"));

		// Verifica se o usuário logado é o dono do documento
		if (!document.getUsuario().getUsername().equals(getLoggedInUser().getUsername())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		documentService.deleteDocument(id);
		return ResponseEntity.noContent().build();
	}

	

	// Buscar documentos

	@GetMapping(value = "/search", produces = "application/json")
	public ResponseEntity<Page<Document>> searchDocuments(@RequestParam String keyword,
	        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
	    // Obter o usuário logado
	    Usuario loggedInUser = getLoggedInUser();
	    
	    // Criação do Pageable
	    Pageable pageable = PageRequest.of(page, size);
	    
	    // Realizar a busca somente para o usuário logado
	    Page<Document> result = documentRepository.findByUsuarioAndNameContainingIgnoreCaseOrUsuarioAndDescriptionContainingIgnoreCase(
	            loggedInUser, keyword, loggedInUser, keyword, pageable);
	    
	    return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	
	
	

	private String determineContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

		switch (extension) {
		case "pdf":
			return "application/pdf";
		case "jpg":
		case "jpeg":
			return "image/jpeg";
		case "png":
			return "image/png";
		case "gif":
			return "image/gif";
		case "txt":
			return "text/plain";
		case "csv":
			return "text/csv";
		case "html":
			return "text/html";
		case "json":
			return "application/json";
		case "xml":
			return "application/xml";
		case "mp4":
			return "video/mp4";
		case "mp3":
			return "audio/mpeg";
		case "wav":
			return "audio/wav";
		case "doc":
		case "docx":
			return "application/msword";
		case "xls":
		case "xlsx":
			return "application/vnd.ms-excel";
		case "ppt":
		case "pptx":
			return "application/vnd.ms-powerpoint";
		default:
			return "application/octet-stream";
		}
	}
}
