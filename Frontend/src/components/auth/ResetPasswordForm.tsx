"use client";
import Input from "@/components/form/input/InputField";
import Label from "@/components/form/Label";
import Button from "@/components/ui/button/Button";
import Link from "next/link";
import React, { useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";

export default function ResetPasswordForm() {
    const [step, setStep] = useState<"email" | "code" | "newPassword" | "success">("email");
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    // const [isSubmitting, setIsSubmitting] = useState(false); // Uncomment if needed (Formik provides isSubmitting)

    const emailValidationSchema = Yup.object({
        email: Yup.string()
            .email("Invalid email address")
            .required("Email is required"),
    });

    const codeValidationSchema = Yup.object({
        code: Yup.string()
            .min(4, "Code must be at least 4 characters")
            .required("Verification code is required"),
    });

    const passwordValidationSchema = Yup.object({
        password: Yup.string()
            .min(6, "Password must be at least 6 characters")
            .required("Password is required"),
        confirmPassword: Yup.string()
            .oneOf([Yup.ref("password")], "Passwords must match")
            .required("Please confirm your password"),
    });

    const handleRequestReset = async (
        values: { email: string },
        { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
    ) => {
        setError("");
        try {
            // TODO: Implement password reset request API call
            // await AuthService.requestPasswordReset(values.email);
            setEmail(values.email);
            setStep("code");
        } catch (err) {
            setError("Failed to send reset code. Please try again.");
            console.error("Reset request error:", err);
        } finally {
            setSubmitting(false);
        }
    };

    const handleVerifyCode = async (
        values: { code: string },
        { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
    ) => {
        setError("");
        try {
            // TODO: Implement code verification API call
            // await AuthService.verifyResetCode({ email, code: values.code });
            setStep("newPassword");
        } catch (err) {
            setError("Invalid or expired code. Please try again.");
            console.error("Code verification error:", err);
        } finally {
            setSubmitting(false);
        }
    };

    const handleResetPassword = async (
        values: { password: string; confirmPassword: string },
        { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
    ) => {
        setError("");
        try {
            // TODO: Implement password reset API call
            // await AuthService.resetPassword({ email, password: values.password });
            setStep("success");
        } catch (err) {
            setError("Failed to reset password. Please try again.");
            console.error("Password reset error:", err);
        } finally {
            setSubmitting(false);
        }
    };

    const handleResendCode = async () => {
        setError("");
        // setIsSubmitting(true); // Uncomment if using local isSubmitting state
        try {
            // TODO: Implement resend reset code API call
            // await AuthService.requestPasswordReset(email);
            alert("A new code has been sent to your email.");
        } catch (err) {
            setError("Failed to resend code. Please try again.");
            console.error("Resend code error:", err);
        }
        // finally {
        //     setIsSubmitting(false);
        // }
    };

    return (
        <div className="flex flex-col flex-1 lg:w-1/2 w-full">
            <div className="w-full max-w-md sm:pt-10 mx-auto mb-5"></div>
            <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
                <div>
                    <div className="mb-5 sm:mb-8">
                        <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
                            {step === "email" && "Forgot Password?"}
                            {step === "code" && "Enter Verification Code"}
                            {step === "newPassword" && "Create New Password"}
                            {step === "success" && "Password Reset Successfully!"}
                        </h1>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            {step === "email" &&
                                "Enter your email address and we'll send you a verification code."}
                            {step === "code" &&
                                `We've sent a verification code to ${email}. Enter it below.`}
                            {step === "newPassword" && "Enter your new password below."}
                            {step === "success" &&
                                "Your password has been reset successfully."}
                        </p>
                    </div>

                    {error && (
                        <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                            <p className="text-red-600 dark:text-red-400 text-sm">{error}</p>
                        </div>
                    )}

                    {/* Step 1: Email Input */}
                    {step === "email" && (
                        <Formik
                            initialValues={{ email: "" }}
                            validationSchema={emailValidationSchema}
                            onSubmit={handleRequestReset}
                        >
                            {({ isSubmitting, isValid, dirty }) => (
                                <Form className="space-y-6">
                                    <div>
                                        <Label>
                                            Email <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="email"
                                            type="email"
                                            placeholder="Enter your email"
                                            as={Input}
                                        />
                                        <ErrorMessage
                                            name="email"
                                            component="div"
                                            className="text-red-500 text-sm mt-1"
                                        />
                                    </div>
                                    <Button
                                        type="submit"
                                        className="w-full"
                                        disabled={isSubmitting || !isValid || !dirty}
                                    >
                                        {isSubmitting ? "Sending..." : "Send Reset Code"}
                                    </Button>
                                </Form>
                            )}
                        </Formik>
                    )}

                    {/* Step 2: Code Verification */}
                    {step === "code" && (
                        <Formik
                            initialValues={{ code: "" }}
                            validationSchema={codeValidationSchema}
                            onSubmit={handleVerifyCode}
                        >
                            {({ isSubmitting, isValid, dirty }) => (
                                <Form className="space-y-6">
                                    <div>
                                        <Label>
                                            Verification Code <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="code"
                                            type="text"
                                            placeholder="Enter verification code"
                                            as={Input}
                                            className="text-center tracking-widest"
                                        />
                                        <ErrorMessage
                                            name="code"
                                            component="div"
                                            className="text-red-500 text-sm mt-1"
                                        />
                                    </div>
                                    <Button
                                        type="submit"
                                        className="w-full"
                                        disabled={isSubmitting || !isValid || !dirty}
                                    >
                                        {isSubmitting ? "Verifying..." : "Verify Code"}
                                    </Button>
                                    <div className="text-center">
                                        <button
                                            type="button"
                                            onClick={handleResendCode}
                                            disabled={isSubmitting}
                                            className="text-sm text-brand-500 hover:text-brand-600 dark:text-brand-400"
                                        >
                                            Resend code
                                        </button>
                                    </div>
                                </Form>
                            )}
                        </Formik>
                    )}

                    {/* Step 3: New Password */}
                    {step === "newPassword" && (
                        <Formik
                            initialValues={{ password: "", confirmPassword: "" }}
                            validationSchema={passwordValidationSchema}
                            onSubmit={handleResetPassword}
                        >
                            {({ isSubmitting, isValid, dirty }) => (
                                <Form className="space-y-6">
                                    <div>
                                        <Label>
                                            New Password <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="password"
                                            type="password"
                                            placeholder="Enter new password"
                                            as={Input}
                                        />
                                        <ErrorMessage
                                            name="password"
                                            component="div"
                                            className="text-red-500 text-sm mt-1"
                                        />
                                    </div>
                                    <div>
                                        <Label>
                                            Confirm Password <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="confirmPassword"
                                            type="password"
                                            placeholder="Confirm new password"
                                            as={Input}
                                        />
                                        <ErrorMessage
                                            name="confirmPassword"
                                            component="div"
                                            className="text-red-500 text-sm mt-1"
                                        />
                                    </div>
                                    <Button
                                        type="submit"
                                        className="w-full"
                                        disabled={isSubmitting || !isValid || !dirty}
                                    >
                                        {isSubmitting ? "Resetting..." : "Reset Password"}
                                    </Button>
                                </Form>
                            )}
                        </Formik>
                    )}

                    {/* Step 4: Success */}
                    {step === "success" && (
                        <div className="text-center">
                            <div className="mb-6">
                                <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 dark:bg-green-900/20 mb-4">
                                    <svg
                                        className="w-8 h-8 text-green-600 dark:text-green-400"
                                        fill="none"
                                        stroke="currentColor"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M5 13l4 4L19 7"
                                        />
                                    </svg>
                                </div>
                            </div>
                            <Link href="/signin">
                                <Button className="w-full">Go to Sign In</Button>
                            </Link>
                        </div>
                    )}

                    {step !== "success" && (
                        <div className="mt-6">
                            <p className="text-sm font-normal text-center text-gray-700 dark:text-gray-400">
                                Remember your password?{" "}
                                <Link
                                    href="/signin"
                                    className="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                                >
                                    Sign In
                                </Link>
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
