import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test.describe('Sign In Page', () => {
    test('loads signin page correctly', async ({ page }) => {
      await page.goto('/signin');

      await expect(page).toHaveTitle(/Netpick/i);
      await expect(page.locator('h1')).toContainText('Sign In');
      await expect(page.locator('input[type="email"]')).toBeVisible();
      await expect(page.locator('input[type="password"]')).toBeVisible();
      await expect(page.locator('[data-testid="signin-submit"]')).toBeVisible();
    });

    test('shows validation errors for invalid email', async ({ page }) => {
      await page.goto('/signin');

      await page.fill('input[type="email"]', 'invalid-email');
      await page.fill('input[type="password"]', 'validpassword123');
      await page.click('[data-testid="signin-submit"]');

      await expect(page.locator('.text-red-500').first()).toBeVisible();
    });

    test('shows validation error for short password', async ({ page }) => {
      await page.goto('/signin');

      await page.fill('input[type="email"]', 'test@example.com');
      await page.fill('input[type="password"]', 'short');
      await page.click('[data-testid="signin-submit"]');

      await expect(page.locator('.text-red-500').first()).toBeVisible();
    });

    test('toggles password visibility', async ({ page }) => {
      await page.goto('/signin');

      const passwordInput = page.locator('input[type="password"]');
      await expect(passwordInput).toHaveAttribute('type', 'password');

      await page.click('[data-testid="password-toggle"]');
      await expect(passwordInput).toHaveAttribute('type', 'text');

      await page.click('[data-testid="password-toggle"]');
      await expect(passwordInput).toHaveAttribute('type', 'password');
    });

    test('navigates to signup page', async ({ page }) => {
      await page.goto('/signin');

      await page.click('[data-testid="signup-link"]');
      await expect(page).toHaveURL(/\/signup/);
    });

    test('navigates to reset password page', async ({ page }) => {
      await page.goto('/signin');

      await page.click('[data-testid="forgot-password-link"]');
      await expect(page).toHaveURL(/\/reset-password/);
    });
  });

  test.describe('Sign Up Page', () => {
    test('loads signup page correctly', async ({ page }) => {
      await page.goto('/signup');

      await expect(page).toHaveTitle(/Netpick/i);
      await expect(page.locator('h1')).toContainText(/Sign Up/i);
      await expect(page.locator('input[name="email"]')).toBeVisible();
      await expect(page.locator('input[name="password"]')).toBeVisible();
      await expect(page.locator('input[name="name"]')).toBeVisible();
      await expect(page.locator('[data-testid="signup-submit"]')).toBeVisible();
    });

    test('toggles password visibility on signup', async ({ page }) => {
      await page.goto('/signup');

      const passwordInput = page.locator('input[type="password"]');
      await expect(passwordInput).toHaveAttribute('type', 'password');

      await page.click('[data-testid="password-toggle"]');
      await expect(passwordInput).toHaveAttribute('type', 'text');
    });
  });

  test.describe('Reset Password Page', () => {
    test('loads reset password page correctly', async ({ page }) => {
      await page.goto('/reset-password');

      await expect(page.locator('[data-testid="reset-password-title"]')).toBeVisible();
      await expect(page.locator('input[type="email"]')).toBeVisible();
      await expect(page.locator('[data-testid="reset-password-submit"]')).toBeVisible();
    });
  });

  test.describe('Protected Routes', () => {
    test('redirects unauthenticated users to signin', async ({ page }) => {
      await page.goto('/dashboard');

      await expect(page).toHaveURL(/\/signin/);
    });

    test('access protected route after signin in dev mode', async ({ page }) => {
      await page.goto('/signin');

      await page.fill('input[type="email"]', 'dev@example.com');
      await page.fill('input[type="password"]', 'validpassword123456');
      await page.click('[data-testid="signin-submit"]');

      await page.waitForURL('/');
      await expect(page).toHaveURL('/');
    });
  });
});
