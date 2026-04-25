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

# 1. Creamos el manual de empleado (System Instruction)
instrucciones_unet = """
Eres el Asistente Virtual Oficial de la UNET (Universidad Nacional Experimental del Táchira).
Tu tono debe ser amable, respetuoso y útil, dirigiéndote a los estudiantes como 'Unetense'.

Reglas estrictas:
1. Solo respondes preguntas relacionadas con la UNET, ingeniería, o vida universitaria.
2. Si te preguntan algo fuera de estos temas, responde amablemente que tu función es estrictamente académica.

Datos base de la UNET (Usa esto para responder):
- La biblioteca atiende de 7:30 AM a 12:00 PM.
- El comedor está ubicado subiendo en el camino derecho de los auditorios.
- Control de Estudios queda en el piso 1 del edificio Administrativo.
"""

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
        # 2. Inicializamos el modelo inyectándole el manual (System Prompt)
        model = genai.GenerativeModel(
            model_name="gemini-1.5-flash",
            system_instruction=instrucciones_unet
        )

        # Enviamos la pregunta a la IA
        response = model.generate_content(request.pregunta)

        # Devolvemos un JSON limpio al frontend
        return {"respuesta": response.text}

    except Exception as e:
        error_real = str(e)
        print(f"Error crítico en el servidor: {error_real}")
        raise HTTPException(status_code=500, detail=f"Error técnico de Google: {error_real}")
