"use client";

import { AuthProvider } from "@/context/AuthContext";

import LayoutWrapper from "@/components/LayoutWrapper";

export default function Providers({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      <LayoutWrapper>{children}</LayoutWrapper>
    </AuthProvider>
  );
}
