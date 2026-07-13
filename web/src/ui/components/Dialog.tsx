import { useEffect, useRef, type ReactNode } from 'react'

interface DialogProps {
  open: boolean
  title: string
  onClose: () => void
  closeLabel: string
  children: ReactNode
}

export function Dialog({ open, title, onClose, closeLabel, children }: DialogProps) {
  const dialogRef = useRef<HTMLDivElement>(null)
  const previousFocusRef = useRef<HTMLElement | null>(null)

  useEffect(() => {
    if (!open) return
    previousFocusRef.current = (document.activeElement as HTMLElement) ?? null
    // Move focus into the dialog after the browser has painted.
    const t = setTimeout(() => {
      const firstFocusable = dialogRef.current?.querySelector(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      ) as HTMLElement | null
      if (firstFocusable) firstFocusable.focus()
      else dialogRef.current?.focus()
    }, 0)
    return () => clearTimeout(t)
  }, [open])

  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.stopPropagation()
        onClose()
      }
    }
    document.addEventListener('keydown', onKey, true)
    return () => document.removeEventListener('keydown', onKey, true)
  }, [open, onClose])

  // Restore previous focus when the dialog closes.
  useEffect(() => {
    if (!open && previousFocusRef.current) {
      previousFocusRef.current.focus?.()
      previousFocusRef.current = null
    }
  }, [open])

  // Lock body scroll while open.
  useEffect(() => {
    if (!open) return
    const prev = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => { document.body.style.overflow = prev }
  }, [open])

  if (!open) return null

  return (
    <div
      className="dialog-overlay"
      role="presentation"
      onClick={onClose}
    >
      <div
        ref={dialogRef}
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        aria-describedby="dialog-desc"
        tabIndex={-1}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="dialog-header">
          <h2 className="dialog-title">{title}</h2>
          <button
            type="button"
            className="dialog-close"
            onClick={onClose}
            aria-label={closeLabel}
          >
            {'\u2715'}
          </button>
        </div>
        <div id="dialog-desc" className="dialog-body">
          {children}
        </div>
      </div>
    </div>
  )
}
