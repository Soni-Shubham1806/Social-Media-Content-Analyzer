import React from "react";

function AnalysisResult({ analysis }) {
  return (
    <div className="result-container">
      <h2>Result</h2>
      <div>
        <h3>Extracted Text</h3>
        <p>{analysis.text || "No text extracted"}</p>
      </div>
     
    </div>
  );
}

export default AnalysisResult;
