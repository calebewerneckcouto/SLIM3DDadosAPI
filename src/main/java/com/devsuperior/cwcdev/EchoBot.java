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
    
    // Senha estática para autenticação
    private static final String SENHA_ADMIN = "cwc3d14694899";
    
    // Classe para controlar o estado da autenticação por usuário
    private class EstadoUsuario {
        boolean autenticado = false;
        String etapa = null; // "aguardando_senha", "aguardando_email", "processando"
    }
    
    // Mapa simples para controle de estado (em produção, use um cache apropriado)
    private java.util.Map<Long, EstadoUsuario> estadosUsuarios = new java.util.HashMap<>();

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
        String textoMensagem = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        Long userId = update.getMessage().getFrom().getId();
        String resposta = "";

        // Inicializa o estado do usuário se não existir
        if (!estadosUsuarios.containsKey(userId)) {
            estadosUsuarios.put(userId, new EstadoUsuario());
        }
        
        EstadoUsuario estado = estadosUsuarios.get(userId);

        // Comandos principais
        switch (textoMensagem.toLowerCase()) {
            case "/start":
                resposta = "🤖 *Bem-vindo ao Bot de Recuperação de Senha!*\n\n";
                resposta += "Eu posso ajudar na geração de novas senhas.\n\n";
                resposta += "*Comandos disponíveis:*\n";
                resposta += "/gerar_senha - Gerar nova senha para usuário\n";
                resposta += "/ajuda - Mostrar ajuda\n";
                resposta += "/sair - Sair do modo administrador\n";
                break;
                
            case "/ajuda":
            case "ajuda":
                resposta = "📋 *Ajuda do Bot*\n\n";
                resposta += "*Comandos:*\n";
                resposta += "/start - Iniciar o bot\n";
                resposta += "/gerar_senha - Gerar nova senha\n";
                resposta += "/sair - Sair do modo administrador\n";
                resposta += "/ajuda - Mostrar esta ajuda\n\n";
                resposta += "*Como usar:*\n";
                resposta += "1. Use /gerar_senha\n";
                resposta += "2. Informe a senha de administrador\n";
                resposta += "3. Digite o e-mail do usuário\n";
                resposta += "4. A senha será gerada automaticamente";
                break;
                
            case "ajuda humanizada":
                resposta = "📞 *Suporte Humanizado*\n";
                resposta += "Telefone: 31 98796-7617\n";
                resposta += "Estou aqui para ajudar!";
                break;
                
            case "/sair":
                if (estado.autenticado) {
                    estado.autenticado = false;
                    estado.etapa = null;
                    resposta = "✅ Você saiu do modo administrador.";
                } else {
                    resposta = "ℹ️ Você não está autenticado.";
                }
                break;
                
            case "/gerar_senha":
                if (estado.autenticado) {
                    resposta = "🔐 *Você já está autenticado!*\n\n";
                    resposta += "Por favor, digite o e-mail do usuário que deseja gerar uma nova senha:";
                    estado.etapa = "aguardando_email";
                } else {
                    resposta = "🔐 *Autenticação Necessária*\n\n";
                    resposta += "Para gerar uma nova senha, por favor informe a senha de administrador:";
                    estado.etapa = "aguardando_senha";
                }
                break;
                
            default:
                // Processa com base no estado atual
                resposta = processarMensagem(textoMensagem, estado, userId);
                break;
        }

        SendMessage mensagem = new SendMessage();
        mensagem.setChatId(chatId);
        mensagem.setText(resposta);
        mensagem.enableMarkdown(true);
        
        return mensagem;
    }

    private String processarMensagem(String textoMensagem, EstadoUsuario estado, Long userId) {
        if (estado.etapa != null) {
            switch (estado.etapa) {
                case "aguardando_senha":
                    return processarSenhaAdmin(textoMensagem, estado);
                    
                case "aguardando_email":
                    return processarEmailUsuario(textoMensagem, estado);
                    
                default:
                    estado.etapa = null;
                    return "Não entendi! Digite /ajuda para ver os comandos.";
            }
        } else {
            return "Não entendi! Digite /ajuda para ver os comandos disponíveis.";
        }
    }

    private String processarSenhaAdmin(String senhaInformada, EstadoUsuario estado) {
        if (senhaInformada.equals(SENHA_ADMIN)) {
            estado.autenticado = true;
            estado.etapa = "aguardando_email";
            return "✅ *Autenticação bem-sucedida!*\n\n";
        } else {
            estado.autenticado = false;
            estado.etapa = null;
            return "❌ *Senha incorreta!*\n\n";
        }
    }

    private String processarEmailUsuario(String email, EstadoUsuario estado) {
        // Valida se é um e-mail
        if (!isEmail(email.toLowerCase())) {
            return "❌ *E-mail inválido!*\n\n";
        }
        
        // Gera a senha
        String resultado = gerarSenhaParaUsuario(email.toLowerCase());
        
        // Reseta o estado após a operação
        estado.etapa = null;
        
        return resultado;
    }

    private String gerarSenhaParaUsuario(String email) {
        try {
            // Gera uma senha aleatória de 8 dígitos
            Random random = new Random();
            int senhaNumerica = 10000000 + random.nextInt(90000000);
            String senhaGerada = String.valueOf(senhaNumerica);

            // Busca o usuário pelo login (e-mail)
            Usuario usuario = usuarioRepository.findUserByLogin(email);

            if (usuario != null) {
                // Codificando a senha com BCrypt
                String senhaCodificada = new BCryptPasswordEncoder().encode(senhaGerada);

                // Atualiza a senha do usuário encontrado
                usuarioRepository.updateSenha(senhaCodificada, usuario.getId());
                
                // Formata a resposta
                String resposta = "✅ *Senha gerada com sucesso!*\n\n";
                resposta += "📧 *E-mail:* " + email + "\n";
                resposta += "🔑 *Nova senha:* `" + senhaGerada + "`\n\n";
                resposta += "⚠️ *Atenção:*\n";
                resposta += "- Esta senha é temporária\n";
                resposta += "- O usuário deve alterá-la no primeiro acesso\n";
                resposta += "- A senha já foi atualizada no sistema";
                
                return resposta;
            } else {
                return "❌ *Usuário não encontrado!*\n\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ *Erro ao gerar senha!*\n\n";
        }
    }

    private boolean isEmail(String texto) {
        // Validação simples de e-mail
        return texto.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }
}