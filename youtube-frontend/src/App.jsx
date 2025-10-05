import React, { useState } from 'react';
import './App.css'; 

const API_BASE_URL = 'http://localhost:8080/api/videos';

function App() {
  const [transcriptText, setTranscriptText] = useState(''); 
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // 💡 MENSAGEM DO BOTÃO 1
  const [button1Text, setButton1Text] = useState('1. Extrair Transcrição');

  // Função para solicitar a transcrição do Content Script
  const getTranscriptFromTab = () => {
    return new Promise((resolve, reject) => {
        // Envia uma mensagem para o Content Script na aba ativa
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            if (tabs.length === 0 || !tabs[0].id) {
                return reject(new Error("Nenhuma aba ativa encontrada. Navegue para um vídeo do YouTube."));
            }

            chrome.tabs.sendMessage(tabs[0].id, { action: 'GET_TRANSCRIPT' }, (response) => {
                if (chrome.runtime.lastError) {
                    // O erro de runtime geralmente ocorre se o Content Script falhou ao injetar (extensão nova, página não recarregada)
                    return reject(new Error("Erro de comunicação com o Content Script. Recarregue a página do YouTube e tente novamente."));
                }
                
                if (response && response.transcript) {
                    resolve(response.transcript);
                } else {
                    reject(new Error("Transcrição não encontrada. O vídeo pode não ter legendas ou o formato do YouTube mudou."));
                }
            });
        });
    });
  };

  const fetchContent = async (endpoint) => {
    setLoading(true);
    setError('');
    setResult('');
    
    try {
      // 💡 PASSO 1: O Front-end OBTÉM O TEXTO DO NAVEGADOR
      setButton1Text('Extraindo...');
      const transcript = await getTranscriptFromTab();
      setTranscriptText(transcript); // Exibe no textarea
      
      let finalResult;
      
      if (endpoint === 'transcribe') {
          // Se for o botão 'Transcrever', apenas exibe o texto extraído
          finalResult = transcript;
          setButton1Text('1. Extração Sucedida');
      } else {
          // 💡 PASSO 2: Chama o endpoint de IA (POST) com o texto real
          setButton1Text('Texto obtido, Processando IA...');
          const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
            method: 'POST',
            headers: {
              'Content-Type': 'text/plain', 
            },
            body: transcript, 
          });
          
          if (!response.ok) {
            let errorMsg = `Erro do Servidor (${response.status}).`;
            const errorBody = await response.text();
            throw new Error(errorBody || errorMsg);
          }
          
          finalResult = await response.text();
          setButton1Text('1. Extração Completa');
      }
      
      setResult(finalResult);

    } catch (err) {
      setError(`Falha: ${err.message || 'Verifique a conexão (backend Java 8080).'}`); 
      setButton1Text('1. Falha na Extração');
    } finally {
      setLoading(false);
      // Reverte o texto do botão principal para o estado original após um tempo
      setTimeout(() => setButton1Text('1. Extrair Transcrição'), 3000);
    }
  };
  
  return (
    <div className="container">
      
      <h1>AI Converter <span className="brain">🧠</span></h1> 
      
      <div className="ad-unit top-ad">Anúncio Aqui (Google AdSense)</div>
      
      <div className="input-area">
        <textarea
          rows="8"
          placeholder="O texto da transcrição será extraído automaticamente aqui após clicar no botão 1."
          value={transcriptText}
          onChange={(e) => setTranscriptText(e.target.value)}
          disabled={loading}
        />
        
        <div className="buttons">
          {/* 💡 BOTÃO PRINCIPAL: Inicia o processo de automação e extração */}
          <button onClick={() => fetchContent('transcribe')} disabled={loading}>
            {loading && button1Text === 'Extraindo...' ? 'Extraindo...' : button1Text}
          </button>
          
          {/* BOTÕES DE IA: Agora chamam o fluxo que inicia a extração */}
          <button onClick={() => fetchContent('summarize')} disabled={loading || transcriptText.length === 0}>
            {loading ? 'Resumindo...' : '2. RESUMIR (Tópicos IA)'}
          </button>
          <button onClick={() => fetchContent('enrich')} disabled={loading || transcriptText.length === 0}>
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
      
      <footer style={{marginTop: '20px', fontSize: '0.75em', color: '#666'}}>
          &copy; [Nome da Empresa] - Projeto de IA
      </footer>
    </div>
  );
}

export default App;