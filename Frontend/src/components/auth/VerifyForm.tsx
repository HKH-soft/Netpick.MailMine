"use client";
import Label from "@/components/form/Label";
import Button from "@/components/ui/button/Button";
import Link from "next/link";
import React, { useState, useEffect, useCallback } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import AuthService, { AuthConfigResponse } from "@/services/authService";

const RESEND_COOLDOWN_DEFAULT = 600;
const RESEND_STORAGE_KEY = "verificationCooldownUntil";

export default function VerifyForm() {
    const [code, setCode] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isResending, setIsResending] = useState(false);
    const [resendCooldown, setResendCooldown] = useState(RESEND_COOLDOWN_DEFAULT);
    const [countdown, setCountdown] = useState(RESEND_COOLDOWN_DEFAULT);
    const [canResend, setCanResend] = useState(false);
    const router = useRouter();
    const searchParams = useSearchParams();
    const email = searchParams.get("email") || "";

    const cooldownKey = email ? `${RESEND_STORAGE_KEY}:${email}` : RESEND_STORAGE_KEY;

    // Load backend config and persisted cooldown on mount/email change
    useEffect(() => {
        if (typeof window === "undefined") return;

        const loadConfigAndCooldown = async () => {
            let effectiveCooldown = RESEND_COOLDOWN_DEFAULT;

            try {
                const config: AuthConfigResponse = await AuthService.getAuthConfig();
                if (config?.resendCooldownSeconds && config.resendCooldownSeconds > 0) {
                    effectiveCooldown = config.resendCooldownSeconds;
                }
            } catch (err) {
                // fallback to default if config endpoint fails
                effectiveCooldown = RESEND_COOLDOWN_DEFAULT;
            }

            setResendCooldown(effectiveCooldown);

            const storedUntil = Number(localStorage.getItem(cooldownKey) || "0");
            const now = Date.now();

            if (storedUntil > now) {
                const remainingSeconds = Math.ceil((storedUntil - now) / 1000);
                setCountdown(remainingSeconds);
                setCanResend(false);
            } else {
                setCountdown(0);
                setCanResend(true);
                localStorage.removeItem(cooldownKey);
            }
        };

        loadConfigAndCooldown();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [cooldownKey]);

    // Countdown timer effect
    useEffect(() => {
        if (countdown > 0) {
            const timer = setTimeout(() => setCountdown((prev) => prev - 1), 1000);
            return () => clearTimeout(timer);
        }

        // When the timer hits zero, allow resend and clear stored cooldown
        if (countdown <= 0) {
            setCanResend(true);
            if (typeof window !== "undefined") {
                localStorage.removeItem(cooldownKey);
            }
        }
    }, [countdown, cooldownKey]);

    const handleVerify = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        if (!email) {
            setError("Email is required. Please go back to signup.");
            return;
        }

        if (!code || code.length < 4) {
            setError("Please enter a valid verification code");
            return;
        }

        setIsSubmitting(true);

        try {
            await AuthService.verify({ email, code });
            setSuccess("Email verified successfully! Redirecting to login...");
            setTimeout(() => {
                router.push("/signin");
            }, 2000);
        } catch (err) {
            setError("Invalid or expired verification code. Please try again.");
            console.error("Verification error:", err);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleResendCode = useCallback(async () => {
        if (!canResend || !email) return;

        setError("");
        setSuccess("");
        setIsResending(true);

        try {
            await AuthService.resendVerification(email);
            setSuccess("Verification code sent! Check your email.");

            // Persist the new cooldown so reloads won't reset it
            const nextUnlock = Date.now() + resendCooldown * 1000;
            if (typeof window !== "undefined") {
                localStorage.setItem(cooldownKey, nextUnlock.toString());
            }

            setCanResend(false);
            setCountdown(resendCooldown);
        } catch (err) {
            setError("Failed to resend verification code. Please try again.");
            console.error("Resend verification error:", err);
        } finally {
            setIsResending(false);
        }
    }, [canResend, email, resendCooldown, cooldownKey]);

    const formatTime = (seconds: number) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, "0")}`;
    };

    return (
        <div className="flex flex-col flex-1 lg:w-1/2 w-full">
            <div className="w-full max-w-md sm:pt-10 mx-auto mb-5"></div>
            <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
                <div>
                    <div className="mb-5 sm:mb-8">
                        <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
                            Verify Your Email
                        </h1>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            We&apos;ve sent a verification code to{" "}
                            <span className="font-medium text-gray-700 dark:text-gray-300">
                                {email || "your email"}
                            </span>
                        </p>
                    </div>

                    <form onSubmit={handleVerify}>
                        {error && (
                            <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                                <p className="text-red-600 dark:text-red-400 text-sm">{error}</p>
                            </div>
                        )}

                        {success && (
                            <div className="mb-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                                <p className="text-green-600 dark:text-green-400 text-sm">{success}</p>
                            </div>
                        )}

                        <div className="space-y-6">
                            {/* Timer Display */}
                            <div className="text-center">
                                <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gray-100 dark:bg-gray-800 mb-4">
                                    <span className="text-2xl font-bold text-gray-700 dark:text-gray-300">
                                        {formatTime(countdown)}
                                    </span>
                                </div>
                                <p className="text-sm text-gray-500 dark:text-gray-400">
                                    {canResend
                                        ? "You can now resend the verification code"
                                        : "Time remaining to enter code"}
                                </p>
                            </div>

                            {/* Verification Code Input */}
                            <div>
                                <Label>
                                    Verification Code <span className="text-error-500">*</span>
                                </Label>
                                <input
                                    type="text"
                                    value={code}
                                    onChange={(e) => setCode(e.target.value.toUpperCase())}
                                    placeholder="Enter verification code"
                                    maxLength={10}
                                    className="h-11 w-full rounded-lg border appearance-none px-4 py-2.5 text-sm shadow-theme-xs placeholder:text-gray-400 focus:outline-hidden focus:ring-3 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800 bg-transparent text-gray-800 border-gray-300 focus:border-brand-300 focus:ring-brand-500/10 dark:border-gray-700 text-center text-lg tracking-widest"
                                />
                            </div>

                            {/* Verify Button */}
                            <div>
                                <Button
                                    type="submit"
                                    className="w-full"
                                    size="sm"
                                    disabled={isSubmitting || !code}
                                >
                                    {isSubmitting ? "Verifying..." : "Verify Email"}
                                </Button>
                            </div>

                            {/* Resend Code */}
                            <div className="text-center">
                                <p className="text-sm text-gray-500 dark:text-gray-400 mb-2">
                                    Didn&apos;t receive the code?
                                </p>
                                <button
                                    type="button"
                                    onClick={handleResendCode}
                                    disabled={!canResend || isResending}
                                    className={`text-sm font-medium ${canResend
                                        ? "text-brand-500 hover:text-brand-600 dark:text-brand-400 cursor-pointer"
                                        : "text-gray-400 dark:text-gray-600 cursor-not-allowed"
                                        }`}
                                >
                                    {isResending
                                        ? "Sending..."
                                        : canResend
                                            ? "Resend verification code"
                                            : `Resend code in ${formatTime(countdown)}`}
                                </button>
                            </div>
                        </div>
                    </form>

                    <div className="mt-8 pt-6 border-t border-gray-200 dark:border-gray-800">
                        <p className="text-sm font-normal text-center text-gray-700 dark:text-gray-400">
                            Wrong email?{" "}
                            <Link
                                href="/signup"
                                className="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                            >
                                Go back to Sign Up
                            </Link>
                        </p>
                    </div>

                    <div className="mt-4">
                        <p className="text-sm font-normal text-center text-gray-700 dark:text-gray-400">
                            Already verified?{" "}
                            <Link
                                href="/signin"
                                className="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                            >
                                Sign In
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
