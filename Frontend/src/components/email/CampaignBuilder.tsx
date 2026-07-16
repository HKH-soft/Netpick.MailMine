// CampaignBuilder.tsx
"use client";
import React, { useState, useEffect } from 'react';
import campaignService, { Campaign } from '@/services/campaignService';

export default function CampaignBuilder() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    subjectLine: '',
    bodyHtml: '',
  });
  const [recipients, setRecipients] = useState('');
  const [selectedCampaign, setSelectedCampaign] = useState<string | null>(null);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    loadCampaigns();
  }, []);

  const loadCampaigns = async () => {
    try {
      setLoading(true);
      const data = await campaignService.listCampaigns();
      setCampaigns(data.content || []);
    } catch (error) {
      console.error('Failed to load campaigns:', error);
    } finally {
      setLoading(false);
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};
    if (!formData.name.trim()) errors.name = 'Campaign name is required';
    if (!formData.subjectLine.trim()) errors.subjectLine = 'Subject line is required';
    if (!formData.bodyHtml.trim()) errors.bodyHtml = 'Email body is required';
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleCreate = async () => {
    if (!validateForm()) return;
    try {
      await campaignService.createCampaign(formData);
      setShowCreate(false);
      setFormData({ name: '', description: '', subjectLine: '', bodyHtml: '' });
      setFormErrors({});
      loadCampaigns();
    } catch (error) {
      console.error('Failed to create campaign:', error);
    }
  };

  const handleAddRecipients = async () => {
    if (!selectedCampaign || !recipients.trim()) return;
    try {
      const emailList = recipients.split('\n').map(e => e.trim()).filter(e => e);
      await campaignService.addRecipients(selectedCampaign, emailList);
      setRecipients('');
      loadCampaigns();
    } catch (error) {
      console.error('Failed to add recipients:', error);
    }
  };

  const handleSendNow = async (id: string) => {
    if (!confirm('Send this campaign now?')) return;
    try {
      await campaignService.sendNow(id);
      loadCampaigns();
    } catch (error) {
      console.error('Failed to send campaign:', error);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this campaign?')) return;
    try {
      await campaignService.deleteCampaign(id);
      loadCampaigns();
    } catch (error) {
      console.error('Failed to delete campaign:', error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'bg-gray-200 text-gray-800';
      case 'SCHEDULED': return 'bg-blue-200 text-blue-800';
      case 'SENDING': return 'bg-yellow-200 text-yellow-800';
      case 'SENT': return 'bg-green-200 text-green-800';
      case 'FAILED': return 'bg-red-200 text-red-800';
      default: return 'bg-gray-200 text-gray-800';
    }
  };

  if (loading) return <div className="p-4">Loading campaigns...</div>;

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold text-black dark:text-white">Campaign Builder</h2>
        <button
          onClick={() => setShowCreate(!showCreate)}
          className="bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90"
        >
          {showCreate ? 'Cancel' : '+ New Campaign'}
        </button>
      </div>

      {showCreate && (
        <div className="bg-white dark:bg-boxdark p-4 rounded-lg mb-4 border border-stroke">
          <h3 className="font-semibold mb-3">Create Campaign</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <input
                type="text"
                placeholder="Campaign Name"
                value={formData.name}
                onChange={e => setFormData({ ...formData, name: e.target.value })}
                className={`border rounded px-3 py-2 w-full dark:bg-form-input dark:border-form-strokedark ${formErrors.name ? 'border-red-500' : ''}`}
              />
              {formErrors.name && <p className="text-red-500 text-xs mt-1">{formErrors.name}</p>}
            </div>
            <div>
              <input
                type="text"
                placeholder="Subject Line"
                value={formData.subjectLine}
                onChange={e => setFormData({ ...formData, subjectLine: e.target.value })}
                className={`border rounded px-3 py-2 w-full dark:bg-form-input dark:border-form-strokedark ${formErrors.subjectLine ? 'border-red-500' : ''}`}
              />
              {formErrors.subjectLine && <p className="text-red-500 text-xs mt-1">{formErrors.subjectLine}</p>}
            </div>
            <textarea
              placeholder="Description"
              value={formData.description}
              onChange={e => setFormData({ ...formData, description: e.target.value })}
              className="border rounded px-3 py-2 md:col-span-2 dark:bg-form-input dark:border-form-strokedark"
              rows={2}
            />
            <div className="md:col-span-2">
              <textarea
                placeholder="Email HTML Body"
                value={formData.bodyHtml}
                onChange={e => setFormData({ ...formData, bodyHtml: e.target.value })}
                className={`border rounded px-3 py-2 w-full dark:bg-form-input dark:border-form-strokedark ${formErrors.bodyHtml ? 'border-red-500' : ''}`}
                rows={6}
              />
              {formErrors.bodyHtml && <p className="text-red-500 text-xs mt-1">{formErrors.bodyHtml}</p>}
            </div>
          </div>
          <button
            onClick={handleCreate}
            disabled={!formData.name.trim() || !formData.subjectLine.trim() || !formData.bodyHtml.trim()}
            className={`mt-3 px-4 py-2 rounded ${
              !formData.name.trim() || !formData.subjectLine.trim() || !formData.bodyHtml.trim()
                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                : 'bg-primary text-white hover:bg-opacity-90'
            }`}
          >
            Create Campaign
          </button>
        </div>
      )}

      {campaigns.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 px-4">
          <div className="text-5xl mb-4">📢</div>
          <h3 className="text-lg font-semibold text-black dark:text-white mb-2">No campaigns yet</h3>
          <p className="text-gray-500 text-center max-w-md mb-4">
            You haven't created any campaigns yet. Click the button above to create your first campaign.
          </p>
        </div>
      ) : (
      <div className="overflow-x-auto">
        <table className="w-full table-auto">
          <thead>
            <tr className="bg-gray-2 dark:bg-meta-4">
              <th className="py-3 px-4 text-left font-medium text-black dark:text-white">Name</th>
              <th className="py-3 px-4 text-left font-medium text-black dark:text-white">Status</th>
              <th className="py-3 px-4 text-left font-medium text-black dark:text-white">Recipients</th>
              <th className="py-3 px-4 text-left font-medium text-black dark:text-white">Sent</th>
              <th className="py-3 px-4 text-left font-medium text-black dark:text-white">Opened</th>
              <th className="py-3 px-4 text-left font-medium text-black dark:text-white">Actions</th>
            </tr>
          </thead>
          <tbody>
            {campaigns.map((campaign) => (
              <tr key={campaign.id} className="border-b border-[#eee] dark:border-strokedark">
                <td className="py-3 px-4">
                  <p className="font-medium text-black dark:text-white">{campaign.name}</p>
                  <p className="text-sm text-gray-500">{campaign.subjectLine}</p>
                </td>
                <td className="py-3 px-4">
                  <span className={`inline-block rounded px-2 py-1 text-xs ${getStatusColor(campaign.status)}`}>
                    {campaign.status}
                  </span>
                </td>
                <td className="py-3 px-4 text-black dark:text-white">{campaign.totalRecipients}</td>
                <td className="py-3 px-4 text-black dark:text-white">{campaign.totalSent}</td>
                <td className="py-3 px-4 text-black dark:text-white">{campaign.totalOpened}</td>
                <td className="py-3 px-4">
                  <div className="flex gap-2">
                    {campaign.status === 'DRAFT' && (
                      <>
                        <button
                          onClick={() => setSelectedCampaign(campaign.id)}
                          className="text-xs bg-blue-500 text-white px-2 py-1 rounded"
                        >
                          Add Recipients
                        </button>
                        <button
                          onClick={() => handleSendNow(campaign.id)}
                          className="text-xs bg-green-500 text-white px-2 py-1 rounded"
                        >
                          Send Now
                        </button>
                        <button
                          onClick={() => handleDelete(campaign.id)}
                          className="text-xs bg-red-500 text-white px-2 py-1 rounded"
                        >
                          Delete
                        </button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      )}

      {selectedCampaign && (
        <div className="mt-4 p-4 bg-white dark:bg-boxdark rounded-lg border border-stroke">
          <h3 className="font-semibold mb-2">Add Recipients</h3>
          <textarea
            placeholder="Enter email addresses (one per line)"
            value={recipients}
            onChange={e => setRecipients(e.target.value)}
            className="w-full border rounded px-3 py-2 mb-2 dark:bg-form-input dark:border-form-strokedark"
            rows={4}
          />
          <div className="flex gap-2">
            <button
              onClick={handleAddRecipients}
              className="bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90"
            >
              Add Recipients
            </button>
            <button
              onClick={() => setSelectedCampaign(null)}
              className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-opacity-90"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
}



