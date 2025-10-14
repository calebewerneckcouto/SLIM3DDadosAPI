package com.devsuperior.cwcdev;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.devsuperior.cwcdev.model.Usuario;
import com.devsuperior.cwcdev.repository.UsuarioRepository;

@Component
public class EchoBot extends TelegramLongPollingBot {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public String getBotUsername() {
        return DadosBot.BOT_USER_NAME;
    }

    @Override
    public String getBotToken() {
        return DadosBot.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage mensagem = responder(update);
            try {
                execute(mensagem);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private SendMessage responder(Update update) {
        String textoMensagem = update.getMessage().getText().toLowerCase();
        String chatId = update.getMessage().getChatId().toString();
        String resposta = "";

        // Verifica a mensagem recebida
        switch (textoMensagem) {
            case "/ajuda":
                resposta = "Utilize um dos comandos:\n- gerar senha\n- ajuda humanizada";
                break;
            case "ajuda humanizada":
                resposta = "Meu número de telefone é: 31 98796-7617\nSe precisar de mais ajuda, estou por aqui!";
                break;
            case "gerar senha":
                resposta = "Por favor, me envie seu e-mail para gerar sua nova senha.";
                break;
            default:
                // Verifica se o e-mail foi enviado pelo usuário
                if (isEmail(textoMensagem)) {
                    String senhaGerada = gerarSenha(textoMensagem);
                    resposta = "Sua nova senha é: " + senhaGerada;
                } else {
                    resposta = "Não entendi!\nDigite /ajuda para ver os comandos disponíveis.";
                }
                break;
        }

        SendMessage mensagem = new SendMessage();
        mensagem.setText(resposta);
        mensagem.setChatId(chatId);
        return mensagem;
    }

    private String gerarSenha(String login) {
        // Gerando uma senha aleatória de 8 dígitos
        Random random = new Random();
        int senha = 10000000 + random.nextInt(90000000);
        String senhaGerada = String.valueOf(senha);

        // Busca o usuário pelo login (e-mail)
        Usuario usuario = usuarioRepository.findUserByLogin(login);

        if (usuario != null) {
            // Codificando a senha com BCrypt
            String senhaCodificada = new BCryptPasswordEncoder().encode(senhaGerada);

            // Atualiza a senha do usuário encontrado
            usuarioRepository.updateSenha(senhaCodificada, usuario.getId());
            return senhaGerada; // Retorna a senha gerada em texto simples
        } else {
            return "Usuário não encontrado!";
        }
    }

    private boolean isEmail(String textoMensagem) {
        // Verifica se o texto é um e-mail válido (simplificado)
        return textoMensagem.contains("@");
    }
}