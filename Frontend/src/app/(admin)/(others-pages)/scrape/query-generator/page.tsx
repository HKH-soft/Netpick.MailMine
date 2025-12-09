"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import React, { useState } from "react";
import Button from "@/components/ui/button/Button";
import SearchQueryGeneratorService from "@/services/searchQueryGeneratorService";
import { useToast } from "@/context/ToastContext";

export default function SearchQueryGeneratorPage() {
    const { addToast } = useToast();
    const [loading, setLoading] = useState(false);
    const [generatorType, setGeneratorType] = useState<"basic" | "variations" | "site" | "email">("basic");

    // Form inputs
    const [topic, setTopic] = useState("");
    const [target, setTarget] = useState("");
    const [count, setCount] = useState(10);
    const [originalQuery, setOriginalQuery] = useState("");
    const [site, setSite] = useState("");
    const [industry, setIndustry] = useState("");
    const [region, setRegion] = useState("");
    const [saveToDatabase, setSaveToDatabase] = useState(false);

    // Results
    const [generatedQueries, setGeneratedQueries] = useState<string[]>([]);

    const handleGenerateQueries = async () => {
        if (!topic || !target) {
            addToast("error", "Error", "Please enter topic and target");
            return;
        }

        try {
            setLoading(true);

            if (saveToDatabase) {
                const result = await SearchQueryGeneratorService.generateAndSaveQueries({
                    topic,
                    target,
                    count,
                });
                // Map the SearchQuery objects to strings using the 'sentence' property
                setGeneratedQueries(result.queries.map(q => q.sentence));
                addToast("success", "Success", `Generated and saved ${result.saved} queries`);
            } else {
                const result = await SearchQueryGeneratorService.generateQueries({
                    topic,
                    target,
                    count,
                });
                setGeneratedQueries(result.queries);
                addToast("success", "Success", `Generated ${result.count} queries`);
            }
        } catch (err) {
            console.error("Failed to generate queries:", err);
            addToast("error", "Error", "Failed to generate queries");
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateVariations = async () => {
        if (!originalQuery) {
            addToast("error", "Error", "Please enter a base query");
            return;
        }

        try {
            setLoading(true);
            const result = await SearchQueryGeneratorService.generateVariations({
                originalQuery,
                count,
            });
            setGeneratedQueries(result.variations);
            addToast("success", "Success", `Generated ${result.count} variations`);
        } catch (err) {
            console.error("Failed to generate variations:", err);
            addToast("error", "Error", "Failed to generate variations");
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateSiteQueries = async () => {
        if (!topic || !site) {
            addToast("error", "Error", "Please enter topic and site");
            return;
        }

        try {
            setLoading(true);
            const result = await SearchQueryGeneratorService.generateSiteQueries({
                topic,
                site,
                count,
            });
            setGeneratedQueries(result.queries);
            addToast("success", "Success", `Generated ${result.count} site queries`);
        } catch (err) {
            console.error("Failed to generate site queries:", err);
            addToast("error", "Error", "Failed to generate site queries");
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateEmailQueries = async () => {
        if (!industry) {
            addToast("error", "Error", "Please enter an industry");
            return;
        }

        try {
            setLoading(true);
            const result = await SearchQueryGeneratorService.generateEmailQueries({
                industry,
                region: region || undefined,
                count,
            });
            setGeneratedQueries(result.queries);
            addToast("success", "Success", `Generated ${result.count} email queries`);
        } catch (err) {
            console.error("Failed to generate email queries:", err);
            addToast("error", "Error", "Failed to generate email queries");
        } finally {
            setLoading(false);
        }
    };

    const handleCopyAll = () => {
        navigator.clipboard.writeText(generatedQueries.join("\n"));
        addToast("success", "Copied", "All queries copied to clipboard");
    };

    const handleClear = () => {
        setTopic("");
        setTarget("");
        setOriginalQuery("");
        setSite("");
        setIndustry("");
        setRegion("");
        setCount(10);
        setGeneratedQueries([]);
        setSaveToDatabase(false);
    };

    const getFormTitle = () => {
        switch (generatorType) {
            case "basic": return "Generate Basic Queries";
            case "variations": return "Generate Query Variations";
            case "site": return "Generate Site-Specific Queries";
            case "email": return "Generate Email Queries";
        }
    };

    const getFormDescription = () => {
        switch (generatorType) {
            case "basic": return "Generate search queries for a specific topic and target audience.";
            case "variations": return "Generate variations of an existing base query.";
            case "site": return "Generate queries targeted at a specific website.";
            case "email": return "Generate queries optimized for finding email addresses.";
        }
    };

    return (
        <div>
            <PageBreadcrumb pageTitle="Search Query Generator" />
            <div className="space-y-6">
                {/* Generator Type Selector */}
                <ComponentCard title="Query Type" desc="Select the type of search queries to generate">
                    <div className="flex flex-wrap gap-2">
                        <Button
                            onClick={() => setGeneratorType("basic")}
                            variant={generatorType === "basic" ? "primary" : "outline"}
                        >
                            Basic Queries
                        </Button>
                        <Button
                            onClick={() => setGeneratorType("variations")}
                            variant={generatorType === "variations" ? "primary" : "outline"}
                        >
                            Query Variations
                        </Button>
                        <Button
                            onClick={() => setGeneratorType("site")}
                            variant={generatorType === "site" ? "primary" : "outline"}
                        >
                            Site-Specific
                        </Button>
                        <Button
                            onClick={() => setGeneratorType("email")}
                            variant={generatorType === "email" ? "primary" : "outline"}
                        >
                            Email Queries
                        </Button>
                    </div>
                </ComponentCard>

                {/* Form Section */}
                <ComponentCard title={getFormTitle()} desc={getFormDescription()}>
                    <div className="space-y-4">
                        {/* Basic Queries Form */}
                        {generatorType === "basic" && (
                            <>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Topic *</label>
                                    <input
                                        type="text"
                                        value={topic}
                                        onChange={(e) => setTopic(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                        placeholder="e.g., Software Development, Marketing, Healthcare"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Target *</label>
                                    <input
                                        type="text"
                                        value={target}
                                        onChange={(e) => setTarget(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                        placeholder="e.g., CEO, CTO, Marketing Manager"
                                    />
                                </div>
                            </>
                        )}

                        {/* Variations Form */}
                        {generatorType === "variations" && (
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Original Query *</label>
                                <input
                                    type="text"
                                    value={originalQuery}
                                    onChange={(e) => setOriginalQuery(e.target.value)}
                                    className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                    placeholder="e.g., software developer contact email"
                                />
                            </div>
                        )}

                        {/* Site-Specific Form */}
                        {generatorType === "site" && (
                            <>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Topic *</label>
                                    <input
                                        type="text"
                                        value={topic}
                                        onChange={(e) => setTopic(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                        placeholder="e.g., Contact Information, About Page"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Site *</label>
                                    <input
                                        type="text"
                                        value={site}
                                        onChange={(e) => setSite(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                        placeholder="e.g., linkedin.com, github.com"
                                    />
                                </div>
                            </>
                        )}

                        {/* Email Queries Form */}
                        {generatorType === "email" && (
                            <>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Industry *</label>
                                    <input
                                        type="text"
                                        value={industry}
                                        onChange={(e) => setIndustry(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                        placeholder="e.g., Technology, Healthcare, Finance"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Region (optional)</label>
                                    <input
                                        type="text"
                                        value={region}
                                        onChange={(e) => setRegion(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                                        placeholder="e.g., USA, Europe, Asia"
                                    />
                                </div>
                            </>
                        )}

                        {/* Count Input */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Number of Queries</label>
                            <input
                                type="number"
                                value={count}
                                onChange={(e) => setCount(parseInt(e.target.value) || 10)}
                                min={1}
                                max={50}
                                className="w-full p-3 border border-gray-300 rounded-lg dark:bg-gray-800 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                            />
                        </div>

                        {/* Save to Database Checkbox (for basic queries) */}
                        {generatorType === "basic" && (
                            <div className="flex items-center gap-2">
                                <input
                                    type="checkbox"
                                    id="saveToDatabase"
                                    checked={saveToDatabase}
                                    onChange={(e) => setSaveToDatabase(e.target.checked)}
                                    className="rounded border-gray-300 dark:border-gray-600"
                                />
                                <label htmlFor="saveToDatabase" className="text-sm text-gray-700 dark:text-gray-300">
                                    Save generated queries to database
                                </label>
                            </div>
                        )}

                        {/* Action Buttons */}
                        <div className="flex gap-4">
                            <Button
                                onClick={
                                    generatorType === "basic" ? handleGenerateQueries :
                                        generatorType === "variations" ? handleGenerateVariations :
                                            generatorType === "site" ? handleGenerateSiteQueries :
                                                handleGenerateEmailQueries
                                }
                                disabled={loading}
                            >
                                {loading ? (
                                    <span className="flex items-center gap-2">
                                        <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                                        </svg>
                                        Generating...
                                    </span>
                                ) : "Generate Queries"}
                            </Button>
                            <Button onClick={handleClear} variant="outline">
                                Clear
                            </Button>
                        </div>
                    </div>
                </ComponentCard>

                {/* Results Section */}
                {generatedQueries.length > 0 && (
                    <ComponentCard title={`Generated Queries (${generatedQueries.length})`}>
                        <div className="flex justify-end mb-4">
                            <Button onClick={handleCopyAll} variant="primary">
                                Copy All
                            </Button>
                        </div>

                        <div className="space-y-2 max-h-96 overflow-y-auto">
                            {generatedQueries.map((query, index) => (
                                <div
                                    key={`${query}-${index}`}
                                    className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 flex justify-between items-center"
                                >
                                    <span className="text-sm text-gray-700 dark:text-gray-300">{query}</span>
                                    <button
                                        onClick={() => {
                                            navigator.clipboard.writeText(query);
                                            addToast("success", "Copied", "Query copied to clipboard");
                                        }}
                                        className="text-brand-500 hover:text-brand-600 text-sm font-medium"
                                    >
                                        Copy
                                    </button>
                                </div>
                            ))}
                        </div>
                    </ComponentCard>
                )}
            </div>
        </div>
    );
}
