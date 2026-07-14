import { NavLink } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { AppIcon } from './SpriteIcon'

export function BottomNav() {
  const { t } = useI18n()

  const items = [
    { to: '/', icon: 'home', label: t('nav_home') },
    { to: '/favorites', icon: 'favorite', label: t('nav_favorites') },
    { to: '/history', icon: 'history', label: t('nav_history') },
    { to: '/knowledge', icon: 'info', label: t('nav_knowledge') },
    { to: '/settings', icon: 'settings', label: t('nav_settings') },
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
          <span className="nav-icon"><AppIcon name={item.icon} size={22} /></span>
          <span>{item.label}</span>
        </NavLink>
      ))}
    </nav>
  )
}
