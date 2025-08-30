import React, { useState, useRef } from "react";

function FileUpload({ onFileUpload }) {
  const [file, setFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const inputRef = useRef(null);

  const handleChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!file) {
      alert("Please select a file first!");
      return;
    }
    onFileUpload(file);
  };

  // 🔹 Drag events
  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
    }
  };

  return (
    <div
      className={`upload-container ${dragActive ? "active" : ""}`}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      <form onSubmit={handleSubmit}>
        <p>
          📂 Drag & Drop your file here <br />
          or <strong>choose from computer</strong>
        </p>

        {/* Hidden input triggered by button */}
        <input
          type="file"
          ref={inputRef}
          accept=".pdf, .png, .jpg, .jpeg"
          onChange={handleChange}
          style={{ display: "block", margin: "10px auto" }}
        />

        {file && <p style={{ marginTop: "10px", color: "#2c3e50" }}>📄 Selected: {file.name}</p>}

        <button type="submit">Upload</button>
      </form>
    </div>
  );
}

export default FileUpload;
