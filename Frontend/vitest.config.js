/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.js',
    include: ['src/**/*.{test,spec}.{js,ts,jsx,tsx}', 'src/**/__tests__/*.{js,ts,jsx,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'clover'],
      exclude: [
        'node_modules/',
        'src/setupTests.js',
        'src/index.js',
        '**/*.config.js',
        'src/__mocks__/**',
      ],
    },
    // CI-specific settings
    run: {
      passWithNoTests: false,
    },
    reporter: process.env.CI ? 'verbose' : 'default',
  },
});
