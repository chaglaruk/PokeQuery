import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'

interface GoalCard {
  goalId: string
  titleKey: string
  descKey: string
  icon: string
  route?: string
}

export function HomeScreen() {
  const { t } = useI18n()
  const navigate = useNavigate()

  const goals: GoalCard[] = [
    { goalId: 'safe_cleanup', titleKey: 'goal_safe_cleanup', descKey: 'goal_safe_cleanup_desc', icon: '🧹' },
    { goalId: 'candy_prep', titleKey: 'goal_candy_prep', descKey: 'goal_candy_prep_desc', icon: '🍬' },
    { goalId: 'trade_fodder', titleKey: 'goal_trade_fodder', descKey: 'goal_trade_fodder_desc', icon: '🔄' },
    { goalId: 'hundo_check', titleKey: 'goal_hundo_check', descKey: 'goal_hundo_check_desc', icon: '⭐' },
    { goalId: 'nundo_finder', titleKey: 'goal_nundo_finder', descKey: 'goal_nundo_finder_desc', icon: '🔍' },
    { goalId: 'pvp_candidates', titleKey: 'goal_pvp_candidates', descKey: 'goal_pvp_candidates_desc', icon: '⚔️' },
    { goalId: 'lucky_trade', titleKey: 'goal_lucky_trade', descKey: 'goal_lucky_trade_desc', icon: '🍀' },
    { goalId: 'untagged', titleKey: 'goal_untagged', descKey: 'goal_untagged_cleanup', icon: '🏷️' },
    { goalId: 'expert', titleKey: 'goal_expert', descKey: 'goal_expert_desc', icon: '⚒️' },
  ]

  const tools: GoalCard[] = [
    { goalId: 'events', titleKey: 'goal_events', descKey: 'goal_events_desc', icon: '📅', route: '/events' },
    { goalId: 'explain', titleKey: 'goal_explain', descKey: 'goal_explain_desc', icon: '🔎', route: '/explain' },
  ]

  return (
    <div className="page content-with-nav">
      <div style={{ paddingTop: '20px', paddingBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 800 }}>PokeQuery</h1>
        <p className="text-dim" style={{ marginTop: '4px' }}>{t('home_header_desc')}</p>
      </div>

      <div className="card" style={{ marginBottom: '24px' }}>
        <p style={{ fontSize: '17px', fontWeight: 600, marginBottom: '4px' }}>{t('home_subtitle')}</p>
        <p className="text-muted">{t('home_subtitle_secondary')}</p>
      </div>

      <div className="section-title">{t('home_more_tools')}</div>
      {goals.map(goal => (
        <div
          key={goal.goalId}
          className="card card-tap"
          onClick={() => navigate(`/goal/${goal.goalId}`)}
          style={{ display: 'flex', alignItems: 'center', gap: '14px' }}
        >
          <span style={{ fontSize: '28px', flexShrink: 0 }}>{goal.icon}</span>
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ fontSize: '16px', fontWeight: 600 }}>{t(goal.titleKey)}</p>
            <p className="text-muted" style={{ marginTop: '2px' }}>{t(goal.descKey)}</p>
          </div>
          <span style={{ color: 'var(--text-muted)', fontSize: '20px' }}>›</span>
        </div>
      ))}

      <div className="section-title">{t('goal_explain')}</div>
      {tools.map(tool => (
        <div
          key={tool.goalId}
          className="card card-tap"
          onClick={() => navigate(tool.route ?? `/goal/${tool.goalId}`)}
          style={{ display: 'flex', alignItems: 'center', gap: '14px' }}
        >
          <span style={{ fontSize: '28px', flexShrink: 0 }}>{tool.icon}</span>
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ fontSize: '16px', fontWeight: 600 }}>{t(tool.titleKey)}</p>
            <p className="text-muted" style={{ marginTop: '2px' }}>{t(tool.descKey)}</p>
          </div>
          <span style={{ color: 'var(--text-muted)', fontSize: '20px' }}>›</span>
        </div>
      ))}

      <div className="card" style={{ marginTop: '24px' }}>
        <p style={{ fontWeight: 600, marginBottom: '8px' }}>🔒 {t('home_chip_no_access_title')}</p>
        <p className="text-muted">{t('home_chip_no_access_desc')}</p>
      </div>
      <div className="card">
        <p style={{ fontWeight: 600, marginBottom: '8px' }}>👁️ {t('home_chip_review_title')}</p>
        <p className="text-muted">{t('home_chip_review_desc')}</p>
      </div>
    </div>
  )
}
