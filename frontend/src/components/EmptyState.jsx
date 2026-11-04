export const EmptyState = ({
  title = 'No hay registros',
  description = '',
  actionLabel,
  onAction,
  compact = false,
  className = '',
  style = {},
}) => {
  return (
    <div
      className={`empty-state ${className}`.trim()}
      style={{
        padding: compact ? '14px 12px' : '20px 16px',
        color: 'var(--suave)',
        textAlign: 'center',
        background: 'var(--papel)',
        border: '1px solid var(--linea)',
        borderRadius: '3px',
        ...style,
      }}
    >
      <div style={{ fontSize: compact ? '13px' : '14px', lineHeight: 1.45 }}>
        <div style={{ fontWeight: 600, color: 'var(--texto)', marginBottom: description ? '4px' : 0 }}>
          {title}
        </div>
        {description && (
          <div style={{ fontSize: compact ? '12px' : '13px', color: 'var(--suave)' }}>
            {description}
          </div>
        )}
      </div>
      {actionLabel && onAction && (
        <button
          type="button"
          className="btn lig chico"
          onClick={onAction}
          style={{ marginTop: '10px' }}
        >
          {actionLabel}
        </button>
      )}
    </div>
  )
}

export default EmptyState
