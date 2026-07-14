import { Metadata } from "next";
import React, { useState } from "react";
import Input from "@/components/form/input/InputField";
import Button from "@/components/ui/button/Button";

export const metadata: Metadata = {
  title: "Contact Us | Netpick",
  description: "Get in touch with the Netpick team",
};

export default function ContactPage() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    subject: "",
    message: "",
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Form submission logic would go here
    console.log("Form submitted:", formData);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Contact Info */}
        <div className="lg:col-span-1 space-y-6">
          <section className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
            <h3 className="text-xl font-semibold text-gray-800 dark:text-white mb-4">
              Get in Touch
            </h3>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              Have questions or need assistance? We're here to help.
            </p>

            <div className="space-y-4">
              <div>
                <h4 className="font-medium text-gray-800 dark:text-white">Email</h4>
                <p className="text-gray-600 dark:text-gray-400">support@netpick.io</p>
              </div>
              <div>
                <h4 className="font-medium text-gray-800 dark:text-white">Phone</h4>
                <p className="text-gray-600 dark:text-gray-400">+1 (555) 123-4567</p>
              </div>
              <div>
                <h4 className="font-medium text-gray-800 dark:text-white">Address</h4>
                <p className="text-gray-600 dark:text-gray-400">
                  123 Business Ave, Suite 100<br />
                  San Francisco, CA 94105
                </p>
              </div>
            </div>
          </section>

          <section className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
            <h3 className="text-xl font-semibold text-gray-800 dark:text-white mb-4">
              Business Hours
            </h3>
            <div className="space-y-2 text-gray-600 dark:text-gray-400">
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
          </section>
        </div>

        {/* Contact Form */}
        <div className="lg:col-span-2">
          <section className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
            <h2 className="text-2xl font-bold text-gray-800 dark:text-white mb-6">
              Send us a Message
            </h2>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Name
                  </label>
                  <Input
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Your name"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Email
                  </label>
                  <Input
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="your@email.com"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Subject
                </label>
                <Input
                  name="subject"
                  value={formData.subject}
                  onChange={handleChange}
                  placeholder="How can we help you?"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Message
                </label>
                <textarea
                  name="message"
                  value={formData.message}
                  onChange={handleChange}
                  rows={6}
                  placeholder="Your message..."
                  className="w-full rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm text-gray-800 focus:border-brand-500 focus:outline-none dark:border-gray-700 dark:bg-gray-900 dark:text-white"
                  required
                />
              </div>

              <Button type="submit" variant="primary">
                Send Message
              </Button>
            </form>
          </section>
        </div>
      </div>
    </div>
  );
}