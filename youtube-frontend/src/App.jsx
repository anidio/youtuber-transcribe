import React, { useState } from 'react';
import { Loader2, Instagram } from 'lucide-react'; 
import './App.css'; 
// AVISO: A importação do App.css já está aqui!

const API_BASE_URL = 'http://localhost:8080/api/videos';

// Componente Footer Institucional (MangueBit Code)
// Usa classes CSS para os estilos cyberpunk
const MangueBitFooter = () => (
    <footer className="institutional-footer">
        <div style={{ maxWidth: '800px', margin: '0 auto', textAlign: 'center' }}>
            <p><strong>Este é um produto MangueBit Code.</strong></p>
            <p style={{ fontSize: '0.9em' }}>
                Criamos tecnologia sob medida para simplificar processos, aumentar resultados e impulsionar o seu negócio. Fale com a gente e descubra como podemos ajudar você.
            </p>

            <a href="https://www.instagram.com/manguebitcode" target="_blank" rel="noopener noreferrer" className="instagram-link">
                <Instagram size={20} style={{ marginRight: '8px' }} />
                Siga-nos no Instagram!
            </a>

            <div style={{ borderTop: '1px solid rgba(138, 43, 226, 0.3)', paddingTop: '15px', marginTop: '15px', fontSize: '0.8em' }}>
                <p>Contato: <a href="mailto:contato@manguebitcode.com">contato@manguebitcode.com</a> | (81) 99999-8888</p>
                <p>&copy; 2025 MangueBit Code. Todos os direitos reservados.</p>
            </div>
        </div>
    </footer>
);


function App() {
  const [platform, setPlatform] = useState('SEO_LONGO');
  const [limit, setLimit] = useState(500);
  
  // Estado principal (o que a IA irá processar)
  const [inputContent, setInputContent] = useState('Análise do novo celular xpto com foco em bateria e câmera de 108MP.');
  
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const getLimitLabel = () => {
    switch(platform) {
        case 'INSTAGRAM_CURTO':
        case 'TIKTOK_CASUAL':
        case 'ECOM_DIRETO':
            return 'caracteres';
        case 'SEO_LONGO':
        case 'ROTEIRO_TOPICOS':
            return 'palavras';
        default:
            return 'unidades';
    }
  }

  const fetchContent = async () => {
    
    if (!inputContent.trim()) {
      setError('O conteúdo de entrada é obrigatório para gerar o conteúdo.');
      return;
    }

    if (limit <= 0) {
        setError('O limite deve ser maior que zero.');
        return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      const endpoint = 'generate-description'; 
      
      const payload = { 
        transcript: inputContent,
        platform: platform, 
        characterLimit: limit 
      };
      
      const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload), 
      });

      if (!response.ok) {
        const errorBody = await response.text();
        let errorMsg = `Erro do Servidor (${response.status}).`;
        
        try {
          errorMsg = JSON.parse(errorBody).message || errorBody;
        } catch (e) {
           errorMsg = errorBody || errorMsg;
        }
        throw new Error(errorMsg);
      }
      
      const data = await response.text();
      setResult(data);

    } catch (err) {
      setError(`Falha na Requisição: ${err.message || 'Verifique a conexão (backend Java 8080). Certifique-se de que o backend esteja rodando.'}`); 
    } finally {
      setLoading(false);
    }
  };
  
  return (
    // O container é o elemento pai de toda a aplicação (100% width/height)
    <div className="container"> 
      
      {/* O app-wrapper contém o conteúdo principal e se expande verticalmente */}
      <div className="app-wrapper">
          {/* 'content-card' é a classe que limita o conteúdo no centro */}
          <div className="content-card">

              <h1>Gerador de Conteúdo AI <span className="brain">🧠</span></h1> 
              <h3 style={{color: '#00bcd4', fontSize: '1em'}}>Ferramenta para Escala e Monetização (Ads Ready)</h3>
              
              <div className="ad-unit top-ad">Anúncio Aqui (Google AdSense)</div>
              
              <div className="input-area">
                {/* INPUT PRINCIPAL */}
                <textarea
                  placeholder="Descreva aqui o produto, os tópicos ou a ideia principal para a IA."
                  value={inputContent}
                  onChange={(e) => setInputContent(e.target.value)}
                  disabled={loading}
                  rows={6} 
                />
                
                {/* CONTROLES DE PLATAFORMA E LIMITE - Usa a classe responsiva controls-group */}
                <div className="controls-group">
                    
                    {/* Seletor de Plataforma/Estilo */}
                    <select
                        value={platform}
                        onChange={(e) => setPlatform(e.target.value)}
                        disabled={loading}
                        className="dynamic-select"
                    >
                        <option value="SEO_LONGO">Artigo SEO (Longo)</option>
                        <option value="ROTEIRO_TOPICOS">Roteiro (Tópicos)</option>
                        <option value="ECOM_DIRETO">E-commerce (Curto e Direto)</option>
                        <option value="INSTAGRAM_CURTO">Instagram (Curto / Emojis)</option>
                        <option value="TIKTOK_CASUAL">TikTok (Casual / Hashtags)</option>
                    </select>
                    
                    {/* Input de Limite */}
                    <input
                        type="number"
                        value={limit}
                        onChange={(e) => setLimit(Math.max(1, parseInt(e.target.value) || 1))} 
                        disabled={loading}
                        placeholder={`Limite de ${getLimitLabel()}`}
                        min="1"
                        className="dynamic-input"
                    />
                    
                </div>
                {/* FIM CONTROLES */}

                <div className="buttons">

                  {/* Botão UNIFICADO para acionar a geração */}
                  <button 
                    onClick={fetchContent}
                    disabled={loading}
                  >
                    {loading ? (
                      <Loader2 className="spinner" size={18} />
                    ) : (
                      `GERAR CONTEÚDO (Limite: ${limit} ${getLimitLabel()})`
                    )}
                  </button>
                </div>
              </div>

              {/* Exibição de Mensagens */}
              {error && <div className="message error">{error}</div>}
              {loading && <div className="message loading">Processando... A IA está criando o conteúdo.</div>}

              {/* Área de Resultado Final */}
              {result && (
                <div className="result-area">
                  <h2>Conteúdo Otimizado:</h2>
                  <pre>{result}</pre>
                </div>
              )}
              
              <div className="ad-unit bottom-ad">Anúncio Aqui (Google AdSense)</div>
              
          </div>
      </div>
      
      {/* FOOTER INSTITUCIONAL */}
      <MangueBitFooter />

    </div>
  );
}

export default App;