const frontendBaseUrl = normalizeBaseUrl(
  process.env.FRONTEND_BASE_URL ?? 'https://library-book-management-alpha.vercel.app',
);
const apiBaseUrl = normalizeBaseUrl(
  process.env.API_BASE_URL ?? 'https://library-book-management-ybt2.onrender.com',
);

const requestTimeoutMs = 15_000;
const maxAttempts = 4;

function normalizeBaseUrl(value) {
  const url = new URL(value);
  url.pathname = `${url.pathname.replace(/\/+$/, '')}/`;
  return url;
}

function endpoint(baseUrl, path) {
  return new URL(path.replace(/^\/+/, ''), baseUrl);
}

async function requestWithRetry(label, url) {
  let lastError;

  for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
    try {
      const response = await fetch(url, {
        headers: { Accept: 'text/html, application/json' },
        signal: AbortSignal.timeout(requestTimeoutMs),
      });
      const body = await response.text();

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      return { response, body };
    } catch (error) {
      lastError = error;
      if (attempt < maxAttempts) {
        await new Promise((resolve) => setTimeout(resolve, attempt * 1_500));
      }
    }
  }

  throw new Error(`${label} failed after ${maxAttempts} attempts: ${lastError.message}`);
}

async function verifyFrontendRoute(path) {
  const label = `Frontend ${path}`;
  const url = endpoint(frontendBaseUrl, path);
  const { response, body } = await requestWithRetry(label, url);
  const contentType = response.headers.get('content-type') ?? '';

  if (!contentType.includes('text/html') || !/<div\s+id=["']root["']\s*>/i.test(body)) {
    throw new Error(`${label} did not return the LibraFlow SPA document`);
  }

  console.log(`PASS ${label}: HTTP ${response.status} ${contentType}`);
}

async function verifyOpenApi() {
  const label = 'Backend OpenAPI';
  const url = endpoint(apiBaseUrl, '/v3/api-docs');
  const { response, body } = await requestWithRetry(label, url);
  let specification;

  try {
    specification = JSON.parse(body);
  } catch {
    throw new Error(`${label} returned invalid JSON`);
  }

  if (!specification.info?.title?.includes('LibraFlow') || !specification.paths?.['/api/v1/books']) {
    throw new Error(`${label} is missing the expected API title or public catalog route`);
  }

  console.log(`PASS ${label}: HTTP ${response.status}, ${Object.keys(specification.paths).length} paths`);
}

async function verifyPublicCatalog() {
  const label = 'Backend public catalog';
  const url = endpoint(apiBaseUrl, '/api/v1/books?size=1');
  const { response, body } = await requestWithRetry(label, url);
  let catalog;

  try {
    catalog = JSON.parse(body);
  } catch {
    throw new Error(`${label} returned invalid JSON`);
  }

  if (!Array.isArray(catalog.content) || !Number.isInteger(catalog.totalElements)) {
    throw new Error(`${label} response does not match the paginated catalog contract`);
  }

  console.log(`PASS ${label}: HTTP ${response.status}, ${catalog.totalElements} total books`);
}

await Promise.all([
  ...[
    '/',
    '/login',
    '/register',
    '/profile',
    '/categories',
    '/rules',
    '/about',
    '/admin/books',
    '/admin/loans',
    '/admin/books/1/copies',
    '/admin/fines',
    '/admin/reports',
    '/admin/users',
  ].map(verifyFrontendRoute),
  verifyOpenApi(),
  verifyPublicCatalog(),
]);

console.log('Production deployment smoke check passed.');
