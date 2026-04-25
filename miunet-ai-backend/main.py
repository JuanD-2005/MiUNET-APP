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

# --- 🔹 RAG NIVEL 2: Bóveda de Conocimiento ---

def cargar_documentos_unet():
    """Lee todos los archivos .md de la carpeta conocimiento_unet"""
    conocimiento = ""
    # Ruta relativa al directorio del script
    base_dir = os.path.dirname(os.path.abspath(__file__))
    carpeta = os.path.join(base_dir, "conocimiento_unet")
    
    if os.path.exists(carpeta):
        for archivo in os.listdir(carpeta):
            if archivo.endswith(".md"):
                try:
                    with open(os.path.join(carpeta, archivo), "r", encoding="utf-8") as f:
                        conocimiento += f"\n\n--- INICIO DEL DOCUMENTO: {archivo} ---\n\n"
                        conocimiento += f.read()
                        conocimiento += f"\n\n--- FIN DEL DOCUMENTO: {archivo} ---\n\n"
                except Exception as e:
                    print(f"Error leyendo {archivo}: {e}")
    else:
        print(f"⚠️ La carpeta {carpeta} no existe.")
    return conocimiento

# Construimos la instrucción maestra dinámica
base_prompt = """Eres el Asistente Virtual Oficial de la UNET (Universidad Nacional Experimental del Táchira).
Dirígete a los estudiantes de manera amable, respetuosa y útil, llamándolos 'Inge' o 'compañero'.

REGLA ESTRICTA: Responde ÚNICAMENTE basándote en los documentos oficiales proporcionados a continuación. 
Si el estudiante pregunta algo que no está en estos documentos, responde: "Aún no tengo esa información en mi base de datos oficial de la UNET, te recomiendo consultar directamente en el departamento correspondiente."

A continuación, la información oficial de la UNET:
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
        # Cargamos el conocimiento actualizado en cada consulta (o podrías hacerlo global)
        conocimiento_actualizado = cargar_documentos_unet()
        instrucciones_completas = base_prompt + conocimiento_actualizado

        # Inicializamos el modelo con el "súper cerebro" inyectado
        model = genai.GenerativeModel(
            model_name="gemini-1.5-flash",
            system_instruction=instrucciones_completas
        )

        # Enviamos la pregunta a la IA
        response = model.generate_content(request.pregunta)

        # Devolvemos un JSON limpio al frontend
        return {"respuesta": response.text}

    except Exception as e:
        error_real = str(e)
        print(f"Error crítico en el servidor: {error_real}")
        raise HTTPException(status_code=500, detail=f"Error técnico de Google: {error_real}")
