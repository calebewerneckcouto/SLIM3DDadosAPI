package com.devsuperior.cwcdev;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.devsuperior.cwcdev.model.Usuario;
import com.devsuperior.cwcdev.repository.UsuarioRepository;

@Component
public class EchoBot extends TelegramLongPollingBot {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            System.out.println("✅ Bot registrado com sucesso!");
            System.out.println("🤖 Bot: " + getBotUsername());
            System.out.println("🔑 Token: " + (getBotToken() != null ? "Configurado" : "NÃO CONFIGURADO"));
        } catch (Exception e) {
            System.err.println("❌ Erro ao registrar bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
        System.out.println("📨 Update recebido! ID: " + update.getUpdateId());
        
        if (update.hasMessage()) {
            System.out.println("💬 Tem mensagem!");
            System.out.println("👤 Chat ID: " + update.getMessage().getChatId());
            System.out.println("👤 From: " + (update.getMessage().getFrom() != null ? 
                update.getMessage().getFrom().getFirstName() : "N/A"));
            
            if (update.getMessage().hasText()) {
                String texto = update.getMessage().getText();
                System.out.println("📝 Texto: " + texto);
                
                SendMessage mensagem = responder(update);
                try {
                    execute(mensagem);
                    System.out.println("✅ Resposta enviada: " + mensagem.getText());
                } catch (TelegramApiException e) {
                    System.err.println("❌ Erro ao enviar mensagem: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ Mensagem sem texto");
            }
        } else {
            System.out.println("⚠️ Update sem mensagem");
        }
    }

    private SendMessage responder(Update update) {
        String textoMensagem = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        String resposta = "";

        // Remove espaços e converte para minúsculas
        String textoLimpo = textoMensagem.trim().toLowerCase();
        
        System.out.println("🔍 Processando comando: " + textoLimpo);

        switch (textoLimpo) {
            case "/start":
                resposta = "Olá! Eu sou o CalebBot 🤖\n\n" +
                          "Comandos disponíveis:\n" +
                          "/ajuda - Mostra esta mensagem\n" +
                          "/gerarsenha - Gera uma nova senha\n" +
                          "ajuda humanizada - Contato humano";
                break;
                
            case "/ajuda":
            case "ajuda":
            case "/help":
            case "help":
                resposta = "🤖 **Comandos disponíveis:**\n\n" +
                          "• `/ajuda` - Mostra esta mensagem\n" +
                          "• `gerar senha` - Inicia o processo de geração de senha\n" +
                          "• `ajuda humanizada` - Mostra contato para ajuda humana\n\n" +
                          "Para gerar uma senha, digite: **gerar senha**";
                break;
                
            case "gerar senha":
            case "gerarsenha":
            case "/gerarsenha":
                resposta = "Por favor, envie seu **e-mail** para que eu possa gerar sua nova senha.";
                break;
                
            case "ajuda humanizada":
                resposta = "📞 **Contato para ajuda humanizada:**\n" +
                          "Telefone: 31 98796-7617\n" +
                          "Se precisar de mais ajuda, estou por aqui!";
                break;
                
            default:
                if (isEmail(textoLimpo)) {
                    String senhaGerada = gerarSenha(textoLimpo);
                    resposta = "✅ Senha gerada com sucesso!\n\n" +
                              "**E-mail:** " + textoLimpo + "\n" +
                              "**Nova senha:** `" + senhaGerada + "`\n\n" +
                              "Guarde esta senha em um local seguro!";
                } else {
                    resposta = "❓ Não entendi seu comando.\n" +
                              "Digite `/ajuda` para ver os comandos disponíveis.";
                }
                break;
        }

        SendMessage mensagem = new SendMessage();
        mensagem.setChatId(chatId);
        mensagem.setText(resposta);
        mensagem.enableMarkdown(true); // Habilita Markdown
        
        return mensagem;
    }

    private String gerarSenha(String login) {
        try {
            System.out.println("🔑 Tentando gerar senha para: " + login);
            
            // Gerando uma senha aleatória de 8 dígitos
            Random random = new Random();
            int senha = 10000000 + random.nextInt(90000000);
            String senhaGerada = String.valueOf(senha);

            // Verifica se o repositório está disponível
            if (usuarioRepository == null) {
                System.err.println("⚠️ Repositório de usuário é nulo!");
                return "Erro: Repositório não disponível";
            }

            // Busca o usuário pelo login (e-mail)
            Usuario usuario = usuarioRepository.findUserByLogin(login);

            if (usuario != null) {
                System.out.println("✅ Usuário encontrado: " + usuario.getLogin());
                
                // Codificando a senha com BCrypt
                String senhaCodificada = new BCryptPasswordEncoder().encode(senhaGerada);

                // Atualiza a senha do usuário encontrado
                usuarioRepository.updateSenha(senhaCodificada, usuario.getId());
                
                System.out.println("✅ Senha atualizada no banco de dados");
                return senhaGerada;
            } else {
                System.out.println("⚠️ Usuário não encontrado para: " + login);
                return "❌ Usuário não encontrado para o e-mail: " + login;
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar senha: " + e.getMessage());
            e.printStackTrace();
            return "⚠️ Erro ao gerar senha. Tente novamente mais tarde.";
        }
    }

    private boolean isEmail(String texto) {
        // Verifica se o texto parece ser um e-mail
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return texto.matches(emailPattern);
    }
}