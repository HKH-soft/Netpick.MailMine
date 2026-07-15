// Mock data service for development mode
// This provides mock data when NEXT_PUBLIC_DEV_MODE=true

export interface MockUser {
  id: string;
  email: string;
  name: string;
  role: string;
}

export interface MockDashboardStats {
  totalUsers: number;
  totalRevenue: number;
  totalOrders: number;
  conversionRate: number;
}

export interface MockCampaign {
  id: string;
  name: string;
  status: string;
  budget: number;
  spent: number;
  startDate: string;
  endDate: string;
}

export interface MockContact {
  id: string;
  name: string;
  email: string;
  phone: string;
  company: string;
  tags: string[];
}

export interface MockDeal {
  id: string;
  title: string;
  value: number;
  stage: string;
  probability: number;
  closeDate: string;
}

// Mock user data
export const mockUser: MockUser = {
  id: 'dev-user-1',
  email: 'dev@example.com',
  name: 'Development User',
  role: 'SUPER_ADMIN',
};

// Mock dashboard stats
export const mockDashboardStats: MockDashboardStats = {
  totalUsers: 1250,
  totalRevenue: 45230,
  totalOrders: 856,
  conversionRate: 3.2,
};

// Mock campaigns
export const mockCampaigns: MockCampaign[] = [
  {
    id: '1',
    name: 'Summer Sale Campaign',
    status: 'ACTIVE',
    budget: 5000,
    spent: 3200,
    startDate: '2024-06-01',
    endDate: '2024-08-31',
  },
  {
    id: '2',
    name: 'Email Newsletter Q3',
    status: 'PAUSED',
    budget: 2000,
    spent: 1500,
    startDate: '2024-07-01',
    endDate: '2024-09-30',
  },
  {
    id: '3',
    name: 'Holiday Special',
    status: 'DRAFT',
    budget: 10000,
    spent: 0,
    startDate: '2024-11-01',
    endDate: '2024-12-31',
  },
];

// Mock contacts
export const mockContacts: MockContact[] = [
  {
    id: '1',
    name: 'John Doe',
    email: 'john@example.com',
    phone: '+1 (555) 123-4567',
    company: 'Acme Corp',
    tags: ['customer', 'vip'],
  },
  {
    id: '2',
    name: 'Jane Smith',
    email: 'jane@example.com',
    phone: '+1 (555) 987-6543',
    company: 'Tech Solutions',
    tags: ['lead', 'interested'],
  },
  {
    id: '3',
    name: 'Bob Wilson',
    email: 'bob@example.com',
    phone: '+1 (555) 456-7890',
    company: 'Global Inc',
    tags: ['customer'],
  },
];

// Mock deals
export const mockDeals: MockDeal[] = [
  {
    id: '1',
    title: 'Enterprise Software Deal',
    value: 50000,
    stage: 'NEGOTIATION',
    probability: 75,
    closeDate: '2024-08-15',
  },
  {
    id: '2',
    title: 'Small Business Package',
    value: 5000,
    stage: 'PROPOSAL',
    probability: 50,
    closeDate: '2024-07-30',
  },
  {
    id: '3',
    title: 'Startup License',
    value: 12000,
    stage: 'QUALIFICATION',
    probability: 25,
    closeDate: '2024-09-15',
  },
];

// Mock authentication response
export const mockAuthResponse = {
  access_token: 'dev-mock-token-' + btoa(JSON.stringify({
    sub: mockUser.id,
    email: mockUser.email,
    name: mockUser.name,
    role: mockUser.role,
    exp: Math.floor(Date.now() / 1000) + 3600, // 1 hour from now
  })),
  refresh_token: 'dev-mock-refresh-token',
  expires_in: 3600,
  token_type: 'Bearer',
};

// Helper to generate mock JWT-like token
export function generateMockToken(): string {
  return mockAuthResponse.access_token;
}

// Helper to create mock paginated response
export function createMockPage<T>(data: T[], page = 0, size = 10): { content: T[]; totalElements: number; totalPages: number } {
  return {
    content: data.slice(page * size, (page + 1) * size),
    totalElements: data.length,
    totalPages: Math.ceil(data.length / size),
  };
}