"use client";

import React, { useState } from "react";

interface CodeBlockProps {
  code: string;
  language?: string;
  filename?: string;
}

export default function CodeBlock({ code, filename }: CodeBlockProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="glass-card overflow-hidden">
      <div className="flex items-center justify-between px-4 py-3 border-b border-[var(--color-glass-border)]">
        <div className="flex items-center gap-2">
          <div className="flex gap-1.5">
            <span className="w-3 h-3 rounded-full bg-[#ff5f57]" />
            <span className="w-3 h-3 rounded-full bg-[#febc2e]" />
            <span className="w-3 h-3 rounded-full bg-[#28c840]" />
          </div>
          {filename && (
            <span className="text-xs text-[var(--color-text-muted)] ml-2 font-mono">{filename}</span>
          )}
        </div>
        <button
          onClick={handleCopy}
          className="text-xs text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors px-3 py-1 rounded-lg hover:bg-[var(--color-glass-bg)] border border-transparent hover:border-[var(--color-glass-border)]"
        >
          {copied ? "Copied!" : "Copy"}
        </button>
      </div>
      <pre className="p-4 overflow-x-auto text-sm leading-relaxed bg-gradient-to-br from-[var(--color-glass-bg)] to-transparent">
        <code className="text-[var(--color-text-secondary)] font-[var(--font-mono)]">{code}</code>
      </pre>
    </div>
  );
}
