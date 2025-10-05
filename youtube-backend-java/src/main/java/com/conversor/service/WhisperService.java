package com.conversor.service;

import org.springframework.stereotype.Service;

@Service
public class WhisperService {

    public String transcribeAudio(String youtubeUrl) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "scripts/whisper_transcribe.py", youtubeUrl);
            Process process = pb.start();
            process.waitFor();
            return new String(process.getInputStream().readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}