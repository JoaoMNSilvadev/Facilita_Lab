const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',
  workers: 1,
  use: {
    baseURL: 'http://localhost:8081',
    actionTimeout: 15000,
    trace: 'on-first-retry',
    launchOptions: {
      slowMo: 500, 
    },
  },
});