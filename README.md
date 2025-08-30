# Social Media Analyzer

This project is a **full-stack web application** that analyzes uploaded media files and extracts useful information using OCR (Optical Character Recognition).  
The project is divided into **Frontend** (React.js) and **Backend** (Spring Boot with Tess4J & Tesseract OCR).

---

## 🚀 Features
- Upload images and extract text using **Tesseract OCR**
- Backend powered by **Spring Boot** and **Tess4J**
- Frontend built with **React.js**
- Attractive and user-friendly UI
- Supports English OCR (default)

---

## 🛠️ Tech Stack
- **Frontend:** React.js, CSS  
- **Backend:** Java Spring Boot, Tess4J, Maven  
- **OCR Engine:** Tesseract (via Homebrew on Mac)  

---

## 📂 Project Structure
project-root/
│── frontend/ # React.js app
│── backend/ # Spring Boot backend
│── README.md
│── .gitignore

## ⚡ Installation & Setup

### 1️⃣ Clone Repository
git clone https://github.com/your-username/social-media-analyzer.git
cd social-media-analyzer

## Backend Setup (Spring Boot)
cd backend
mvn clean install
mvn spring-boot:run

Backend runs by default on http://localhost:8080

## Frontend Setup (React.js)
cd ../frontend
npm install
npm start

Frontend runs by default on http://localhost:3000
