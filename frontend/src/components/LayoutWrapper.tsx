"use client";

import { useAuth } from '@/context/AuthContext';
import { usePathname } from 'next/navigation';
import Sidebar from './Sidebar';
import { Loader2 } from 'lucide-react';

export default function LayoutWrapper({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const pathname = usePathname();

  // Public routes that don't need sidebar/auth
  const isPublic = pathname === '/login' || pathname === '/register';

  if (loading) {
    return (
      <div className="flex h-screen w-full items-center justify-center bg-gray-50">
        <Loader2 className="h-10 w-10 animate-spin text-emerald" />
      </div>
    );
  }

  // Redirect to login if not authenticated and not on a public route
  if (!user && !isPublic) {
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
    return null;
  }

  // Redirect to dashboard if logged in and trying to access login page
  if (user && isPublic) {
    if (typeof window !== 'undefined') {
      window.location.href = '/';
    }
    return null;
  }

  if (isPublic) {
    return <main className="min-h-screen bg-gray-50">{children}</main>;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex">
      <Sidebar />
      <div className="flex-1 md:ml-64 w-full">
        <main className="p-6 md:p-10 max-w-7xl mx-auto min-h-screen">
          {children}
        </main>
      </div>
    </div>
  );
}
