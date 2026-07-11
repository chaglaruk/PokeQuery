import { NavLink } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'

export function BottomNav() {
  const { t } = useI18n()

  const items = [
    { to: '/', icon: '🏠', label: t('nav_home') },
    { to: '/events', icon: '📅', label: t('goal_events') },
    { to: '/settings', icon: '⚙️', label: t('nav_settings') },
  ]

  return (
    <nav className="bottom-nav">
      {items.map(item => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.to === '/'}
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <span className="nav-icon">{item.icon}</span>
          <span>{item.label}</span>
        </NavLink>
      ))}
    </nav>
  )
}
