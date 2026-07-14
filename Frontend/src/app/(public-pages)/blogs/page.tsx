import { Metadata } from "next";
import React from "react";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Blogs | Netpick",
  description: "Latest insights and updates from Netpick",
};

const blogPosts = [
  {
    id: 1,
    title: "Getting Started with Email Scraping: A Complete Guide",
    excerpt: "Learn the fundamentals of email scraping and how to use Netpick to extract valuable contact data ethically and efficiently.",
    date: "2026-07-10",
    author: "Sarah Johnson",
    readTime: "5 min read",
    category: "Guide",
  },
  {
    id: 2,
    title: "5 Best Practices for Email Data Validation",
    excerpt: "Ensure your scraped email data is accurate and actionable with these essential validation techniques.",
    date: "2026-07-05",
    author: "Michael Chen",
    readTime: "4 min read",
    category: "Tips",
  },
  {
    id: 3,
    title: "New Feature: Advanced Pipeline Automation",
    excerpt: "We've just launched our most powerful feature yet - automated pipelines that can process thousands of contacts per hour.",
    date: "2026-06-28",
    author: "Emily Rodriguez",
    readTime: "3 min read",
    category: "Updates",
  },
  {
    id: 4,
    title: "Understanding Email Verification Standards",
    excerpt: "A deep dive into the technical aspects of email verification and why it matters for your campaigns.",
    date: "2026-06-20",
    author: "David Kim",
    readTime: "6 min read",
    category: "Technical",
  },
  {
    id: 5,
    title: "How to Scale Your Email Outreach Campaigns",
    excerpt: "Learn how to grow your outreach efforts while maintaining deliverability and engagement rates.",
    date: "2026-06-15",
    author: "Lisa Anderson",
    readTime: "7 min read",
    category: "Marketing",
  },
  {
    id: 6,
    title: "GDPR Compliance in Email Scraping",
    excerpt: "Everything you need to know about staying compliant with data privacy regulations in your scraping activities.",
    date: "2026-06-01",
    author: "Sarah Johnson",
    readTime: "8 min read",
    category: "Legal",
  },
];

export default function BlogsPage() {
  return (
    <div>

      {/* Featured Post */}
      <section className="mb-12">
        <div className="rounded-2xl border border-gray-200 bg-white overflow-hidden dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="p-8">
            <span className="inline-block px-3 py-1 text-xs font-medium text-brand-500 bg-brand-100 rounded-full mb-4">
              Featured
            </span>
            <h2 className="text-3xl font-bold text-gray-800 dark:text-white mb-4">
              {blogPosts[0].title}
            </h2>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              {blogPosts[0].excerpt}
            </p>
            <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400 mb-6">
              <span>{blogPosts[0].author}</span>
              <span>•</span>
              <span>{blogPosts[0].date}</span>
              <span>•</span>
              <span>{blogPosts[0].readTime}</span>
            </div>
            <Link
              href={`/blogs/${blogPosts[0].id}`}
              className="inline-flex items-center justify-center px-6 py-3 text-base font-medium text-white bg-brand-500 rounded-lg hover:bg-brand-600 transition-colors"
            >
              Read Article
            </Link>
          </div>
        </div>
      </section>

      {/* Blog Grid */}
      <section>
        <h3 className="text-2xl font-bold text-gray-800 dark:text-white mb-6">
          Latest Articles
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {blogPosts.slice(1).map((post) => (
            <article
              key={post.id}
              className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03] flex flex-col"
            >
              <span className="inline-block px-2 py-1 text-xs font-medium text-brand-500 bg-brand-100 rounded-full mb-3 self-start">
                {post.category}
              </span>
              <h4 className="text-lg font-semibold text-gray-800 dark:text-white mb-2">
                {post.title}
              </h4>
              <p className="text-gray-600 dark:text-gray-400 text-sm mb-4 flex-grow">
                {post.excerpt}
              </p>
              <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
                <span>{post.author}</span>
                <span>{post.date}</span>
              </div>
              <Link
                href={`/blogs/${post.id}`}
                className="mt-4 text-sm font-medium text-brand-500 hover:text-brand-600"
              >
                Read more →
              </Link>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}