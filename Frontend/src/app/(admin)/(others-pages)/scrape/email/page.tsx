"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import React, { useState } from "react";
import Button from "@/components/ui/button/Button";
import EmailService from "@/services/emailService";
import { useToast } from "@/context/ToastContext";

export default function EmailPage() {
    const { addToast } = useToast();
    const [loading, setLoading] = useState(false);
    const [emailType, setEmailType] = useState<"single" | "mass">("single");

    // Single email form
    const [recipient, setRecipient] = useState("");
    const [subject, setSubject] = useState("");
    const [body, setBody] = useState("");
    const [attachment, setAttachment] = useState("");

    // Mass email form
    const [recipients, setRecipients] = useState("");
    const [templateName, setTemplateName] = useState("");

    const handleSendSingleEmail = async () => {
        if (!recipient || !subject || !body) {
            addToast("error", "Error", "Please fill in all required fields");
            return;
        }

        try {
            setLoading(true);
            await EmailService.sendEmail({
                recipient,
                subject,
                body,
                attachment: attachment || undefined,
            });
            addToast("success", "Success", "Email sent successfully");
            // Reset form
            setRecipient("");
            setSubject("");
            setBody("");
            setAttachment("");
        } catch (err) {
            console.error("Failed to send email:", err);
            addToast("error", "Error", "Failed to send email");
        } finally {
            setLoading(false);
        }
    };

    const handleSendWithAttachment = async () => {
        if (!recipient || !subject || !body || !attachment) {
            addToast("error", "Error", "Please fill in all required fields including attachment");
            return;
        }

        try {
            setLoading(true);
            await EmailService.sendEmailWithAttachment({
                recipient,
                subject,
                body,
                attachment,
            });
            addToast("success", "Success", "Email with attachment sent successfully");
            // Reset form
            setRecipient("");
            setSubject("");
            setBody("");
            setAttachment("");
        } catch (err) {
            console.error("Failed to send email:", err);
            addToast("error", "Error", "Failed to send email with attachment");
        } finally {
            setLoading(false);
        }
    };

    const handleSendMassEmail = async () => {
        if (!recipients || !subject || !body) {
            addToast("error", "Error", "Please fill in all required fields");
            return;
        }

        const recipientList = recipients.split("\n").map(r => r.trim()).filter(r => r);

        if (recipientList.length === 0) {
            addToast("error", "Error", "Please enter at least one recipient");
            return;
        }

        try {
            setLoading(true);
            const result = await EmailService.sendMassEmail({
                recipients: recipientList,
                subject,
                body,
                templateName: templateName || undefined,
            });
            addToast("success", "Success", `Mass email job started for ${result.recipientCount} recipients`);
            // Reset form
            setRecipients("");
            setSubject("");
            setBody("");
            setTemplateName("");
        } catch (err) {
            console.error("Failed to send mass email:", err);
            addToast("error", "Error", "Failed to send mass email");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <PageBreadcrumb pageTitle="Email" />
            <div className="space-y-6">
                {/* Email Type Selector */}
                <ComponentCard title="Email Type" desc="Select single or mass email sending">
                    <div className="flex gap-4">
                        <Button
                            onClick={() => setEmailType("single")}
                            variant={emailType === "single" ? "primary" : "outline"}
                        >
                            Single Email
                        </Button>
                        <Button
                            onClick={() => setEmailType("mass")}
                            variant={emailType === "mass" ? "primary" : "outline"}
                        >
                            Mass Email
                        </Button>
                    </div>
                </ComponentCard>

                {emailType === "single" ? (
                    <ComponentCard title="Send Single Email" desc="Send an email to a single recipient">
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Recipient Email *</label>
                                <input
                                    type="email"
                                    value={recipient}
                                    onChange={(e) => setRecipient(e.target.value)}
                                    className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="recipient@example.com"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Subject *</label>
                                <input
                                    type="text"
                                    value={subject}
                                    onChange={(e) => setSubject(e.target.value)}
                                    className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="Email subject"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Body *</label>
                                <textarea
                                    value={body}
                                    onChange={(e) => setBody(e.target.value)}
                                    className="w-full h-40 p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="Email body content..."
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Attachment (optional)</label>
                                <input
                                    type="text"
                                    value={attachment}
                                    onChange={(e) => setAttachment(e.target.value)}
                                    className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="Path to attachment file"
                                />
                            </div>

                            <div className="flex gap-4">
                                <Button onClick={handleSendSingleEmail} disabled={loading}>
                                    {loading ? "Sending..." : "Send Email"}
                                </Button>
                                <Button
                                    onClick={handleSendWithAttachment}
                                    disabled={loading || !attachment}
                                    variant="outline"
                                >
                                    Send with Attachment
                                </Button>
                            </div>
                        </div>
                    </ComponentCard>
                ) : (
                    <ComponentCard title="Send Mass Email" desc="Send emails to multiple recipients at once">
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Recipients (one per line) *</label>
                                <textarea
                                    value={recipients}
                                    onChange={(e) => setRecipients(e.target.value)}
                                    className="w-full h-32 p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="recipient1@example.com&#10;recipient2@example.com&#10;recipient3@example.com"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Subject *</label>
                                <input
                                    type="text"
                                    value={subject}
                                    onChange={(e) => setSubject(e.target.value)}
                                    className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="Email subject"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Body *</label>
                                <textarea
                                    value={body}
                                    onChange={(e) => setBody(e.target.value)}
                                    className="w-full h-40 p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="Email body content..."
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Template Name (optional)</label>
                                <input
                                    type="text"
                                    value={templateName}
                                    onChange={(e) => setTemplateName(e.target.value)}
                                    className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="Template name"
                                />
                            </div>

                            <Button onClick={handleSendMassEmail} disabled={loading}>
                                {loading ? "Sending..." : "Send Mass Email"}
                            </Button>
                        </div>
                    </ComponentCard>
                )}
            </div>
        </div>
    );
}
