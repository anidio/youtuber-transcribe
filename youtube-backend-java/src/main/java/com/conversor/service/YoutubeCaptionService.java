package com.conversor.service;

import org.springframework.stereotype.Service;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço Híbrido: Executa um script Python externo para extrair a transcrição real do YouTube,
 * contornando o bloqueio de requisições diretas do YouTube em Java.
 */
@Service
public class YoutubeCaptionService {

    private final Gson gson = new Gson();

    private String extractVideoId(String youtubeUrl) {
        Pattern pattern = Pattern.compile("(?:v=|youtu\\.be\\/|embed\\/|v\\/|shorts\\/)([0-9A-Za-z_-]{11})");
        Matcher matcher = pattern.matcher(youtubeUrl);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Executa o script Python e retorna a transcrição.
     */
    public String getCaptions(String youtubeUrl) {
        String videoId = extractVideoId(youtubeUrl);
        if (videoId == null) {
            throw new RuntimeException("URL do YouTube inválida.");
        }

        String scriptName = "youtube_script.py";

        System.out.println("⏳ PYTHON HYBRID: Iniciando execução do Python para o ID: " + videoId);

        try {
            // 💡 CORREÇÃO: Usamos o diretório de trabalho atual (./), onde o Spring está rodando.
            // Isso resolve o erro 'CreateProcess error=267'.
            File workingDirectory = new File("./");

            // O nome do interpretador 'python' e o nome do arquivo 'youtube_script.py'
            ProcessBuilder pb = new ProcessBuilder("python", scriptName, videoId);

            // Define o diretório de trabalho (o diretório atual)
            pb.directory(workingDirectory);
            Process process = pb.start();

            // Lê a saída padrão do script Python (stdout)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            // Espera o processo terminar
            int exitCode = process.waitFor();
            String output = builder.toString().trim();

            if (exitCode != 0) {
                // Se o Python retornar um erro (exitCode != 0), lê o JSON de erro
                try {
                    Map<String, String> errorMap = gson.fromJson(output, Map.class);
                    if (errorMap != null && errorMap.containsKey("error")) {
                        throw new RuntimeException("Falha de Transcrição Python: " + errorMap.get("error"));
                    }
                } catch (Exception jsonEx) {
                    // Se não for JSON, retorna o erro bruto do console
                    throw new RuntimeException("Erro desconhecido na execução Python. Saída: " + output);
                }
            }

            if (output.isEmpty()) {
                throw new RuntimeException("Script Python não retornou transcrição.");
            }

            System.out.println("✅ PYTHON HYBRID: Transcrição obtida com sucesso.");
            return output;

        } catch (Exception e) {
            System.err.println("❌ Erro ao executar o Python via Java: " + e.getMessage());
            // Lançamos uma exceção para o Controller retornar um 404/400
            throw new RuntimeException("Falha na Transcrição: " + e.getMessage());
        }
    }
}