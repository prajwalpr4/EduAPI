"use client";

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import { Loader2, Calendar, Plus, X } from 'lucide-react';
import { format } from 'date-fns';

interface Enrollment {
  id: number;
  studentId: number;
  studentName: string;
  studentCode: string;
  courseId: number;
  courseTitle: string;
  courseCode: string;
  enrollmentDate: string;
  grade: string | null;
  status: string;
}

interface DropdownItem {
  id: number;
  name: string;
}

export default function EnrollmentsPage() {
  const { user } = useAuth();
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  
  // Dropdown options
  const [studentsList, setStudentsList] = useState<DropdownItem[]>([]);
  const [coursesList, setCoursesList] = useState<DropdownItem[]>([]);
  
  // Form fields
  const [form, setForm] = useState({
    studentId: '',
    courseId: '',
    enrollmentDate: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    fetchData();
  }, [user]);

  const fetchData = async () => {
    if (!user) return;
    setLoading(true);
    try {
      // 1. Fetch user profile to get student ID (for students) or to verify Admin role
      const profileRes = await api.get('/profile');
      const profile = profileRes.data;

      // 2. Fetch Enrollments based on role
      let url = '/enrollments';
      if (profile.role === 'STUDENT' && profile.studentId) {
        url = `/enrollments/student/${profile.studentId}`;
      }
      const response = await api.get(url);
      setEnrollments(response.data || []);

      // 3. If Admin, fetch student and course dropdown lists
      if (profile.role === 'ADMIN') {
        const studRes = await api.get('/students?size=100');
        const courRes = await api.get('/courses?size=100');
        
        setStudentsList(
          (studRes.data.content || []).map((s: { id: number; firstName: string; lastName: string; studentCode: string }) => ({
            id: s.id,
            name: `${s.firstName} ${s.lastName} (${s.studentCode})`,
          }))
        );
        
        setCoursesList(
          (courRes.data.content || []).map((c: { id: number; title: string; courseCode: string }) => ({
            id: c.id,
            name: `${c.title} (${c.courseCode})`,
          }))
        );
      }
    } catch (error) {
      console.error('Failed to fetch enrollments data', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      await api.post('/enrollments', {
        studentId: Number(form.studentId),
        courseId: Number(form.courseId),
        enrollmentDate: form.enrollmentDate,
      });
      setShowModal(false);
      setForm({
        studentId: '',
        courseId: '',
        enrollmentDate: new Date().toISOString().split('T')[0],
      });
      fetchData();
    } catch (err: unknown) {
      const apiErr = err as { response?: { data?: { message?: string } } };
      setError(apiErr.response?.data?.message || 'Failed to enroll student. Verify course capacity and duplication.');
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch(status) {
      case 'ACTIVE': return 'bg-emerald/10 text-emerald border-emerald/20';
      case 'COMPLETED': return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'DROPPED': return 'bg-red-100 text-red-700 border-red-200';
      default: return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  return (
    <div>
      <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold text-oxford">Enrollments</h1>
          <p className="text-gray-500 mt-1">
            {user?.role === 'STUDENT' ? 'Your academic journey and course registrations.' : 'Manage course registrations across all students.'}
          </p>
        </div>

        {user?.role === 'ADMIN' && (
          <button 
            onClick={() => setShowModal(true)}
            className="flex items-center space-x-2 bg-emerald text-oxford font-semibold px-4 py-2 rounded-lg hover:bg-emerald-light transition-colors whitespace-nowrap self-start md:self-auto"
          >
            <Plus size={18} />
            <span>New Enrollment</span>
          </button>
        )}
      </div>

      {/* New Enrollment Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden relative">
            <div className="flex justify-between items-center px-6 py-4 bg-oxford text-white">
              <h3 className="text-lg font-bold font-heading text-white">New Enrollment</h3>
              <button onClick={() => setShowModal(false)} className="text-gray-300 hover:text-white">
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {error && (
                <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm border border-red-100">
                  {error}
                </div>
              )}
              
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Select Student *</label>
                <select name="studentId" required value={form.studentId} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none bg-white">
                  <option value="">-- Choose Student --</option>
                  {studentsList.map((s) => (
                    <option key={s.id} value={s.id}>{s.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Select Course / Subject *</label>
                <select name="courseId" required value={form.courseId} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none bg-white">
                  <option value="">-- Choose Course --</option>
                  {coursesList.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Enrollment Date *</label>
                <input type="date" name="enrollmentDate" required value={form.enrollmentDate} onChange={handleInputChange} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 border rounded-lg text-sm font-medium hover:bg-gray-50">Cancel</button>
                <button type="submit" disabled={submitting} className="px-4 py-2 bg-oxford text-white rounded-lg text-sm font-medium hover:bg-oxford-light flex items-center justify-center min-w-[100px]">
                  {submitting ? <Loader2 size={16} className="animate-spin" /> : 'Enroll Student'}
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
        ) : enrollments.length === 0 ? (
          <div className="flex h-48 flex-col items-center justify-center text-gray-500">
            <p>No enrollments found.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  {user?.role === 'ADMIN' && <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Student</th>}
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Course</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider text-center">Grade</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {enrollments.map((e) => (
                  <tr key={e.id} className="hover:bg-gray-50/50 transition-colors">
                    {user?.role === 'ADMIN' && (
                      <td className="px-6 py-4">
                        <div className="font-medium text-gray-900">{e.studentName}</div>
                        <div className="text-xs text-gray-500 font-mono mt-1">{e.studentCode}</div>
                      </td>
                    )}
                    <td className="px-6 py-4">
                      <div className="font-medium text-oxford">{e.courseTitle}</div>
                      <div className="text-xs text-gray-500 font-mono mt-1">{e.courseCode}</div>
                    </td>
                    <td className="px-6 py-4 text-gray-500 text-sm">
                      <div className="flex items-center">
                        <Calendar size={14} className="mr-2 text-gray-400" />
                        {e.enrollmentDate ? format(new Date(e.enrollmentDate), 'MMM dd, yyyy') : 'N/A'}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`text-xs font-bold px-2.5 py-1 rounded-full border ${getStatusColor(e.status)}`}>
                        {e.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className={`font-heading font-bold text-lg ${e.grade ? 'text-oxford' : 'text-gray-300'}`}>
                        {e.grade || '-'}
                      </span>
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
