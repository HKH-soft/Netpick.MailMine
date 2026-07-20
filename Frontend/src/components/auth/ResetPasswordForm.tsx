"use client";
import Input from "@/components/form/input/InputField";
import Label from "@/components/form/Label";
import Button from "@/components/ui/button/Button";
import Link from "next/link";
import React, { useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import AuthService from "@/services/authService";
import { useTranslation } from "react-i18next";

export default function ResetPasswordForm() {
    const [step, setStep] = useState<"email" | "code" | "newPassword" | "success">("email");
    const [email, setEmail] = useState("");
    const [code, setCode] = useState("");
    const [error, setError] = useState("");
    const { t } = useTranslation('common');

    const emailValidationSchema = Yup.object({
        email: Yup.string()
            .email(t('auth.resetPassword.emailInvalid'))
            .required(t('auth.resetPassword.emailRequired')),
    });

    const codeValidationSchema = Yup.object({
        code: Yup.string()
            .min(4, t('auth.resetPassword.codeMinLength'))
            .required(t('auth.resetPassword.codeRequired')),
    });

    const passwordValidationSchema = Yup.object({
        password: Yup.string()
            .min(12, t('auth.resetPassword.passwordMinLength'))
            .required(t('auth.resetPassword.passwordRequired')),
        confirmPassword: Yup.string()
            .oneOf([Yup.ref("password")], t('auth.resetPassword.passwordMatch'))
            .required(t('auth.resetPassword.confirmPasswordRequired')),
    });

    const handleRequestReset = async (
        values: { email: string },
        { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
    ) => {
        setError("");
        try {
            await AuthService.requestPasswordReset(values.email);
            setEmail(values.email);
            setStep("code");
        } catch {
            setError(t('auth.resetPassword.sendResetError'));
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
            await AuthService.verifyPasswordResetCode(email, values.code);
            setCode(values.code);
            setStep("newPassword");
        } catch {
            setError(t('auth.resetPassword.verifyCodeError'));
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
            await AuthService.confirmPasswordReset(email, code, values.password);
            setStep("success");
        } catch {
            setError(t('auth.resetPassword.resetPasswordError'));
        } finally {
            setSubmitting(false);
        }
    };

    const handleResendCode = async () => {
        setError("");
        try {
            await AuthService.requestPasswordReset(email);
            alert(t('auth.resetPassword.newCodeSent'));
        } catch {
            setError(t('auth.resetPassword.resendCodeError'));
        }
    };

    return (
        <div className="flex flex-col flex-1 lg:w-1/2 w-full">
            <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
                <div>
                    <div className="mb-5 sm:mb-8">
                        <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md" data-testid="reset-password-title">
                            {step === "email" && t('auth.resetPassword.forgotPasswordTitle')}
                            {step === "code" && t('auth.resetPassword.enterCodeTitle')}
                            {step === "newPassword" && t('auth.resetPassword.newPasswordTitle')}
                            {step === "success" && t('auth.resetPassword.successTitle')}
                        </h1>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            {step === "email" &&
                                t('auth.resetPassword.emailSubtitle')}
                            {step === "code" &&
                                t('auth.resetPassword.codeSubtitle', { email })}
                            {step === "newPassword" && t('auth.resetPassword.newPasswordSubtitle')}
                            {step === "success" &&
                                t('auth.resetPassword.successSubtitle')}
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
                                            {t('auth.resetPassword.emailLabel')} <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="email"
                                            type="email"
                                            placeholder={t('auth.resetPassword.emailPlaceholder')}
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
                                        data-testid="reset-password-submit"
                                    >
                                        {isSubmitting ? t('auth.resetPassword.sendingCode') : t('auth.resetPassword.sendCode')}
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
                                            {t('auth.resetPassword.codeLabel')} <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="code"
                                            type="text"
                                            placeholder={t('auth.resetPassword.codePlaceholder')}
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
                                        data-testid="reset-password-submit"
                                    >
                                        {isSubmitting ? t('auth.resetPassword.verifying') : t('auth.resetPassword.verifyCode')}
                                    </Button>
                                    <div className="text-center">
                                        <button
                                            type="button"
                                            onClick={handleResendCode}
                                            disabled={isSubmitting}
                                            className="text-sm text-brand-500 hover:text-brand-600 dark:text-brand-400"
                                            data-testid="resend-code"
                                        >
                                            {t('auth.resetPassword.resendCode')}
                                        </button>
                                    </div>
                                    <div className="text-center">
                                        <button
                                            type="button"
                                            onClick={() => { setError(""); setStep("email"); }}
                                            className="text-sm text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
                                            data-testid="back-to-email"
                                        >
                                            {t('auth.resetPassword.back', { defaultValue: 'Back' })}
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
                                            {t('auth.resetPassword.newPasswordLabel')} <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="password"
                                            type="password"
                                            placeholder={t('auth.resetPassword.newPasswordPlaceholder')}
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
                                            {t('auth.resetPassword.confirmPasswordLabel')} <span className="text-error-500">*</span>
                                        </Label>
                                        <Field
                                            name="confirmPassword"
                                            type="password"
                                            placeholder={t('auth.resetPassword.confirmPasswordPlaceholder')}
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
                                        data-testid="reset-password-submit"
                                    >
                                        {isSubmitting ? t('auth.resetPassword.resetting') : t('auth.resetPassword.resetPasswordBtn')}
                                    </Button>
                                    <div className="text-center">
                                        <button
                                            type="button"
                                            onClick={() => { setError(""); setStep("code"); }}
                                            className="text-sm text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
                                            data-testid="back-to-code"
                                        >
                                            {t('auth.resetPassword.back', { defaultValue: 'Back' })}
                                        </button>
                                    </div>
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
                                <Button className="w-full">{t('auth.resetPassword.goToSignIn')}</Button>
                            </Link>
                        </div>
                    )}

                    {step !== "success" && (
                        <div className="mt-6">
                            <p className="text-sm font-normal text-center text-gray-700 dark:text-gray-400">
                                {t('auth.resetPassword.rememberPassword')}{" "}
                                <Link
                                    href="/signin"
                                    className="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                                >
                                    {t('auth.resetPassword.signIn')}
                                </Link>
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}



