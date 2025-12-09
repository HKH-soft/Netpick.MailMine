"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import React, { useState } from "react";
import Button from "@/components/ui/button/Button";
import AIService from "@/services/aiService";
import { useToast } from "@/context/ToastContext";

export default function AIPage() {
    const { addToast } = useToast();
    const [loading, setLoading] = useState(false);

    // Generate text form
    const [prompt, setPrompt] = useState("");
    const [generateType, setGenerateType] = useState<"long" | "short">("long");
    const [generatedText, setGeneratedText] = useState("");

    const handleGenerateText = async () => {
        if (!prompt) {
            addToast("error", "Error", "Please enter a prompt");
            return;
        }

        try {
            setLoading(true);
            let result;

            if (generateType === "long") {
                result = await AIService.generateText(prompt);
            } else {
                result = await AIService.generateShortText(prompt);
            }

            setGeneratedText(result.response);
            addToast("success", "Success", "Text generated successfully");
        } catch (err) {
            console.error("Failed to generate text:", err);
            addToast("error", "Error", "Failed to generate text");
        } finally {
            setLoading(false);
        }
    };

    const handleCopyToClipboard = () => {
        navigator.clipboard.writeText(generatedText);
        addToast("success", "Copied", "Text copied to clipboard");
    };

    const handleClear = () => {
        setPrompt("");
        setGeneratedText("");
    };

    return (
        <div>
            <PageBreadcrumb pageTitle="AI Text Generation" />
            <div className="space-y-6">
                {/* Input Section */}
                <ComponentCard title="Generate Text with AI" desc="Use AI to generate text content based on your prompts">
                    <div className="space-y-4">
                        {/* Generation Type Toggle */}
                        <div className="flex gap-4">
                            <Button
                                onClick={() => setGenerateType("long")}
                                variant={generateType === "long" ? "primary" : "outline"}
                            >
                                Long Text
                            </Button>
                            <Button
                                onClick={() => setGenerateType("short")}
                                variant={generateType === "short" ? "primary" : "outline"}
                            >
                                Short Text
                            </Button>
                        </div>

                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            {generateType === "long"
                                ? "Generate detailed, longer form text content."
                                : "Generate concise, brief text responses."}
                        </p>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Prompt *</label>
                            <textarea
                                value={prompt}
                                onChange={(e) => setPrompt(e.target.value)}
                                className="w-full h-40 p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                placeholder="Enter your prompt here..."
                            />
                        </div>

                        <div className="flex gap-4">
                            <Button onClick={handleGenerateText} disabled={loading}>
                                {loading ? (
                                    <span className="flex items-center gap-2">
                                        <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                                        </svg>
                                        Generating...
                                    </span>
                                ) : "Generate Text"}
                            </Button>
                            <Button onClick={handleClear} variant="outline">
                                Clear
                            </Button>
                        </div>
                    </div>
                </ComponentCard>

                {/* Output Section */}
                {generatedText && (
                    <ComponentCard title="Generated Text">
                        <div className="flex justify-end mb-4">
                            <Button onClick={handleCopyToClipboard} variant="primary">
                                Copy to Clipboard
                            </Button>
                        </div>
                        <div className="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
                            <pre className="whitespace-pre-wrap text-sm text-gray-700 dark:text-gray-300">{generatedText}</pre>
                        </div>
                    </ComponentCard>
                )}

                {/* Quick Prompts */}
                <ComponentCard title="Quick Prompts" desc="Click on any prompt to use it">
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                        {[
                            "Write a professional email introduction",
                            "Generate a product description",
                            "Create a company overview",
                            "Write a cold outreach message",
                            "Generate a follow-up email",
                            "Create marketing copy",
                        ].map((quickPrompt) => (
                            <button
                                key={quickPrompt}
                                onClick={() => setPrompt(quickPrompt)}
                                className="p-3 text-left text-sm rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                            >
                                {quickPrompt}
                            </button>
                        ))}
                    </div>
                </ComponentCard>
            </div>
        </div>
    );
}
