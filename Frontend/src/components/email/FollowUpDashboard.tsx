// FollowUpDashboard.tsx
"use client";
import React, { useState, useEffect } from 'react';
import followUpService, { FollowUpItem } from '@/services/followUpService';

export default function FollowUpDashboard() {
  const [followUps, setFollowUps] = useState<FollowUpItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [streaming, setStreaming] = useState(false);

  useEffect(() => {
    loadFollowUps();
  }, []);

  const loadFollowUps = async () => {
    try {
      setLoading(true);
      const data = await followUpService.getDashboard();
      setFollowUps(data);
    } catch (error) {
      console.error('Failed to load follow-ups:', error);
    } finally {
      setLoading(false);
    }
  };

  const startStreaming = () => {
    const eventSource = followUpService.createEventSource();
    setStreaming(true);

    eventSource.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data);
        setFollowUps(prev => [notification, ...prev]);
      } catch (error) {
        console.error('Failed to parse SSE event:', error);
      }
    };

    eventSource.onerror = () => {
      setStreaming(false);
      eventSource.close();
    };
  };

  const triggerDetection = async () => {
    try {
      await followUpService.triggerDetection();
      loadFollowUps();
    } catch (error) {
      console.error('Failed to trigger detection:', error);
    }
  };

  if (loading) return <div className="p-4">Loading follow-ups...</div>;

  const urgent = followUps.filter(f => f.priority === 'URGENT');
  const normal = followUps.filter(f => f.priority === 'NORMAL');

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold text-black dark:text-white">
          Follow-up Dashboard
          {streaming && <span className="ml-2 text-sm text-green-500">● Live</span>}
        </h2>
        <div className="flex gap-2">
          <button
            onClick={triggerDetection}
            className="bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90"
          >
            Refresh
          </button>
          <button
            onClick={startStreaming}
            disabled={streaming}
            className={`px-4 py-2 rounded ${
              streaming
                ? 'bg-gray-300 text-gray-500'
                : 'bg-green-500 text-white hover:bg-opacity-90'
            }`}
          >
            {streaming ? 'Streaming...' : 'Start Live Updates'}
          </button>
        </div>
      </div>

      {/* Urgent */}
      {urgent.length > 0 && (
        <div className="mb-6">
          <h3 className="font-semibold text-red-500 mb-2">🔴 Urgent ({urgent.length})</h3>
          <div className="space-y-2">
            {urgent.map((item) => (
              <div key={item.emailId} className="bg-red-50 dark:bg-red-900/20 p-3 rounded border border-red-200">
                <div className="flex justify-between items-start">
                  <div>
                    <p className="font-medium text-black dark:text-white">{item.subject}</p>
                    <p className="text-sm text-gray-500">From: {item.sender}</p>
                  </div>
                  <span className="bg-red-500 text-white px-2 py-1 rounded text-xs">
                    {item.hoursSinceReceived}h
                  </span>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                  Assigned to: {item.assignedTo || 'Unassigned'}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Normal */}
      {normal.length > 0 && (
        <div className="mb-6">
          <h3 className="font-semibold text-yellow-500 mb-2">🟡 Needs Attention ({normal.length})</h3>
          <div className="space-y-2">
            {normal.map((item) => (
              <div key={item.emailId} className="bg-yellow-50 dark:bg-yellow-900/20 p-3 rounded border border-yellow-200">
                <div className="flex justify-between items-start">
                  <div>
                    <p className="font-medium text-black dark:text-white">{item.subject}</p>
                    <p className="text-sm text-gray-500">From: {item.sender}</p>
                  </div>
                  <span className="bg-yellow-500 text-white px-2 py-1 rounded text-xs">
                    {item.hoursSinceReceived}h
                  </span>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                  Assigned to: {item.assignedTo || 'Unassigned'}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {followUps.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          No follow-ups needed. All emails are being handled. ✅
        </div>
      )}
    </div>
  );
}



