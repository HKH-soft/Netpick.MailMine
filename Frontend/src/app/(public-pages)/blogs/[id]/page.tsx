import { Metadata } from "next";
import React from "react";
import Link from "next/link";

const blogPosts = [
  {
    id: 1,
    title: "Getting Started with Email Scraping: A Complete Guide",
    content: "Email scraping is the process of extracting email addresses from various online sources to build targeted contact lists. This guide covers everything from the basics of how email scraping works to advanced techniques for maximizing your results while maintaining ethical standards.\n\nWhen getting started, it's important to understand the legal landscape around email scraping. Different jurisdictions have different rules about data collection, and staying compliant is crucial for any business.\n\nNetpick provides a powerful yet ethical approach to email scraping. Our platform uses advanced algorithms to extract valid email addresses from public sources while respecting robots.txt files and rate limits.\n\nKey features of Netpick include real-time email verification, bulk processing capabilities, and integration with popular CRM platforms. These tools help ensure that your scraped data is both accurate and actionable.",
    date: "2026-07-10",
    author: "Sarah Johnson",
    readTime: "5 min read",
    category: "Guide",
  },
  {
    id: 2,
    title: "5 Best Practices for Email Data Validation",
    content: "Email validation is a critical step in any data collection workflow. Invalid emails lead to bounced messages, wasted resources, and potential damage to your sender reputation. Here are five best practices to ensure your email data is accurate and actionable.\n\nFirst, always verify email syntax before accepting an address. This catches obvious typos and formatting errors at the point of collection.\n\nSecond, use DNS lookup to verify that the domain exists and has valid MX records. A domain without mail servers cannot receive emails.\n\nThird, implement SMTP verification to check if the specific mailbox exists without sending an actual email. This is the most reliable way to confirm deliverability.\n\nFourth, clean your email lists regularly. Email addresses become invalid over time as people change jobs or abandon accounts.\n\nFifth, monitor your bounce rates and spam complaints. These metrics are key indicators of your list quality.",
    date: "2026-07-05",
    author: "Michael Chen",
    readTime: "4 min read",
    category: "Tips",
  },
  {
    id: 3,
    title: "New Feature: Advanced Pipeline Automation",
    content: "We're excited to announce the launch of our most powerful feature yet - automated pipelines that can process thousands of contacts per hour. This feature represents a major step forward in email outreach automation.\n\nPipeline automation allows you to create custom workflows that trigger actions based on specific conditions. For example, you can automatically send a follow-up email when a contact opens your initial message, or route high-value leads to your sales team.\n\nThe new pipeline builder features a drag-and-drop interface that makes it easy to create complex automation sequences without any coding knowledge. Simply connect your triggers, conditions, and actions to build your workflow.\n\nPerformance improvements in this release mean that pipelines can now process up to 10,000 contacts per hour, making it possible to scale your outreach efforts significantly.",
    date: "2026-06-28",
    author: "Emily Rodriguez",
    readTime: "3 min read",
    category: "Updates",
  },
  {
    id: 4,
    title: "Understanding Email Verification Standards",
    content: "Email verification is more complex than it might appear on the surface. There are multiple layers of validation that a proper system should perform, each catching different types of issues.\n\nAt the most basic level, syntax validation checks that an email address conforms to the standard format defined in RFC 5322. This catches typos, missing @ symbols, and other formatting errors.\n\nDNS validation goes a step further by checking that the domain portion of the email address actually exists and has the proper mail server configuration. Without valid MX records, an email cannot be delivered.\n\nSMTP verification is the most thorough check. It involves connecting to the mail server and simulating the initial steps of sending an email to determine if the specific mailbox exists. This method can catch issues like full mailboxes and disabled accounts.\n\nUnderstanding these standards helps you choose the right verification approach for your needs and interpret the results accurately.",
    date: "2026-06-20",
    author: "David Kim",
    readTime: "6 min read",
    category: "Technical",
  },
  {
    id: 5,
    title: "How to Scale Your Email Outreach Campaigns",
    content: "Scaling email outreach is a common challenge for growing businesses. As your contact lists grow, maintaining deliverability and engagement becomes increasingly difficult. Here's how to scale effectively.\n\nStart with proper list segmentation. Divide your contacts into meaningful groups based on demographics, behavior, or engagement history. This allows you to send more relevant messages to each segment.\n\nPersonalization is key to maintaining engagement at scale. Use the data you have about each contact to customize your messages. Even small personalizations like using the recipient's name can significantly improve open rates.\n\nAutomate your follow-up sequences. Manual follow-ups don't scale. Set up automated sequences that send timely follow-ups based on recipient behavior.\n\nMonitor your key metrics closely as you scale. Track open rates, click rates, bounce rates, and unsubscribe rates for each campaign. These metrics will alert you to problems before they become critical.",
    date: "2026-06-15",
    author: "Lisa Anderson",
    readTime: "7 min read",
    category: "Marketing",
  },
  {
    id: 6,
    title: "GDPR Compliance in Email Scraping",
    content: "GDPR compliance is essential for any business that collects or processes personal data of EU citizens. Email scraping activities must adhere to specific requirements to remain compliant.\n\nThe first principle to understand is that GDPR requires a lawful basis for processing personal data. For email scraping, this typically means either consent or legitimate interest.\n\nLegitimate interest can be a valid basis for email scraping in certain circumstances, but it requires a documented balancing test that weighs your business interests against the data subject's rights and freedoms.\n\nData minimization is another core GDPR principle. You should only collect email addresses and related data that you actually need for your stated purpose. Excessive data collection violates this principle.\n\nData subjects have rights under GDPR that you must respect, including the right to access, rectification, and erasure. Your processes must support exercising these rights promptly.",
    date: "2026-06-01",
    author: "Sarah Johnson",
    readTime: "8 min read",
    category: "Legal",
  },
];

export async function generateMetadata({ params }: { params: Promise<{ id: string }> }): Promise<Metadata> {
  const { id } = await params;
  const post = blogPosts.find((p) => p.id === Number(id));
  return {
    title: post ? `${post.title} | Netpick` : "Blog Post Not Found | Netpick",
    description: post ? post.content.slice(0, 160) : "Blog post not found",
  };
}

export default async function BlogDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const post = blogPosts.find((p) => p.id === Number(id));

  if (!post) {
    return (
      <div className="text-center py-20">
        <h1 className="text-3xl font-bold text-white mb-4">
          Post Not Found
        </h1>
        <p className="text-[var(--color-text-secondary)] mb-8">
          The blog post you&apos;re looking for doesn&apos;t exist.
        </p>
        <Link
          href="/blogs"
          className="btn-gradient inline-flex items-center justify-center"
        >
          <span>Back to Blogs</span>
        </Link>
      </div>
    );
  }

  return (
    <div className="py-24">
      <article className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <Link
          href="/blogs"
          className="inline-flex items-center gap-2 text-sm font-medium text-[var(--color-accent)] hover:text-[var(--color-accent-light)] mb-8 transition-colors duration-300"
        >
          <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
          Back to Blogs
        </Link>

        <span className="inline-block px-3 py-1 text-xs font-medium text-[var(--color-accent)] bg-[var(--color-accent)]/10 rounded-full border border-[var(--color-accent)]/20 mb-4">
          {post.category}
        </span>

        <h1 className="text-3xl lg:text-4xl font-bold text-gradient-white mb-4">
          {post.title}
        </h1>

        <div className="flex items-center gap-4 text-sm text-[var(--color-text-muted)] mb-8">
          <span>{post.author}</span>
          <span>·</span>
          <span>{post.date}</span>
          <span>·</span>
          <span>{post.readTime}</span>
        </div>

        <div className="glass-card-lg p-8 md:p-10">
          <div className="space-y-4">
            {post.content.split("\n\n").map((paragraph, i) => (
              <p
                key={i}
                className="text-[var(--color-text-secondary)] leading-relaxed last:mb-0"
              >
                {paragraph}
              </p>
            ))}
          </div>
        </div>
      </article>
    </div>
  );
}
