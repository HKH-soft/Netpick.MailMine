"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import AuthService from "@/services/authService";

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles
}) => {
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthorized, setIsAuthorized] = useState(false);
  const { isAuthenticated } = useAuth();
  const [hasCheckedAuth, setHasCheckedAuth] = useState(false);

  useEffect(() => {
    // Skip if we've already checked auth to prevent multiple executions
    if (hasCheckedAuth) {
      return;
    }

    const checkAuthorization = () => {
      // Validate token and check if user is authenticated
      const isValid = AuthService.isAuthenticated();

      // If not authenticated, redirect to login
      if (!isValid) {
        window.location.href = "/signin";
        setIsLoading(false);
        setHasCheckedAuth(true);
        return;
      }

      // If no specific roles required, just check authentication
      if (!allowedRoles || allowedRoles.length === 0) {
        setIsAuthorized(true);
        setIsLoading(false);
        setHasCheckedAuth(true);
        return;
      }

      // Check role-based access
      const token = AuthService.getToken();
      if (token) {
        try {
          // Parse JWT token
          const base64Url = token.split('.')[1];
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
          const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          }).join(''));

          const payload = JSON.parse(jsonPayload);

          // Check for role in different possible locations
          let userRole = null;

          // Check direct role property
          if (payload.role) {
            userRole = payload.role;
          }
          // Check scopes array - this is where SUPER_ADMIN is located based on your logs
          else if (payload.scopes && Array.isArray(payload.scopes) && payload.scopes.length > 0) {
            // Handle the case where scopes is an array with the role directly
            // Based on your logs, it seems like scopes[0] is "SUPER_ADMIN"
            if (typeof payload.scopes[0] === 'string') {
              userRole = payload.scopes[0];
            }
            // If scopes is an array of objects, find the one with role property
            else if (typeof payload.scopes[0] === 'object') {
              // Look for a role property in any of the scope objects
              for (const scope of payload.scopes) {
                if (scope.role) {
                  userRole = scope.role;
                  break;
                }
              }
            }
          }
          // Check authorities array (another common pattern)
          else if (payload.authorities && Array.isArray(payload.authorities)) {
            userRole = payload.authorities.find((auth: string | { [key: string]: unknown }) =>
              typeof auth === 'string' && auth.toLowerCase().includes('admin')
            ) || payload.authorities[0];
          }

          // Deny access by default when no role is found
          if (!userRole) {
            setIsAuthorized(false);
            setIsLoading(false);
            setHasCheckedAuth(true);
            // Redirect to home page or unauthorized page
            window.location.href = "/signin";
            return;
          }

          // Check if user role is in allowed roles or if it's an admin role
          if (allowedRoles.includes(userRole) || userRole.toString().toUpperCase().includes('ADMIN')) {
            setIsAuthorized(true);
          } else {
            // Redirect to home page
            window.location.href = "/";
          }
        } catch (error) {
          console.error("Error parsing token", error);
          // Deny access when there's an error parsing the token

          setIsAuthorized(false);
          setIsLoading(false);
          setHasCheckedAuth(true);
          // Redirect to home page or unauthorized page
          window.location.href = "/signin";
          return;
        }
      } else {
        window.location.href = "/signin";
      }

      setIsLoading(false);
      setHasCheckedAuth(true);
    };

    checkAuthorization();
  }, [isAuthenticated, allowedRoles, hasCheckedAuth]);

  if (isLoading) {
    return <div className="flex items-center justify-center h-screen">Loading...</div>;
  }

  if (!isAuthorized) {
    return <div className="flex items-center justify-center h-screen">Access Denied</div>;
  }

  return <>{children}</>;
};

export default ProtectedRoute;