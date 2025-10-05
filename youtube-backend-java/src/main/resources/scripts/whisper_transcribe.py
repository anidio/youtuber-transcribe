import sys
import whisper
import yt_dlp

url = sys.argv[1]
audio_path = "temp_audio.mp3"

# Baixar áudio
ydl_opts = {
    'format': 'bestaudio/best',
    'outtmpl': audio_path,
    'quiet': True,
    'postprocessors': [{
        'key': 'FFmpegExtractAudio',
        'preferredcodec': 'mp3',
        'preferredquality': '192',
    }],
}

with yt_dlp.YoutubeDL(ydl_opts) as ydl:
    ydl.download([url])

# Transcrever com Whisper
model = whisper.load_model("base")
result = model.transcribe(audio_path)
print(result["text"])
