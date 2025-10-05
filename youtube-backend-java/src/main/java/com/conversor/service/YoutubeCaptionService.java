package com.conversor.service;

import org.springframework.stereotype.Service;

@Service
public class YoutubeCaptionService {

    public String getCaptions(String youtubeUrl) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "scripts/get_youtube_captions.py", youtubeUrl);
            Process process = pb.start();
            process.waitFor();
            return new String(process.getInputStream().readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
