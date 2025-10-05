package com.conversor.service;

import com.conversor.dto.TranscriptResponse;
import com.conversor.service.GeminiService;
import com.conversor.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TranscriptionService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private WhisperService whisperService;

    @Autowired
    private YoutubeCaptionService youtubeCaptionService;

    public TranscriptResponse processVideo(String youtubeUrl) {
        String transcript = youtubeCaptionService.getCaptions(youtubeUrl);

        if (transcript == null || transcript.isEmpty()) {
            transcript = whisperService.transcribeAudio(youtubeUrl);
        }

        String summary = geminiService.generateSummary(transcript);
        String topics = geminiService.extractTopics(transcript);
        String improvedDescription = geminiService.enhanceDescription(transcript);

        return new TranscriptResponse(transcript, summary, topics, improvedDescription);
    }
}
