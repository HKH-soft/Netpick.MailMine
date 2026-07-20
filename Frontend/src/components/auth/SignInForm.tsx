"use client";
import Checkbox from "@/components/form/input/Checkbox";
import Input from "@/components/form/input/InputField";
import Label from "@/components/form/Label";
import Button from "@/components/ui/button/Button";
import { EyeCloseIcon, EyeIcon } from "@/icons";
import Link from "next/link";
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import AuthService, { SigninRequest } from "@/services/authService";
import { useAuth } from "@/context/AuthContext";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { useTranslation } from "react-i18next";

export default function SignInForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [isChecked, setIsChecked] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();
  const { login } = useAuth();
  const { t } = useTranslation('common');

  const validationSchema = Yup.object({
    email: Yup.string()
      .email(t('auth.signIn.emailInvalid'))
      .required(t('auth.signIn.emailRequired')),
    password: Yup.string()
      .min(12, t('auth.signIn.passwordMinLength'))
      .required(t('auth.signIn.passwordRequired')),
  });

  const handleSubmit = async (
    values: { email: string; password: string },
    { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
  ) => {
    setError("");
    try {
      const request: SigninRequest = {
        email: values.email,
        password: values.password,
      };
      await AuthService.signin(request, isChecked); // Pass rememberMe option
      login(); // Update auth context
      router.push("/"); // Redirect to dashboard after successful login
      router.refresh(); // Refresh the page to update the auth state
    } catch {
      setError(t('auth.signIn.signinError'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col flex-1 lg:w-1/2 w-full">
      <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
        <div>
          <div className="mb-5 sm:mb-8">
            <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
              {t('auth.signIn.title')}
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {t('auth.signIn.subtitle')}
            </p>
          </div>
          <div>
            <Formik
              initialValues={{ email: "", password: "" }}
              validationSchema={validationSchema}
              onSubmit={handleSubmit}
            >
              {({ isSubmitting, isValid, dirty }) => (
                <Form>
                  {error && (
                    <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                        <p className="text-red-600 dark:text-red-400 text-sm">{error}</p>
                    </div>
                  )}
                  <div className="space-y-6">
                    <div>
                      <Label>
                        {t('auth.signIn.email')} <span className="text-error-500">*</span>{" "}
                      </Label>
                      <Field
                        name="email"
                        type="email"
                        placeholder="info@gmail.com"
                        as={Input}
                        autoComplete="email"
                      />
                      <ErrorMessage name="email" component="div" className="text-red-500 text-sm mt-1" />
                    </div>
                    <div>
                      <Label>
                        {t('auth.signIn.password')} <span className="text-error-500">*</span>{" "}
                      </Label>
                      <div className="relative">
                        <Field
                          name="password"
                          type={showPassword ? "text" : "password"}
                          placeholder={t('auth.signIn.passwordPlaceholder')}
                          as={Input}
                          autoComplete="current-password"
                        />
                        <span
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute z-30 -translate-y-1/2 cursor-pointer right-4 top-1/2"
                          data-testid="password-toggle"
                          role="button"
                          aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                          {showPassword ? (
                            <EyeIcon className="fill-gray-500 dark:fill-gray-400" />
                          ) : (
                            <EyeCloseIcon className="fill-gray-500 dark:fill-gray-400" />
                          )}
                        </span>
                      </div>
                      <ErrorMessage name="password" component="div" className="text-red-500 text-sm mt-1" />
                    </div>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <Checkbox checked={isChecked} onChange={setIsChecked} />
                        <span className="block font-normal text-gray-700 text-theme-sm dark:text-gray-400">
                          {t('auth.signIn.rememberMe')}
                        </span>
                      </div>
                <Link
                  href="/reset-password"
                  className="text-sm text-brand-500 hover:text-brand-600 dark:text-brand-400"
                  data-testid="forgot-password-link"
                >
                        {t('auth.signIn.forgotPassword')}
                      </Link>
                    </div>
                    <div>
                      <Button
                        type="submit"
                        className="w-full"
                        size="sm"
                        disabled={isSubmitting || !isValid || !dirty}
                        loading={isSubmitting}
                        data-testid="signin-submit"
                      >
                        {isSubmitting ? t('auth.signIn.submitting') : t('auth.signIn.submit')}
                      </Button>
                    </div>
                  </div>
                </Form>
              )}
            </Formik>

            <div className="mt-5">
              <p className="text-sm font-normal text-center text-gray-700 dark:text-gray-400 sm:text-start">
                {t('auth.signIn.noAccount')}
                <Link
                  href="/signup"
                  className="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                  data-testid="signup-link"
                >
                  {t('auth.signIn.signUp')}
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


