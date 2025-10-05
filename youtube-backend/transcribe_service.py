from flask import Flask, request, jsonify
from flask_cors import CORS 
import re
import traceback

# ⚠️ Importação de compatibilidade: A versão 0.6.2 garante que este método funciona
from youtube_transcript_api import YouTubeTranscriptApi 
from youtube_transcript_api._errors import TranscriptsDisabled, NoTranscriptFound, VideoUnavailable

app = Flask(__name__)
CORS(app) 

def extract_video_id(url):
    # Regex robusta (já corrigida no passo anterior)
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
        # CHAMADA CORRETA: O método estático da classe principal
        transcript_list = YouTubeTranscriptApi.get_transcript(
            video_id, 
            languages=['pt', 'en']
        )
        
        full_transcript = " ".join([item['text'] for item in transcript_list])

        return jsonify({
            "video_id": video_id,
            "transcript": full_transcript
        })

    except (TranscriptsDisabled, NoTranscriptFound, VideoUnavailable) as e:
        print(f"ERRO DE TRANSCRICAO: {str(e)}")
        return jsonify({
            "error": "O vídeo não possui legendas disponíveis ou está indisponível para esta região/API.",
            "details": str(e)
        }), 404 
    
    except Exception as e:
        print(f"ERRO CRÍTICO NO PYTHON: {str(e)}")
        traceback.print_exc()
        return jsonify({
            "error": "Erro interno desconhecido no serviço de transcrição Python.",
            "details": str(e)
        }), 500

if __name__ == '__main__':
    print("Iniciando o Microserviço de Transcrição...")
    app.run(port=5000, debug=True)