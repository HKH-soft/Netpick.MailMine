// AiAssistantPanel.tsx
"use client";
import React, { useState } from 'react';
import aiService from '@/services/aiService';
import { SentimentResult, SpamResult } from '@/services/aiService';

interface AiAssistantPanelProps {
  emailId: string;
  onClose: () => void;
}

export default function AiAssistantPanel({ emailId, onClose }: AiAssistantPanelProps) {
  const [activeTab, setActiveTab] = useState<'summary' | 'draft' | 'sentiment' | 'spam'>('summary');
  const [summary, setSummary] = useState<string>('');
  const [draft, setDraft] = useState<string>('');
  const [sentiment, setSentiment] = useState<SentimentResult | null>(null);
  const [spamResult, setSpamResult] = useState<SpamResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [draftInstructions, setDraftInstructions] = useState('');

  const loadSummary = async () => {
    setLoading(true);
    try {
      const result = await aiService.summarizeEmail(emailId);
      setSummary(result);
    } catch (e) {
      setSummary('Failed to generate summary');
    } finally {
      setLoading(false);
    }
  };

  const loadDraft = async () => {
    setLoading(true);
    try {
      const result = await aiService.generateDraft(emailId);
      setDraft(result);
    } catch (e) {
      setDraft('Failed to generate draft');
    } finally {
      setLoading(false);
    }
  };

  const improveDraft = async () => {
    if (!draftInstructions.trim()) return;
    setLoading(true);
    try {
      const result = await aiService.improveDraft(draft, draftInstructions);
      setDraft(result);
      setDraftInstructions('');
    } catch (e) {
      console.error('Failed to improve draft:', e);
    } finally {
      setLoading(false);
    }
  };

  const loadSentiment = async () => {
    setLoading(true);
    try {
      const result = await aiService.analyzeSentiment(emailId);
      setSentiment(result);
    } catch (e) {
      setSentiment({ sentiment: 'neutral', confidence: 0, urgency: 'low', emotion: 'unknown' });
    } finally {
      setLoading(false);
    }
  };

  const loadSpam = async () => {
    setLoading(true);
    try {
      const result = await aiService.detectSpam(emailId);
      setSpamResult(result);
    } catch (e) {
      setSpamResult({ risk_level: 'low', is_spam: false, is_phishing: false, is_scam: false, confidence: 0 });
    } finally {
      setLoading(false);
    }
  };

  const tabs = [
    { key: 'summary', label: 'Summary', icon: '📝' },
    { key: 'draft', label: 'Draft Reply', icon: '✍️' },
    { key: 'sentiment', label: 'Sentiment', icon: '😊' },
    { key: 'spam', label: 'Spam Check', icon: '🛡️' },
  ] as const;

  return (
    <div className="bg-white dark:bg-boxdark border-l border-stroke h-full overflow-y-auto">
      <div className="flex justify-between items-center p-4 border-b border-stroke">
        <h3 className="font-semibold text-black dark:text-white">AI Assistant</h3>
        <button onClick={onClose} className="text-gray-500 hover:text-black">✕</button>
      </div>

      <div className="flex border-b border-stroke">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 py-2 text-sm font-medium ${
              activeTab === tab.key
                ? 'text-primary border-b-2 border-primary'
                : 'text-gray-500 hover:text-black'
            }`}
          >
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      <div className="p-4">
        {activeTab === 'summary' && (
          <div>
            <button
              onClick={loadSummary}
              disabled={loading}
              className="w-full bg-primary text-white py-2 rounded hover:bg-opacity-90 mb-3"
            >
              {loading ? 'Generating...' : 'Generate Summary'}
            </button>
            {summary && (
              <div className="bg-gray-50 dark:bg-meta-4 p-3 rounded text-sm whitespace-pre-wrap">
                {summary}
              </div>
            )}
          </div>
        )}

        {activeTab === 'draft' && (
          <div>
            <button
              onClick={loadDraft}
              disabled={loading}
              className="w-full bg-primary text-white py-2 rounded hover:bg-opacity-90 mb-3"
            >
              {loading ? 'Generating...' : 'Generate Draft Reply'}
            </button>
            {draft && (
              <>
                <textarea
                  value={draft}
                  onChange={e => setDraft(e.target.value)}
                  className="w-full border rounded px-3 py-2 mb-2 dark:bg-form-input dark:border-form-strokedark"
                  rows={8}
                />
                <div className="flex gap-2 mb-3">
                  <input
                    type="text"
                    placeholder="Improvement instructions..."
                    value={draftInstructions}
                    onChange={e => setDraftInstructions(e.target.value)}
                    className="flex-1 border rounded px-3 py-2 dark:bg-form-input dark:border-form-strokedark"
                  />
                  <button
                    onClick={improveDraft}
                    disabled={loading || !draftInstructions.trim()}
                    className="bg-blue-500 text-white px-3 py-2 rounded hover:bg-opacity-90"
                  >
                    Improve
                  </button>
                </div>
                <button
                  onClick={() => navigator.clipboard.writeText(draft)}
                  className="w-full bg-gray-500 text-white py-2 rounded hover:bg-opacity-90"
                >
                  Copy to Clipboard
                </button>
              </>
            )}
          </div>
        )}

        {activeTab === 'sentiment' && (
          <div>
            <button
              onClick={loadSentiment}
              disabled={loading}
              className="w-full bg-primary text-white py-2 rounded hover:bg-opacity-90 mb-3"
            >
              {loading ? 'Analyzing...' : 'Analyze Sentiment'}
            </button>
            {sentiment && (
              <div className="space-y-2">
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Sentiment</span>
                  <span className={`font-medium ${
                    sentiment.sentiment === 'positive' ? 'text-green-500' :
                    sentiment.sentiment === 'negative' ? 'text-red-500' :
                    sentiment.sentiment === 'angry' ? 'text-red-700' : 'text-gray-500'
                  }`}>
                    {sentiment.sentiment}
                  </span>
                </div>
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Urgency</span>
                  <span className={`font-medium ${
                    sentiment.urgency === 'critical' ? 'text-red-700' :
                    sentiment.urgency === 'high' ? 'text-red-500' :
                    sentiment.urgency === 'medium' ? 'text-yellow-500' : 'text-gray-500'
                  }`}>
                    {sentiment.urgency}
                  </span>
                </div>
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Emotion</span>
                  <span className="font-medium text-black dark:text-white">{sentiment.emotion}</span>
                </div>
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Confidence</span>
                  <span className="font-medium text-black dark:text-white">
                    {Math.round(sentiment.confidence * 100)}%
                  </span>
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'spam' && (
          <div>
            <button
              onClick={loadSpam}
              disabled={loading}
              className="w-full bg-primary text-white py-2 rounded hover:bg-opacity-90 mb-3"
            >
              {loading ? 'Checking...' : 'Check for Spam/Phishing'}
            </button>
            {spamResult && (
              <div className="space-y-2">
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Risk Level</span>
                  <span className={`font-medium px-2 py-1 rounded ${
                    spamResult.risk_level === 'critical' ? 'bg-red-200 text-red-800' :
                    spamResult.risk_level === 'high' ? 'bg-orange-200 text-orange-800' :
                    spamResult.risk_level === 'medium' ? 'bg-yellow-200 text-yellow-800' :
                    'bg-green-200 text-green-800'
                  }`}>
                    {spamResult.risk_level}
                  </span>
                </div>
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Spam</span>
                  <span className={spamResult.is_spam ? 'text-red-500' : 'text-green-500'}>
                    {spamResult.is_spam ? 'Yes' : 'No'}
                  </span>
                </div>
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Phishing</span>
                  <span className={spamResult.is_phishing ? 'text-red-500' : 'text-green-500'}>
                    {spamResult.is_phishing ? 'Yes' : 'No'}
                  </span>
                </div>
                <div className="flex justify-between items-center p-2 bg-gray-50 dark:bg-meta-4 rounded">
                  <span className="text-sm">Scam</span>
                  <span className={spamResult.is_scam ? 'text-red-500' : 'text-green-500'}>
                    {spamResult.is_scam ? 'Yes' : 'No'}
                  </span>
                </div>
                {spamResult.recommendation && (
                  <div className="p-2 bg-blue-50 dark:bg-blue-900/20 rounded text-sm">
                    {spamResult.recommendation}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
