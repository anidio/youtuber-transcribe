package com.conversor.dto;

import lombok.Data;

@Data
public class TranscriptResponse {
    //usamos 'videoId' e 'transcript' para corresponder ao JSON do Python.
    private String videoId;
    private String transcript;
}
