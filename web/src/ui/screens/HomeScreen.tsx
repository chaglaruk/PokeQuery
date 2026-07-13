import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { SpriteIcon, goalIcon } from '../components/SpriteIcon'

interface GoalCard {
  goalId: string
  titleKey: string
  descKey: string
  route?: string
}

export function HomeScreen() {
  const { t } = useI18n()
  const navigate = useNavigate()

  const goals: GoalCard[] = [
    { goalId: 'safe_cleanup', titleKey: 'goal_safe_cleanup', descKey: 'goal_safe_cleanup_desc' },
    { goalId: 'candy_prep', titleKey: 'goal_candy_prep', descKey: 'goal_candy_prep_desc' },
    { goalId: 'trade_fodder', titleKey: 'goal_trade_fodder', descKey: 'goal_trade_fodder_desc' },
    { goalId: 'hundo_check', titleKey: 'goal_hundo_check', descKey: 'goal_hundo_check_desc' },
    { goalId: 'nundo_finder', titleKey: 'goal_nundo_finder', descKey: 'goal_nundo_finder_desc' },
    { goalId: 'pvp_candidates', titleKey: 'goal_pvp_candidates', descKey: 'goal_pvp_candidates_desc' },
    { goalId: 'lucky_trade', titleKey: 'goal_lucky_trade', descKey: 'goal_lucky_trade_desc' },
    { goalId: 'untagged', titleKey: 'goal_untagged', descKey: 'goal_untagged_cleanup' },
    { goalId: 'expert', titleKey: 'goal_expert', descKey: 'goal_expert_desc' },
  ]

  const tools: GoalCard[] = [
    { goalId: 'assistant', titleKey: 'search_assistant_title', descKey: 'search_assistant_desc_text', route: '/assistant' },
    { goalId: 'events', titleKey: 'goal_events', descKey: 'goal_events_desc', route: '/events' },
    { goalId: 'explain', titleKey: 'goal_explain', descKey: 'goal_explain_desc', route: '/explain' },
  ]

  return (
    <div className="page content-with-nav">
      {/* Wordmark logo */}
      <div style={{ paddingTop: '16px', paddingBottom: '16px', textAlign: 'center' }}>
        <h1 className="visually-hidden">PokeQuery</h1>
        <SpriteIcon sprite="pokequery_wordmark" alt="PokeQuery" size={200} />
        <p className="text-dim" style={{ marginTop: '6px', fontSize: '14px' }}>{t('home_header_desc')}</p>
      </div>

      {/* Intro card */}
      <div className="card" style={{ marginBottom: '20px' }}>
        <p style={{ fontSize: '16px', fontWeight: 600, marginBottom: '4px' }}>{t('home_subtitle')}</p>
        <p className="text-muted">{t('home_subtitle_secondary')}</p>
      </div>

      {/* Goal grid — compact 2-column layout */}
      <div className="section-title" style={{ marginTop: 0 }}>{t('home_more_tools')}</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
        {goals.map(goal => (
          <div
            key={goal.goalId}
            className="card card-tap"
            onClick={() => navigate(`/goal/${goal.goalId}`)}
            style={{ padding: '12px 10px' }}
          >
            <span style={{ fontSize: '22px', lineHeight: 1, display: 'block', color: 'var(--accent)', marginBottom: '4px' }}>
              {goalIcon(goal.goalId)}
            </span>
            <p style={{ fontSize: '13px', fontWeight: 600, lineHeight: 1.2 }}>{t(goal.titleKey)}</p>
            <p className="text-muted" style={{ marginTop: '2px', fontSize: '11px', lineHeight: 1.3 }}>{t(goal.descKey)}</p>
          </div>
        ))}
      </div>

      {/* Tools */}
      <div className="section-title">{t('goal_explain')}</div>
      {tools.map(tool => (
        <div
          key={tool.goalId}
          className="card card-tap"
          onClick={() => navigate(tool.route ?? `/goal/${tool.goalId}`)}
          style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '14px 16px' }}
        >
          <span style={{ fontSize: '24px', lineHeight: 1, flexShrink: 0, color: 'var(--accent)' }}>
            {goalIcon(tool.goalId)}
          </span>
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ fontSize: '15px', fontWeight: 600 }}>{t(tool.titleKey)}</p>
            <p className="text-muted" style={{ marginTop: '2px', fontSize: '12px' }}>{t(tool.descKey)}</p>
          </div>
          <span style={{ color: 'var(--text-muted)', fontSize: '18px' }}>›</span>
        </div>
      ))}

      {/* Trust strips */}
      <div className="card" style={{ marginTop: '20px' }}>
        <p style={{ fontWeight: 600, marginBottom: '6px', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span aria-hidden="true" style={{
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            width: '26px', height: '26px', borderRadius: '50%',
            background: 'rgba(11,140,156,0.12)', color: 'var(--accent)',
            fontSize: '14px', fontWeight: 700, flexShrink: 0,
          }}>{'\u29BF'}</span>
          {t('trust_home_no_access')}
        </p>
        <p className="text-muted" style={{ fontSize: '12px' }}>{t('home_chip_no_access_desc')}</p>
      </div>
      <div className="card" style={{ marginBottom: '0' }}>
        <p style={{ fontWeight: 600, marginBottom: '6px', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span aria-hidden="true" style={{
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            width: '26px', height: '26px', borderRadius: '50%',
            background: 'rgba(11,140,156,0.12)', color: 'var(--accent)',
            fontSize: '14px', fontWeight: 700, flexShrink: 0,
          }}>{'\u2714'}</span>
          {t('trust_home_review_first')}
        </p>
        <p className="text-muted" style={{ fontSize: '12px' }}>{t('home_chip_review_desc')}</p>
      </div>
    </div>
  )
}