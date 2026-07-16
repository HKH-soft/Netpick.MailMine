import { Metadata } from "next";
import Link from "next/link";
import RevealOnScroll from "@/components/landing/ui/RevealOnScroll";

export const metadata: Metadata = {
  title: "Blog | Netpick",
  description: "Latest insights, tutorials, and updates from the Netpick team.",
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
  const featured = blogPosts[0];
  const rest = blogPosts.slice(1);

  return (
    <div>
      {/* Hero */}
      <section className="relative py-24 md:py-32 overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-brand-500/10 via-black to-black" />
        <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <div className="max-w-3xl">
              <span className="inline-block text-sm font-semibold tracking-widest uppercase text-brand-400 mb-4">
                Blog
              </span>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-6">
                Insights & updates
              </h1>
              <p className="text-lg text-zinc-400 leading-relaxed">
                Tutorials, product updates, and industry insights from the Netpick team.
              </p>
            </div>
          </RevealOnScroll>
        </div>
      </section>

      {/* Featured Post */}
      <section className="py-16 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <RevealOnScroll>
            <Link
              href={`/blogs/${featured.id}`}
              className="group block rounded-3xl border border-zinc-800 bg-zinc-900/30 p-8 md:p-12 transition-all duration-300 hover:border-brand-500/30"
            >
              <span className="inline-block px-3 py-1 text-xs font-medium text-brand-400 bg-brand-500/10 rounded-full mb-4">
                Featured
              </span>
              <h2 className="text-2xl md:text-3xl font-bold text-white mb-4 group-hover:text-brand-400 transition-colors">
                {featured.title}
              </h2>
              <p className="text-zinc-400 text-lg mb-6 max-w-3xl">
                {featured.excerpt}
              </p>
              <div className="flex items-center gap-4 text-sm text-zinc-500">
                <span>{featured.author}</span>
                <span>·</span>
                <span>{featured.date}</span>
                <span>·</span>
                <span>{featured.readTime}</span>
              </div>
            </Link>
          </RevealOnScroll>
        </div>
      </section>

      {/* Post Grid */}
      <section className="py-16 border-t border-zinc-800/50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <h2 className="text-2xl font-bold text-white mb-8">Latest Articles</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {rest.map((post, i) => (
              <RevealOnScroll key={post.id} delay={i * 0.1}>
                <Link
                  href={`/blogs/${post.id}`}
                  className="group block rounded-2xl border border-zinc-800 bg-zinc-900/30 p-6 h-full transition-all duration-300 hover:border-brand-500/30"
                >
                  <span className="inline-block px-2.5 py-1 text-xs font-medium text-brand-400 bg-brand-500/10 rounded-full mb-3">
                    {post.category}
                  </span>
                  <h3 className="text-lg font-semibold text-white mb-2 group-hover:text-brand-400 transition-colors">
                    {post.title}
                  </h3>
                  <p className="text-zinc-400 text-sm leading-relaxed mb-4">
                    {post.excerpt}
                  </p>
                  <div className="flex items-center justify-between text-xs text-zinc-500">
                    <span>{post.author}</span>
                    <span>{post.readTime}</span>
                  </div>
                </Link>
              </RevealOnScroll>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
