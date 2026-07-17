import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        oxford: {
          DEFAULT: "#002147",
          light: "#003366",
          dark: "#001122",
        },
        emerald: {
          DEFAULT: "#50C878",
          light: "#70D890",
          dark: "#30A858",
        },
        background: "var(--background)",
        foreground: "var(--foreground)",
      },
      fontFamily: {
        heading: ["var(--font-playfair)", "serif"],
        sans: ["var(--font-poppins)", "sans-serif"],
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'auth-pattern': 'linear-gradient(135deg, #002147 0%, #001122 100%)',
      }
    },
  },
  plugins: [],
};
export default config;
