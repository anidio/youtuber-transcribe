package com.conversor.service;

import com.conversor.dto.TranscriptResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TranscriptionService {

    // Mantenha a injeção, mas não a use.
    private final WebClient webClient;

    public TranscriptionService(WebClient.Builder webClientBuilder) {
        // Inicializa o WebClient, mas ele não será usado nesta versão de teste
        this.webClient = webClientBuilder.build();
    }

    /**
     * MOCK ATIVO: Retorna um texto de transcrição simulado para que o serviço de IA
     * possa ser testado. ESTA É A VERSÃO DE PRODUÇÃO DEVIDO A ERROS DE AMBIENTE.
     */
    public TranscriptResponse getTranscription(String youtubeUrl) {
        System.out.println("⚠️ MOCK ATIVO: Retornando texto de teste para a URL: " + youtubeUrl);

        // Texto Mock de qualidade para a IA processar
        TranscriptResponse mockResponse = new TranscriptResponse();
        mockResponse.setVideoId("MOCK_ID");
        mockResponse.setTranscript("O desenvolvimento ágil é essencial para startups. Ele se concentra na entrega rápida de software funcional e na colaboração com o cliente. Diferente dos métodos tradicionais, o ágil permite mudanças de requisitos de forma mais fácil. Scrum e Kanban são frameworks populares do desenvolvimento ágil.");

        return mockResponse;
    }
}