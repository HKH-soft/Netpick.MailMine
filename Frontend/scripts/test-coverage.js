#!/usr/bin/env node

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('Running test coverage analysis...\n');

try {
  // Run vitest with coverage
  execSync('npx vitest run --coverage', { stdio: 'inherit' });
  
  // Read coverage report
  const coveragePath = path.join(__dirname, '..', 'coverage');
  const coverageSummaryPath = path.join(coveragePath, 'coverage-summary.json');
  
  if (fs.existsSync(coverageSummaryPath)) {
    const summary = JSON.parse(fs.readFileSync(coverageSummaryPath, 'utf8'));
    const total = summary.total;
    
    console.log('\n=== Coverage Summary ===');
    console.log(`Lines: ${Math.round(total.lines.pct)}% (target: 80%)`);
    console.log(`Functions: ${Math.round(total.functions.pct)}% (target: 75%)`);
    console.log(`Branches: ${Math.round(total.branches.pct)}% (target: 70%)`);
    console.log(`Statements: ${Math.round(total.stmts.pct)}% (target: 80%)`);
  }
  
  console.log('\nCoverage report generated at:', coveragePath);
} catch (error) {
  console.error('Coverage check failed:', error.message);
  process.exit(1);
}