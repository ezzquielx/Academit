#!/usr/bin/env node
/**
 * Auditoría automática de UI de Academit (VITE_USE_MOCK=true).
 *
 * Recorre el menú de src/config/menu.js con cada rol y a 4 anchos, y reporta
 * errores de consola/página, respuestas fallidas, pantallas en blanco,
 * redirecciones inesperadas, claves crudas de enum, scroll horizontal y
 * controles chicos en móvil.
 *
 * Uso:
 *   VITE_USE_MOCK=true npm run dev -- --port 5199   (en otra terminal)
 *   node tools/auditar-ui.mjs [--base http://127.0.0.1:5199] [--out docs/estado/auditoria-ui.md]
 *
 * Requiere `playwright-core` (instalado en el proyecto o accesible por NODE_PATH).
 * El navegador se toma de $AUDIT_BROWSER o de Helium/LibreWolf del sistema.
 */
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const repoRoot = resolve(__dirname, '..')

const args = process.argv.slice(2)
const arg = (name, fallback) => {
  const i = args.indexOf(name)
  return i >= 0 && args[i + 1] ? args[i + 1] : fallback
}

const BASE = arg('--base', 'http://127.0.0.1:5199')
const OUT = resolve(repoRoot, arg('--out', 'docs/estado/auditoria-ui.md'))
const WIDTHS = [360, 768, 1024, 1440]
const CREDS = [
  { rol: 'ADMINISTRADOR', username: 'admin' },
  { rol: 'ADMINISTRATIVO', username: 'administrativo' },
  { rol: 'DOCENTE', username: 'docente' },
  { rol: 'ALUMNO', username: 'alumno' },
]
const PASSWORD = '1234'

const ENUM_CRUDO = /\b(?:ESTADO|TIPO|CONDICION|TURNO|REGIMEN|MODALIDAD)_[A-Z_]+\b/

async function loadPlaywright() {
  const candidates = [
    'playwright-core',
    '/tmp/opencode/pw/node_modules/playwright-core/index.js',
  ]
  for (const c of candidates) {
    try {
      const mod = await import(c)
      return mod.chromium ? mod : mod.default
    } catch {
      /* siguiente */
    }
  }
  throw new Error('No se encontró playwright-core. Instalalo o definí NODE_PATH.')
}

/** Extrae path/label/roles de los ítems de menu.js sin ejecutar JSX. */
function leerMenu() {
  const src = readFileSync(resolve(repoRoot, 'frontend/src/config/menu.js'), 'utf8')
  const items = []
  const re = /path:\s*'([^']+)',\s*\n\s*label:\s*'([^']+)',[\s\S]*?roles:\s*\[([^\]]*)\]/g
  let m
  while ((m = re.exec(src))) {
    items.push({
      path: m[1],
      label: m[2],
      roles: m[3].split(',').map((r) => r.trim().replace(/'/g, '')).filter(Boolean),
    })
  }
  return items
}

function rutasDeRol(menu, rol) {
  return menu.filter((i) => i.roles.includes(rol)).map((i) => i.path)
}

async function login(page, username, { password = PASSWORD, navigate = true } = {}) {
  if (navigate) {
    await page.goto(`${BASE}/login`, { waitUntil: 'domcontentloaded', timeout: 60000 })
  }
  await page.waitForSelector('input[type="password"]', { timeout: 30000 })
  await page.fill('#username, input[name="username"], input[type="text"]', username)
  await page.fill('input[type="password"]', password)
  await Promise.all([
    page.waitForURL((u) => !u.pathname.startsWith('/login'), { timeout: 30000 }),
    page.click('button[type="submit"]'),
  ])
  // Dejar que la navegación SPA posterior al login termine antes de seguir.
  await page.waitForTimeout(400)
}

async function registrarAlumnoNuevo(page) {
  await page.goto(`${BASE}/registro`, { waitUntil: 'domcontentloaded', timeout: 60000 })
  await page.waitForSelector('#regUsername', { timeout: 30000 })
  const sello = String(Date.now()).slice(-6)
  await page.fill('#nombre', 'Prueba')
  await page.fill('#apellido', `Auditoría${sello}`)
  await page.fill('#dni', `4${sello}1`)
  await page.fill('#email', `auditoria${sello}@correo.com`)
  await page.fill('#telefono', '11-0000-0000')
  await page.fill('#fechaNacimiento', '2003-05-05')
  await page.fill('#domicilio', 'Calle Prueba 123')
  await page.fill('#regUsername', `auditoria${sello}`)
  await page.fill('#regPassword', 'prueba123')
  await page.fill('#confirmPassword', 'prueba123')
  await page.click('button[type="submit"]')
  await page.waitForSelector('text=Registro Recibido', { timeout: 30000 })
  // Navegación SPA al login: conserva el banco mock recién creado.
  await page.click('a[href="/login"]')
  await page.waitForURL((u) => u.pathname === '/login', { timeout: 30000 })
  return { username: `auditoria${sello}`, password: 'prueba123' }
}

async function revisarPagina(page, { rol, ruta, ancho, problemas }) {
  const registrar = (problema) => problemas.push({ rol, ruta, ancho, problema })

  const consola = []
  const erroresPagina = []
  const fallidas = []
  const onConsole = (msg) => {
    if (msg.type() === 'error') consola.push(msg.text())
  }
  const onPageError = (err) => erroresPagina.push(err.message)
  const onResponse = (res) => {
    if (res.status() >= 400) fallidas.push(`${res.status()} ${res.url()}`)
  }
  page.on('console', onConsole)
  page.on('pageerror', onPageError)
  page.on('response', onResponse)

  try {
    const irA = () => page.goto(`${BASE}${ruta}`, { waitUntil: 'domcontentloaded', timeout: 45000 })
    try {
      await irA()
    } catch (err) {
      // ERR_ABORTED puede ocurrir si la navegación SPA previa todavía está cerrándose.
      if (!/ERR_ABORTED/.test(err.message)) throw err
      await page.waitForTimeout(500)
      await irA()
    }
    await page.waitForTimeout(800)

    const url = new URL(page.url())
    if (url.pathname !== ruta && ruta !== '/') {
      registrar(`redirección inesperada a ${url.pathname}`)
    }

    const info = await page.evaluate(() => {
      const main = document.querySelector('main, .pagina, #root')
      const texto = (main?.innerText || document.body.innerText || '').trim()
      const doc = document.documentElement
      // El menú off-canvas y el encabezado de tabla en modo ficha quedan
      // fuera del viewport a propósito: no son defectos de layout.
      const fueraDeJuego = (el) =>
        el.closest('thead') ||
        el.closest('aside.lateral:not(.abierto)') ||
        el.closest('.tabla-caja') ||
        el.closest('.velo') ||
        el.closest('[aria-hidden="true"]')
      const fuera = []
      for (const el of document.querySelectorAll('main *, aside *')) {
        if (fueraDeJuego(el)) continue
        const r = el.getBoundingClientRect()
        const estilo = getComputedStyle(el)
        if (estilo.position === 'absolute' && r.right < 0) continue
        if (r.width > 0 && r.height > 0 && (r.right > window.innerWidth + 2 || r.left < -2)) {
          const desc = `${el.tagName.toLowerCase()}${el.className && typeof el.className === 'string' ? '.' + el.className.split(' ')[0] : ''}`
          if (!fuera.includes(desc)) fuera.push(desc)
          if (fuera.length >= 3) break
        }
      }
      const chicos = []
      if (window.innerWidth <= 700) {
        for (const el of document.querySelectorAll('button, a.btn, .btn')) {
          if (fueraDeJuego(el)) continue
          const r = el.getBoundingClientRect()
          if (r.width > 0 && r.height > 0 && r.right > 0 && r.height < 40) {
            const desc = (el.innerText || el.getAttribute('aria-label') || el.tagName).trim().slice(0, 30)
            if (!chicos.includes(desc)) chicos.push(desc)
          }
        }
      }
      return {
        texto,
        scrollX: doc.scrollWidth > doc.clientWidth + 1,
        fuera,
        chicos,
      }
    })

    if (!info.texto || info.texto.length < 20) registrar('pantalla vacía o sin contenido')
    if (/Cannot read/i.test(info.texto)) registrar('texto de error "Cannot read…" en pantalla')
    const crudo = info.texto.match(ENUM_CRUDO)
    if (crudo) registrar(`clave cruda visible: ${crudo[0]}`)
    if (info.scrollX) registrar('scroll horizontal de la página')
    if (info.fuera.length) registrar(`elementos fuera del viewport: ${info.fuera.join(', ')}`)
    if (info.chicos.length) registrar(`controles < 40px: ${info.chicos.join(' | ')}`)
  } catch (err) {
    registrar(`no se pudo cargar: ${err.message.split('\n')[0]}`)
  }

  const limpiar = (lista) => [...new Set(lista)].slice(0, 3)
  for (const c of limpiar(consola)) registrar(`consola: ${c}`)
  for (const e of limpiar(erroresPagina)) registrar(`pageerror: ${e}`)
  for (const f of limpiar(fallidas)) registrar(`respuesta fallida: ${f}`)

  page.off('console', onConsole)
  page.off('pageerror', onPageError)
  page.off('response', onResponse)
}

async function main() {
  const { chromium } = await loadPlaywright()
  const executablePath =
    process.env.AUDIT_BROWSER ||
    `${process.env.HOME}/.local/belka/bin/helium`
  const menu = leerMenu()
  if (menu.length === 0) throw new Error('No se pudo leer menu.js')

  const browser = await chromium.launch({
    executablePath,
    headless: true,
    args: ['--no-sandbox'],
  })

  const problemas = []

  for (const cred of CREDS) {
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
    const page = await context.newPage()
    try {
      await login(page, cred.username)
    } catch (err) {
      problemas.push({
        rol: cred.rol,
        ruta: '/login',
        ancho: 1440,
        problema: `login falló: ${err.message.split('\n')[0]}`,
      })
      await context.close()
      continue
    }
    const rutas = rutasDeRol(menu, cred.rol)
    for (const ancho of WIDTHS) {
      await page.setViewportSize({ width: ancho, height: 900 })
      for (const ruta of rutas) {
        await revisarPagina(page, { rol: cred.rol, ruta, ancho, problemas })
      }
    }
    await context.close()
  }

  // Alumno recién registrado: debe navegar sin datos precargados
  {
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
    const page = await context.newPage()
    try {
      const nuevo = await registrarAlumnoNuevo(page)
      await login(page, nuevo.username, { password: nuevo.password, navigate: false })
      const rutas = rutasDeRol(menu, 'ALUMNO')
      for (const ancho of [360, 1440]) {
        await page.setViewportSize({ width: ancho, height: 900 })
        for (const ruta of rutas) {
          await revisarPagina(page, { rol: 'ALUMNO_NUEVO', ruta, ancho, problemas })
        }
      }
    } catch (err) {
      try {
        const texto = (await page.locator('body').innerText()).slice(0, 400)
        console.log('DEBUG ALUMNO_NUEVO', page.url(), JSON.stringify(texto))
      } catch {
        /* sin texto */
      }
      problemas.push({
        rol: 'ALUMNO_NUEVO',
        ruta: '/registro',
        ancho: 1440,
        problema: `registro/login falló: ${err.message.split('\n')[0]}`,
      })
    }
    await context.close()
  }

  await browser.close()

  const filas = problemas.length
    ? problemas.map((p) => `| ${p.rol} | ${p.ruta} | ${p.ancho} | ${p.problema.replace(/\|/g, '/')} |`)
    : ['| — | — | — | Sin problemas detectados |']

  // Hallazgos de la primera corrida (punto de partida del bugfix).
  const baseline = [
    ['ADMINISTRADOR', '/solicitudes/bandeja', 360, 'Botones de filtro/estado fuera del viewport', 'Resuelto: `.btn-group` se apila a ancho completo en móvil'],
    ['ADMINISTRADOR', '/administrativo/cuotas', 360, 'Botones de filtro fuera del viewport', 'Resuelto: idem filtros de cuotas'],
    ['ADMINISTRATIVO', '/solicitudes/bandeja', 360, 'Botones de filtro/estado fuera del viewport', 'Resuelto: idem bandeja'],
    ['ADMINISTRATIVO', '/administrativo/cuotas', 360, 'Botones de filtro fuera del viewport', 'Resuelto: idem cuotas'],
    ['DOCENTE', '/docencia/mis-cursos', 360, 'Acciones de la tarjeta fuera del viewport', 'Resuelto: filas de acciones con `flex-wrap`'],
    ['DOCENTE', '/docencia/mis-cursos', 768, 'Acciones de la tarjeta fuera del viewport', 'Resuelto: idem'],
    ['DOCENTE', '/docencia/mis-cursos', 1024, 'Acciones de la tarjeta fuera del viewport', 'Resuelto: idem'],
    ['ALUMNO_NUEVO', '/registro', 1440, 'El alumno registrado no podía iniciar sesión en mock', 'Resuelto: el registro mock crea alumno + usuario con `idAlumno`'],
  ]

  const md = [
    '# Auditoría automática de UI',
    '',
    `Generada con \`node tools/auditar-ui.mjs\` contra ${BASE} (VITE_USE_MOCK=true).`,
    `Anchos: ${WIDTHS.join(', ')} px. Roles: ${CREDS.map((c) => c.rol).join(', ')}, ALUMNO_NUEVO.`,
    '',
    '## Resultado actual',
    '',
    '| Rol | Ruta | Ancho | Problema |',
    '| --- | --- | ---: | --- |',
    ...filas,
    '',
    `Total de hallazgos: ${problemas.length}.`,
    '',
    '## Antes / después',
    '',
    '| Rol | Ruta | Ancho | Hallazgo inicial | Estado |',
    '| --- | --- | ---: | --- | --- |',
    ...baseline.map((b) => `| ${b[0]} | ${b[1]} | ${b[2]} | ${b[3]} | ${b[4]} |`),
    '',
    '## Pendientes que el script no puede resolver',
    '',
    '- **Aprobar/rechazar declaraciones juradas y descargar PDF:** no hay endpoint en el contrato;',
    '  el botón de descarga queda deshabilitado con nota (ver `docs/pendientes.md`).',
    '- **`idAlumno` en la sesión real:** `UsuarioSesionDTO` no lo expone, así que con `VITE_USE_MOCK=false`',
    '  el frontend no puede identificar al alumno logueado y muestra el aviso de legajo (ver `docs/pendientes.md`).',
    '',
  ].join('\n')

  mkdirSync(dirname(OUT), { recursive: true })
  writeFileSync(OUT, md)
  console.log(md.split('\n').slice(0, 40).join('\n'))
  console.log(`\nGuardado en ${OUT} (${problemas.length} hallazgos)`)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
