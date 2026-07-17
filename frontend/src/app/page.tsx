"use client";

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import { Users, BookOpen, GraduationCap, Loader2 } from 'lucide-react';

interface DashboardStats {
  totalStudents: number;
  totalCourses: number;
  totalEnrollments: number;
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = await api.get('/dashboard/stats');
        setStats(response.data);
      } catch (error) {
        console.error('Failed to fetch dashboard stats', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-emerald" />
      </div>
    );
  }

  const statCards = [
    { title: 'Total Students', value: stats?.totalStudents || 0, icon: Users, color: 'text-blue-500', bg: 'bg-blue-100' },
    { title: 'Total Courses', value: stats?.totalCourses || 0, icon: BookOpen, color: 'text-emerald-500', bg: 'bg-emerald-100' },
    { title: 'Active Enrollments', value: stats?.totalEnrollments || 0, icon: GraduationCap, color: 'text-purple-500', bg: 'bg-purple-100' },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-heading font-bold text-oxford">Welcome back, {user?.firstName}!</h1>
        <p className="text-gray-500 mt-1">Here&apos;s what&apos;s happening in your system today.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        {statCards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <div key={idx} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center space-x-4 transition-transform hover:-translate-y-1">
              <div className={`p-4 rounded-full ${card.bg}`}>
                <Icon size={28} className={card.color} />
              </div>
              <div>
                <p className="text-sm text-gray-500 font-medium">{card.title}</p>
                <h3 className="text-2xl font-bold text-oxford mt-1">{card.value}</h3>
              </div>
            </div>
          );
        })}
      </div>

      <div className="bg-white p-8 rounded-xl shadow-sm border border-gray-100">
        <h2 className="text-xl font-heading font-semibold text-oxford mb-4">Recent Activity Overview</h2>
        <div className="h-48 flex items-center justify-center bg-gray-50 rounded-lg border border-dashed border-gray-300">
          <p className="text-gray-400">Activity chart will appear here</p>
        </div>
      </div>
    </div>
  );
}
