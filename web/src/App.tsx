import { useEffect } from 'react'
import { Routes, Route, useLocation, useNavigate } from 'react-router-dom'
import { HomeScreen } from './ui/screens/HomeScreen'
import { GoalDetailScreen } from './ui/screens/GoalDetailScreen'
import { SettingsScreen } from './ui/screens/SettingsScreen'
import { EventsScreen } from './ui/screens/EventsScreen'
import { ExplainScreen } from './ui/screens/ExplainScreen'
import { OnboardingScreen, isOnboardingComplete } from './ui/screens/OnboardingScreen'
import { ChangelogScreen } from './ui/screens/ChangelogScreen'
import { SearchAssistantScreen } from './ui/screens/SearchAssistantScreen'
import { PresetsScreen } from './ui/screens/PresetsScreen'
import { SavedSearchesScreen } from './ui/screens/SavedSearchesScreen'
import { KnowledgeScreen } from './ui/screens/KnowledgeScreen'
import { BottomNav } from './ui/components/BottomNav'
import { PwaUpdateBanner } from './ui/components/PwaUpdateBanner'

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
      <PwaUpdateBanner />
      <Routes>
        <Route path="/" element={<HomeScreen />} />
        <Route path="/goal/:goalId" element={<GoalDetailScreen />} />
        <Route path="/events" element={<EventsScreen />} />
        <Route path="/explain" element={<ExplainScreen />} />
        <Route path="/settings" element={<SettingsScreen />} />
        <Route path="/favorites" element={<SavedSearchesScreen kind="favorites" />} />
        <Route path="/history" element={<SavedSearchesScreen kind="history" />} />
        <Route path="/knowledge" element={<KnowledgeScreen />} />
        <Route path="/onboarding" element={<OnboardingScreen />} />
        <Route path="/changelog" element={<ChangelogScreen />} />
        <Route path="/assistant" element={<SearchAssistantScreen />} />
        <Route path="/presets" element={<PresetsScreen />} />
        <Route path="/my-presets" element={<PresetsScreen personal />} />
      </Routes>
      {showBottomNav && <BottomNav />}
    </>
  )
}

