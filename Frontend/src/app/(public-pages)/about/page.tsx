import { Metadata } from "next";
import React from "react";

export const metadata: Metadata = {
  title: "About Us | Netpick",
  description: "Learn more about Netpick and our mission",
};

export default function AboutPage() {
  return (
    <div>

      <div className="space-y-12">
        {/* Our Story */}
        <section className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
          <h2 className="text-3xl font-bold text-gray-800 dark:text-white mb-6">
            Our Story
          </h2>
          <p className="text-gray-600 dark:text-gray-300 mb-4">
            Netpick was founded in 2024 with a simple mission: to make email data
            collection and analysis accessible to businesses of all sizes. We recognized
            that traditional email scraping tools were either too complex or lacked the
            necessary features for professional use.
          </p>
          <p className="text-gray-600 dark:text-gray-300 mb-4">
            Our platform combines cutting-edge technology with intuitive design to
            provide a seamless experience for marketers, researchers, and businesses
            looking to gather and analyze contact information ethically and efficiently.
          </p>
        </section>

        {/* Mission & Vision */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <section className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
            <h3 className="text-2xl font-bold text-gray-800 dark:text-white mb-4">
              Our Mission
            </h3>
            <p className="text-gray-600 dark:text-gray-300">
              To empower businesses with reliable, ethical, and efficient email
              scraping solutions that drive growth while maintaining the highest
              standards of data privacy and compliance.
            </p>
          </section>

          <section className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
            <h3 className="text-2xl font-bold text-gray-800 dark:text-white mb-4">
              Our Vision
            </h3>
            <p className="text-gray-600 dark:text-gray-300">
              To become the leading platform for email intelligence, helping businesses
              worldwide make data-driven decisions through accurate and actionable insights.
            </p>
          </section>
        </div>

        {/* Team Section */}
        <section>
          <h2 className="text-3xl font-bold text-center text-gray-800 dark:text-white mb-8">
            Meet Our Team
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              { name: "Sarah Johnson", role: "CEO & Founder" },
              { name: "Michael Chen", role: "CTO" },
              { name: "Emily Rodriguez", role: "Head of Product" },
            ].map((member) => (
              <div
                key={member.name}
                className="rounded-2xl border border-gray-200 bg-white p-6 text-center dark:border-gray-800 dark:bg-white/[0.03]"
              >
                <div className="w-24 h-24 bg-gray-200 rounded-full mx-auto mb-4 dark:bg-gray-700" />
                <h4 className="text-xl font-semibold text-gray-800 dark:text-white">
                  {member.name}
                </h4>
                <p className="text-gray-500 dark:text-gray-400">{member.role}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Values */}
        <section className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
          <h2 className="text-3xl font-bold text-gray-800 dark:text-white mb-6">
            Our Values
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h4 className="font-semibold text-gray-800 dark:text-white mb-2">
                Transparency
              </h4>
              <p className="text-gray-600 dark:text-gray-400">
                We believe in clear communication and honest business practices.
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-gray-800 dark:text-white mb-2">
                Innovation
              </h4>
              <p className="text-gray-600 dark:text-gray-400">
                Continuously improving our technology to stay ahead of industry trends.
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-gray-800 dark:text-white mb-2">
                Privacy
              </h4>
              <p className="text-gray-600 dark:text-gray-400">
                Your data security and privacy are our top priorities.
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-gray-800 dark:text-white mb-2">
                Excellence
              </h4>
              <p className="text-gray-600 dark:text-gray-400">
                Delivering the highest quality service and support to our customers.
              </p>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}