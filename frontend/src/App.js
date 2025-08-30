import React, { useState } from "react";
import FileUpload from "./components/FileUpload";
import AnalysisResult from "./components/AnalysisResult";
import Loader from "./components/Loader";
import "./App.css";

function App() {
  const [loading, setLoading] = useState(false);
  const [analysis, setAnalysis] = useState(null);

  const handleFileUpload = async (file) => {
    setLoading(true);
    setAnalysis(null);

    try {
      const formData = new FormData();
      formData.append("file", file);

      // ⬇️ Backend API URL will go here
      const response = await fetch("http://localhost:8080/api/extract/text", {
        method: "POST",
        body: formData,
      });

      const data = await response.json();
      setAnalysis(data); // expected { text: "...", sentiment: "...", suggestions: [...] }
    } catch (error) {
      console.error("Upload failed:", error);
      alert("Something went wrong! Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <h1>📊 Social Media Content Analyzer</h1>
      <FileUpload onFileUpload={handleFileUpload} />
      {loading && <Loader />}
      {analysis && <AnalysisResult analysis={analysis} />}
    </div>
  );
}

export default App;
