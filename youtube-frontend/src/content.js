// Arquivo: youtube-frontend/src/content.js

// 💡 Esta função tenta extrair o texto da transcrição atualmente visível no DOM do YouTube.
function getTranscriptText() {
    // Tenta encontrar o contêiner principal da transcrição, que geralmente tem o atributo 'id' ou uma classe estável.
    const transcriptPanel = document.getElementById('panels-container'); 
    
    if (transcriptPanel) {
        // Tenta encontrar todos os elementos que contêm os segmentos de texto de forma mais genérica.
        // O texto geralmente está dentro de uma <div role="listitem"> ou similar.
        const segmentContainers = transcriptPanel.querySelectorAll('div[role="listitem"]'); 
        
        let fullTranscript = '';

        if (segmentContainers.length > 0) {
             // Itera sobre os contêineres e extrai o texto formatado.
            segmentContainers.forEach(container => {
                // Tenta encontrar o texto dentro de yt-formatted-string ou similar
                const textElement = container.querySelector('yt-formatted-string, span'); 
                if (textElement) {
                    fullTranscript += textElement.textContent + ' ';
                }
            });
        }

        if (fullTranscript.trim().length > 0) {
            return fullTranscript.trim();
        }
    }
    
    // Se não for encontrado na lateral, retorna a URL (aqui você colocaria um fallback com uma API de scraping paga)
    return null; 
}

// 💡 Ouve mensagens do Pop-up (App.jsx)
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === 'GET_TRANSCRIPT') {
        const transcript = getTranscriptText();
        
        sendResponse({ transcript: transcript });
        return true; 
    }
});