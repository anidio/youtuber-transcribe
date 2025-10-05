from flask import Flask, request, jsonify
from flask_cors import CORS 
import re
import traceback

# NOVO IMPORT: ElementTree para o erro 404
from xml.etree import ElementTree 
# ⚠️ IMPORTAÇÃO FINAL: Importa o módulo inteiro para contornar o AttributeError
import youtube_transcript_api
# Importa as exceções originais (agora importadas da API)
from youtube_transcript_api._errors import TranscriptsDisabled, NoTranscriptFound, VideoUnavailable

app = Flask(__name__)
CORS(app) 

def extract_video_id(url):
    # Regex robusta
    pattern = r"(?:v=|youtu\.be\/|embed\/|v\/|shorts\/)([0-9A-Za-z_-]{11})"
    match = re.search(pattern, url)
    return match.group(1) if match else None


@app.route('/api/transcribe', methods=['GET'])
def transcribe_video():
    youtube_url = request.args.get('url')

    if not youtube_url:
        return jsonify({"error": "Parâmetro 'url' ausente. Forneça a URL do YouTube."}), 400

    video_id = extract_video_id(youtube_url)

    if not video_id:
        return jsonify({"error": "URL do YouTube inválida ou ID do vídeo não encontrado."}), 400

    try:
        # CHAMADA CORRIGIDA: Usa o módulo como objeto para acessar o método
        transcript_list = youtube_transcript_api.get_transcript( 
            video_id, 
            languages=['pt', 'en'],
            allow_automatic_transcripts=True
        )
        
        full_transcript = " ".join([item['text'] for item in transcript_list])

        return jsonify({
            "video_id": video_id,
            "transcript": full_transcript
        })

    # BLOCO DE EXCEÇÃO ATUALIZADO (Trata 404 e erro de parseamento)
    except (TranscriptsDisabled, NoTranscriptFound, VideoUnavailable, ElementTree.ParseError) as e:
        print(f"ERRO DE TRANSCRICAO (404): {str(e)}")
        # Retorna 404 para o Java
        return jsonify({
            "error": "O vídeo não possui legendas disponíveis (apenas automáticas) ou está indisponível para esta região/API.",
            "details": str(e)
        }), 404 
    
    except Exception as e:
        print(f"ERRO CRÍTICO NO PYTHON (500): {str(e)}")
        traceback.print_exc()
        return jsonify({
            "error": "Erro interno desconhecido no serviço de transcrição Python.",
            "details": str(e)
        }), 500

if __name__ == '__main__':
    print("Iniciando o Microserviço de Transcrição...")
    app.run(port=5000, debug=True)