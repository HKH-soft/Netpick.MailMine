export interface JwtPayload {
  role?: string;
  scopes?: Array<string | { role?: string }>;
  authorities?: Array<string | { role?: string }>;
  exp?: number;
  sub?: string;
  [key: string]: unknown;
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export function extractRoleFromJwt(token: string): string | null {
  const payload = decodeJwtPayload(token);
  if (!payload) return null;

  if (payload.role) {
    return payload.role;
  }

  if (payload.scopes && Array.isArray(payload.scopes) && payload.scopes.length > 0) {
    if (typeof payload.scopes[0] === 'string') {
      return payload.scopes[0];
    }
    for (const scope of payload.scopes) {
      if (typeof scope === 'object' && scope.role) {
        return scope.role;
      }
    }
  }

  if (payload.authorities && Array.isArray(payload.authorities)) {
    const adminAuth = payload.authorities.find(
      (auth) => typeof auth === 'string' && auth.toLowerCase().includes('admin')
    );
    if (adminAuth) return typeof adminAuth === 'string' ? adminAuth : null;
    if (payload.authorities.length > 0) {
      const first = payload.authorities[0];
      return typeof first === 'string' ? first : null;
    }
  }

  return null;
}
