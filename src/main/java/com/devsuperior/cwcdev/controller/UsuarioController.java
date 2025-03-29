package com.devsuperior.cwcdev.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    
    
    @GetMapping("/me")
    public String getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Obtendo apenas os nomes das permissões
        String permissoes = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));

        return "Usuário logado: " + authentication.getName() + " | Permissões: " + permissoes;
    }


    @PostMapping("/add")
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario, @RequestParam boolean isAdmin) {
        // Buscar o papel (admin ou usuário comum)
        Role role = isAdmin ? roleRepository.findById(1L).orElse(null) : roleRepository.findById(2L).orElse(null);

        if (role != null) {
            // Criptografar a senha antes de salvar
            usuario.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));

           

            // Salvar o usuário
            usuario = usuarioRepository.save(usuario);

            // Associar o usuário ao papel
            usuarioRepository.addRoleToUsuario(usuario.getId(), role.getId());

            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.badRequest().build();
    }


    // Buscar todos os usuarios com paginação
    @GetMapping("/")
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
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
