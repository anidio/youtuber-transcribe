import React, { useState } from 'react';
import './App.css'; 

const API_BASE_URL = 'http://localhost:8080/api/videos';

function App() {
  const [transcriptText, setTranscriptText] = useState(''); 
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const fetchContent = async (endpoint) => {
    
    if (endpoint === 'transcribe') {
        setError('A extração automática foi desativada. Cole o texto no campo e use a IA.');
        return;
    }
    
    const effectiveText = transcriptText; 

    if (!effectiveText) {
      setError('Por favor, cole o texto da transcrição no campo para usar a IA.');
      return;
    }

    setLoading(true);
    setError('');
    setResult('');

    try {
      // Chamada POST (corpo da requisição) - Fluxo Tactiq
      const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain', 
        },
        body: effectiveText, 
      });

      // 1. CHECAGEM DE ERRO
      if (!response.ok) {
        let errorMsg = `Erro do Servidor (${response.status}).`;

        const errorBody = await response.text();
        
        if (response.status === 429) {
          errorMsg = "Limite de uso da IA (429 Too Many Requests) atingido. Tente novamente em alguns minutos.";
        } else {
          try {
            const errorJson = JSON.parse(errorBody);
            errorMsg = errorJson.message || errorBody;
          } catch (e) {
             errorMsg = errorBody || errorMsg;
          }
        }

        throw new Error(errorMsg);
      }
      
      // 2. CHECAGEM DE SUCESSO
      const data = await response.text();
      setResult(data);

    } catch (err) {
      setError(`Falha na Requisição: ${err.message || 'Verifique a conexão (backend Java 8080).'}`); 
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="container">
      
      {/* 💡 CABEÇALHO LIMPO E FOCO NO SERVIÇO */}
      <h1>AI Converter <span className="brain">🧠</span></h1> 
      
      <div className="ad-unit top-ad">Anúncio Aqui (Google AdSense)</div>
      
      <div className="input-area">
        
        {/* 💡 REMOVEMOS INFORMAÇÕES DE URL CONFUSAS */}
        <textarea
          rows="8"
          placeholder="COLE A TRANSCRIÇÃO BRUTA AQUI (Obtida do vídeo, modelo Tactiq para IA)..."
          value={transcriptText}
          onChange={(e) => setTranscriptText(e.target.value)}
          disabled={loading}
        />
        
        <div className="buttons">
          {/* BOTÃO DE INFORMAÇÃO */}
          <button onClick={() => fetchContent('transcribe')} disabled={loading}>
            {loading ? 'Processando...' : '1. INFO: Como Obter o Texto?'}
          </button>
          
          {/* BOTÕES DE IA */}
          <button onClick={() => fetchContent('summarize')} disabled={loading}>
            {loading ? 'Resumindo...' : '2. RESUMIR (Tópicos IA)'}
          </button>
          <button onClick={() => fetchContent('enrich')} disabled={loading}>
            {loading ? 'Incrementando...' : '3. APRIMORAR (Artigo IA)'}
          </button>
        </div>
      </div>

      {/* Exibição de Mensagens */}
      {error && <div className="message error">{error}</div>}
      {loading && <div className="message loading">Processando... Isso pode levar alguns segundos com a IA.</div>}

      {/* Área de Resultado */}
      {result && (
        <div className="result-area">
          <h2>Resultado:</h2>
          <pre>{result}</pre>
        </div>
      )}
      
      <div className="ad-unit bottom-ad">Anúncio Aqui (Google AdSense)</div>
      
      {/* 💡 ESPAÇO PARA O FUTURO FOOTER DA EMPRESA */}
      <footer style={{marginTop: '20px', fontSize: '0.75em', color: '#666'}}>
          &copy; [Nome da Empresa] - Projeto de IA
      </footer>
    </div>
  );
}

export default App;