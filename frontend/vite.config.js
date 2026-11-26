import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite development configuration
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Proxy API calls directly to the Java backend running on port 8080
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
