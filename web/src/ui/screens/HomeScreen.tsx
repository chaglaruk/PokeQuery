import type { CSSProperties } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { AppIcon, SpriteIcon } from '../components/SpriteIcon'

interface GoalCard {
  id: string
  titleKey: string
  descKey: string
  route: string
  accent: string
}

export function HomeScreen() {
  const { t } = useI18n()
  const navigate = useNavigate()

  const primaryGoals: GoalCard[] = [
    { id: 'safe_cleanup', titleKey: 'goal_safe_cleanup', descKey: 'goal_safe_cleanup_desc', route: '/goal/safe_cleanup', accent: '#00E5FF' },
    { id: 'candy_prep', titleKey: 'goal_candy_prep', descKey: 'goal_candy_prep_desc', route: '/goal/candy_prep', accent: '#FFD700' },
    { id: 'lucky_trade', titleKey: 'goal_lucky_trade', descKey: 'goal_lucky_trade_desc', route: '/goal/lucky_trade', accent: '#FFD700' },
    { id: 'assistant', titleKey: 'goal_assistant', descKey: 'goal_assistant_desc', route: '/assistant', accent: '#B14BFF' },
    { id: 'pvp_candidates', titleKey: 'goal_pvp_candidates', descKey: 'goal_pvp_candidates_desc', route: '/goal/pvp_candidates', accent: '#4FC3F7' },
    { id: 'events', titleKey: 'goal_events', descKey: 'goal_events_desc', route: '/events', accent: '#00E5FF' },
    { id: 'presets', titleKey: 'goal_presets', descKey: 'goal_presets_desc', route: '/presets', accent: '#64B5F6' },
    { id: 'my_presets', titleKey: 'goal_my_presets', descKey: 'goal_my_presets_desc', route: '/my-presets', accent: '#1DE9FF' },
  ]

  const moreGoals: GoalCard[] = [
    { id: 'nundo_finder', titleKey: 'goal_nundo_finder', descKey: 'goal_nundo_finder_desc', route: '/goal/nundo_finder', accent: '#4FC3F7' },
    { id: 'hundo_check', titleKey: 'goal_hundo_check', descKey: 'goal_hundo_check_desc', route: '/goal/hundo_check', accent: '#FF6B8A' },
    { id: 'trade_fodder', titleKey: 'goal_trade_fodder', descKey: 'goal_trade_fodder_desc', route: '/goal/trade_fodder', accent: '#FFD700' },
    { id: 'untagged', titleKey: 'goal_untagged', descKey: 'goal_untagged_cleanup', route: '/goal/untagged', accent: '#00E676' },
    { id: 'expert', titleKey: 'goal_expert', descKey: 'goal_expert_desc', route: '/goal/expert', accent: '#B14BFF' },
    { id: 'explain', titleKey: 'goal_explain', descKey: 'goal_explain_desc', route: '/explain', accent: '#4FC3F7' },
  ]

  const renderCard = (goal: GoalCard) => (
    <button
      key={goal.id}
      type="button"
      className="goal-card"
      onClick={() => navigate(goal.route)}
      style={{ '--goal-accent': goal.accent } as CSSProperties}
    >
      <span className="goal-card-heading">
        <span className="goal-icon"><AppIcon name={goal.id} size={18} /></span>
        <strong className="goal-card-title">{t(goal.titleKey)}</strong>
      </span>
      <span className="goal-card-desc">{t(goal.descKey)}</span>
      <span className="goal-card-art" aria-hidden="true"><AppIcon name={goal.id} size={42} /></span>
    </button>
  )

  return (
    <main className="page home-page content-with-nav">
      <section className="home-hero" aria-labelledby="home-title">
        <span className="home-hero-line" aria-hidden="true" />
        <span className="home-hero-dot" aria-hidden="true" />
        <div className="home-hero-content">
          <h1 id="home-title" className="visually-hidden">PokeQuery</h1>
          <SpriteIcon sprite="pokequery_wordmark" alt="PokeQuery" width={188} height={50} />
          <h2 className="home-title">{t('home_subtitle')}</h2>
          <p className="home-subtitle">{t('home_subtitle_secondary')}</p>
        </div>
      </section>

      <div className="goal-grid" aria-label={t('home_goals')}>
        {primaryGoals.map(renderCard)}
      </div>

      <section className="more-tools">
        <div className="more-tools-heading">
          <span className="more-tools-copy"><strong>{t('home_more_tools')}</strong><span>{t('home_more_tools_subtitle')}</span></span>
        </div>
        <div className="goal-grid">{moreGoals.map(renderCard)}</div>
      </section>
    </main>
  )
}
