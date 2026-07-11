import { useEffect } from 'react'
import { Routes, Route, useLocation, useNavigate } from 'react-router-dom'
import { HomeScreen } from './ui/screens/HomeScreen'
import { GoalDetailScreen } from './ui/screens/GoalDetailScreen'
import { SettingsScreen } from './ui/screens/SettingsScreen'
import { EventsScreen } from './ui/screens/EventsScreen'
import { ExplainScreen } from './ui/screens/ExplainScreen'
import { OnboardingScreen, isOnboardingComplete } from './ui/screens/OnboardingScreen'
import { ChangelogScreen } from './ui/screens/ChangelogScreen'
import { BottomNav } from './ui/components/BottomNav'

export default function App() {
  const location = useLocation()
  const navigate = useNavigate()

  // First-run onboarding: redirect once if the user has not completed it and is not already on /onboarding.
  useEffect(() => {
    if (!isOnboardingComplete() && location.pathname !== '/onboarding') {
      navigate('/onboarding', { replace: true })
    }
  }, [location.pathname, navigate])

  const showBottomNav = location.pathname !== '/onboarding'

  return (
    <>
      <Routes>
        <Route path="/" element={<HomeScreen />} />
        <Route path="/goal/:goalId" element={<GoalDetailScreen />} />
        <Route path="/events" element={<EventsScreen />} />
        <Route path="/explain" element={<ExplainScreen />} />
        <Route path="/settings" element={<SettingsScreen />} />
        <Route path="/onboarding" element={<OnboardingScreen />} />
        <Route path="/changelog" element={<ChangelogScreen />} />
      </Routes>
      {showBottomNav && <BottomNav />}
    </>
  )
}
