package com.conversor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptResponse {
    private String videoId;
    private String transcript;
    private String summary;
    private String topics;
    private String improvedDescription;
}
