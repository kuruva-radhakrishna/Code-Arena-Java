import React from 'react';
import MonacoEditor from '@monaco-editor/react';

function CodeEditor({ value, onChange, language = 'cpp', placeholder = 'Write your code here...' }) {
  return (
    <div style={{ width: '100%' }}>
      <label style={{
        display: 'block',
        marginBottom: 6,
        fontWeight: 500,
        color: '#888',
        fontSize: '1rem'
      }}>
        {language.toUpperCase()} Code
      </label>
      <MonacoEditor
        height="300px"
        defaultLanguage={language}
        language={language}
        value={value}
        onChange={onChange}
        theme="vs-light"
        options={{
          fontSize: 16,
          minimap: { enabled: false },
          scrollBeyondLastLine: false,
          wordWrap: 'on',
          fontFamily: 'Fira Mono, Consolas, monospace',
          automaticLayout: true,
        }}
      />
    </div>
  );
}

export default CodeEditor; 