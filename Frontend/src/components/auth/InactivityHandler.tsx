"use client";

import { useEffect, useRef, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import AuthService from '@/services/authService';
import { useToast } from '@/context/ToastContext';

const INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 minutes

export default function InactivityHandler() {
    const router = useRouter();
    const { addToast } = useToast();
    const timerRef = useRef<NodeJS.Timeout | null>(null);

    const logout = useCallback(() => {
        // Check if we have any tokens (access or refresh)
        if (AuthService.getToken() || AuthService.getRefreshToken()) {
            AuthService.logout();
            addToast('info', 'Session Expired', 'You have been logged out due to inactivity.');
            router.push('/signin');
        }
    }, [router, addToast]);

    const resetTimer = useCallback(() => {
        if (timerRef.current) {
            clearTimeout(timerRef.current);
        }
        // Only set timer if user is logged in (has tokens)
        if (AuthService.getToken() || AuthService.getRefreshToken()) {
            timerRef.current = setTimeout(logout, INACTIVITY_TIMEOUT);
        }
    }, [logout]);

    useEffect(() => {
        // Initial setup
        resetTimer();

        // Event listeners
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];

        let isThrottled = false;
        const throttledHandleActivity = () => {
            if (!isThrottled) {
                resetTimer();
                isThrottled = true;
                setTimeout(() => { isThrottled = false; }, 1000);
            }
        };

        events.forEach(event => {
            window.addEventListener(event, throttledHandleActivity);
        });

        return () => {
            if (timerRef.current) {
                clearTimeout(timerRef.current);
            }
            events.forEach(event => {
                window.removeEventListener(event, throttledHandleActivity);
            });
        };
    }, [resetTimer]);

    return null;
}
