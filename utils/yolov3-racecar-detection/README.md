## Requirements:

1. TensorFlow GPU 1.x
2. Flask
3. OpenCV

## RTMP Stream

If you use RTMP, use `rtmp_multiclient.py` and modify:
```
myrtmp_addr = "rtmp://j-093.juliet.futuresystems.org/live/indycar live=1"
```

## Video

If you use a video file as source, use `stream_video.py` and modify:

`cap = cv2.VideoCapture("./video-file.mp4")`





You can see the detection results on `http://localhost:61521/`.