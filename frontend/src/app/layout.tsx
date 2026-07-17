import type { Metadata } from "next";
import "./globals.css";
import Providers from "./providers";
import { Analytics } from "@vercel/analytics/react";

export const metadata: Metadata = {
  title: "EduAPI | Student Management System",
  description: "A comprehensive platform for managing students, courses, and academic records.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="font-sans antialiased bg-gray-50 text-gray-900">
        <Providers>{children}</Providers>
        <Analytics />
      </body>
    </html>
  );
}
