// AnalyticsDashboard.tsx
"use client";
import React, { useState, useEffect } from 'react';
import analyticsService, { DashboardSummary } from '@/services/analyticsService';

export default function AnalyticsDashboard() {
  const [data, setData] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const dashboard = await analyticsService.getDashboard();
      setData(dashboard);
    } catch (error) {
      console.error('Failed to load analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="p-4">Loading analytics...</div>;
  if (!data) return <div className="p-4 text-red-500">Failed to load analytics</div>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold text-black dark:text-white mb-4">Mail Analytics</h2>

      {/* Today's Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
          <p className="text-sm text-gray-500">Emails Today</p>
          <p className="text-2xl font-bold text-black dark:text-white">{data.today.emailsReceived}</p>
        </div>
        <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
          <p className="text-sm text-gray-500">Replied</p>
          <p className="text-2xl font-bold text-green-500">{data.today.emailsReplied}</p>
        </div>
        <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
          <p className="text-sm text-gray-500">Avg Response Time</p>
          <p className="text-2xl font-bold text-blue-500">{data.today.averageResponseTimeHours}h</p>
        </div>
        <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
          <p className="text-sm text-gray-500">Unanswered</p>
          <p className="text-2xl font-bold text-red-500">{data.unansweredCount}</p>
        </div>
      </div>

      {/* Weekly Summary */}
      <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke mb-6">
        <h3 className="font-semibold mb-3">This Week</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <p className="text-sm text-gray-500">Total Received</p>
            <p className="text-lg font-bold text-black dark:text-white">{data.thisWeek.totalReceived}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Total Replied</p>
            <p className="text-lg font-bold text-green-500">{data.thisWeek.totalReplied}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Response Rate</p>
            <p className="text-lg font-bold text-blue-500">
              {data.thisWeek.totalReceived > 0
                ? Math.round((data.thisWeek.totalReplied / data.thisWeek.totalReceived) * 100)
                : 0}%
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Unanswered</p>
            <p className="text-lg font-bold text-red-500">{data.thisWeek.unansweredCount}</p>
          </div>
        </div>
      </div>

      {/* Response Time Metrics */}
      <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke mb-6">
        <h3 className="font-semibold mb-3">Response Time</h3>
        <div className="grid grid-cols-3 gap-4">
          <div>
            <p className="text-sm text-gray-500">Average</p>
            <p className="text-lg font-bold text-black dark:text-white">{data.responseTime.averageHours}h</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Median</p>
            <p className="text-lg font-bold text-black dark:text-white">{data.responseTime.medianHours}h</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">95th Percentile</p>
            <p className="text-lg font-bold text-black dark:text-white">{data.responseTime.p95Hours}h</p>
          </div>
        </div>
      </div>

      {/* Top Senders */}
      <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
        <h3 className="font-semibold mb-3">Top Senders (7 days)</h3>
        <div className="space-y-2">
          {data.topSenders.map((sender, index) => (
            <div key={sender.email} className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-500">{index + 1}.</span>
                <span className="text-black dark:text-white">{sender.email}</span>
              </div>
              <span className="bg-primary/10 text-primary px-2 py-1 rounded text-sm">{sender.count}</span>
            </div>
          ))}
          {data.topSenders.length === 0 && (
            <p className="text-gray-500 text-sm">No data available</p>
          )}
        </div>
      </div>
    </div>
  );
}
