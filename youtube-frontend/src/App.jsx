import React, { useState } from 'react';
import './App.css'; 

const API_BASE_URL = 'http://localhost:8080/api/videos';

function App() {
  const [url, setUrl] = useState(''); // Voltamos ao input de URL
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Função genérica para chamar os endpoints do backend
  const fetchContent = async (endpoint) => {
    const effectiveUrl = url;

    if (!effectiveUrl) {
      setError('Por favor, insira a URL do YouTube.');
      return;
    }

    setLoading(true);
    setError('');
    setResult('');

    try {
      // ⚠️ FLUXO WHISPER: Chamada GET enviando a URL para o Java
      const response = await fetch(`${API_BASE_URL}/${endpoint}?url=${encodeURIComponent(effectiveUrl)}`);

      // Checagem de Erro e Sucesso... (mantida)

      if (!response.ok) {
        let errorMsg = `Erro do Servidor (${response.status}).`;
        const errorBody = await response.text();
        
        if (response.status === 429) {
          errorMsg = "Limite de uso da IA (429 Too Many Requests) atingido. Tente novamente em alguns minutos.";
        } else if (response.status === 404) {
          errorMsg = `Falha na Transcrição. O vídeo pode não ter legendas.`;
        } else {
          try {
            const errorJson = JSON.parse(errorBody);
            errorMsg = errorJson.error || errorBody;
          } catch (e) {
             errorMsg = errorBody || errorMsg;
          }
        }
        throw new Error(errorMsg);
      }
      
      // Recebe o texto (transcrição bruta ou resultado da IA)
      const data = await response.text();

      // Ajusta o resultado para exibir corretamente
      if (endpoint === 'transcribe') {
          try {
            const dataJson = JSON.parse(data);
            setResult(dataJson.transcript); 
          } catch(e) {
            setResult(data);
          }
      } else {
        setResult(data);
      }

    } catch (err) {
      setError(`Falha na Requisição: ${err.message || 'Verifique a conexão (backend Java 8080).'}`); 
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="container">
      
      <h1>AI Converter <span className="brain">🧠</span></h1> 
      
      <div className="ad-unit top-ad">Anúncio Aqui (Google AdSense)</div>
      
      <div className="input-area">
        {/* 💡 INPUT DE URL */}
        <input
          type="text"
          placeholder="Cole a URL do YouTube aqui (Ex: https://www.youtube.com/watch?v=...)"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          disabled={loading}
        />
        
        <div className="buttons">
          <button onClick={() => fetchContent('transcribe')} disabled={loading}>
            {loading ? 'Transcrevendo...' : '1. Transcrever (Áudio)'}
          </button>
          
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
      {loading && <div className="message loading">Processando... Isso pode levar alguns minutos com o Whisper.</div>}

      {/* Área de Resultado */}
      {result && (
        <div className="result-area">
          <h2>Resultado:</h2>
          <pre>{result}</pre>
        </div>
      )}
      
      <div className="ad-unit bottom-ad">Anúncio Aqui (Google AdSense)</div>
      
      <footer style={{marginTop: '20px', fontSize: '0.75em', color: '#666'}}>
          &copy; [Nome da Empresa] - Projeto de IA
      </footer>
    </div>
  );
}

export default App;