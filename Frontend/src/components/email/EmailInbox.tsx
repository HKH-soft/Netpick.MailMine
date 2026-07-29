// EmailInbox.tsx
"use client";
import React, { useState } from 'react';
import { useEmailMessages } from '@/hooks/useEmailMessages';
import { EmailTag } from '@/services/emailMessageService';
import AiAssistantPanel from './AiAssistantPanel';

export default function EmailInbox() {
  const { emails, loading, error } = useEmailMessages();
  const [selectedEmailId, setSelectedEmailId] = useState<string | null>(null);

  if (loading) return <div className="p-4">Loading emails...</div>;
  if (error) return <div className="p-4 text-red-500">{error}</div>;

  if (emails.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 px-4">
        <div className="text-5xl mb-4">📭</div>
        <h3 className="text-lg font-semibold text-black dark:text-white mb-2">No emails yet</h3>
        <p className="text-gray-500 text-center max-w-md">
          Your inbox is empty. Emails will appear here once they arrive. Check back later.
        </p>
      </div>
    );
  }

  return (
    <div className="flex">
      <div className="flex-1 overflow-x-auto">
        <table className="w-full table-auto">
          <thead>
            <tr className="bg-gray-200 dark:bg-gray-500">
              <th className="min-w-[220px] py-3 px-4 text-left font-medium text-black dark:text-white">
                Sender
              </th>
              <th className="min-w-[220px] py-3 px-4 text-left font-medium text-black dark:text-white">
                Subject
              </th>
              <th className="min-w-[150px] py-3 px-4 text-left font-medium text-black dark:text-white">
                Received
              </th>
              <th className="min-w-[120px] py-3 px-4 text-left font-medium text-black dark:text-white">
                Tags
              </th>
              <th className="min-w-[120px] py-3 px-4 text-left font-medium text-black dark:text-white">
                Status
              </th>
              <th className="min-w-[80px] py-3 px-4 text-left font-medium text-black dark:text-white">
                AI
              </th>
            </tr>
          </thead>
          <tbody>
            {emails.map((email) => (
              <tr
                key={email.id}
                className={`border-b border-[#eee] dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-white/5 ${
                  selectedEmailId === email.id ? 'bg-primary/5 dark:bg-primary/10' : ''
                }`}
                onClick={() => setSelectedEmailId(selectedEmailId === email.id ? null : email.id)}
              >
                <td className="py-3 px-4">
                  <p className="text-black dark:text-white">{email.senderName || email.senderEmail}</p>
                  <p className="text-sm text-gray-500">{email.senderEmail}</p>
                </td>
                <td className="py-3 px-4">
                  <p className="text-black dark:text-white">{email.subject || '(no subject)'}</p>
                </td>
                <td className="py-3 px-4">
                  <p className="text-black dark:text-white">
                    {new Date(email.receivedAt).toLocaleDateString()}
                  </p>
                </td>
                <td className="py-3 px-4">
                  <div className="flex flex-wrap gap-1">
                    {email.tags?.map((tag: EmailTag) => (
                      <span
                        key={tag.id}
                        className="inline-block rounded bg-primary px-2 py-1 text-xs text-white"
                        style={{ backgroundColor: tag.colorHex || '#3b82f6' }}
                      >
                        {tag.name}
                      </span>
                    ))}
                  </div>
                </td>
                <td className="py-3 px-4">
                  <span className={`inline-block rounded px-2 py-1 text-xs ${
                    email.isAnswered ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400' : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400'
                  }`}>
                    {email.isAnswered ? 'Replied' : 'Pending'}
                  </span>
                </td>
                <td className="py-3 px-4">
                  <button
                    onClick={(e) => { e.stopPropagation(); setSelectedEmailId(selectedEmailId === email.id ? null : email.id); }}
                    className="text-primary hover:underline text-sm"
                  >
                    {selectedEmailId === email.id ? '✕' : '🤖'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedEmailId && (
        <div className="w-96 flex-shrink-0 border-l border-gray-200 dark:border-gray-700">
          <AiAssistantPanel
            emailId={selectedEmailId}
            onClose={() => setSelectedEmailId(null)}
          />
        </div>
      )}
    </div>
  );
}


