// SharedInboxView.tsx
"use client";
import React, { useState, useEffect } from 'react';
import sharedInboxService, { SharedInbox } from '@/services/sharedInboxService';

export default function SharedInboxView() {
  const [inboxes, setInboxes] = useState<SharedInbox[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [selectedInbox, setSelectedInbox] = useState<SharedInbox | null>(null);
  const [formData, setFormData] = useState({ name: '', emailAddress: '', description: '' });
  const [assignEmailId, setAssignEmailId] = useState('');
  const [assignUserId, setAssignUserId] = useState('');

  useEffect(() => {
    loadInboxes();
  }, []);

  const loadInboxes = async () => {
    try {
      setLoading(true);
      const data = await sharedInboxService.listInboxes();
      setInboxes(data.context || []);
    } catch (error) {
      console.error('Failed to load inboxes:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    try {
      await sharedInboxService.createInbox(formData);
      setShowCreate(false);
      setFormData({ name: '', emailAddress: '', description: '' });
      loadInboxes();
    } catch (error) {
      console.error('Failed to create inbox:', error);
    }
  };

  const handleAssign = async () => {
    if (!selectedInbox || !assignEmailId || !assignUserId) return;
    try {
      await sharedInboxService.assignEmail(selectedInbox.id, assignEmailId, assignUserId);
      setAssignEmailId('');
      setAssignUserId('');
    } catch (error) {
      console.error('Failed to assign email:', error);
    }
  };

  if (loading) return <div className="p-4">Loading shared inboxes...</div>;

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold text-black dark:text-white">Shared Inboxes</h2>
        <button
          onClick={() => setShowCreate(!showCreate)}
          className="bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90"
        >
          {showCreate ? 'Cancel' : '+ New Inbox'}
        </button>
      </div>

      {showCreate && (
        <div className="bg-white dark:bg-boxdark p-4 rounded-lg mb-4 border border-stroke">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <input
              type="text"
              placeholder="Inbox Name"
              value={formData.name}
              onChange={e => setFormData({ ...formData, name: e.target.value })}
              className="border rounded px-3 py-2 dark:bg-form-input dark:border-form-strokedark"
            />
            <input
              type="email"
              placeholder="Email Address"
              value={formData.emailAddress}
              onChange={e => setFormData({ ...formData, emailAddress: e.target.value })}
              className="border rounded px-3 py-2 dark:bg-form-input dark:border-form-strokedark"
            />
            <input
              type="text"
              placeholder="Description (optional)"
              value={formData.description}
              onChange={e => setFormData({ ...formData, description: e.target.value })}
              className="border rounded px-3 py-2 dark:bg-form-input dark:border-form-strokedark"
            />
          </div>
          <button
            onClick={handleCreate}
            className="mt-3 bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90"
          >
            Create Inbox
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {inboxes.map((inbox) => (
          <div
            key={inbox.id}
            className={`bg-white dark:bg-boxdark p-4 rounded-lg border cursor-pointer transition-all ${
              selectedInbox?.id === inbox.id ? 'border-primary' : 'border-stroke hover:border-gray-300'
            }`}
            onClick={() => setSelectedInbox(inbox)}
          >
            <div className="flex justify-between items-start mb-2">
              <h3 className="font-semibold text-black dark:text-white">{inbox.name}</h3>
              <span className={`text-xs px-2 py-1 rounded ${
                inbox.isActive ? 'bg-green-200 text-green-800' : 'bg-gray-200 text-gray-800'
              }`}>
                {inbox.isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
            <p className="text-sm text-gray-500 mb-2">{inbox.emailAddress}</p>
            {inbox.description && (
              <p className="text-sm text-gray-600 mb-2">{inbox.description}</p>
            )}
            <div className="text-xs text-gray-500">
              {inbox.members?.length || 0} member(s)
            </div>
          </div>
        ))}
      </div>

      {selectedInbox && (
        <div className="mt-6 bg-white dark:bg-boxdark p-4 rounded-lg border border-stroke">
          <h3 className="font-semibold mb-3">Assign Email - {selectedInbox.name}</h3>
          <div className="flex gap-4">
            <input
              type="text"
              placeholder="Email ID"
              value={assignEmailId}
              onChange={e => setAssignEmailId(e.target.value)}
              className="flex-1 border rounded px-3 py-2 dark:bg-form-input dark:border-form-strokedark"
            />
            <input
              type="text"
              placeholder="User ID"
              value={assignUserId}
              onChange={e => setAssignUserId(e.target.value)}
              className="flex-1 border rounded px-3 py-2 dark:bg-form-input dark:border-form-strokedark"
            />
            <button
              onClick={handleAssign}
              className="bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90"
            >
              Assign
            </button>
          </div>
          <div className="mt-3">
            <h4 className="text-sm font-medium mb-2">Members</h4>
            <div className="flex flex-wrap gap-2">
              {selectedInbox.members?.map((member) => (
                <span key={member.id} className="bg-gray-100 dark:bg-meta-4 px-3 py-1 rounded-full text-sm">
                  {member.name || member.email}
                </span>
              ))}
            </div>
          </div>
        </div>
      )}

      {inboxes.length === 0 && !showCreate && (
        <div className="text-center py-8 text-gray-500">
          No shared inboxes. Create one to get started.
        </div>
      )}
    </div>
  );
}
