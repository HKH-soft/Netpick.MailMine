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

export default function SignUpForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [isChecked, setIsChecked] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();

  const validationSchema = Yup.object({
    name: Yup.string().required("Name is required"),
    email: Yup.string()
      .email("Invalid email address")
      .required("Email is required"),
    password: Yup.string()
      .min(6, "Password must be at least 6 characters")
      .required("Password is required"),
  });

  const handleSubmit = async (
    values: { name: string; email: string; password: string },
    { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
  ) => {
    setError("");

    if (!isChecked) {
      setError("You must agree to the Terms and Conditions and Privacy Policy");
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
    } catch (err) {
      setError("Failed to create account. Please try again.");
      console.error("Sign up error:", err);
    } finally {
      setSubmitting(false);
    }
  };


  return (
    <div className="flex flex-col flex-1 lg:w-1/2 w-full overflow-y-auto no-scrollbar">
      <div className="w-full max-w-md sm:pt-10 mx-auto mb-5">
        {/* Removed "Back to dashboard" link */}
      </div>
      <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
        <div>
          <div className="mb-5 sm:mb-8">
            <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
              Sign Up
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Enter your email and password to sign up!
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
                    <div className="mb-4 text-red-500 text-sm">{error}</div>
                  )}
                  <div className="space-y-5">
                    {/* <!-- Name --> */}
                    <div>
                      <Label>
                        Name<span className="text-error-500">*</span>
                      </Label>
                      <Field
                        name="name"
                        type="text"
                        placeholder="Enter your name"
                        as={Input}
                      />
                      <ErrorMessage name="name" component="div" className="text-red-500 text-sm mt-1" />
                    </div>
                    {/* <!-- Email --> */}
                    <div>
                      <Label>
                        Email<span className="text-error-500">*</span>
                      </Label>
                      <Field
                        name="email"
                        type="email"
                        placeholder="Enter your email"
                        as={Input}
                      />
                      <ErrorMessage name="email" component="div" className="text-red-500 text-sm mt-1" />
                    </div>
                    {/* <!-- Password --> */}
                    <div>
                      <Label>
                        Password<span className="text-error-500">*</span>
                      </Label>
                      <div className="relative">
                        <Field
                          name="password"
                          placeholder="Enter your password"
                          type={showPassword ? "text" : "password"}
                          as={Input}
                        />
                        <span
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute z-30 -translate-y-1/2 cursor-pointer right-4 top-1/2"
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
                    {/* <!-- Checkbox --> */}
                    <div className="flex items-center gap-3">
                      <Checkbox
                        className="w-5 h-5"
                        checked={isChecked}
                        onChange={setIsChecked}
                      />
                      <p className="inline-block font-normal text-gray-500 dark:text-gray-400">
                        By creating an account means you agree to the{" "}
                        <span className="text-gray-800 dark:text-white/90">
                          Terms and Conditions,
                        </span>{" "}
                        and our{" "}
                        <span className="text-gray-800 dark:text-white">
                          Privacy Policy
                        </span>
                      </p>
                    </div>
                    {/* <!-- Button --> */}
                    <div>
                      <Button
                        type="submit"
                        className="flex items-center justify-center w-full"
                        disabled={isSubmitting || !isValid || !dirty}
                      >
                        {isSubmitting ? "Signing up..." : "Sign Up"}
                      </Button>
                    </div>
                  </div>
                </Form>
              )}
            </Formik>

            <div className="mt-5">
              <p className="text-sm font-normal text-center text-gray-700 dark:text-gray-400 sm:text-start">
                Already have an account?
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
    </div>
  );
}