package com.devsuperior.cwcdev.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.devsuperior.cwcdev.ApplicationContextLoad;
import com.devsuperior.cwcdev.model.Usuario;
import com.devsuperior.cwcdev.repository.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@Service
public class JWTTokenAutenticacaoService {

    private static final long EXPIRATION_TIME = 172800000; // 2 dias
    private static final String SECRET = "SenhaExtremamenteSecreta";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";

    public void addAuthentication(HttpServletResponse response, String username) throws IOException {
        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        response.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
        response.setContentType("application/json");
        response.getWriter().write("{\"Authorization\": \"" + TOKEN_PREFIX + token + "\"}");
        liberacaoCors(response);
    }

    public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null && token.startsWith(TOKEN_PREFIX)) {
            try {
                String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();
                Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(tokenLimpo).getBody();
                String username = claims.getSubject();

                if (username != null) {
                    Usuario usuario = ApplicationContextLoad.getApplicationContext()
                            .getBean(UsuarioRepository.class).findUserByLogin(username);
                    
                    if (usuario != null) {
                        return new UsernamePasswordAuthenticationToken(
                                usuario.getLogin(),
                                usuario.getSenha(),
                                usuario.getAuthorities());
                    }
                }
            } catch (ExpiredJwtException e) {
                System.err.println("Token expirado: " + e.getMessage());
            } catch (SignatureException | MalformedJwtException e) {
                System.err.println("Token inválido: " + e.getMessage());
            }
        }

        liberacaoCors(response);
        return null;
    }

    private void liberacaoCors(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    }
}