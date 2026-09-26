import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../contexts/AuthContext';
import CircularProgress from '@mui/material/CircularProgress';
import SendIcon from '@mui/icons-material/Send';
import PersonIcon from '@mui/icons-material/Person';
import ReactMarkdown from 'react-markdown';
import CloseIcon from '@mui/icons-material/Close';
import CropSquareIcon from '@mui/icons-material/CropSquare';
import FullscreenExitIcon from '@mui/icons-material/FullscreenExit';
import { Rnd } from 'react-rnd';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import ForumIcon from '@mui/icons-material/Forum';
import './AIChat.css';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

const AIChat = () => {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [chatHistory, setChatHistory] = useState([]);
  const [open, setOpen] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [size, setSize] = useState({ width: 700, height: window.innerHeight * 0.8 });
  const [position, setPosition] = useState({ x: window.innerWidth - 750, y: window.innerHeight - 600 });
  const messagesEndRef = useRef(null);
  const prevSize = useRef(size);
  const prevPosition = useRef(position);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  // Helper to get user-specific key
  const getUserKey = (base) => {
    return user && user._id ? `${base}_${user._id}` : base;
  };

  // Initialize with welcome message or load from localStorage
  useEffect(() => {
    const savedMessages = localStorage.getItem(getUserKey('aiChatMessages'));
    const savedHistory = localStorage.getItem(getUserKey('aiChatHistory'));
    
    if (savedMessages && savedHistory) {
      try {
        // Parse and revive timestamps as Date objects
        const parsedMessages = JSON.parse(savedMessages).map(msg => ({
          ...msg,
          timestamp: new Date(msg.timestamp)
        }));
        setMessages(parsedMessages);
        setChatHistory(JSON.parse(savedHistory));
      } catch (error) {
        console.error('Error loading chat history:', error);
        initializeWelcomeMessage();
      }
    } else {
      initializeWelcomeMessage();
    }
  }, [user]);

  const initializeWelcomeMessage = () => {
    setMessages([
      {
        id: 1,
        type: 'ai',
        content: `Hello ${user?.firstname || 'there'}! 👋 I'm your AI programming assistant. I can help you with:\n\n• **Code Review & Debugging** - Analyze your code and find issues\n• **Problem Solving** - Get hints and approaches for coding problems\n• **Algorithm Explanations** - Understand complex algorithms\n• **Best Practices** - Learn coding standards and patterns\n• **General Programming Questions** - Ask anything about programming\n\nWhat would you like help with today?`,
        timestamp: new Date()
      }
    ]);
  };

  const MAX_HISTORY = 30;
  const chatFull = messages.length >= MAX_HISTORY;

  const handleSendMessage = async () => {
    if (!inputMessage.trim() || isLoading) return;

    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: inputMessage,
      timestamp: new Date()
    };

    // Prepare new messages/history arrays, keeping only the last MAX_HISTORY
    const updatedMessages = [...messages, userMessage].slice(-MAX_HISTORY);
    setMessages(updatedMessages);
    setInputMessage('');
    setIsLoading(true);

    try {
      const response = await axios.post(`${BACKEND_URL}/ai/chat`, {
        message: inputMessage,
        chatHistory: [...chatHistory, { role: 'user', content: inputMessage }].slice(-MAX_HISTORY)
      }, { withCredentials: true });

      const aiMessage = {
        id: Date.now() + 1,
        type: 'ai',
        content: response.data.response,
        timestamp: new Date()
      };

      // Add AI message and trim to last MAX_HISTORY
      const finalMessages = [...updatedMessages, aiMessage].slice(-MAX_HISTORY);
      setMessages(finalMessages);
      const finalHistory = [...chatHistory, { role: 'user', content: inputMessage }, { role: 'assistant', content: response.data.response }].slice(-MAX_HISTORY);
      setChatHistory(finalHistory);

      // Save to user-specific localStorage
      localStorage.setItem(getUserKey('aiChatMessages'), JSON.stringify(finalMessages));
      localStorage.setItem(getUserKey('aiChatHistory'), JSON.stringify(finalHistory));
    } catch (error) {
      const errorMessage = {
        id: Date.now() + 1,
        type: 'ai',
        content: 'Sorry, I encountered an error. Please try again.',
        timestamp: new Date()
      };
      const finalMessages = [...updatedMessages, errorMessage].slice(-MAX_HISTORY);
      setMessages(finalMessages);
      localStorage.setItem(getUserKey('aiChatMessages'), JSON.stringify(finalMessages));
    }

    setIsLoading(false);
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const formatMessage = (content) => {
    // Convert markdown-like formatting to HTML
    return content
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/\n/g, '<br>');
  };

  const clearChat = () => {
    const welcomeMessage = {
      id: Date.now(),
      type: 'ai',
      content: `Hello ${user?.firstname || 'there'}! 👋 I'm your AI programming assistant. I can help you with:\n\n• **Code Review & Debugging** - Analyze your code and find issues\n• **Problem Solving** - Get hints and approaches for coding problems\n• **Algorithm Explanations** - Understand complex algorithms\n• **Best Practices** - Learn coding standards and patterns\n• **General Programming Questions** - Ask anything about programming\n\nWhat would you like help with today?`,
      timestamp: new Date()
    };
    
    setMessages([welcomeMessage]);
    setChatHistory([]);
    
    // Clear user-specific localStorage
    localStorage.setItem(getUserKey('aiChatMessages'), JSON.stringify([welcomeMessage]));
    localStorage.setItem(getUserKey('aiChatHistory'), JSON.stringify([]));
  };

  const quickActions = [
  "What are the most important graph algorithms to know?",
  "How do I prepare for coding interviews?",
  "What are common types of  problems asked in interviews?",
  "What are common mistakes to avoid in algorithm interviews?",
  "How to approach graph traversal questions in interviews?",
  "Tips for optimizing space complexity in algorithms"
];

  const handleQuickAction = (action) => {
    setInputMessage(action);
  };

  const exportChat = () => {
    const chatText = messages
      .filter(msg => msg.type !== 'ai' || !msg.content.includes('Hello') || !msg.content.includes('👋'))
      .map(msg => {
        const time = msg.timestamp.toLocaleString();
        const sender = msg.type === 'ai' ? 'AI Assistant' : 'You';
        return `[${time}] ${sender}: ${msg.content}`;
      })
      .join('\n\n');
    
    const blob = new Blob([chatText], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ai-chat-${new Date().toISOString().split('T')[0]}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleExpand = () => {
    prevSize.current = size;
    prevPosition.current = position;
    setExpanded(true);
    setSize({ width: window.innerWidth * 0.9, height: window.innerHeight * 0.9 });
    setPosition({ x: window.innerWidth * 0.05, y: window.innerHeight * 0.05 });
  };

  const handleCompress = () => {
    setExpanded(false);
    setSize(prevSize.current);
    setPosition(prevPosition.current);
  };

  // Floating button
  if (!open) {
    return (
      <button className="ai-chat-fab" onClick={() => setOpen(true)} title="Open AI Chat">
        <ForumIcon style={{ fontSize: 32 }} />
      </button>
    );
  }

  // Floating chat window
  return (
    <Rnd
      size={size}
      position={position}
      minWidth={400}
      minHeight={300}
      bounds="window"
      dragHandleClassName="ai-chat-header"
      enableResizing={true}
      onResizeStop={(e, direction, ref, delta, pos) => {
        setSize({ width: ref.offsetWidth, height: ref.offsetHeight });
        setPosition(pos);
      }}
      onDragStop={(e, d) => {
        setPosition({ x: d.x, y: d.y });
      }}
      className={`ai-chat-float-overlay${expanded ? ' expanded' : ''}`}
    >
      <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <div className="ai-chat-header">
          <ForumIcon style={{ marginRight: 8, color: '#1976d2' }} />
          <span style={{ fontWeight: 600 }}>AI Chat</span>
          <div className="ai-chat-float-controls">
            {!expanded ? (
              <button onClick={handleExpand} title="Expand">
                <CropSquareIcon />
              </button>
            ) : (
              <button onClick={handleCompress} title="Restore">
                <FullscreenExitIcon />
              </button>
            )}
            <button onClick={clearChat} title="Clear Chat">
              <DeleteOutlineIcon />
            </button>
            <button onClick={() => setOpen(false)} title="Close">
              <CloseIcon />
            </button>
          </div>
        </div>
        <div className="ai-chat-widget-body">
          <div className="ai-chat-messages">
            {messages.map((message) => (
              <div key={message.id} className={`message ${message.type}`}>
                <div className="message-avatar">
                  {message.type === 'ai' ? (
                    <ForumIcon className="ai-avatar" />
                  ) : (
                    <PersonIcon className="user-avatar" />
                  )}
                </div>
                <div className="message-content">
                  <div className="message-text">
                    {message.type === 'ai' ? (
                      <ReactMarkdown>{message.content}</ReactMarkdown>
                    ) : (
                      message.content.split('\n').map((line, i) => <span key={i}>{line}<br /></span>)
                    )}
                  </div>
                  <div className="message-timestamp">
                    {message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                </div>
              </div>
            ))}
            
            {messages.length === 1 && (
              <div className="quick-actions">
                <h4>Quick Actions:</h4>
                <div className="quick-actions-grid">
                  {quickActions.map((action, index) => (
                    <button
                      key={index}
                      onClick={() => handleQuickAction(action)}
                      className="quick-action-btn"
                    >
                      {action}
                    </button>
                  ))}
                </div>
              </div>
            )}
            
            {isLoading && (
              <div className="message ai">
                <div className="message-avatar">
                  <ForumIcon className="ai-avatar" />
                </div>
                <div className="message-content">
                  <div className="message-text">
                    <CircularProgress size={20} style={{ color: '#1976d2' }} />
                    <span style={{ marginLeft: '10px' }}>AI is thinking...</span>
                  </div>
                </div>
              </div>
            )}
            
            <div ref={messagesEndRef} />
          </div>

          <div className="ai-chat-input">
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder={chatFull ? "You've reached the maximum chat length. Please clear the chat to continue." : "Ask me anything about programming..."}
              disabled={isLoading || chatFull}
              rows={1}
            />
            <button 
              onClick={handleSendMessage} 
              disabled={!inputMessage.trim() || isLoading || chatFull}
              className="send-button"
            >
              <SendIcon />
            </button>
          </div>
          {chatFull && (
            <div className="chat-full-warning">
              You've reached the maximum chat length (30 messages).<br />
              Please clear the chat to continue.
            </div>
          )}
        </div>
      </div>
    </Rnd>
  );
};

export default AIChat; 