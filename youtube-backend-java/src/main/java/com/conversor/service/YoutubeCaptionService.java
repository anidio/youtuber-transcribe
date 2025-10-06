package com.conversor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Service
public class YoutubeCaptionService {

    private final ResourceLoader resourceLoader;

    @Autowired
    public YoutubeCaptionService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // Método utilitário para extrair o script do JAR para um arquivo temporário no disco
    private File extractScript(String scriptName) throws Exception {
        // Acessa o script dentro da pasta 'resources' do classpath
        Resource resource = resourceLoader.getResource("classpath:scripts/" + scriptName);

        // Cria um arquivo temporário no sistema de arquivos
        File tempFile = Files.createTempFile(scriptName.replace(".py", ""), ".py").toFile();
        tempFile.deleteOnExit(); // Garante que o arquivo será deletado ao sair do sistema

        // Copia o conteúdo do JAR para o arquivo temporário
        try (InputStream is = resource.getInputStream();
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            FileCopyUtils.copy(is, fos);
        }
        return tempFile;
    }

    public String getCaptions(String youtubeUrl) {
        File scriptFile = null;
        try {
            // 1. Extrair o script para um local acessível no disco
            scriptFile = extractScript("get_youtube_captions.py");

            // 2. Usar o caminho absoluto do arquivo temporário na ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder("python", scriptFile.getAbsolutePath(), youtubeUrl);            Process process = pb.start();

            // Espera a execução por um tempo razoável
            process.waitFor(10, TimeUnit.SECONDS);

            // Captura a saída do Python
            String output = new String(process.getInputStream().readAllBytes()).trim();

            if (process.exitValue() != 0 || output.isEmpty()) {
                String errorOutput = new String(process.getErrorStream().readAllBytes());
                System.err.println("Python Script Error (get_youtube_captions.py): " + errorOutput);
                return null;
            }
            return output;

        } catch (Exception e) {
            System.err.println("Falha ao tentar executar get_youtube_captions.py: " + e.getMessage());
            return null;
        } finally {
            // 3. Limpar o arquivo temporário
            if (scriptFile != null) {
                scriptFile.delete();
            }
        }
    }
}