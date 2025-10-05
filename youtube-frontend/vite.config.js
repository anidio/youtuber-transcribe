import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // 💡 CONFIGURAÇÃO PARA BUILD DE EXTENSÃO
  build: {
    outDir: 'build', // Pasta de saída
    rollupOptions: {
      output: {
        // Garante que os nomes dos chunks e assets sejam consistentes
        entryFileNames: `assets/[name].js`,
        chunkFileNames: `assets/[name].js`,
        assetFileNames: `assets/[name].[ext]`,
      }
    }
  }
})