"use client";

import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import React, { useState, useEffect } from "react";
import { useScrapeControls } from "@/hooks/useScrape";
import Button from "@/components/ui/button/Button";
import { useToast } from "@/context/ToastContext";
import { PipelineStageEnum } from "@/services/scrapeService";

export default function ScrapeControlPage() {
    const { addToast } = useToast();
    const {
        loading,
        status,
        startGoogleSearch,
        startScraping,
        startExtract,
        executeAll,
        executeSteps,
        pausePipeline,
        resumePipeline,
        skipCurrentStep,
        cancelPipeline,
        fetchStatus,
    } = useScrapeControls();

    const [selectedSteps, setSelectedSteps] = useState<PipelineStageEnum[]>([]);

    useEffect(() => {
        fetchStatus();
        // Poll for status every 5 seconds
        const interval = setInterval(fetchStatus, 5000);
        return () => clearInterval(interval);
    }, [fetchStatus]);

    const handleStartGoogleSearch = async () => {
        try {
            await startGoogleSearch();
            addToast("success", "Success", "Google search started");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to start Google search");
        }
    };

    const handleStartScraping = async () => {
        try {
            await startScraping();
            addToast("success", "Success", "Scraping started");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to start scraping");
        }
    };

    const handleStartExtract = async () => {
        try {
            await startExtract();
            addToast("success", "Success", "Extraction started");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to start extraction");
        }
    };

    const handleExecuteAll = async () => {
        try {
            await executeAll();
            addToast("success", "Success", "Full pipeline started");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to start full pipeline");
        }
    };

    const handlePause = async () => {
        try {
            await pausePipeline();
            addToast("success", "Success", "Pipeline paused");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to pause pipeline");
        }
    };

    const handleResume = async () => {
        try {
            await resumePipeline();
            addToast("success", "Success", "Pipeline resumed");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to resume pipeline");
        }
    };

    const handleSkip = async () => {
        try {
            await skipCurrentStep();
            addToast("success", "Success", "Current step skipped");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to skip current step");
        }
    };

    const handleCancel = async () => {
        try {
            await cancelPipeline();
            addToast("success", "Success", "Pipeline cancelled");
            fetchStatus();
        } catch {
            addToast("error", "Error", "Failed to cancel pipeline");
        }
    };

    const pipelineSteps: { id: PipelineStageEnum; label: string }[] = [
        { id: "API_CALLER_STARTED", label: "Google Search" },
        { id: "SCRAPER_STARTED", label: "Web Scraping" },
        { id: "PARSER_STARTED", label: "Data Extraction" },
    ];

    return (
        <div>
            <PageBreadcrumb pageTitle="Scrape Control" />
            <div className="space-y-6">
                {/* Status Card */}
                <ComponentCard title="Pipeline Status">
                    <div className="flex items-center gap-4">
                        <div className={`w-4 h-4 rounded-full ${status?.active ? 'bg-green-500 animate-pulse' : 'bg-gray-400 dark:bg-gray-600'}`}></div>
                        <span className="text-base text-gray-800 dark:text-white/90">
                            {status?.active ? 'Pipeline Running' : 'No Active Pipeline'}
                        </span>
                    </div>
                    {status?.message && (
                        <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">{status.message}</p>
                    )}
                </ComponentCard>

                <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                    {/* Quick Actions */}
                    <ComponentCard title="Quick Actions" desc="Start individual pipeline steps or execute the full pipeline">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <Button
                                onClick={handleStartGoogleSearch}
                                disabled={loading || status?.active}
                                className="w-full"
                            >
                                Start Google Search
                            </Button>
                            <Button
                                onClick={handleStartScraping}
                                disabled={loading || status?.active}
                                className="w-full"
                            >
                                Start Scraping
                            </Button>
                            <Button
                                onClick={handleStartExtract}
                                disabled={loading || status?.active}
                                className="w-full"
                            >
                                Start Extraction
                            </Button>
                            <Button
                                onClick={handleExecuteAll}
                                disabled={loading || status?.active}
                                variant="primary"
                                className="w-full"
                            >
                                Execute Full Pipeline
                            </Button>
                        </div>
                    </ComponentCard>

                    {/* Pipeline Controls */}
                    <ComponentCard title="Pipeline Controls" desc="Control the active pipeline">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <Button
                                onClick={handlePause}
                                disabled={loading || !status?.active}
                                variant="outline"
                                className="w-full"
                            >
                                Pause
                            </Button>
                            <Button
                                onClick={handleResume}
                                disabled={loading || !status?.active}
                                variant="outline"
                                className="w-full"
                            >
                                Resume
                            </Button>
                            <Button
                                onClick={handleSkip}
                                disabled={loading || !status?.active}
                                variant="outline"
                                className="w-full"
                            >
                                Skip Step
                            </Button>
                            <Button
                                onClick={handleCancel}
                                disabled={loading || !status?.active}
                                variant="outline"
                                className="w-full text-red-600 border-red-600 hover:bg-red-50 dark:text-red-400 dark:border-red-400 dark:hover:bg-red-900/20"
                            >
                                Cancel
                            </Button>
                        </div>
                    </ComponentCard>

                    {/* Step Selection */}
                    <div className="xl:col-span-2">
                        <ComponentCard title="Custom Step Selection" desc="Select specific steps to execute in the pipeline">
                            <div className="space-y-3 mb-4">
                                {pipelineSteps.map(step => (
                                    <label key={step.id} className="flex items-center gap-3 cursor-pointer text-gray-700 dark:text-gray-300">
                                        <input
                                            type="checkbox"
                                            checked={selectedSteps.includes(step.id)}
                                            onChange={(e) => {
                                                if (e.target.checked) {
                                                    setSelectedSteps([...selectedSteps, step.id]);
                                                } else {
                                                    setSelectedSteps(selectedSteps.filter(s => s !== step.id));
                                                }
                                            }}
                                            className="w-4 h-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500 dark:border-gray-600 dark:bg-gray-700"
                                        />
                                        <span>{step.label}</span>
                                    </label>
                                ))}
                            </div>
                            <Button
                                onClick={async () => {
                                    try {
                                        await executeSteps(selectedSteps);
                                        addToast("success", "Success", "Selected steps started");
                                        fetchStatus();
                                    } catch {
                                        addToast("error", "Error", "Failed to execute selected steps");
                                    }
                                }}
                                disabled={loading || status?.active || selectedSteps.length === 0}
                            >
                                Execute Selected Steps
                            </Button>
                        </ComponentCard>
                    </div>
                </div>
            </div>
        </div>
    );
}
