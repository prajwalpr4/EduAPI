"use client";

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { Loader2, Mail, Phone, Calendar, User as UserIcon } from 'lucide-react';
import { format } from 'date-fns';

interface UserProfile {
  id: number;
  username: string;
  email: string;
  role: string;
  firstName: string;
  lastName: string;
  phone: string;
  profilePictureUrl: string;
  studentId: number;
  studentCode: string;
  createdAt: string;
}

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await api.get('/profile');
        setProfile(response.data);
      } catch (error) {
        console.error('Failed to fetch profile', error);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-emerald" />
      </div>
    );
  }

  if (!profile) {
    return <div className="text-center text-gray-500 mt-10">Failed to load profile.</div>;
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-heading font-bold text-oxford">My Profile</h1>
        <p className="text-gray-500 mt-1">Manage your personal information and credentials.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        
        {/* Left Col: ID Card */}
        <div className="md:col-span-1">
          <div className="id-card p-6 flex flex-col items-center text-center">
            
            <div className="w-full flex justify-between items-start mb-6">
              <span className="text-xs font-bold text-oxford tracking-widest uppercase">EduAPI</span>
              {profile.role === 'STUDENT' && (
                <span className="bg-emerald/10 text-emerald text-xs px-2 py-1 rounded font-bold tracking-wide">STUDENT</span>
              )}
              {profile.role === 'ADMIN' && (
                <span className="bg-purple-100 text-purple-600 text-xs px-2 py-1 rounded font-bold tracking-wide">ADMIN</span>
              )}
            </div>

            <div className="w-24 h-24 rounded-full bg-gray-200 mb-4 border-4 border-white shadow-md overflow-hidden flex items-center justify-center relative">
              {profile.profilePictureUrl ? (
                <img src={profile.profilePictureUrl} alt="Profile" className="w-full h-full object-cover" />
              ) : (
                <UserIcon size={40} className="text-gray-400" />
              )}
            </div>

            <h2 className="text-xl font-heading font-bold text-oxford">{profile.firstName} {profile.lastName}</h2>
            <p className="text-sm text-gray-500 mb-6">@{profile.username}</p>

            {profile.studentCode && (
              <div className="w-full bg-gray-50 p-3 rounded-lg border border-gray-100 mb-4 flex flex-col items-center">
                <span className="text-xs text-gray-400 uppercase tracking-wider mb-1">Student ID</span>
                <span className="font-mono font-bold text-oxford">{profile.studentCode}</span>
              </div>
            )}

            <div className="w-full text-left space-y-2 mt-2">
              <div className="flex items-center text-xs text-gray-500">
                <Calendar size={14} className="mr-2" />
                <span>Joined {profile.createdAt ? format(new Date(profile.createdAt), 'MMM yyyy') : 'N/A'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Col: Details */}
        <div className="md:col-span-2">
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h3 className="text-lg font-heading font-semibold text-oxford mb-6 border-b pb-4">Personal Details</h3>
            
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-xs font-medium text-gray-400 uppercase tracking-wider mb-1">First Name</label>
                  <p className="font-medium text-gray-900">{profile.firstName}</p>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-400 uppercase tracking-wider mb-1">Last Name</label>
                  <p className="font-medium text-gray-900">{profile.lastName}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-xs font-medium text-gray-400 uppercase tracking-wider mb-1 flex items-center">
                    <Mail size={14} className="mr-1" /> Email
                  </label>
                  <p className="font-medium text-gray-900">{profile.email}</p>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-400 uppercase tracking-wider mb-1 flex items-center">
                    <Phone size={14} className="mr-1" /> Phone
                  </label>
                  <p className="font-medium text-gray-900">{profile.phone || 'Not provided'}</p>
                </div>
              </div>
            </div>
            
            <div className="mt-8 pt-6 border-t flex justify-end">
              <button className="px-4 py-2 bg-oxford text-white rounded-lg text-sm font-medium hover:bg-oxford-light transition-colors">
                Edit Profile
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
