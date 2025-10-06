package com.conversor.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptRequest {
    private String transcript;
    private String url;
    private String platform;
    private int characterLimit;
}