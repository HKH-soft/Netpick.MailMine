"use client";

import React, { useState } from "react";
import RevealOnScroll from "@/components/landing/ui/RevealOnScroll";
import contactFormService from "@/services/contactFormService";

export default function ContactPage() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    subject: "",
    message: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.name.trim()) newErrors.name = "Name is required";
    if (!formData.email.trim()) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Invalid email format";
    }
    if (!formData.subject.trim()) newErrors.subject = "Subject is required";
    if (!formData.message.trim()) newErrors.message = "Message is required";
    return newErrors;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    setErrors({});
    setIsSubmitting(true);
    try {
      await contactFormService.submitContactForm(formData);
      setSubmitted(true);
    } catch {
      setErrors({ form: "Something went wrong. Please try again." });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (errors[e.target.name]) {
      setErrors({ ...errors, [e.target.name]: "" });
    }
  };

  return (
    <div>
      <section className="relative py-24 md:py-32 overflow-hidden aurora-bg">
        <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <div className="max-w-3xl">
              <span className="inline-block text-xs font-semibold tracking-widest uppercase text-gradient-accent mb-4">
                Contact
              </span>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-gradient-white mb-6">
                Get in touch
              </h1>
              <p className="text-lg text-[var(--color-text-secondary)] leading-relaxed">
                Have questions? We&apos;d love to hear from you. Send us a message and
                we&apos;ll respond as soon as possible.
              </p>
            </div>
          </RevealOnScroll>
        </div>
      </section>

      <div className="section-divider" />

      <section className="py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-3 gap-12">
            <RevealOnScroll>
              <div className="space-y-6">
                <div className="glass-card p-6">
                  <h3 className="text-lg font-semibold text-gradient-white mb-2">Email</h3>
                  <p className="text-[var(--color-text-secondary)]">support@netpick.io</p>
                </div>
                <div className="glass-card p-6">
                  <h3 className="text-lg font-semibold text-gradient-white mb-2">Phone</h3>
                  <p className="text-[var(--color-text-secondary)]">+1 (555) 123-4567</p>
                </div>
                <div className="glass-card p-6">
                  <h3 className="text-lg font-semibold text-gradient-white mb-2">Address</h3>
                  <p className="text-[var(--color-text-secondary)]">
                    123 Business Ave, Suite 100<br />
                    San Francisco, CA 94105
                  </p>
                </div>
                <div className="glass-card p-6">
                  <h3 className="text-lg font-semibold text-gradient-white mb-3">Business Hours</h3>
                  <div className="space-y-2 text-[var(--color-text-secondary)] text-sm">
                    <div className="flex justify-between">
                      <span>Monday - Friday</span>
                      <span>9:00 AM - 6:00 PM</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Saturday</span>
                      <span>10:00 AM - 4:00 PM</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Sunday</span>
                      <span>Closed</span>
                    </div>
                  </div>
                </div>
              </div>
            </RevealOnScroll>

            <RevealOnScroll delay={0.1}>
              <div className="lg:col-span-2">
                {submitted ? (
                  <div className="glass-card-lg p-8 text-center glow-accent">
                    <svg className="w-12 h-12 text-[var(--color-accent)] mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <h3 className="text-xl font-semibold text-gradient-white mb-2">Message Sent!</h3>
                    <p className="text-[var(--color-text-secondary)]">We&apos;ll get back to you shortly.</p>
                  </div>
                ) : (
                  <form onSubmit={handleSubmit} className="space-y-6">
                    {errors.form && (
                      <div className="rounded-2xl bg-red-500/10 border border-red-500/30 p-4 text-sm text-red-400 backdrop-blur-sm">
                        {errors.form}
                      </div>
                    )}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <label className="block text-sm font-medium text-[var(--color-text-secondary)] mb-2">Name</label>
                        <input
                          name="name"
                          value={formData.name}
                          onChange={handleChange}
                          placeholder="Your name"
                          className="w-full rounded-xl border border-[var(--color-glass-border)] bg-[var(--color-glass-bg)] backdrop-blur-md px-4 py-3 text-sm text-white placeholder-[var(--color-text-muted)] focus:border-[var(--color-accent)] focus:outline-none focus:ring-1 focus:ring-[var(--color-accent)]/50 focus:shadow-[0_0_16px_rgba(41,141,255,0.1)] transition-all duration-300"
                        />
                        {errors.name && <p className="mt-1 text-sm text-red-400">{errors.name}</p>}
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-[var(--color-text-secondary)] mb-2">Email</label>
                        <input
                          name="email"
                          type="email"
                          value={formData.email}
                          onChange={handleChange}
                          placeholder="your@email.com"
                          className="w-full rounded-xl border border-[var(--color-glass-border)] bg-[var(--color-glass-bg)] backdrop-blur-md px-4 py-3 text-sm text-white placeholder-[var(--color-text-muted)] focus:border-[var(--color-accent)] focus:outline-none focus:ring-1 focus:ring-[var(--color-accent)]/50 focus:shadow-[0_0_16px_rgba(41,141,255,0.1)] transition-all duration-300"
                        />
                        {errors.email && <p className="mt-1 text-sm text-red-400">{errors.email}</p>}
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-[var(--color-text-secondary)] mb-2">Subject</label>
                      <input
                        name="subject"
                        value={formData.subject}
                        onChange={handleChange}
                        placeholder="How can we help you?"
                        className="w-full rounded-xl border border-[var(--color-glass-border)] bg-[var(--color-glass-bg)] backdrop-blur-md px-4 py-3 text-sm text-white placeholder-[var(--color-text-muted)] focus:border-[var(--color-accent)] focus:outline-none focus:ring-1 focus:ring-[var(--color-accent)]/50 focus:shadow-[0_0_16px_rgba(41,141,255,0.1)] transition-all duration-300"
                      />
                      {errors.subject && <p className="mt-1 text-sm text-red-400">{errors.subject}</p>}
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-[var(--color-text-secondary)] mb-2">Message</label>
                      <textarea
                        name="message"
                        value={formData.message}
                        onChange={handleChange}
                        rows={6}
                        placeholder="Your message..."
                        className="w-full rounded-xl border border-[var(--color-glass-border)] bg-[var(--color-glass-bg)] backdrop-blur-md px-4 py-3 text-sm text-white placeholder-[var(--color-text-muted)] focus:border-[var(--color-accent)] focus:outline-none focus:ring-1 focus:ring-[var(--color-accent)]/50 focus:shadow-[0_0_16px_rgba(41,141,255,0.1)] transition-all duration-300 resize-none"
                      />
                      {errors.message && <p className="mt-1 text-sm text-red-400">{errors.message}</p>}
                    </div>
                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="btn-gradient inline-flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span>{isSubmitting ? "Sending..." : "Send Message"}</span>
                    </button>
                  </form>
                )}
              </div>
            </RevealOnScroll>
          </div>
        </div>
      </section>
    </div>
  );
}
