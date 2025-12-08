# MailMine Frontend

A modern, responsive Next.js dashboard for the MailMine web scraping and lead generation platform.

## 🚀 Overview

The MailMine frontend is a sophisticated admin dashboard built with Next.js 15, React 19, and TypeScript. It provides an intuitive interface for managing web scraping jobs, viewing analytics, and handling collected data with real-time updates and beautiful visualizations.

## 🛠️ Tech Stack

- **Framework**: Next.js 15.2.3 (App Router)
- **Language**: TypeScript 5.x
- **UI Library**: React 19.0
- **Styling**: Tailwind CSS 4.0
- **Charts**: ApexCharts with React wrapper
- **Forms**: Formik with Yup validation
- **Calendars**: FullCalendar
- **Maps**: React JVectorMap
- **Drag & Drop**: React DnD
- **Carousel**: Swiper
- **Icons**: Heroicons & Custom SVGs
- **Date Picker**: Flatpickr

## 📁 Project Structure

```
Frontend/
├── src/
│   ├── app/                    # Next.js 15 App Router
│   │   ├── (auth)/             # Authentication routes
│   │   │   ├── signin/         # Sign in page
│   │   │   └── signup/         # Sign up page
│   │   ├── (dashboard)/        # Protected dashboard routes
│   │   │   ├── page.tsx        # Dashboard home
│   │   │   ├── scrape/         # Scraping management
│   │   │   ├── emails/         # Email management
│   │   │   ├── analytics/      # Analytics & reports
│   │   │   └── settings/       # User settings
│   │   ├── layout.tsx          # Root layout
│   │   ├── globals.css         # Global styles
│   │   └── providers.tsx       # Context providers
│   ├── components/             # Reusable React components
│   │   ├── Charts/             # Chart components
│   │   ├── Dashboard/          # Dashboard widgets
│   │   ├── Forms/              # Form components
│   │   ├── Header/             # Header & navigation
│   │   ├── Sidebar/            # Sidebar navigation
│   │   ├── Tables/             # Data tables
│   │   └── common/             # Common UI components
│   ├── types/                  # TypeScript type definitions
│   │   ├── scrape.ts           # Scraping types
│   │   ├── user.ts             # User types
│   │   └── index.ts            # Common types
│   ├── lib/                    # Utility libraries
│   │   ├── api.ts              # API client
│   │   ├── auth.ts             # Auth utilities
│   │   └── utils.ts            # Helper functions
│   └── hooks/                  # Custom React hooks
│       ├── useAuth.ts          # Authentication hook
│       └── useScrape.ts        # Scraping data hook
├── public/                     # Static assets
│   ├── images/                 # Images & logos
│   │   ├── logo/               # Logo variations
│   │   ├── user/               # User avatars
│   │   └── product/            # Product images
│   └── favicon.ico             # Favicon
├── .eslintrc.json             # ESLint configuration
├── next.config.ts             # Next.js configuration
├── tailwind.config.js         # Tailwind CSS configuration
├── tsconfig.json              # TypeScript configuration
├── package.json               # npm dependencies
└── README.md                  # This file
```

## 🏁 Getting Started

### Prerequisites

- Node.js 18.x or higher
- npm 9.x or higher (or yarn/pnpm)
- MailMine Backend running (see [Backend README](../Backend/README.md))

### Installation

1. **Clone the repository** (if not already done)
   ```bash
   git clone https://github.com/HKH-soft/Netpick.MailMine.git
   cd Netpick.MailMine/Frontend
   ```

2. **Install Dependencies**
   ```bash
   npm install
   # or
   yarn install
   # or
   pnpm install
   ```

3. **Configure Environment Variables**
   
   Create a `.env.local` file in the Frontend directory:
   ```env
   # Backend API URL
   NEXT_PUBLIC_API_URL=http://localhost:8080/api
   
   # App Configuration
   NEXT_PUBLIC_APP_NAME=MailMine
   NEXT_PUBLIC_APP_VERSION=1.0.0
   
   # Optional: Analytics
   NEXT_PUBLIC_GA_ID=your-google-analytics-id
   ```

4. **Run Development Server**
   ```bash
   npm run dev
   # or
   yarn dev
   # or
   pnpm dev
   ```

   Open [http://localhost:3000](http://localhost:3000) in your browser.

5. **Build for Production**
   ```bash
   npm run build
   npm run start
   ```

## 🎨 Features

### Dashboard
- **Real-time Analytics**: View scraping statistics, success rates, and trends
- **Job Monitoring**: Track active, completed, and failed scraping jobs
- **Quick Actions**: Start new scrapes, export data, manage settings
- **Responsive Design**: Fully optimized for desktop, tablet, and mobile

### Scraping Management
- **Job Creation**: Create and configure web scraping jobs
- **Query Builder**: AI-powered search query generation
- **Progress Tracking**: Real-time job status and progress indicators
- **Results Viewing**: Browse and filter scraped data

### Email Management
- **Email List**: View all collected email addresses
- **Filtering & Search**: Advanced filtering by domain, date, source
- **Export Options**: Export to CSV, Excel, or JSON
- **Bulk Actions**: Delete, tag, or categorize multiple emails

### Analytics & Reports
- **Interactive Charts**: Visualize scraping performance over time
- **Success Metrics**: Track success rates and error patterns
- **Domain Analytics**: Analyze performance by domain
- **Export Reports**: Generate and download reports

### User Authentication
- **Secure Login**: JWT-based authentication
- **Remember Me**: Persistent login sessions
- **Password Reset**: Email-based password recovery
- **Profile Management**: Update user details and preferences

## 🧪 Development

### Running Linter
```bash
npm run lint
# or with auto-fix
npm run lint --fix
```

### Type Checking
```bash
# TypeScript compiler check
npx tsc --noEmit
```

### Code Formatting
```bash
# Using Prettier (if configured)
npm run format
```

### Environment-Specific Builds
```bash
# Development build
npm run dev

# Production build
npm run build

# Production server
npm run start
```

## 📱 Responsive Design

The dashboard is fully responsive and optimized for:
- **Desktop**: 1920px and above (full dashboard view)
- **Laptop**: 1366px - 1920px (standard laptop screens)
- **Tablet**: 768px - 1366px (iPad and similar)
- **Mobile**: 320px - 768px (smartphones)

Tailwind CSS breakpoints:
- `sm`: 640px
- `md`: 768px
- `lg`: 1024px
- `xl`: 1280px
- `2xl`: 1536px

## 🎨 Styling

### Tailwind CSS
The project uses Tailwind CSS 4.0 with custom configuration:

```javascript
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: '#3C50E0',
        secondary: '#80CAEE',
        // ... custom colors
      },
    },
  },
}
```

### Custom Components
Reusable components are located in `src/components/`:
- All components use TypeScript for type safety
- Styled with Tailwind utility classes
- Support dark mode (if implemented)

## 🔧 Configuration

### Next.js Config
Key configurations in `next.config.ts`:

```typescript
const nextConfig = {
  reactStrictMode: true,
  images: {
    domains: ['localhost'], // Add allowed image domains
  },
  // ... other configs
}
```

### ESLint Config
The project uses Next.js recommended ESLint configuration with custom rules:

```json
{
  "extends": ["next/core-web-vitals"],
  "rules": {
    // Custom rules
  }
}
```

## 🌐 API Integration

### API Client
The frontend communicates with the backend via a centralized API client:

```typescript
// src/lib/api.ts
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Example API Usage
```typescript
// Fetch scraping jobs
const response = await api.get('/scrape/jobs');
const jobs = response.data;

// Start a new scrape
await api.post('/scrape/start', {
  url: 'https://example.com',
  depth: 2,
});
```

## 🧩 Component Library

### Common Components
- **Button**: Primary, secondary, outline variants
- **Input**: Text, email, password inputs with validation
- **Card**: Container for content sections
- **Modal**: Popup dialogs and confirmations
- **Dropdown**: Select menus and action dropdowns
- **Alert**: Success, error, warning, info notifications
- **Loader**: Loading spinners and skeletons

### Chart Components
- **LineChart**: Time-series data visualization
- **BarChart**: Comparative data visualization
- **PieChart**: Proportional data visualization
- **AreaChart**: Trend visualization with filled areas

### Form Components
- **FormInput**: Input with label and validation
- **FormSelect**: Dropdown with validation
- **FormCheckbox**: Checkbox with label
- **FormTextarea**: Multi-line text input

## 🔒 Authentication Flow

1. **Login**: User enters credentials
2. **Token Storage**: JWT token stored in localStorage
3. **Auto-redirect**: Redirects to dashboard on successful login
4. **Protected Routes**: Middleware checks authentication
5. **Token Refresh**: Automatic token refresh before expiration
6. **Logout**: Clears token and redirects to login

## 📦 Building & Deployment

### Production Build
```bash
# Create optimized production build
npm run build

# Test production build locally
npm run start
```

### Deployment Options

#### Vercel (Recommended for Next.js)
```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel
```

#### Docker
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

Build and run:
```bash
docker build -t mailmine-frontend .
docker run -p 3000:3000 mailmine-frontend
```

#### Static Export (Optional)
For static hosting:
```bash
# Update next.config.ts
output: 'export'

# Build static files
npm run build
# Output in 'out' directory
```

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Change port (Next.js default is 3000)
PORT=3001 npm run dev
```

### Module Not Found Errors
```bash
# Clear cache and reinstall
rm -rf node_modules .next
npm install
```

### TypeScript Errors
```bash
# Regenerate types
npx next build --no-lint
```

### Slow Development Server
```bash
# Clear Next.js cache
rm -rf .next
npm run dev
```

## 🧰 Useful Scripts

```json
{
  "dev": "next dev",                    // Start dev server
  "build": "next build",                // Build for production
  "start": "next start",                // Start production server
  "lint": "next lint",                  // Run ESLint
  "type-check": "tsc --noEmit"         // Check TypeScript types
}
```

## 📊 Performance Optimization

- **Code Splitting**: Automatic route-based splitting
- **Image Optimization**: Next.js Image component
- **Font Optimization**: Next.js Font optimization
- **Lazy Loading**: Components loaded on demand
- **Caching**: API responses cached when appropriate
- **Memoization**: React.memo for expensive components

## ♿ Accessibility

The dashboard follows WCAG 2.1 guidelines:
- Semantic HTML elements
- ARIA labels where needed
- Keyboard navigation support
- Screen reader friendly
- Sufficient color contrast

## 🤝 Contributing

When contributing to the frontend:
1. Follow the existing code style
2. Use TypeScript for type safety
3. Test responsive design on multiple devices
4. Ensure ESLint passes without errors
5. Update documentation as needed

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

For frontend-specific issues, please create an issue on GitHub:
[https://github.com/HKH-soft/Netpick.MailMine/issues](https://github.com/HKH-soft/Netpick.MailMine/issues)

## 🔗 Related Documentation

- [Main Project README](../README.md)
- [Backend README](../Backend/README.md)
- [Next.js Documentation](https://nextjs.org/docs)
- [React Documentation](https://react.dev)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [TypeScript Documentation](https://www.typescriptlang.org/docs)
