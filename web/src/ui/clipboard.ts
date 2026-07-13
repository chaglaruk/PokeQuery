// Localized clipboard helper that distinguishes outcomes:
// - 'copied'        -> success
// - 'denied'        -> permission denied (NotAllowedError)
// - 'unavailable'   -> no Clipboard API in this context
// - 'failed'        -> any other error
//
// Returns a `{ status, key }` pair so the screen can render localized feedback
// without having to interpret DOM exception names.

export type ClipboardStatus = 'copied' | 'denied' | 'unavailable' | 'failed'

export interface ClipboardResult {
  status: ClipboardStatus
  i18nKey: string
}

const keyFor: Record<ClipboardStatus, string> = {
  copied: 'clipboard_copied',
  denied: 'clipboard_denied',
  unavailable: 'clipboard_unavailable',
  failed: 'clipboard_failed',
}

export async function copyToClipboard(text: string): Promise<ClipboardResult> {
  if (typeof navigator === 'undefined' || !navigator.clipboard) {
    return { status: 'unavailable', i18nKey: keyFor.unavailable }
  }
  try {
    await navigator.clipboard.writeText(text)
    return { status: 'copied', i18nKey: keyFor.copied }
  } catch (err: unknown) {
    if (err instanceof DOMException) {
      if (err.name === 'NotAllowedError' || err.name === 'SecurityError') {
        return { status: 'denied', i18nKey: keyFor.denied }
      }
    }
    return { status: 'failed', i18nKey: keyFor.failed }
  }
}
