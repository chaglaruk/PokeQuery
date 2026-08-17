import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'

export function PrivacyScreen() {
  const { t } = useI18n()
  const navigate = useNavigate()
  const fullPolicyUrl = `${import.meta.env.BASE_URL}privacy.html`

  return (
    <main className="page content-with-nav">
      <header className="page-header">
        <button type="button" className="back-btn" onClick={() => navigate(-1)} aria-label="Back">
          ←
        </button>
        <h1>{t('settings_privacy_policy')}</h1>
      </header>

      <div className="settings-stack">
        <section className="settings-panel">
          <h2 className="panel-title">Privacy Policy Summary</h2>
          <p className="setting-help">Effective Date: 17 August 2026</p>
          <p className="setting-help">
            This screen provides a concise overview of PokeQuery privacy practices. The full, authoritative policy document is available at{' '}
            <a
              href={fullPolicyUrl}
              target="_blank"
              rel="noopener noreferrer"
              style={{ color: 'var(--accent)', textDecoration: 'underline' }}
            >
              privacy.html
            </a>.
          </p>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">1. No Accounts or Gameplay Automation</h2>
          <p className="setting-help">
            PokeQuery never requests, collects, or accesses your Pokémon GO account credentials, tokens, or inventory. It communicates with no private game APIs and performs no OCR or automated gameplay.
          </p>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">2. No Ads, Tracking, or Analytics</h2>
          <p className="setting-help">
            PokeQuery contains zero third-party advertising SDKs, tracking pixels, or user analytics frameworks. We do not track users or sell user data.
          </p>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">3. Local Storage &amp; Clipboard</h2>
          <p className="setting-help">
            Settings, favorites, search history, and presets are saved strictly locally on your device. Android users can delete all local data by clearing app storage; Web/PWA users can clear site data in browser settings.
          </p>
          <p className="setting-help">
            When the user explicitly chooses Copy, PokeQuery places the generated search string on the system clipboard. PokeQuery does not upload copied search strings to the developer.
          </p>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">4. Network Access &amp; Event Guide Requests</h2>
          <p className="setting-help">
            The Android application&apos;s feature-level Internet access is used for read-only retrieval of the public GitHub-hosted Event Guide feed. The Web/PWA is served through its normal web host and loads application assets over the network, with feature-level external data retrieval limited to the public Event Guide feed.
          </p>
          <p className="setting-help">
            PokeQuery does not intentionally include Pokémon GO account data, search history, favourites, presets, or PokeQuery account identifiers in Event Guide requests. Standard HTTP connection metadata, such as IP address and User-Agent, may be processed by GitHub and network providers when serving the request.
          </p>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">5. Secure Data Handling &amp; Retention</h2>
          <p className="setting-help">
            All Event Guide requests use encrypted HTTPS connections. Local data relies on platform-level sandboxing and browser site-storage protections. Because the developer maintains no remote server database of user data, there is no remote account to delete.
          </p>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">6. Enquiries &amp; Full Document</h2>
          <p className="setting-help">
            Read the complete policy document at{' '}
            <a
              href={fullPolicyUrl}
              target="_blank"
              rel="noopener noreferrer"
              style={{ color: 'var(--accent)', textDecoration: 'underline' }}
            >
              privacy.html
            </a>{' '}
            or submit privacy enquiries on GitHub at{' '}
            <a
              href="https://github.com/chaglaruk/PokeQuery/issues"
              target="_blank"
              rel="noopener noreferrer"
              style={{ color: 'var(--accent)', textDecoration: 'underline' }}
            >
              https://github.com/chaglaruk/PokeQuery/issues
            </a>.
          </p>
        </section>
      </div>
    </main>
  )
}
