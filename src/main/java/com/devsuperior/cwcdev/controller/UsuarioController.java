package com.devsuperior.cwcdev.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.cwcdev.model.Document;
import com.devsuperior.cwcdev.model.Role;
import com.devsuperior.cwcdev.model.Usuario;
import com.devsuperior.cwcdev.repository.RoleRepository;
import com.devsuperior.cwcdev.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/add")
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario, @RequestParam boolean isAdmin) {
        // Definir o papel (admin ou não)
        Role role = isAdmin ? roleRepository.findById(1L).orElse(null) : roleRepository.findById(2L).orElse(null);

        if (role != null) {
            // Salvar o usuário
            usuario = usuarioRepository.save(usuario); 

            // Associar o usuário ao papel
            usuarioRepository.addRoleToUsuario(usuario.getId(), role.getId());

            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.badRequest().build();
    }

    // Buscar todos os usuarios com paginação
    @GetMapping("/all")
    public ResponseEntity<List<Usuario>> getAllDocuments() {
        List<Usuario> usuarios = (List<Usuario>) usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }
    
 // Deletar documento
 	@DeleteMapping("/{id}")
 	public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
 		usuarioRepository.deleteById(id);
 		return ResponseEntity.noContent().build();
 	}
 	
 	
 	@GetMapping("/{id}")
	public ResponseEntity<Usuario> getUsuario(@PathVariable Long id) {
	    Optional<Usuario> document = usuarioRepository.findById(id);
	    
	    if (document.isPresent()) {
	        return ResponseEntity.ok(document.get());
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}
}
