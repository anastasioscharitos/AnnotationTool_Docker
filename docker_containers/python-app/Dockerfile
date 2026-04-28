# Χρήση slim έκδοσης Python
FROM python:3.10-slim

# Εγκατάσταση Git και βιβλιοθηκών συστήματος
RUN apt-get update && apt-get install -y \
    build-essential \
    libgl1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    git \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Ορισμός φακέλου εργασίας
WORKDIR /app

# Αναβάθμιση pip και εγκατάσταση βασικών απαιτήσεων
RUN pip install --no-cache-dir --upgrade pip
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Ορισμός φακέλου ρυθμίσεων για Ultralytics, για να μην εμφανίζει warning
ENV YOLO_CONFIG_DIR=/app/config

# Δημιουργία φακέλου /app/config
RUN mkdir -p /app/config

# Εγκατάσταση των μοντέλων Yolo και Yolo World
RUN pip install git+https://github.com/ultralytics/CLIP.git
RUN wget https://github.com/ultralytics/assets/releases/download/v8.2.0/yolov8n.pt -O /app/yolov8n.pt
RUN wget https://github.com/ultralytics/assets/releases/download/v8.4.0/yolov8s-world.pt -O /app/yolov8s-world.pt

# Εγκατάσταση του μοντέλου CLIP
RUN pip install --no-cache-dir git+https://github.com/openai/CLIP.git
RUN python3 -c "import clip; clip.load('ViT-B/32', device='cpu')"
 
# Εγκατάσταση του μοντέλου Spacy (ρητά)
RUN python -m spacy download en_core_web_sm

#Αντιγραφή υπόλοιπου κώδικα και δημιουργία φακέλου δεδομένων
COPY . .
RUN mkdir -p data

# Έκθεση στην θύρα 8000
EXPOSE 8000

# Εκκίνηση FastAPI με uvicorn
CMD ["python", "-m", "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]