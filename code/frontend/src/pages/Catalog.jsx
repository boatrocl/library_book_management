import { useContext, useEffect, useState } from 'react';
import api from '../api';
import BookCard from '../components/BookCard';
import BookDetailsDialog from '../components/BookDetailsDialog';
import { LanguageContext } from '../context/LanguageContext';

const PAGE_SIZE = 8;

const COPY = {
  th: {
    heroTitle: 'ค้นพบหนังสือที่ใช่สำหรับคุณ',
    searchPlaceholder: 'ค้นหาชื่อหนังสือ ผู้แต่ง หรือ ISBN',
    searchSubmit: 'ค้นหา',
    searchHint: 'ค้นหาจากชื่อหนังสือ ผู้แต่ง หรือหมายเลข ISBN',
    filters: 'ตัวกรอง',
    categories: 'หมวดหมู่',
    allCategories: 'ทั้งหมด',
    status: 'สถานะหนังสือ',
    allStatuses: 'ทุกสถานะ',
    available: 'พร้อมให้ยืม',
    unavailable: 'ไม่พร้อมให้ยืม',
    recommended: 'หนังสือแนะนำ',
    loading: 'กำลังค้นหารายการ…',
    count: (amount) => `พบหนังสือ ${amount.toLocaleString('th-TH')} รายการ`,
    forKeyword: (value) => `สำหรับ “${value}”`,
    sortLabel: 'เรียงตาม',
    newest: 'ล่าสุด',
    titleAsc: 'ชื่อหนังสือ (ก–ฮ)',
    titleDesc: 'ชื่อหนังสือ (ฮ–ก)',
    clear: 'ล้างตัวกรอง',
    searchError: 'ยังโหลดรายการหนังสือไม่ได้ ลองตรวจสอบการเชื่อมต่อแล้วกดอีกครั้ง',
    errorHeading: 'เชื่อมต่อรายการหนังสือไม่ได้',
    retry: 'ลองอีกครั้ง',
    emptyHeading: 'ยังไม่พบหนังสือที่ตรงกับการค้นหา',
    emptyCopy: 'ลองใช้คำค้นอื่น หรือกลับไปเลือกดูหนังสือทั้งหมด',
    showAll: 'ดูหนังสือทั้งหมด',
    details: 'ดูรายละเอียด',
    authorUnknown: 'ไม่ระบุผู้แต่ง',
    general: 'หนังสือทั่วไป',
    footer: 'เรื่องราวดี ๆ เริ่มต้นที่หน้าถัดไป',
    previous: 'ก่อนหน้า',
    next: 'ถัดไป',
    page: 'หน้า',
    of: 'จาก',
    searchByTitle: 'ชื่อหนังสือ',
    clearSearch: 'ล้างคำค้น',
    yearUnknown: 'ไม่ระบุปีพิมพ์',
    copiesUnavailable: 'ครบทุกเล่ม',
    copiesAvailable: (amount) => `ว่าง ${amount} เล่ม`,
  },
  en: {
    heroTitle: 'Find the book that feels right for you',
    searchPlaceholder: 'Search by title, author, or ISBN',
    searchSubmit: 'Search',
    searchHint: 'Search by book title, author, or ISBN',
    filters: 'Filters',
    categories: 'Categories',
    allCategories: 'All books',
    status: 'Availability',
    allStatuses: 'All statuses',
    available: 'Available',
    unavailable: 'Currently unavailable',
    recommended: 'Recommended books',
    loading: 'Searching books…',
    count: (amount) => `${amount.toLocaleString('en-US')} books found`,
    forKeyword: (value) => `for “${value}”`,
    sortLabel: 'Sort by',
    newest: 'Newest',
    titleAsc: 'Title (A–Z)',
    titleDesc: 'Title (Z–A)',
    clear: 'Clear filters',
    searchError: 'Books could not be loaded. Check your connection and try again.',
    errorHeading: 'Could not connect to the catalog',
    retry: 'Try again',
    emptyHeading: 'No books match your search',
    emptyCopy: 'Try another search or browse the full catalog.',
    showAll: 'Browse all books',
    details: 'View details',
    authorUnknown: 'Author not listed',
    general: 'General',
    footer: 'A good story begins on the next page',
    previous: 'Previous',
    next: 'Next',
    page: 'Page',
    of: 'of',
    searchByTitle: 'Book title',
    clearSearch: 'Clear search',
    yearUnknown: 'Year not listed',
    copiesUnavailable: 'All copies on loan',
    copiesAvailable: (amount) => `${amount} available`,
  },
};

function SearchIcon({ className = '' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="10.8" cy="10.8" r="6.8" stroke="currentColor" strokeWidth="1.8" />
      <path d="m16 16 4.2 4.2" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

function FilterIcon({ className = '' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 6h16M7 12h10m-7 6h4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

function BookOpenIcon({ className = '' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 7.2c-2.1-1.7-5.2-2.1-8.2-1.2v12.2c3-.9 6.1-.5 8.2 1.2m0-12.2c2.1-1.7 5.2-2.1 8.2-1.2v12.2c-3-.9-6.1-.5-8.2 1.2m0-12.2v12.2" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export default function Catalog() {
  const { language } = useContext(LanguageContext);
  const text = COPY[language] || COPY.th;
  const [books, setBooks] = useState([]);
  const [categories, setCategories] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loadedRequest, setLoadedRequest] = useState(null);
  const [searchInput, setSearchInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [availability, setAvailability] = useState('ALL');
  const [sort, setSort] = useState('id,desc');
  const [hasError, setHasError] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  const [selectedBook, setSelectedBook] = useState(null);

  const requestKey = `${page}:${keyword}:${categoryId}:${availability}:${sort}:${retryCount}`;
  const loading = loadedRequest !== requestKey;

  useEffect(() => {
    const controller = new AbortController();

    async function loadCategories() {
      try {
        const response = await api.get('/api/v1/categories', { signal: controller.signal });
        if (!controller.signal.aborted) setCategories(response.data || []);
      } catch {
        if (!controller.signal.aborted) setCategories([]);
      }
    }

    void loadCategories();
    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();

    async function loadBooks() {
      try {
        const params = {
          page,
          size: PAGE_SIZE,
          sort,
          availability,
          ...(keyword ? { keyword } : {}),
          ...(categoryId ? { categoryId } : {}),
        };
        const response = await api.get('/api/v1/books', {
          params,
          signal: controller.signal,
        });

        if (controller.signal.aborted) return;
        setBooks(response.data.content || []);
        setTotalPages(Math.max(response.data.totalPages || 0, 1));
        setTotalElements(response.data.totalElements || 0);
        setHasError(false);
      } catch {
        if (!controller.signal.aborted) {
          setBooks([]);
          setTotalPages(1);
          setTotalElements(0);
          setHasError(true);
        }
      } finally {
        if (!controller.signal.aborted) setLoadedRequest(requestKey);
      }
    }

    void loadBooks();
    return () => controller.abort();
  }, [availability, categoryId, keyword, page, requestKey, sort]);

  function handleSearch(event) {
    event.preventDefault();
    setPage(0);
    setKeyword(searchInput.trim());
  }

  function clearFilters() {
    setSearchInput('');
    setKeyword('');
    setCategoryId('');
    setAvailability('ALL');
    setPage(0);
  }

  function chooseCategory(nextCategoryId) {
    setCategoryId(nextCategoryId);
    setPage(0);
  }

  function chooseAvailability(nextAvailability) {
    setAvailability(nextAvailability);
    setPage(0);
  }

  return (
    <main className="catalog-page">
      <section className="catalog-hero" aria-labelledby="catalog-heading">
        <div className="catalog-hero__inner">
          <div className="catalog-hero__copy">
            <h1 id="catalog-heading">{text.heroTitle}</h1>
            <span className="catalog-hero__underline" />
            <form className="catalog-search" onSubmit={handleSearch} role="search">
              <SearchIcon className="catalog-search__icon" />
              <label className="sr-only" htmlFor="catalog-search-input">{text.searchByTitle}</label>
              <input
                id="catalog-search-input"
                type="search"
                placeholder={text.searchPlaceholder}
                value={searchInput}
                onChange={(event) => setSearchInput(event.target.value)}
              />
              {searchInput && (
                <button className="catalog-search__clear" type="button" onClick={() => setSearchInput('')} aria-label={text.clearSearch}>
                  ×
                </button>
              )}
              <button className="button button--primary catalog-search__submit" type="submit">
                <SearchIcon className="catalog-search__submit-icon" />
                <span>{text.searchSubmit}</span>
              </button>
            </form>
            <p className="catalog-hero__hint">{text.searchHint}</p>
          </div>
          <div className="catalog-hero__art" aria-hidden="true">
            <img src="/libraflow-hero.svg" alt="" />
          </div>
        </div>
      </section>

      <section className="catalog-layout" aria-label={text.recommended}>
        <aside className="category-sidebar" id="categories">
          <div className="filter-group">
            <div className="filter-group__heading">
              <FilterIcon className="filter-group__icon" />
              <h2>{text.categories}</h2>
              <span className="filter-group__chevron" aria-hidden="true">⌃</span>
            </div>
            <div className="category-list" aria-label={text.categories}>
              <button
                type="button"
                className={`category-option ${categoryId === '' ? 'is-active' : ''}`}
                aria-pressed={categoryId === ''}
                onClick={() => chooseCategory('')}
              >
                <span className="category-checkbox" aria-hidden="true">{categoryId === '' ? '✓' : ''}</span>
                <span>{text.allCategories}</span>
              </button>
              {categories.map((category) => (
                <button
                  type="button"
                  className={`category-option ${categoryId === String(category.id) ? 'is-active' : ''}`}
                  aria-pressed={categoryId === String(category.id)}
                  key={category.id}
                  onClick={() => chooseCategory(String(category.id))}
                >
                  <span className="category-checkbox" aria-hidden="true">{categoryId === String(category.id) ? '✓' : ''}</span>
                  <span>{category.name}</span>
                </button>
              ))}
              {categories.length === 0 && <p className="category-empty">{text.allCategories}</p>}
            </div>
          </div>

          <div className="filter-group filter-group--status">
            <div className="filter-group__heading">
              <BookOpenIcon className="filter-group__icon" />
              <h2>{text.status}</h2>
              <span className="filter-group__chevron" aria-hidden="true">⌃</span>
            </div>
            <div className="status-filter-list" aria-label={text.status}>
              <button
                type="button"
                className={`status-filter-option ${availability === 'ALL' ? 'is-active' : ''}`}
                aria-pressed={availability === 'ALL'}
                onClick={() => chooseAvailability('ALL')}
              >
                <span className="category-checkbox" aria-hidden="true">{availability === 'ALL' ? '✓' : ''}</span>
                <span>{text.allStatuses}</span>
              </button>
              <button
                type="button"
                className={`status-filter-option ${availability === 'AVAILABLE' ? 'is-active' : ''}`}
                aria-pressed={availability === 'AVAILABLE'}
                onClick={() => chooseAvailability('AVAILABLE')}
              >
                <span className="category-checkbox" aria-hidden="true">{availability === 'AVAILABLE' ? '✓' : ''}</span>
                <span>{text.available}</span>
              </button>
              <button
                type="button"
                className={`status-filter-option ${availability === 'UNAVAILABLE' ? 'is-active' : ''}`}
                aria-pressed={availability === 'UNAVAILABLE'}
                onClick={() => chooseAvailability('UNAVAILABLE')}
              >
                <span className="category-checkbox" aria-hidden="true">{availability === 'UNAVAILABLE' ? '✓' : ''}</span>
                <span>{text.unavailable}</span>
              </button>
            </div>
          </div>
        </aside>

        <div className="catalog-results">
          <div className="catalog-results__topline">
            <div>
              <h2>{text.recommended}</h2>
              <p className="results-count" aria-live="polite">
                {loading ? text.loading : text.count(totalElements)}
                {keyword && <> {text.forKeyword(keyword)}</>}
              </p>
            </div>
            <label className="sort-control">
              <span>{text.sortLabel}</span>
              <select value={sort} onChange={(event) => { setSort(event.target.value); setPage(0); }}>
                <option value="id,desc">{text.newest}</option>
                <option value="title,asc">{text.titleAsc}</option>
                <option value="title,desc">{text.titleDesc}</option>
              </select>
            </label>
          </div>

          {(keyword || categoryId || availability !== 'ALL') && (
            <div className="active-filters">
              {keyword && <span className="filter-pill">{keyword}</span>}
              {categoryId && <span className="filter-pill">{categories.find((item) => String(item.id) === categoryId)?.name || text.categories}</span>}
              {availability !== 'ALL' && <span className="filter-pill">{availability === 'AVAILABLE' ? text.available : text.unavailable}</span>}
              <button type="button" onClick={clearFilters}>{text.clear}</button>
            </div>
          )}

          {loading ? (
            <div className="book-grid" aria-label={text.loading} aria-busy="true">
              {Array.from({ length: 8 }, (_, index) => <div className="book-skeleton" key={index} />)}
            </div>
          ) : hasError ? (
            <div className="catalog-message catalog-message--error" role="alert">
              <span className="catalog-message__icon">!</span>
              <h3>{text.errorHeading}</h3>
              <p>{text.searchError}</p>
              <button className="button button--primary" type="button" onClick={() => setRetryCount((count) => count + 1)}>{text.retry}</button>
            </div>
          ) : books.length === 0 ? (
            <div className="catalog-message catalog-message--empty">
              <span className="catalog-message__book"><BookOpenIcon /></span>
              <h3>{text.emptyHeading}</h3>
              <p>{text.emptyCopy}</p>
              <button className="button button--secondary" type="button" onClick={clearFilters}>{text.showAll}</button>
            </div>
          ) : (
            <>
              <div className="book-grid">
                {books.map((book, index) => (
                  <BookCard key={book.id} book={book} index={index} labels={text} onDetails={setSelectedBook} />
                ))}
              </div>
              {totalPages > 1 && (
                <nav className="pagination" aria-label={text.recommended}>
                  <button
                    className="pagination__button pagination__button--icon"
                    type="button"
                    aria-label={text.previous}
                    disabled={page === 0}
                    onClick={() => setPage((current) => Math.max(current - 1, 0))}
                  >‹</button>
                  {Array.from({ length: totalPages }, (_, index) => (
                    <button
                      className={`pagination__button pagination__button--page ${page === index ? 'is-current' : ''}`}
                      type="button"
                      aria-current={page === index ? 'page' : undefined}
                      key={index}
                      onClick={() => setPage(index)}
                    >{index + 1}</button>
                  ))}
                  <button
                    className="pagination__button pagination__button--icon"
                    type="button"
                    aria-label={text.next}
                    disabled={page >= totalPages - 1}
                    onClick={() => setPage((current) => Math.min(current + 1, totalPages - 1))}
                  >›</button>
                  <span className="sr-only" aria-live="polite">{text.page} {page + 1} {text.of} {totalPages}</span>
                </nav>
              )}
            </>
          )}
        </div>
      </section>

      <footer className="catalog-footer" id="about">
        <BookOpenIcon />
        <span>LibraFlow</span>
        <span className="catalog-footer__dot">·</span>
        <span>{text.footer}</span>
      </footer>

      {selectedBook && <BookDetailsDialog book={selectedBook} onClose={() => setSelectedBook(null)} />}
    </main>
  );
}
