package com.devsuperior.cwcdev.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.cwcdev.ObjetoErro;
import com.devsuperior.cwcdev.model.Usuario;
import com.devsuperior.cwcdev.repository.UsuarioRepository;

@RestController
@RequestMapping(value = "/recuperar")
public class RecuperaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @ResponseBody
    @PostMapping(value = "/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ObjetoErro> recuperar(@RequestBody Usuario login) throws Exception {

    	ObjetoErro objectError = new ObjetoErro();

        // Verifica se o usuário existe no banco de dados
        Usuario user = usuarioRepository.findUserByLogin(login.getLogin());

        if (user == null) {
            // Se o usuário não for encontrado
            objectError.setCode("404");
            objectError.setError("Usuario não encontrado!!");
        } else {

            // Gera uma nova senha aleatória de 6 dígitos
            Random random = new Random();
            int randomNumber = 100000 + random.nextInt(900000); // Gera um número aleatório de 6 dígitos
            String senhaNova = String.valueOf(randomNumber);

            // Criptografa a nova senha
            String senhaCriptografada = new BCryptPasswordEncoder().encode(senhaNova);

            // Atualiza a senha do usuário no banco de dados
            usuarioRepository.updateSenha(senhaCriptografada, user.getId());

            // Retorna um sucesso
            objectError.setCode("200");
            objectError.setError("Senha alterada com sucesso!, sua senha é "+ senhaNova);
        }

        return new ResponseEntity<ObjetoErro>(objectError, HttpStatus.OK);
    }
}
