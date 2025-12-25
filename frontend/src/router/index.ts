import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import PlantsListView from '@/views/public/PlantsListView.vue'
import PlantDetailView from '@/views/public/PlantDetailView.vue'
import RecommendationsView from '@/views/public/RecommendationsView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ProfileView from '@/views/profile/ProfileView.vue'
import AdminPlantsListView from '@/views/admin/AdminPlantsListView.vue'
import AdminPlantEditView from '@/views/admin/AdminPlantEditView.vue'
import AdminPlantCreateView from '@/views/admin/AdminPlantCreateView.vue'
import DiaryMyPlantsView from '@/views/diary/DiaryMyPlantsView.vue'
import DiaryPlantCareView from '@/views/diary/DiaryPlantCareView.vue'
import RemindersView from '@/views/diary/RemindersView.vue'
import CalendarDayView from '@/views/calendar/CalendarDayView.vue'
import CalendarWeekView from '@/views/calendar/CalendarWeekView.vue'
import CalendarMonthView from '@/views/calendar/CalendarMonthView.vue'
import { useAuthStore } from '@/stores/auth'
import { authEnabled } from '@/config'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/plants' },
  // public
  { path: '/plants', component: PlantsListView },
  { path: '/plants/:id', component: PlantDetailView },
  { path: '/plants/recommendations', component: RecommendationsView },
  // auth
  { path: '/auth/login', component: LoginView },
  { path: '/auth/register', component: RegisterView },
  // profile
  { path: '/profile', component: ProfileView, meta: { auth: true } },
  // admin
  { path: '/admin/plants', component: AdminPlantsListView, meta: { auth: true, admin: true } },
  { path: '/admin/plants/new', component: AdminPlantCreateView, meta: { auth: true, admin: true } },
  { path: '/admin/plants/:id', component: AdminPlantEditView, meta: { auth: true, admin: true } },
  // diary
  { path: '/diary/plants', component: DiaryMyPlantsView, meta: { auth: true } },
  { path: '/diary/plants/:id/care', component: DiaryPlantCareView, meta: { auth: true } },
  { path: '/diary/reminders', component: RemindersView, meta: { auth: true } },
  // calendar
  { path: '/calendar/day', component: CalendarDayView, meta: { auth: true } },
  { path: '/calendar/week', component: CalendarWeekView, meta: { auth: true } },
  { path: '/calendar/month', component: CalendarMonthView, meta: { auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  // When auth is disabled on backend, skip guards entirely
  if (!authEnabled) {
    return true
  }
  if (auth.token && !auth.profile) {
    await auth.fetchProfile()
  }
  if (to.meta?.auth && !auth.isAuthenticated) {
    return { path: '/auth/login', query: { redirect: to.fullPath } }
  }
  if (to.meta?.admin && !auth.isAdmin) {
    return { path: '/' }
  }
})

export default router
