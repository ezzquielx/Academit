import { useState, useMemo } from 'react'
import EmptyState from './EmptyState'
import { ETIQUETAS_VACIO } from '../config/etiquetas'

export const DataTable = ({
  columns = [],
  data = [],
  keyField = 'id',
  searchable = true,
  searchPlaceholder = 'Buscar en los registros...',
  searchKeys = [],
  pageSize = 10,
  actions = null,
  caption = null,
  renderExpanded = null,
  emptyTitle = ETIQUETAS_VACIO.SIN_REGISTROS,
  emptyDescription = ETIQUETAS_VACIO.SIN_REGISTROS_DETALLE,
}) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [sortConfig, setSortConfig] = useState({ key: null, asc: true })
  const [expandedRows, setExpandedRows] = useState({})

  // Filtrado por buscador
  const filteredData = useMemo(() => {
    if (!searchTerm.trim()) return data

    const query = searchTerm.toLowerCase().trim()
    return data.filter((row) => {
      if (searchKeys.length > 0) {
        return searchKeys.some((k) => {
          const val = row[k]
          return val !== undefined && val !== null && String(val).toLowerCase().includes(query)
        })
      }
      return Object.values(row).some((val) => {
        if (typeof val === 'string' || typeof val === 'number') {
          return String(val).toLowerCase().includes(query)
        }
        return false
      })
    })
  }, [data, searchTerm, searchKeys])

  // Ordenamiento por columna
  const sortedData = useMemo(() => {
    if (!sortConfig.key) return filteredData

    return [...filteredData].sort((a, b) => {
      const valA = a[sortConfig.key]
      const valB = b[sortConfig.key]

      if (valA === valB) return 0
      if (valA === null || valA === undefined) return 1
      if (valB === null || valB === undefined) return -1

      const comp =
        typeof valA === 'number' && typeof valB === 'number'
          ? valA - valB
          : String(valA).localeCompare(String(valB), 'es', { numeric: true })
      return sortConfig.asc ? comp : -comp
    })
  }, [filteredData, sortConfig])

  // Paginación
  const totalPages = Math.ceil(sortedData.length / pageSize) || 1
  const validCurrentPage = Math.min(currentPage, totalPages)

  const paginatedData = useMemo(() => {
    const start = (validCurrentPage - 1) * pageSize
    return sortedData.slice(start, start + pageSize)
  }, [sortedData, validCurrentPage, pageSize])

  const handleSort = (key) => {
    if (!key) return
    setSortConfig((prev) => {
      if (prev.key === key) {
        return { key, asc: !prev.asc }
      }
      return { key, asc: true }
    })
  }

  const toggleRow = (id) => {
    setExpandedRows((prev) => ({
      ...prev,
      [id]: !prev[id],
    }))
  }

  const getRowKey = (row, idx) => {
    if (typeof keyField === 'function') return keyField(row)
    return row[keyField] || row.id || row.idAlumno || row.idDocente || row.idCurso || row.idMateria || row.idCuota || row.idPago || row.idSolicitud || row.idMesa || idx
  }

  return (
    <div className="bloque">
      {/* Barra de filtros / buscador */}
      {(searchable || actions) && (
        <div className="filtros" style={{ marginBottom: '16px' }}>
          {searchable && (
            <div className="campo" style={{ flex: '1 1 260px', maxWidth: '400px' }}>
              <input
                type="search"
                className="c"
                placeholder={searchPlaceholder}
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value)
                  setCurrentPage(1)
                }}
              />
            </div>
          )}

          {actions && (
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginLeft: 'auto' }}>
              {actions}
            </div>
          )}
        </div>
      )}

      {sortedData.length === 0 ? (
        <EmptyState
          title={searchTerm ? 'No se encontraron resultados' : emptyTitle}
          description={
            searchTerm
              ? `No se encontró ningún registro que coincida con "${searchTerm}".`
              : emptyDescription
          }
          actionLabel={searchTerm ? 'Limpiar búsqueda' : undefined}
          onAction={searchTerm ? () => setSearchTerm('') : undefined}
        />
      ) : (
        <div className="tabla-caja">
          <table>
            {caption && <caption>{caption}</caption>}
            <thead>
              <tr>
                {columns.map((col, idx) => {
                  const isNum = col.align === 'right' || col.isNumber
                  const isSortable = col.sortable !== false && (col.key || col.sortKey)
                  const sortKey = col.sortKey || col.key
                  const isSorted = sortConfig.key === sortKey

                  return (
                    <th
                      key={col.key || idx}
                      scope="col"
                      className={isNum ? 'd' : ''}
                      style={{ width: col.width || 'auto' }}
                    >
                      {isSortable ? (
                        <button
                          type="button"
                          onClick={() => handleSort(sortKey)}
                          aria-label={`Ordenar por ${col.label}`}
                        >
                          {col.label}
                          {isSorted && (sortConfig.asc ? ' ↑' : ' ↓')}
                        </button>
                      ) : (
                        col.label
                      )}
                    </th>
                  )
                })}
              </tr>
            </thead>
            <tbody>
              {paginatedData.map((row, rIdx) => {
                const rowKey = getRowKey(row, rIdx)
                const isExpanded = !!expandedRows[rowKey]

                return (
                  <tr key={rowKey}>
                    {columns.map((col, cIdx) => {
                      const isNum = col.align === 'right' || col.isNumber
                      const isFirst = cIdx === 0
                      const cellClasses = [
                        isNum ? 'd num' : '',
                        col.className || '',
                        isFirst ? 'titulo-ficha' : '',
                      ].filter(Boolean).join(' ')

                      return (
                        <td
                          key={col.key || cIdx}
                          data-l={col.label}
                          className={cellClasses}
                        >
                          {isFirst && renderExpanded && (
                            <button
                              className="mas"
                              type="button"
                              aria-expanded={isExpanded}
                              onClick={() => toggleRow(rowKey)}
                              aria-label={isExpanded ? 'Ocultar detalle' : 'Ver detalle'}
                            >
                              {isExpanded ? '−' : '+'}
                            </button>
                          )}
                          {col.render ? col.render(row) : (row[col.key] !== undefined && row[col.key] !== null ? row[col.key] : '')}
                        </td>
                      )
                    })}
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Paginación simple y contador de registros */}
      {sortedData.length > 0 && (
        <div
          className="d-flex flex-wrap justify-content-between align-items-center gap-2 pt-3"
          style={{ color: 'var(--suave)', fontSize: '13.5px' }}
        >
          <span>
            Mostrando <strong>{(validCurrentPage - 1) * pageSize + 1}</strong> a{' '}
            <strong>{Math.min(validCurrentPage * pageSize, sortedData.length)}</strong> de{' '}
            <strong>{sortedData.length}</strong> registros
          </span>

          {totalPages > 1 && (
            <div className="d-flex align-items-center gap-1">
              <button
                type="button"
                className="btn lig chico"
                disabled={validCurrentPage === 1}
                onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
              >
                Anterior
              </button>
              <span style={{ padding: '0 8px', fontVariantNumeric: 'tabular-nums' }}>
                {validCurrentPage} / {totalPages}
              </span>
              <button
                type="button"
                className="btn lig chico"
                disabled={validCurrentPage === totalPages}
                onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
              >
                Siguiente
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export const TablaDatos = DataTable
export default DataTable
