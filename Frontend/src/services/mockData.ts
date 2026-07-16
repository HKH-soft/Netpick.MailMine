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

export interface MockScrapeJob {
  id: string;
  link: string;
  attempt: number;
  beenScraped: boolean;
  scrapeFailed: boolean;
  description: string;
  createdAt: string;
  updatedAt: string;
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

// Mock contacts (matching Contact interface: emails: string[])
export const mockContacts: { id: string; emails: string[]; createdAt: string; updatedAt: string }[] = [
  {
    id: '1',
    emails: ['john@example.com', 'john.doe@company.com'],
    createdAt: '2024-06-15T10:30:00Z',
    updatedAt: '2024-06-15T10:30:00Z',
  },
  {
    id: '2',
    emails: ['jane@example.com'],
    createdAt: '2024-06-14T14:20:00Z',
    updatedAt: '2024-06-14T14:20:00Z',
  },
  {
    id: '3',
    emails: ['bob@example.com', 'bob.wilson@startup.io', 'bob.wilson@personal.com'],
    createdAt: '2024-06-13T09:15:00Z',
    updatedAt: '2024-06-13T09:15:00Z',
  },
];

// Mock scrape jobs
export const mockScrapeJobs: MockScrapeJob[] = [
  {
    id: '1',
    link: 'https://example.com/page1',
    attempt: 1,
    beenScraped: true,
    scrapeFailed: false,
    description: 'Scraped contact data from example.com',
    createdAt: '2024-06-15T10:30:00Z',
    updatedAt: '2024-06-15T10:35:00Z',
  },
  {
    id: '2',
    link: 'https://example.org/page2',
    attempt: 2,
    beenScraped: false,
    scrapeFailed: true,
    description: 'Failed to scrape - connection timeout',
    createdAt: '2024-06-14T14:20:00Z',
    updatedAt: '2024-06-14T14:25:00Z',
  },
  {
    id: '3',
    link: 'https://test.com/page3',
    attempt: 1,
    beenScraped: false,
    scrapeFailed: false,
    description: 'Pending scrape job',
    createdAt: '2024-06-13T09:15:00Z',
    updatedAt: '2024-06-13T09:15:00Z',
  },
];

// Mock scrape data
export const mockScrapeData = [
  {
    id: '1',
    fileName: 'scraped_data_1.json',
    attemptNumber: 1,
    parsed: true,
    createdAt: '2024-06-15T10:30:00Z',
    updatedAt: '2024-06-15T10:35:00Z',
  },
  {
    id: '2',
    fileName: 'scraped_data_2.json',
    attemptNumber: 2,
    parsed: false,
    createdAt: '2024-06-14T14:20:00Z',
    updatedAt: '2024-06-14T14:25:00Z',
  },
];

// Mock email messages
export const mockEmailMessages = [
  {
    id: '1',
    messageId: '<msg-001@example.com>',
    threadId: 'thread-1',
    senderEmail: 'john.doe@company.com',
    senderName: 'John Doe',
    recipients: ['admin@netpick.com'],
    subject: 'Inquiry about product pricing',
    bodyText: 'Hello, I would like to know more about your pricing options.',
    receivedAt: '2024-06-15T10:30:00Z',
    isRead: false,
    isAnswered: false,
    isFlagged: false,
    hasAttachments: false,
    mailboxFolder: 'INBOX',
    status: 'INBOX' as const,
    tags: [{ id: '1', name: 'Sales', category: 'INBOX', colorHex: '#3b82f6' }],
  },
  {
    id: '2',
    messageId: '<msg-002@example.com>',
    threadId: 'thread-2',
    senderEmail: 'jane.smith@example.org',
    senderName: 'Jane Smith',
    recipients: ['admin@netpick.com'],
    subject: 'Support request - account access',
    bodyText: 'I cannot access my account. Please help.',
    receivedAt: '2024-06-14T14:20:00Z',
    isRead: true,
    isAnswered: true,
    isFlagged: false,
    hasAttachments: true,
    mailboxFolder: 'INBOX',
    status: 'ASSIGNED' as const,
    tags: [{ id: '2', name: 'Support', category: 'ASSIGNED', colorHex: '#10b981' }],
  },
  {
    id: '3',
    messageId: '<msg-003@example.com>',
    threadId: 'thread-3',
    senderEmail: 'sales@leadcompany.com',
    senderName: 'Sales Lead',
    recipients: ['admin@netpick.com'],
    subject: 'Partnership opportunity',
    bodyText: 'We would like to discuss partnership opportunities.',
    receivedAt: '2024-06-13T09:15:00Z',
    isRead: false,
    isAnswered: false,
    isFlagged: true,
    hasAttachments: false,
    mailboxFolder: 'INBOX',
    status: 'INBOX' as const,
    tags: [{ id: '3', name: 'Lead', category: 'INBOX', colorHex: '#f59e0b' }],
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
export function createMockPage<T>(data: T[], page = 0, size = 10): { content: T[]; totalPages: number; totalElements: number; currentPage: number; pageSize: number; numberOfElements: number; hasNext: boolean; hasPrevious: boolean; isFirst: boolean; isLast: boolean } {
  const content = data.slice(page * size, (page + 1) * size);
  const totalPages = Math.ceil(data.length / size);
  return {
    content,
    totalPages,
    totalElements: data.length,
    currentPage: page + 1,
    pageSize: size,
    numberOfElements: content.length,
    hasNext: page + 1 < totalPages,
    hasPrevious: page > 0,
    isFirst: page === 0,
    isLast: page + 1 >= totalPages,
  };
}