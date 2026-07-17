"use client";

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import { Loader2, Plus, BookOpen, X, Check } from 'lucide-react';

interface Course {
  id: number;
  courseCode: string;
  title: string;
  description?: string;
  credits: number;
  maxCapacity: number;
  enrolledCount: number;
}

interface UserProfile {
  id: number;
  role: string;
  studentId?: number;
}

export default function CoursesPage() {
  const { user } = useAuth();
  const [courses, setCourses] = useState<Course[]>([]);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [enrolledCourseIds, setEnrolledCourseIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  
  // Form fields
  const [form, setForm] = useState({
    courseCode: '',
    title: '',
    description: '',
    credits: 3,
    maxCapacity: 30,
  });

  useEffect(() => {
    fetchData();
  }, [user]);

  const fetchData = async () => {
    if (!user) return;
    setLoading(true);
    try {
      // 1. Fetch courses catalog
      const courseRes = await api.get('/courses?size=50');
      setCourses(courseRes.data.content || []);

      // 2. Fetch profile info to get studentId
      const profileRes = await api.get('/profile');
      setProfile(profileRes.data);

      // 3. If student, fetch current enrollments to highlight already registered courses
      if (profileRes.data.role === 'STUDENT' && profileRes.data.studentId) {
        const enrollmentRes = await api.get(`/enrollments/student/${profileRes.data.studentId}`);
        const ids = (enrollmentRes.data || []).map((e: { courseId: number }) => e.courseId);
        setEnrolledCourseIds(ids);
      }
    } catch (error) {
      console.error('Failed to fetch catalog details', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateCourse = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      await api.post('/courses', {
        ...form,
        credits: Number(form.credits),
        maxCapacity: Number(form.maxCapacity),
      });
      setShowModal(false);
      // Reset form
      setForm({
        courseCode: '',
        title: '',
        description: '',
        credits: 3,
        maxCapacity: 30,
      });
      fetchData();
    } catch (err: unknown) {
      const apiErr = err as { response?: { data?: { message?: string } } };
      setError(apiErr.response?.data?.message || 'Failed to create course. Verify details.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleEnroll = async (courseId: number) => {
    if (!profile?.studentId) return;
    try {
      await api.post('/enrollments', {
        studentId: profile.studentId,
        courseId,
      });
      fetchData(); // Refresh counts and highlight
    } catch (error: unknown) {
      const apiErr = error as { response?: { data?: { message?: string } } };
      alert(apiErr.response?.data?.message || 'Failed to enroll in this course.');
    }
  };

  return (
    <div>
      <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold text-oxford">Course Catalog</h1>
          <p className="text-gray-500 mt-1">Browse and manage available academic courses.</p>
        </div>
        
        {profile?.role === 'ADMIN' && (
          <button 
            onClick={() => setShowModal(true)}
            className="flex items-center space-x-2 bg-oxford text-white font-semibold px-4 py-2 rounded-lg hover:bg-oxford-light transition-colors whitespace-nowrap self-start md:self-auto"
          >
            <Plus size={18} />
            <span>New Course</span>
          </button>
        )}
      </div>

      {/* New Course Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden relative">
            <div className="flex justify-between items-center px-6 py-4 bg-oxford text-white">
              <h3 className="text-lg font-bold font-heading text-white">Add New Subject</h3>
              <button onClick={() => setShowModal(false)} className="text-gray-300 hover:text-white">
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleCreateCourse} className="p-6 space-y-4">
              {error && (
                <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm border border-red-100">
                  {error}
                </div>
              )}
              
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Course Code *</label>
                <input type="text" name="courseCode" required value={form.courseCode} onChange={handleInputChange} placeholder="e.g. CS-101" className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Subject Title *</label>
                <input type="text" name="title" required value={form.title} onChange={handleInputChange} placeholder="e.g. Database Management Systems" className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Description</label>
                <textarea name="description" value={form.description} onChange={handleInputChange} placeholder="Brief subject syllabus..." rows={3} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Credits *</label>
                  <input type="number" name="credits" required value={form.credits} onChange={handleInputChange} min={1} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Max Capacity *</label>
                  <input type="number" name="maxCapacity" required value={form.maxCapacity} onChange={handleInputChange} min={1} className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-emerald outline-none" />
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 border rounded-lg text-sm font-medium hover:bg-gray-50">Cancel</button>
                <button type="submit" disabled={submitting} className="px-4 py-2 bg-oxford text-white rounded-lg text-sm font-medium hover:bg-oxford-light flex items-center justify-center min-w-[100px]">
                  {submitting ? <Loader2 size={16} className="animate-spin" /> : 'Save Course'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {loading ? (
        <div className="flex h-48 items-center justify-center bg-white rounded-xl shadow-sm border border-gray-100">
          <Loader2 className="h-8 w-8 animate-spin text-emerald" />
        </div>
      ) : courses.length === 0 ? (
        <div className="flex h-48 flex-col items-center justify-center bg-white rounded-xl shadow-sm border border-gray-100 text-gray-500">
          <BookOpen size={48} className="text-gray-300 mb-4" />
          <p>No courses available at the moment.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {courses.map((course) => {
            const isFull = course.enrolledCount >= course.maxCapacity;
            const progress = (course.enrolledCount / course.maxCapacity) * 100;
            const isEnrolled = enrolledCourseIds.includes(course.id);
            
            return (
              <div key={course.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow flex flex-col justify-between">
                <div>
                  <div className="flex justify-between items-start mb-4">
                    <span className="font-mono text-sm font-bold text-oxford bg-gray-100 px-2 py-1 rounded">
                      {course.courseCode}
                    </span>
                    <span className="text-xs font-semibold px-2 py-1 rounded bg-blue-50 text-blue-600 border border-blue-100">
                      {course.credits} Credits
                    </span>
                  </div>
                  
                  <h3 className="text-lg font-heading font-bold text-gray-900 mb-2 line-clamp-2 leading-tight">
                    {course.title}
                  </h3>
                  
                  {course.description && (
                    <p className="text-xs text-gray-500 mb-4 line-clamp-2">{course.description}</p>
                  )}
                </div>
                
                <div className="pt-4 border-t mt-4">
                  <div className="flex justify-between text-xs mb-1">
                    <span className="text-gray-500 font-medium">Enrollment</span>
                    <span className={isFull ? 'text-red-500 font-bold' : 'text-emerald font-bold'}>
                      {course.enrolledCount} / {course.maxCapacity}
                    </span>
                  </div>
                  <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden mb-4">
                    <div 
                      className={`h-2 rounded-full ${isFull ? 'bg-red-500' : 'bg-emerald'}`} 
                      style={{ width: `${Math.min(progress, 100)}%` }}
                    />
                  </div>

                  {profile?.role === 'STUDENT' && (
                    isEnrolled ? (
                      <button disabled className="w-full py-2 bg-emerald/10 text-emerald border border-emerald/20 font-semibold rounded-lg text-sm flex items-center justify-center space-x-1">
                        <Check size={16} />
                        <span>Enrolled</span>
                      </button>
                    ) : (
                      <button 
                        onClick={() => handleEnroll(course.id)}
                        disabled={isFull}
                        className="w-full py-2 bg-emerald text-oxford font-semibold rounded-lg text-sm hover:bg-emerald-light transition-colors disabled:opacity-50"
                      >
                        {isFull ? 'Course Full' : 'Enroll Now'}
                      </button>
                    )
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
