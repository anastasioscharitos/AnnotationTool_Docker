from contextlib import asynccontextmanager
from json import dumps

import requests
import numpy as np
import cv2
import uvicorn
import ftfy
from fastapi import FastAPI, Body, HTTPException, Request
from pydantic import BaseModel
from ultralytics import YOLO, YOLOWorld
from PIL import Image
from typing import List, Optional
import torch
import clip
import spacy
import gc
from contextlib import asynccontextmanager
import certifi

#------------Δεδομένα-------
class UpDateSettingRequest(BaseModel):
    img_model : str

class AnnotateRequest(BaseModel):
    urls : Optional[List[str]] = None
    labels : Optional[List[str]] = None
    threshold : float = 0.3
    topK : Optional[int] = 5
    text : Optional[str] = None
    #προσθήκη εδώ
    ignoreSSL : Optional[bool] = False

#----------Διαχείριση Μνήμης---------
device = "cuda" if torch.cuda.is_available() else "cpu"
#print(f"--- Service initializing on {device} ---")

# Μοντέλα
models = {"text": None, "image": None, "image_name": None}


def clear_memory():
    models["image"] = None
    gc.collect()
    if torch.cuda.is_available():
        torch.cuda.empty_cache()


@asynccontextmanager
async def load_default_models(app: FastAPI):

    clear_memory()
    models["text"] = spacy.load("en_core_web_sm")
    models["image"] = YOLO('yolov8n.pt').to(device)
    models["image_name"] = "yolo"

    yield
    clear_memory()


app = FastAPI(lifespan=load_default_models)

#-------- endPoint----

@app.get("/")
async def root():
    dictionary = {"text": "spaCy en_core_web_sm",
                  "image": models["image_name"]
                  }

    return dictionary


@app.get("/Status")
async def get_status():
    dictionary = {"image": models["image_name"]}

    return dictionary


@app.post("/Update_Settings")
async def update_settings(data: UpDateSettingRequest):
    clear_memory()

    img_choice = data.img_model

    try:
        if img_choice == "yolo":
            models["image"] = YOLO('yolov8n.pt').to(device)
        elif img_choice == "yolo_world":
            models["image"] = YOLOWorld('yolov8s-world.pt').to(device)
        elif img_choice == "clip":
            m, p = clip.load("ViT-B/32", device=device)
            models["image"] = {"model": m, "preprocess": p}
        else:
          #  print(f"error to load model: {img_choice}")
            return {"status": "error", "loaded_models": img_choice}

        models["image_name"] = img_choice

        dictionary = {"status": "ok", "loaded_models": img_choice}


        return dictionary

    except Exception as e:
        s = str(e) + "!!!"
        raise HTTPException(status_code=400, detail=s)


@app.post("/TextAnnotation")
async def spacy_endpoint(data: AnnotateRequest):
    text = ftfy.fix_text(data.text)

    if not text:
        raise HTTPException(status_code=400, detail="No text provided")

    doc = models["text"](text)
    entities_dict = {}
    for ent in doc.ents:
        if ent.label_ not in entities_dict:
            entities_dict[ent.label_] = []
        entities_dict[ent.label_].append(ent.text)


    return entities_dict

@app.post("/ImageAnnotation")
@torch.no_grad()
async def annotate(data: AnnotateRequest):


    if models["image"] is None:
        raise HTTPException(status_code=400, detail="No model loaded")

    headers = {
        'User-Agent': 'Mozilla/5.0'
    }

    results = {}
    with requests.Session() as session:

        if models["image_name"] == "yolo_world" and data.labels:
            models["image"].set_classes(data.labels)

        for url in data.urls:
            try:
                #προσθήκη verify_ssl και στο resp
                if data.ignoreSSL:
                    verify_ssl = False
                else:
                    verify_ssl = certifi.where()

                resp = session.get(url, headers=headers, timeout=10 , verify=verify_ssl)
                img_bytes = np.frombuffer(resp.content, np.uint8)
                img_cv = cv2.imdecode(img_bytes, cv2.IMREAD_COLOR)

                del resp

                if img_cv is None:
                    raise HTTPException(status_code=400, detail=f"Could not download or decode image {url}")

                if models["image_name"] == "yolo":
                    res = models["image"].predict(img_cv, conf=data.threshold, verbose=False)
                    all_detections = []
                    for r in res:
                        for box in r.boxes:
                            label =  models["image"].names[int(box.cls)]
                            score = float(box.conf)
                            all_detections.append((label,score))
                    all_detections.sort(key=lambda x: x[1], reverse=True)

                    final_results = []
                    for label, score in all_detections:
                        if label not in final_results:
                            final_results.append(label)
                        if len(final_results) >= data.topK:
                            break

                    results[url] = final_results
                    del res

                elif models["image_name"] == "clip":
                    img_pil = Image.fromarray(cv2.cvtColor(img_cv, cv2.COLOR_BGR2RGB))
                    img_input = models["image"]["preprocess"](img_pil).unsqueeze(0).to(device)
                    tokens = clip.tokenize(data.labels).to(device)

                    logits, _ = models["image"]["model"](img_input, tokens)
                    #προσθήκη detach()
                    probs = torch.sigmoid(logits).detach().cpu().numpy()[0]

                    all_detections = []
                    for i in range(len(data.labels)):
                        label = data.labels[i]
                        score = float(probs[i])
                        if score > data.threshold:
                            all_detections.append((label,score))

                    all_detections.sort(key=lambda x: x[1], reverse=True)

                    final_results = []
                    for label, score in all_detections:
                        final_results.append(label)
                        if len(final_results) >= data.topK:
                            break

                    results[url] = final_results
                    del img_pil, img_input, tokens, probs
                    if torch.cuda.is_available():
                        torch.cuda.empty_cache()

                elif models["image_name"] == "yolo_world":
                    #models["image"].set_classes(data.labels)
                    res = models["image"].predict(img_cv, conf=data.threshold, verbose=False)
                    detections = []


                    for r in res:
                        for box in r.boxes:
                            label = models["image"].names[int(box.cls)]
                            score = float(box.conf)
                            detections.append((label,score))

                    detections.sort(key=lambda x: x[1], reverse=True)
                    final_results = []

                    for label, score in detections:
                        if label not in final_results:
                            final_results.append(label)
                        if len(final_results) >= data.topK:
                            break

                    results[url] = final_results
                    del res

                del img_cv, img_bytes

            except Exception as e:
                results[url]=[f"Error: {str(e)}"]


            finally:
                if "img_cv" in locals():
                    del img_cv
                if 'res' in locals():
                    del res
                gc.collect()
                if torch.cuda.is_available():
                    torch.cuda.empty_cache()

    return results


@app.middleware("http")
async def add_connection_close_header(request: Request, call_next):
    response = await call_next(request)
    response.headers["Connection"] = "close"
    return response