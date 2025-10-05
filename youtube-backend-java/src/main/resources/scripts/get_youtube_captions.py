import sys
from youtube_transcript_api import YouTubeTranscriptApi

url = sys.argv[1]
video_id = url.split("v=")[-1]
transcript = YouTubeTranscriptApi.get_transcript(video_id)
text = " ".join([x["text"] for x in transcript])
print(text)
