"use client";

import React, { useRef, useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RevealOnScroll from "./ui/RevealOnScroll";

export default function Hero() {
  const sectionRef = useRef<HTMLElement>(null);
  const mouseRef = useRef({ x: 0.5, y: 0.5 });
  const smoothRef = useRef({ x: 0.5, y: 0.5 });
  const rafRef = useRef<number>(0);
  const [pos, setPos] = useState({ x: 50, y: 50 });

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    const section = sectionRef.current;
    if (!section) return;
    const rect = section.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width;
    const y = (e.clientY - rect.top) / rect.height;
    mouseRef.current = { x: Math.max(0, Math.min(1, x)), y: Math.max(0, Math.min(1, y)) };
  }, []);

  useEffect(() => {
    const lerp = (a: number, b: number, t: number) => a + (b - a) * t;

    const tick = () => {
      const s = smoothRef.current;
      const m = mouseRef.current;
      s.x = lerp(s.x, m.x, 0.035);
      s.y = lerp(s.y, m.y, 0.035);

      const section = sectionRef.current;
      if (section) {
        section.style.setProperty("--cursor-x", `${s.x * 100}%`);
        section.style.setProperty("--cursor-y", `${s.y * 100}%`);
      }

      setPos({ x: s.x * 100, y: s.y * 100 });
      rafRef.current = requestAnimationFrame(tick);
    };

    rafRef.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafRef.current);
  }, []);

  return (
    <section
      ref={sectionRef}
      className="relative overflow-hidden flex items-center"
      onMouseMove={handleMouseMove}
      style={{
        "--cursor-x": "50%",
        "--cursor-y": "50%",
        minHeight: "100vh",
        background: "#000000",
      } as React.CSSProperties}
    >
      {/* Subtle grid pattern */}
      <div
        className="absolute inset-0 pointer-events-none opacity-[0.03]"
        style={{
          backgroundImage: "linear-gradient(rgba(255,255,255,0.5) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.5) 1px, transparent 1px)",
          backgroundSize: "64px 64px",
          maskImage: "radial-gradient(ellipse 70% 60% at 50% 40%, black 0%, transparent 70%)",
          WebkitMaskImage: "radial-gradient(ellipse 70% 60% at 50% 40%, black 0%, transparent 70%)",
        }}
      />

      {/* Cursor-following spotlight gradient */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          background: `radial-gradient(ellipse 60% 50% at ${pos.x * 100}% ${pos.y * 100}%, rgba(34,197,94,0.1) 0%, transparent 55%)`,
        }}
      />

      {/* Bottom blue glow orb — follows cursor */}
      <div
        className="absolute pointer-events-none"
        style={{
          width: "600px",
          height: "400px",
          bottom: "-15%",
          left: `${pos.x * 100}%`,
          transform: "translateX(-50%)",
          background: "radial-gradient(ellipse at center, rgba(34,197,94,0.2) 0%, rgba(34,197,94,0.06) 40%, transparent 70%)",
          filter: "blur(50px)",
        }}
      />

      {/* Top-right soft cyan glow */}
      <div
        className="absolute pointer-events-none"
        style={{
          width: "400px",
          height: "400px",
          top: "5%",
          right: "10%",
          background: "radial-gradient(circle, rgba(34,197,94,0.04) 0%, transparent 60%)",
          filter: "blur(40px)",
        }}
      />

      {/* Left soft glow */}
      <div
        className="absolute pointer-events-none"
        style={{
          width: "350px",
          height: "500px",
          top: "30%",
          left: "5%",
          background: "radial-gradient(circle, rgba(34,197,94,0.05) 0%, transparent 60%)",
          filter: "blur(50px)",
        }}
      />

      {/* Floating accent dots */}
      <div className="absolute inset-0 pointer-events-none">
        {[
          { top: "18%", left: "12%", size: 2, opacity: 0.15, delay: "0s" },
          { top: "28%", right: "15%", size: 3, opacity: 0.1, delay: "-2s" },
          { top: "65%", left: "20%", size: 2, opacity: 0.12, delay: "-4s" },
          { top: "55%", right: "22%", size: 2.5, opacity: 0.08, delay: "-1s" },
          { top: "75%", left: "70%", size: 2, opacity: 0.1, delay: "-3s" },
        ].map((dot, i) => (
          <div
            key={i}
            className="absolute rounded-full animate-float"
            style={{
              top: dot.top,
              left: dot.left,
              right: (dot as { right?: string }).right,
              width: dot.size,
              height: dot.size,
              background: "var(--color-accent)",
              opacity: dot.opacity,
              animationDelay: dot.delay,
              animationDuration: "6s",
            }}
          />
        ))}
      </div>

      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-20 md:py-28 lg:py-36 z-10 w-full">
        <RevealOnScroll>
          <div className="text-center max-w-5xl mx-auto">
            <RevealOnScroll delay={0.1}>
              <div
                className="inline-flex items-center gap-2 px-4 py-2 rounded-full mb-8"
                style={{
                  background: "rgba(34, 197, 94, 0.06)",
                  border: "1px solid rgba(34, 197, 94, 0.12)",
                }}
              >
                <div className="w-1.5 h-1.5 rounded-full" style={{ background: "var(--color-accent)" }} />
                <span className="text-xs font-medium tracking-wide" style={{ color: "var(--color-accent)" }}>
                  Email Intelligence Platform
                </span>
              </div>
            </RevealOnScroll>

            <h1
              className="hero-heading font-bold mb-8 leading-[0.9] tracking-tight"
              style={{
                fontFamily: "var(--font-heading)",
                fontSize: "clamp(52px, 9vw, 110px)",
              }}
            >
              Build full stack
              <br />
              apps on Netpick
            </h1>

            <p
              className="text-lg md:text-xl max-w-xl mx-auto mb-12 leading-relaxed"
              style={{ color: "var(--color-text-secondary)" }}
            >
              The platform for building intelligent email data applications.
              Scrape, analyze, and automate at scale.
            </p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link href="/developers" className="btn-glass inline-flex items-center justify-center text-base">
                Go to docs
              </Link>
              <Link href="/signup" className="btn-gradient group inline-flex items-center justify-center text-base">
                <span className="relative z-10 flex items-center">
                  Get a wallet
                  <svg className="ml-2 w-4 h-4 transition-transform duration-500 ease-out group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                  </svg>
                </span>
              </Link>
            </div>
          </div>
        </RevealOnScroll>
      </div>

      {/* Bottom fade */}
      <div
        className="absolute bottom-0 left-0 right-0 h-32 pointer-events-none"
        style={{ background: "linear-gradient(to top, #000 0%, transparent 100%)" }}
      />
    </section>
  );
}
