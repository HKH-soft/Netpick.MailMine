// EmailInbox.tsx
"use client";
import React, { useState } from 'react';
import { useEmailMessages } from '@/hooks/useEmailMessages';
import { EmailTag } from '@/services/emailMessageService';
import AiAssistantPanel from './AiAssistantPanel';

export default function EmailInbox() {
  const { emails, loading, error, refetch } = useEmailMessages();
  const [selectedEmailId, setSelectedEmailId] = useState<string | null>(null);

  if (loading) return <div className="p-4">Loading emails...</div>;
  if (error) return <div className="p-4 text-red-500">{error}</div>;

  return (
    <div className="flex">
      <div className="flex-1 overflow-x-auto">
        <table className="w-full table-auto">
          <thead>
            <tr className="bg-gray-2 dark:bg-meta-4">
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
                className={`border-b border-[#eee] dark:border-strokedark cursor-pointer hover:bg-gray-50 dark:hover:bg-meta-4 ${
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
                    email.isAnswered ? 'bg-green-200' : 'bg-yellow-200'
                  }`}>
                    {email.isAnswered ? 'Replied' : 'Pending'}
                  </span>
                </td>
                <td className="py-3 px-4">
                  <button className="text-primary hover:underline text-sm">
                    {selectedEmailId === email.id ? '✕' : '🤖'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedEmailId && (
        <div className="w-96 flex-shrink-0 border-l border-stroke">
          <AiAssistantPanel
            emailId={selectedEmailId}
            onClose={() => setSelectedEmailId(null)}
          />
        </div>
      )}
    </div>
  );
}