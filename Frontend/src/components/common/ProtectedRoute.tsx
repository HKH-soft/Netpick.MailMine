"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import AuthService from "@/services/authService";
import { extractRoleFromJwt } from "@/utils/jwt";
import { useTranslation } from "react-i18next";

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

// Check if dev mode is enabled (mock data, no auth required)
const isDevMode = process.env.NEXT_PUBLIC_DEV_MODE === 'true';

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles
}) => {
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthorized, setIsAuthorized] = useState(false);
  const { isAuthenticated } = useAuth();
  const [hasCheckedAuth, setHasCheckedAuth] = useState(false);
  const router = useRouter();
  const { t } = useTranslation('common');

  useEffect(() => {
    if (hasCheckedAuth) {
      return;
    }

    const checkAuthorization = () => {
      if (isDevMode) {
        setIsAuthorized(true);
        setIsLoading(false);
        setHasCheckedAuth(true);
        return;
      }

      const isValid = AuthService.isAuthenticated();

      if (!isValid) {
        router.push("/signin");
        setIsLoading(false);
        setHasCheckedAuth(true);
        return;
      }

      if (!allowedRoles || allowedRoles.length === 0) {
        setIsAuthorized(true);
        setIsLoading(false);
        setHasCheckedAuth(true);
        return;
      }

      const token = AuthService.getToken();
      if (token) {
        const userRole = extractRoleFromJwt(token);

        if (!userRole) {
          setIsAuthorized(false);
          setIsLoading(false);
          setHasCheckedAuth(true);
          router.push("/signin");
          return;
        }

        if (allowedRoles.includes(userRole)) {
          setIsAuthorized(true);
        } else {
          router.push("/");
        }
      } else {
        router.push("/signin");
      }

      setIsLoading(false);
      setHasCheckedAuth(true);
    };

    checkAuthorization();
  }, [isAuthenticated, allowedRoles, hasCheckedAuth, router]);

  if (isLoading) {
    return <div className="flex items-center justify-center h-screen">{t('common.loading', { defaultValue: 'Loadingâ€¦' })}</div>;
  }

  if (!isAuthorized) {
    return <div className="flex items-center justify-center h-screen">{t('common.accessDenied', { defaultValue: 'Access Denied' })}</div>;
  }

  return <>{children}</>;
};

export default ProtectedRoute;



