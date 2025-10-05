import React, { useState } from 'react';
import './App.css'; 

// URL base do seu Spring Boot
const API_BASE_URL = 'http://localhost:8080/api/videos';

function App() {
  const [url, setUrl] = useState('');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Função genérica para chamar os endpoints do backend
  const fetchContent = async (endpoint) => {
    // Para fins de teste, garantimos que a URL não esteja vazia, mas você pode usar qualquer valor.
    const effectiveUrl = url || 'teste-mock'; 
    
    if (!effectiveUrl) {
      setError('Por favor, insira uma URL do YouTube.');
      return;
    }

    setLoading(true);
    setError('');
    setResult('');

    try {
      // Fazendo a chamada para o backend Java (que está com o mock ativo)
      const response = await fetch(`${API_BASE_URL}/${endpoint}?url=${encodeURIComponent(effectiveUrl)}`);

      // 1. CHECAGEM INICIAL DE SUCESSO/FALHA (Status 200 OK)
      if (!response.ok) {
        let errorMsg = `Erro do Servidor (${response.status}).`;

        // Tenta ler o corpo da resposta como texto (é mais seguro para erros)
        const errorBody = await response.text();
        
        if (response.status === 429) {
          errorMsg = "Limite de uso da IA (429 Too Many Requests) atingido. Tente novamente em alguns minutos.";
        } else if (response.status === 404) {
          errorMsg = `Falha na Transcrição. O vídeo pode não ter legendas.`;
        } else {
           // Tenta analisar o JSON para pegar a mensagem de erro detalhada do Spring Boot
          try {
            const errorJson = JSON.parse(errorBody);
            errorMsg = errorJson.error || errorBody;
          } catch (e) {
             errorMsg = errorBody || errorMsg;
          }
        }

        throw new Error(errorMsg);
      }
      
      // 2. CHECAGEM DE SUCESSO (Status 200 OK)
      // Checa o Content-Type para saber se é JSON (transcribe) ou texto (IA)
      const contentType = response.headers.get('content-type');
      let data;

      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
        // Para a rota /transcribe (que retorna JSON), pega o 'transcript'
        setResult(data.transcript); 
      } else {
        // Para rotas de IA (/summarize, /enrich) que retornam texto puro
        data = await response.text();
        setResult(data);
      }

    } catch (err) {
      // 3. ERRO DE REDE/CORS/JS
      setError(`Falha na Requisição: ${err.message || 'Verifique a conexão (backend Java 8080).'}`); 

    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="container">
      <h1>YouTube Text Converter <span className="brain">🧠</span></h1> 
      
      {/* ⚠️ ESPAÇO PARA ANÚNCIOS (MONETIZAÇÃO) ⚠️ */}
      <div className="ad-unit top-ad">Anúncio Aqui (Google AdSense)</div>
      
      <div className="input-area">
        <input
          type="text"
          placeholder="Cole a URL do YouTube aqui (Ex: https://www.youtube.com/watch?v=...)"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          disabled={loading}
        />
        <div className="buttons">
          <button onClick={() => fetchContent('transcribe')} disabled={loading}>
            {loading ? 'Transcrevendo...' : '1. Transcrever (Bruto)'}
          </button>
          <button onClick={() => fetchContent('summarize')} disabled={loading}>
            {loading ? 'Resumindo...' : '2. Resumir (Tópicos IA)'}
          </button>
          <button onClick={() => fetchContent('enrich')} disabled={loading}>
            {loading ? 'Incrementando...' : '3. Aprimorar (Artigo IA)'}
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
          {/* Usa <pre> para respeitar a formatação Markdown da IA */}
          <pre>{result}</pre>
        </div>
      )}
      
      {/* ⚠️ ESPAÇO PARA ANÚNCIOS (MONETIZAÇÃO) ⚠️ */}
      <div className="ad-unit bottom-ad">Anúncio Aqui (Google AdSense)</div>
    </div>
  );
}

export default App;