import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import google.generativeai as genai
from dotenv import load_dotenv

# Cargamos la clave del archivo .env para mantenerla segura
load_dotenv()

app = FastAPI(title="MiUNET AI Agent API")

# Configuramos Gemini de forma global al iniciar el servidor
api_key = os.getenv("GEMINI_API_KEY")
if not api_key:
    print("⚠️ ADVERTENCIA: No se encontró GEMINI_API_KEY en el archivo .env")
else:
    genai.configure(api_key=api_key)

# Definimos la estructura exacta que esperamos recibir desde Kotlin
class ChatRequest(BaseModel):
    pregunta: str

@app.get("/")
def home():
    return {"status": "El servidor de MiUNET está vivo 🚀"}

@app.post("/api/chat")
async def chat_con_gemini(request: ChatRequest):
    if not request.pregunta.strip():
        raise HTTPException(status_code=400, detail="La pregunta no puede estar vacía.")

    try:
        # Configuramos el modelo y su "personalidad"
        model = genai.GenerativeModel(
                    model_name="gemini-2.5-flash",  # <--- NUEVO MOTOR V8 INSTALADO
                    system_instruction="Eres el asistente virtual no oficial de la UNET. Tu tono es útil, directo y amigable con los estudiantes de ingeniería. Responde de forma concisa."
        )

        # Enviamos la pregunta a la IA
        response = model.generate_content(request.pregunta)

        # Devolvemos un JSON limpio al frontend
        return {"respuesta": response.text}

    except Exception as e:
            error_real = str(e)
            print(f"Error crítico en el servidor: {error_real}")
            raise HTTPException(status_code=500, detail=f"Error técnico de Google: {error_real}")