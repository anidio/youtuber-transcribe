import React, { useState } from 'react';
import { Loader2 } from 'lucide-react'; 

const API_BASE_URL = 'http://localhost:8080/api/videos';

// Injeta estilos básicos para evitar a falha de compilação em tags <style> externas
const GlobalStyles = () => (
    <style jsx global>{`
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
        .spinner {
            animation: spin 1s linear infinite;
        }
        .buttons button:not(:disabled) {
            box-shadow: 0 4px 10px rgba(138, 43, 226, 0.4);
            transition: all 0.2s ease-in-out;
        }
        .buttons button:hover:not(:disabled) {
            transform: translateY(-2px);
        }
        .result-area h2 {
            color: #ff005c; 
        }
        .container {
            background: #1a1a2e;
            color: #e0ffff;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
        }
        textarea {
            border: 2px solid #8a2be2;
            background-color: #0f0f18;
            color: #e0ffff;
            resize: vertical;
        }
        .message.error {
            color: #ff005c;
            border: 1px solid #ff005c;
            padding: 10px;
            margin-top: 15px;
        }
    `}</style>
);


function App() {
  const [inputContent, setInputContent] = useState('Descreva aqui o produto ou os tópicos-chave para o seu artigo (Ex: "Análise do novo celular xpto com foco em bateria e câmera").');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchContent = async (endpoint) => {
    
    if (!inputContent.trim()) {
      setError('O conteúdo de entrada é obrigatório para gerar o artigo/roteiro.');
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      // Envia o texto como 'transcript' para reusar os endpoints existentes do backend
      const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ transcript: inputContent }), // Envia o conteúdo do campo como 'transcript'
      });

      if (!response.ok) {
        const errorBody = await response.text();
        let errorMsg = `Erro do Servidor (${response.status}).`;
        
        try {
          // Tenta extrair a mensagem de erro da exceção lançada pelo Controller
          errorMsg = JSON.parse(errorBody).message || errorBody;
        } catch (e) {
           errorMsg = errorBody || errorMsg;
        }
        throw new Error(errorMsg);
      }
      
      const data = await response.text();
      setResult(data);

    } catch (err) {
      setError(`Falha na Requisição: ${err.message || 'Verifique a conexão (backend Java 8080).'}`); 
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="container" style={{maxWidth: '600px', margin: '40px auto'}}>
      <GlobalStyles />

      <h1>Gerador de Conteúdo AI <span className="brain" style={{color: '#ff005c'}}>🧠</span></h1> 
      <h3 style={{color: '#00bcd4', fontSize: '1em'}}>Ferramenta para Escala e SEO (Ads Ready)</h3>
      
      <div className="ad-unit top-ad" style={{border: '1px solid #8a2be2', padding: '10px', marginTop: '15px'}}>Anúncio Aqui (Google AdSense)</div>
      
      <div className="input-area" style={{marginTop: '20px'}}>
        {/* INPUT PRINCIPAL: Tópicos / Descrição de Produto */}
        <textarea
          placeholder="Descreva aqui o produto, os tópicos ou a ideia principal para a IA."
          value={inputContent}
          onChange={(e) => setInputContent(e.target.value)}
          disabled={loading}
          rows={6} 
          style={{width: '100%', padding: '15px', marginBottom: '15px'}}
        />
        
        <div className="buttons" style={{marginTop: '15px', display: 'flex', flexDirection: 'column', gap: '10px'}}>

          {/* Botão para Roteiro/Tópicos (Ideia 3) */}
          <button 
            onClick={() => fetchContent('summarize')}
            disabled={loading}
            style={{background: loading ? '#444' : '#00bcd4', color: 'white', fontWeight: 'bold', padding: '12px', borderRadius: '8px'}}
          >
            {loading ? (
              <Loader2 className="spinner" size={18} />
            ) : (
              '1. Gerar Roteiro/Esboço (Tópicos IA)'
            )}
          </button>

          {/* Botão para Artigo/Otimização (Ideia 1) */}
          <button 
            onClick={() => fetchContent('enrich')}
            disabled={loading}
            style={{background: loading ? '#444' : '#ff005c', color: 'white', fontWeight: 'bold', padding: '12px', borderRadius: '8px'}}
          >
            {loading ? (
              <Loader2 className="spinner" size={18} />
            ) : (
              '2. Gerar Artigo Otimizado (500 Palavras)'
            )}
          </button>
        </div>
      </div>

      {/* Exibição de Mensagens */}
      {error && <div className="message error">{error}</div>}
      {loading && <div className="message loading" style={{color: '#00bcd4', marginTop: '15px'}}>Processando... A IA está criando o conteúdo.</div>}

      {/* Área de Resultado Final */}
      {result && (
        <div className="result-area" style={{marginTop: '30px', borderTop: '1px dashed #8a2be2', paddingTop: '20px'}}>
          <h2>Resultado da IA (Pronto para SEO):</h2>
          <pre style={{whiteSpace: 'pre-wrap', wordWrap: 'break-word', padding: '15px', border: '1px solid #ff005c', backgroundColor: '#0f0f18', color: '#e0ffff', borderRadius: '8px', overflowX: 'auto'}}>{result}</pre>
        </div>
      )}
      
      <div className="ad-unit bottom-ad" style={{border: '1px solid #8a2be2', padding: '10px', marginTop: '15px'}}>Anúncio Aqui (Google AdSense)</div>
      
      <footer style={{marginTop: '20px', fontSize: '0.75em', color: '#666', textAlign: 'center'}}>
          &copy; [Nome da Empresa] - Projeto de IA para SEO
      </footer>
    </div>
  );
}

export default App;