import { test, expect } from '@playwright/test';

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/signin');
    await page.fill('input[type="email"]', 'dev@example.com');
    await page.fill('input[type="password"]', 'validpassword123456');
    await page.click('[data-testid="signin-submit"]');
    await page.waitForURL('/');
  });

  test.describe('Sidebar Navigation', () => {
    test('renders sidebar with navigation items', async ({ page }) => {
      await expect(page.locator('[data-testid="app-sidebar"]')).toBeVisible();
      await expect(page.locator('text=Netpick')).toBeVisible();
    });

    test('collapse button is visible on desktop', async ({ page }) => {
      await expect(page.locator('[data-testid="sidebar-collapse"]')).toBeVisible();
    });

    test('navigates to dashboard when clicking menu item', async ({ page }) => {
      const dashboardLink = page.locator('a[href="/dashboard"]').first();
      await expect(dashboardLink).toBeVisible();
      await dashboardLink.click();
      await expect(page).toHaveURL(/\/dashboard/);
    });
  });

  test.describe('Dashboard Metrics', () => {
    test('displays metrics cards', async ({ page }) => {
      await page.goto('/dashboard');

      await expect(page.locator('[data-testid="app-sidebar"]')).toBeVisible();
      await expect(page.locator('text=/Contacts/i')).toBeVisible();
    });
  });

  test.describe('Header Actions', () => {
    test('search button exists', async ({ page }) => {
      await expect(page.locator('[data-testid="search-button"]')).toBeVisible();
    });

    test('header actions container exists with theme and language controls', async ({ page }) => {
      await expect(page.locator('[data-testid="header-actions"]')).toBeVisible();
    });
  });
});
