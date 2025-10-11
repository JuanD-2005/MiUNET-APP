# 📱 MiUNET-APP

**Plataforma Integral Universitaria - UNET**

MiUNET es una aplicación móvil desarrollada en **Kotlin (Android Studio)** que busca centralizar la información universitaria de la **Universidad Nacional Experimental del Táchira (UNET)**, mejorando la comunicación entre estudiantes, profesores y departamentos a través de un entorno moderno, intuitivo y escalable.

---

## 🌐 Descripción General

Actualmente, los estudiantes de la UNET enfrentan dificultades para acceder a información actualizada sobre trámites, horarios, eventos y servicios universitarios.  
Esta aplicación surge como una **solución tecnológica integral**, combinando una arquitectura basada en **Firebase** y una interfaz moderna bajo **Material Design 3**.

---

## 🚀 Funcionalidades Principales

- 🔐 **Autenticación con roles (Firebase Auth)**
  - Estudiantes, Profesores y Administradores con permisos diferenciados.
- 🤖 **Chatbot académico**
  - Asistente virtual integrado para responder preguntas frecuentes.
- 🗓️ **Gestión de información**
  - Visualización de eventos, horarios y trámites.
- 🧩 **Firebase Cloud Firestore**
  - Base de datos en tiempo real para mantener la información actualizada.
- 🖥️ **Diseño basado en Material Design**
  - Interfaz amigable, moderna y adaptativa.
- 🧑‍💻 **Módulo de administración**
  - Permite editar la información institucional directamente desde la app.

---

## 🏗️ Arquitectura del Sistema

**Arquitectura Cliente - Servidor:**
- **Cliente:** Aplicación Android desarrollada en Kotlin.
- **Servidor:** Firebase (Firestore, Auth, Storage).

MiUNET App (Cliente Android)
↓ ↑
Firebase Cloud Firestore
↓ ↑
Usuarios / Datos Institucionales

📘 *Estructura modular:*  
Cada fragmento representa una sección funcional del sistema:
- `UnetInfoFragment` → Departamentos, servicios y eventos.  
- `TramitesFragment` → Trámites y precios administrativos.  
- `ChatbotFragment` → Asistente virtual UNET.  
- `UsuarioFragment` → Perfil y configuración.  

---

## 📂 Estructura del Proyecto
MiUNET/
├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/com/example/miunet01/
│ │ │ │ ├── ui/
│ │ │ │ │ ├── chatbot/
│ │ │ │ │ ├── login/
│ │ │ │ │ ├── tramites/
│ │ │ │ │ └── unetinfo/
│ │ │ ├── res/
│ │ │ │ ├── layout/
│ │ │ │ ├── drawable/
│ │ │ │ ├── menu/
│ │ │ │ └── values/
│ ├── build.gradle
│ ├── AndroidManifest.xml
│ └── ...
├── gradle/
└── README.md


---

## 🧪 Tecnologías Utilizadas

| Tipo | Herramienta / Tecnología |
|------|--------------------------|
| Lenguaje | Kotlin |
| IDE | Android Studio |
| Base de Datos | Firebase Cloud Firestore |
| Autenticación | Firebase Auth |
| UI | Material Design 3 |
| Arquitectura | Cliente-Servidor |
| Control de Versiones | Git / GitHub |

---

## ⚙️ Instalación

1. Clona el repositorio:
   ```bash
   git clone https://github.com/JuanD-2005/MiUNET.git
