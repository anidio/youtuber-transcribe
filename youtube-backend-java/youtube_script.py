import sys
import json
from youtube_transcript_api import YouTubeTranscriptApi, TranscriptsDisabled, NoTranscriptFound

# Lista de idiomas para tentar (PT e EN)
LANGS = ['pt', 'en']

# Função auxiliar para retornar erros formatados em JSON
def return_error(message):
    print(json.dumps({"error": message}))
    sys.exit(1)

# O ID do vídeo é passado como o primeiro argumento da linha de comando (sys.argv[1]).
if len(sys.argv) < 2:
    return_error("Video ID not provided by Java backend.")

# Pega o ID do vídeo (o primeiro argumento após o nome do script)
video_id = sys.argv[1]

try:
    # 1. Busca a transcrição, priorizando o idioma de destino
    # Usamos o método get_transcript que tenta as linguagens na ordem de LANGS
    transcript_list = YouTubeTranscriptApi.get_transcript(video_id, languages=LANGS)

    # 2. Concatena os textos, ignorando os timestamps
    full_transcript = " ".join([item['text'] for item in transcript_list])

    # 3. Retorna a transcrição limpa para o Java (stdout)
    print(full_transcript)

except TranscriptsDisabled:
    return_error("Transcrições desabilitadas pelo autor do vídeo ou vídeo inválido.")
except NoTranscriptFound:
    # Este erro é o mais provável se não houver legenda manual ou automática.
    return_error("Nenhuma legenda manual ou automática encontrada para PT/EN. A transcrição falhou.")
except Exception as e:
    # Captura o erro, mas o formatamos para evitar o erro de JSON não parseável no Java
    return_error(f"Erro inesperado do Python (Scraping): {str(e)}")