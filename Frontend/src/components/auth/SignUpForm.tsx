"use client";
import Checkbox from "@/components/form/input/Checkbox";
import Input from "@/components/form/input/InputField";
import Label from "@/components/form/Label";
import Button from "@/components/ui/button/Button";
import { EyeCloseIcon, EyeIcon } from "@/icons";
import Link from "next/link";
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import AuthService, { SignupRequest } from "@/services/authService";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { useTranslation } from "react-i18next";

export default function SignUpForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [isChecked, setIsChecked] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();
  const { t } = useTranslation('common');

  const validationSchema = Yup.object({
    name: Yup.string().required(t('auth.signUp.nameRequired')),
    email: Yup.string()
      .email(t('auth.signUp.emailInvalid'))
      .required(t('auth.signUp.emailRequired')),
    password: Yup.string()
      .min(12, t('auth.signUp.passwordMinLength'))
      .required(t('auth.signUp.passwordRequired')),
  });

  const handleSubmit = async (
    values: { name: string; email: string; password: string },
    { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
  ) => {
    setError("");

    if (!isChecked) {
      setError(t('auth.signUp.agreeError'));
      setSubmitting(false);
      return;
    }

    try {
      const request: SignupRequest = {
        email: values.email,
        password: values.password,
        name: values.name
      };
      await AuthService.signup(request);
      // Redirect to verification page with email as query param
      router.push(`/verify?email=${encodeURIComponent(values.email)}`);
    } catch {
      setError(t('auth.signUp.signupError'));
    } finally {
      setSubmitting(false);
    }
  };


  return (
    <div className="flex flex-col flex-1 lg:w-1/2 w-full overflow-y-auto no-scrollbar">
      <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
        <div>
          <div className="mb-5 sm:mb-8">
            <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
              {t('auth.signUp.title')}
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {t('auth.signUp.subtitle')}
            </p>
          </div>
          <div>
            <Formik
              initialValues={{ name: "", email: "", password: "" }}
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
                    {/* <!-- Name --> */}
                    <div>
                      <Label>
                        {t('auth.signUp.name')}<span className="text-error-500">*</span>
                      </Label>
                      <Field
                        name="name"
                        type="text"
                        placeholder={t('auth.signUp.name')}
                        as={Input}
                        autoComplete="name"
                      />
                      <ErrorMessage name="name" component="div" className="text-red-500 text-sm mt-1" />
                    </div>
                    {/* <!-- Email --> */}
                    <div>
                      <Label>
                        {t('auth.signUp.email')}<span className="text-error-500">*</span>
                      </Label>
                      <Field
                        name="email"
                        type="email"
                        placeholder={t('auth.signUp.email')}
                        as={Input}
                        autoComplete="email"
                      />
                      <ErrorMessage name="email" component="div" className="text-red-500 text-sm mt-1" />
                    </div>
                    {/* <!-- Password --> */}
                    <div>
                      <Label>
                        {t('auth.signUp.password')}<span className="text-error-500">*</span>
                      </Label>
                      <div className="relative">
                        <Field
                          name="password"
                          placeholder={t('auth.signUp.password')}
                          type={showPassword ? "text" : "password"}
                          as={Input}
                          autoComplete="new-password"
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
                      <Field name="password">
                        {({ field }: { field: { value: string } }) => {
                          const val = field.value || "";
                          const checks = {
                            length: val.length >= 12,
                            upper: /[A-Z]/.test(val),
                            lower: /[a-z]/.test(val),
                            number: /[0-9]/.test(val),
                            special: /[^A-Za-z0-9]/.test(val),
                          };
                          const score = Object.values(checks).filter(Boolean).length;
                          const level = score <= 2 ? "weak" : score <= 4 ? "fair" : "strong";
                          const colors = { weak: "bg-error-500", fair: "bg-warning-500", strong: "bg-success-500" };
                          const labels = { weak: t('auth.signUp.weak'), fair: t('auth.signUp.fair'), strong: t('auth.signUp.strong') };
                          if (!val) return null;
                          return (
                            <div className="mt-2">
                              <div className="flex gap-1 mb-1">
                                <div className={`h-1 flex-1 rounded-full ${score >= 1 ? colors[level] : "bg-gray-200"}`} />
                                <div className={`h-1 flex-1 rounded-full ${score >= 3 ? colors[level] : "bg-gray-200"}`} />
                                <div className={`h-1 flex-1 rounded-full ${score >= 5 ? colors[level] : "bg-gray-200"}`} />
                              </div>
                              <p className={`text-xs font-medium ${level === "weak" ? "text-error-500" : level === "fair" ? "text-warning-500" : "text-success-500"}`}>
                                {labels[level]}
                              </p>
                            </div>
                          );
                        }}
                      </Field>
                    </div>
                    {/* <!-- Checkbox --> */}
                    <div className="flex items-center gap-3">
                      <Checkbox
                        className="w-5 h-5"
                        checked={isChecked}
                        onChange={setIsChecked}
                      />
                      <p className="inline-block font-normal text-gray-500 dark:text-gray-400">
                        {t('auth.signUp.agree')}{" "}
                        <span className="text-gray-800 dark:text-white/90">
                          {t('auth.signUp.terms')}
                        </span>{" "}
                        {t('auth.signUp.and')}{" "}
                        <span className="text-gray-800 dark:text-white">
                          {t('auth.signUp.privacy')}
                        </span>
                      </p>
                    </div>
                    {/* <!-- Button --> */}
                    <div>
                      <Button
                        type="submit"
                        className="flex items-center justify-center w-full"
                        disabled={isSubmitting || !isValid || !dirty}
                        loading={isSubmitting}
                        data-testid="signup-submit"
                      >
                        {isSubmitting ? t('auth.signUp.submitting') : t('auth.signUp.submit')}
                      </Button>
                    </div>
                  </div>
                </Form>
              )}
            </Formik>

            <div className="mt-5">
              <p className="text-sm font-normal text-center text-gray-700 dark:text-gray-400 sm:text-start">
                {t('auth.signUp.haveAccount')}
                <Link
                  href="/signin"
                  className="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                  data-testid="signin-link"
                >
                  {t('auth.signUp.signIn')}
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


