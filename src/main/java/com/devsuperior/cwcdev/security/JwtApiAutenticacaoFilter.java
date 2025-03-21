package com.devsuperior.cwcdev.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/* Filtro onde todas as requisições serão capturadas para autenticação */
public class JwtApiAutenticacaoFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        try {
            Authentication authentication = new JWTTokenAutenticacaoService().getAuthentication(req, res);

            if (authentication == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\": \"Token inválido ou ausente\"}");
                res.getWriter().flush();
                System.out.println("Autenticação falhou: Token inválido ou ausente.");
                return; // Interrompe a execução para evitar chamadas sem autenticação
            }

            System.out.println("Autenticação bem-sucedida para: " + authentication.getName());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"Erro ao processar token\"}");
            res.getWriter().flush();
            System.err.println("Erro na autenticação: " + e.getMessage());
            return;
        }

        chain.doFilter(request, response);
    }
}
