// Arquivo: youtube-frontend/src/content.js

// 💡 Esta função tenta extrair o texto de transcrição atualmente visível no DOM do YouTube.
// A estrutura do DOM pode mudar, mas este é o ponto principal a ser inspecionado.
function getTranscriptText() {
    // Tenta encontrar a div principal que contém a transcrição (classe específica do YouTube)
    const transcriptContainer = document.querySelector('div.ytd-transcript-renderer');
    
    if (transcriptContainer) {
        // Seleciona todos os segmentos de texto
        const segments = transcriptContainer.querySelectorAll('.segment-text'); 
        
        let fullTranscript = '';

        segments.forEach(segment => {
            // Adiciona o texto de cada segmento com um espaço
            fullTranscript += segment.textContent + ' ';
        });

        if (fullTranscript.trim().length > 0) {
            return fullTranscript.trim();
        }
    }
    
    // Fallback: Tenta encontrar o texto da descrição (menos ideal)
    const descriptionElement = document.querySelector('#description yt-formatted-string.ytd-text-inline-details-renderer');
    if (descriptionElement) {
        return descriptionElement.textContent;
    }

    return null; 
}

// 💡 Ouve mensagens do Pop-up (App.jsx)
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === 'GET_TRANSCRIPT') {
        const transcript = getTranscriptText();
        
        // Envia o texto da transcrição de volta para o Pop-up
        sendResponse({ transcript: transcript });
        return true; // Indica que a resposta será enviada assincronamente
    }
});