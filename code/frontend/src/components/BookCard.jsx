const COVER_THEMES = [
  { background: '#e7e0f3', ink: '#182c58', accent: '#7970c8', accent2: '#b2a5dd', accent3: '#f3c76a' },
  { background: '#277f96', ink: '#fffdf4', accent: '#f4bd63', accent2: '#155c75', accent3: '#3e9bb0' },
  { background: '#f7eee0', ink: '#1b3151', accent: '#ed664a', accent2: '#eaa676', accent3: '#d6bd9c' },
  { background: '#176274', ink: '#fffdf4', accent: '#f3c965', accent2: '#0c4659', accent3: '#37818b' },
  { background: '#e4ebd8', ink: '#203a43', accent: '#427c68', accent2: '#99b69a', accent3: '#d79c64' },
  { background: '#e8e2d6', ink: '#293954', accent: '#567995', accent2: '#b96e5e', accent3: '#e0b25e' },
];

function CoverScene({ variant, theme }) {
  return (
    <svg className="book-cover__scene" viewBox="0 0 240 300" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      <rect width="240" height="300" fill={theme.background} />
      <circle cx={variant % 2 === 0 ? 178 : 168} cy="149" r={variant === 2 ? 42 : 34} fill={theme.accent} />
      {variant === 0 && (
        <>
          <path d="M0 194 82 147l57 35 47-50 54 33v135H0Z" fill={theme.accent2} />
          <path d="m0 227 74-33 60 38 48-44 58 35v77H0Z" fill={theme.accent} />
          <path d="m0 265 69-34 60 28 48-32 63 23v48H0Z" fill="#f7f2e8" />
          <path d="M18 240h105v9H18zm11 14h105v8H29zm12 13h105v8H41z" fill={theme.accent3} opacity=".9" />
        </>
      )}
      {variant === 1 && (
        <>
          <path d="m0 215 52-61 35 37 49-74 49 66 22-24 33 40v101H0Z" fill={theme.accent2} />
          <path d="m0 240 62-38 38 26 51-61 40 38 49-20v85H0Z" fill={theme.accent} />
          <path d="m0 273 57-25 45 18 55-34 37 21 46-13v60H0Z" fill={theme.accent2} opacity=".78" />
          <path d="M0 282q62-20 118 0t122-2v20H0Z" fill="#e1b76a" />
        </>
      )}
      {variant === 2 && (
        <>
          <path d="M14 300V167a43 43 0 0 1 86 0v133Z" fill={theme.accent2} />
          <path d="M72 300V197a42 42 0 0 1 84 0v103Z" fill={theme.accent} />
          <path d="M139 300V151a40 40 0 0 1 80 0v149Z" fill={theme.accent2} opacity=".74" />
          <path d="M0 273h120v27H0zm117-17h123v44H117Z" fill="#dbbd99" />
          <path d="M179 0h61v171h-61z" fill="#f0f2ea" opacity=".45" />
        </>
      )}
      {variant === 3 && (
        <>
          <path d="M0 242 60 194l51 23 58-49 71 44v88H0Z" fill={theme.accent2} />
          <path d="M0 266q57-30 120-4t120 0v38H0Z" fill={theme.accent} />
          <path d="M57 240h10v60H57z" fill="#543d35" />
          <ellipse cx="60" cy="207" rx="46" ry="24" fill="#174d57" />
          <ellipse cx="37" cy="194" rx="24" ry="18" fill="#2b6c69" />
          <ellipse cx="78" cy="185" rx="30" ry="21" fill="#347d72" />
          <path d="M0 286q47-18 90 0t150-4v18H0Z" fill="#e4bd6f" />
        </>
      )}
      {variant === 4 && (
        <>
          <path d="M0 205q55-42 112 0t128-5v100H0Z" fill={theme.accent2} />
          <path d="M0 231q61-33 120 0t120-3v72H0Z" fill={theme.accent} />
          <path d="M0 265q70-28 143 0t97-1v36H0Z" fill="#f2e8d4" />
          <path d="M23 252c27-37 48-38 69 0m-53-13c27-35 49-37 68 0" fill="none" stroke={theme.accent3} strokeWidth="7" strokeLinecap="round" />
        </>
      )}
      {variant === 5 && (
        <>
          <path d="M0 185h58v115H0zm60-35h61v150H60zm63 54h51v96h-51zm53-74h64v170h-64z" fill={theme.accent2} />
          <path d="M0 232h58v68H0zm60 9h61v59H60zm63-9h51v68h-51zm53-20h64v88h-64z" fill={theme.accent} />
          <path d="M0 280h240v20H0Z" fill="#f2e9d7" opacity=".75" />
          <path d="M27 194v38m62-62v68m58 15v32m56-112v90" stroke="#fffdf4" strokeWidth="3" opacity=".5" />
        </>
      )}
      <path d="M0 0h240v300H0z" fill="url(#cover-grain)" opacity=".045" />
      <defs>
        <pattern id="cover-grain" width="8" height="8" patternUnits="userSpaceOnUse">
          <path d="M0 8 8 0M-2 2 2-2M6 10l4-4" stroke="#172b42" strokeWidth=".6" />
        </pattern>
      </defs>
    </svg>
  );
}

function BookCover({ book, index, labels }) {
  const seed = Number(book.id) || index;
  const themeIndex = Math.abs(seed - 1) % COVER_THEMES.length;
  const theme = COVER_THEMES[themeIndex];
  const title = book.title || labels.newBook;
  const author = book.authors?.[0] || labels.library;

  return (
    <div className="book-cover" style={{ '--cover-ink': theme.ink }} aria-hidden="true">
      <CoverScene variant={themeIndex} theme={theme} />
      <span className="book-cover__topline">LIBRAFLOW · {labels.readingRoom}</span>
      <span className="book-cover__title">{title}</span>
      <span className="book-cover__author">{author}</span>
      <span className="book-cover__spine" />
    </div>
  );
}

export default function BookCard({ book, index, labels, onDetails }) {
  const available = Number(book.availableCopies || 0);

  return (
    <article className="book-card">
      <div className="book-card__art">
        <BookCover book={book} index={index} labels={labels} />
        <span className={`availability ${available > 0 ? 'availability--available' : 'availability--unavailable'}`}>
          <span className="availability__dot" />
          {available > 0 ? labels.available : labels.unavailable}
        </span>
      </div>
      <div className="book-card__body">
        <span className="book-card__category">{labels.categoryName(book.categoryName) || labels.general}</span>
        <h3 title={book.title}>{book.title || labels.bookTitleUnknown}</h3>
        <p className="book-card__author">{book.authors?.length ? book.authors.join(', ') : labels.authorUnknown}</p>
        <div className="book-card__meta">
          <span>{book.publishYear || labels.yearUnknown}</span>
          <span className="book-card__meta-dot">·</span>
          <span>{available > 0 ? labels.copiesAvailable(available) : labels.copiesUnavailable}</span>
        </div>
        <button className="book-card__details" type="button" onClick={() => onDetails(book)} aria-label={`${labels.details}: ${book.title}`}>
          {labels.details} <span aria-hidden="true">›</span>
        </button>
      </div>
    </article>
  );
}
