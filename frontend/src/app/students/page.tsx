"use client";

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { Loader2, Search, UserPlus, X } from 'lucide-react';
import { format } from 'date-fns';

interface Student {
  id: number;
  studentCode: string;
  firstName: string;
  lastName: string;
  email: string;
  enrollmentDate: string;
}

export default function StudentsPage() {
  const [students, setStudents] = useState<Student[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  
  // Form fields
  const [form, setForm] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    dateOfBirth: '',
    enrollmentDate: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    fetchStudents();
  }, [search]);

  const fetchStudents = async () => {
    try {
      const response = await api.get(`/students?search=${search}&size=50`);
      setStudents(response.data.content || []);
    } catch (error) {
      console.error('Failed to fetch students', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      await api.post('/students', {
        ...form,
        dateOfBirth: form.dateOfBirth ? form.dateOfBirth : null,
      });
      setShowModal(false);
      // Reset form
      setForm({
        username: '',
        password: '',
        email: '',
        firstName: '',
        lastName: '',
        phone: '',
        dateOfBirth: '',
        enrollmentDate: new Date().toISOString().split('T')[0],
      });
      fetchStudents();
    } catch (err: unknown) {
      const apiErr = err as { response?: { data?: { message?: string } } };
      setError(apiErr.response?.data?.message || 'Failed to create student. Please verify all details.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold text-oxford">Students</h1>
          <p className="text-gray-500 mt-1">Manage student records and information.</p>
        </div>
        
        <div className="flex items-center space-x-3 w-full md:w-auto">
          <div className="relative w-full md:w-64">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
            <input 
              type="text" 
              placeholder="Search students..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald focus:border-emerald outline-none text-sm"
            />
          </div>
          <button 
            onClick={() => setShowModal(true)}
            className="flex items-center space-x-2 bg-emerald text-oxford font-semibold px-4 py-2 rounded-lg hover:bg-emerald-light transition-colors whitespace-nowrap"
          >
            <UserPlus size={18} />
            <span>Add Student</span>
          </button>
        </div>
      </div>

      {/* Add Student Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg overflow-hidden relative">
            <div className="flex justify-between items-center px-6 py-4 bg-oxford text-white">
              <h3 className="text-lg font-bold font-heading text-white">Add New Student</h3>
              <button onClick={() => setShowModal(false)} className="text-gray-300 hover:text-white">
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto">
              {error && (
                <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm border border-red-100">
                  {error}
                </div>
              )}
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">First Name *</label>
                  <input type="text" name="firstName" required value={form.firstName} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Last Name *</label>
                  <input type="text" name="lastName" required value={form.lastName} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Username *</label>
                  <input type="text" name="username" required value={form.username} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Password *</label>
                  <input type="password" name="password" required value={form.password} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Email *</label>
                <input type="email" name="email" required value={form.email} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Phone</label>
                  <input type="text" name="phone" value={form.phone} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Date of Birth</label>
                  <input type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Enrollment Date *</label>
                <input type="date" name="enrollmentDate" required value={form.enrollmentDate} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 border rounded-lg text-sm font-medium hover:bg-gray-50">Cancel</button>
                <button type="submit" disabled={submitting} className="px-4 py-2 bg-oxford text-white rounded-lg text-sm font-medium hover:bg-oxford-light flex items-center justify-center min-w-[100px]">
                  {submitting ? <Loader2 size={16} className="animate-spin" /> : 'Save Student'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="flex h-48 items-center justify-center">
            <Loader2 className="h-8 w-8 animate-spin text-emerald" />
          </div>
        ) : students.length === 0 ? (
          <div className="flex h-48 flex-col items-center justify-center text-gray-500">
            <p>No students found.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Student ID</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Name</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Email</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Enrolled</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {students.map((student) => (
                  <tr key={student.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-6 py-4">
                      <span className="font-mono text-sm font-medium text-oxford bg-gray-100 px-2 py-1 rounded">
                        {student.studentCode}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-medium text-gray-900">{student.firstName} {student.lastName}</div>
                    </td>
                    <td className="px-6 py-4 text-gray-500 text-sm">{student.email}</td>
                    <td className="px-6 py-4 text-gray-500 text-sm">
                      {student.enrollmentDate ? format(new Date(student.enrollmentDate), 'MMM dd, yyyy') : 'N/A'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
