import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

async function proxyRequest(request: NextRequest) {
  const url = new URL(request.url);
  const path = url.pathname.replace(/^\/api/, '');
  const targetUrl = `${BACKEND_URL}/api${path}${url.search}`;

  // Get the request body if present
  let body: string | undefined;
  if (request.method !== 'GET' && request.method !== 'HEAD') {
    try {
      body = await request.text();
    } catch {
      // No body
    }
  }

  // Forward headers but remove CORS-related ones
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    const lowerKey = key.toLowerCase();
    // Skip headers that shouldn't be forwarded
    if (
      lowerKey !== 'host' &&
      lowerKey !== 'origin' &&
      lowerKey !== 'referer' &&
      !lowerKey.startsWith('sec-') &&
      lowerKey !== 'connection'
    ) {
      headers.set(key, value);
    }
  });

  try {
    const response = await fetch(targetUrl, {
      method: request.method,
      headers,
      body: body || undefined,
    });

    // Get response body
    const responseBody = await response.text();

    // Create response with forwarded headers
    const responseHeaders = new Headers();
    response.headers.forEach((value, key) => {
      const lowerKey = key.toLowerCase();
      // Skip CORS headers from backend - we'll handle CORS ourselves
      if (
        !lowerKey.startsWith('access-control-') &&
        lowerKey !== 'transfer-encoding'
      ) {
        responseHeaders.set(key, value);
      }
    });

    return new NextResponse(responseBody, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders,
    });
  } catch (error) {
    console.error('Proxy error:', error);
    return NextResponse.json(
      { error: 'Failed to connect to backend' },
      { status: 502 }
    );
  }
}

export async function GET(request: NextRequest) {
  return proxyRequest(request);
}

export async function POST(request: NextRequest) {
  return proxyRequest(request);
}

export async function PUT(request: NextRequest) {
  return proxyRequest(request);
}

export async function DELETE(request: NextRequest) {
  return proxyRequest(request);
}

export async function PATCH(request: NextRequest) {
  return proxyRequest(request);
}

export async function OPTIONS() {
   // Handle preflight requests
   const allowedOrigins = process.env.CORS_ALLOWED_ORIGINS?.split(',') || 
                          process.env.NODE_ENV === 'development' 
                            ? ['http://localhost:3000'] 
                            : [];
   
   return new NextResponse(null, {
     status: 204,
     headers: {
       'Access-Control-Allow-Origin': allowedOrigins.length > 0 ? allowedOrigins[0] : 'same-origin',
       'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, PATCH, OPTIONS',
       'Access-Control-Allow-Headers': 'Content-Type, Authorization',
     },
   });
 }
