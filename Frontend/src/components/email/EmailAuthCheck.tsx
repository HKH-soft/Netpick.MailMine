// EmailAuthCheck.tsx
"use client";
import React, { useState } from 'react';
import emailAuthService, { DomainAuthResult } from '@/services/emailAuthService';

export default function EmailAuthCheck() {
  const [domain, setDomain] = useState('');
  const [result, setResult] = useState<DomainAuthResult | null>(null);
  const [loading, setLoading] = useState(false);

  const handleCheck = async () => {
    if (!domain.trim()) return;
    try {
      setLoading(true);
      const data = await emailAuthService.validateDomain(domain.trim());
      setResult(data);
    } catch (error) {
      console.error('Validation failed:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold text-black dark:text-white mb-4">Email Auth Check (SPF/DKIM/DMARC)</h2>
      <div className="flex gap-4 mb-4">
        <input
          type="text"
          placeholder="Enter domain (e.g., example.com)"
          value={domain}
          onChange={e => setDomain(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleCheck()}
          className="flex-1 border rounded px-3 py-2 dark:bg-form-input dark:border-form-strokedark"
        />
        <button
          onClick={handleCheck}
          disabled={loading}
          className="bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90"
        >
          {loading ? 'Checking...' : 'Check Domain'}
        </button>
      </div>

      {result && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* SPF */}
          <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
            <h3 className="font-semibold mb-2">SPF</h3>
            <div className="space-y-1">
              <div className="flex justify-between">
                <span className="text-sm">Status</span>
                <span className={`font-medium ${result.spf.valid ? 'text-green-500' : 'text-red-500'}`}>
                  {result.spf.valid ? '✓ Record Found' : '✗ No Record'}
                </span>
              </div>
              {result.spf.valid && (
                <>
                  <div className="flex justify-between">
                    <span className="text-sm">Policy</span>
                    <span className="text-sm">{result.spf.allMechanismType}</span>
                  </div>
                  <div className="mt-2 p-2 bg-gray-50 dark:bg-meta-4 rounded text-xs break-all">
                    {result.spf.record}
                  </div>
                </>
              )}
              {result.spf.note && (
                <p className="text-xs text-gray-500">{result.spf.note}</p>
              )}
            </div>
          </div>

          {/* DMARC */}
          <div className="bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
            <h3 className="font-semibold mb-2">DMARC</h3>
            <div className="space-y-1">
              <div className="flex justify-between">
                <span className="text-sm">Status</span>
                <span className={`font-medium ${result.dmarc.valid ? 'text-green-500' : 'text-red-500'}`}>
                  {result.dmarc.valid ? '✓ Record Found' : '✗ No Record'}
                </span>
              </div>
              {result.dmarc.valid && (
                <>
                  <div className="flex justify-between">
                    <span className="text-sm">Policy</span>
                    <span className={`font-medium ${
                      result.dmarc.policy === 'reject' ? 'text-red-500' :
                      result.dmarc.policy === 'quarantine' ? 'text-yellow-500' : 'text-gray-500'
                    }`}>
                      {result.dmarc.policy}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm">Percentage</span>
                    <span className="text-sm">{result.dmarc.pct}%</span>
                  </div>
                  <div className="mt-2 p-2 bg-gray-50 dark:bg-meta-4 rounded text-xs break-all">
                    {result.dmarc.record}
                  </div>
                </>
              )}
              {result.dmarc.note && (
                <p className="text-xs text-gray-500">{result.dmarc.note}</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}



