import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import LibraryRules from './LibraryRules';

export default function BookDetailsDialog({ book, labels, categoryName, user, borrowState, reservationState, onBorrow, onReserve, onClose }) {
  const dialogRef = useRef(null);
  const [activeTab, setActiveTab] = useState('details');
  const [termsAccepted, setTermsAccepted] = useState(false);
  const handleTabKeyDown = (event) => {
    const tabs = ['details', 'rules'];
    let nextTab;
    if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
      nextTab = tabs[(tabs.indexOf(activeTab) + 1) % tabs.length];
    } else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
      nextTab = tabs[(tabs.indexOf(activeTab) - 1 + tabs.length) % tabs.length];
    } else if (event.key === 'Home') {
      nextTab = tabs[0];
    } else if (event.key === 'End') {
      nextTab = tabs[tabs.length - 1];
    } else {
      return;
    }

    event.preventDefault();
    setActiveTab(nextTab);
    document.getElementById(`book-${nextTab}-tab`)?.focus();
  };
  const availableCopies = Number(book.availableCopies || 0);
  const memberActionPending = user?.role === 'MEMBER'
    && borrowState.status !== 'success'
    && reservationState.status !== 'success';
  const dueDate = borrowState.loan?.items?.[0]?.dueDate;
  const formattedDueDate = dueDate
    ? new Intl.DateTimeFormat(labels.locale, { dateStyle: 'medium' })
      .format(new Date(`${dueDate}T00:00:00`))
    : null;

  useEffect(() => {
    const dialog = dialogRef.current;
    if (dialog && !dialog.open) dialog.showModal();
  }, []);

  return (
    <dialog
      ref={dialogRef}
      className="book-dialog"
      aria-labelledby="book-dialog-title"
      onClose={onClose}
    >
      <button className="book-dialog__close" type="button" onClick={() => dialogRef.current?.close()} aria-label={labels.closeDetails}>×</button>
      <div className="book-dialog__cover" aria-hidden="true">
        <span className="book-dialog__cover-label">LIBRAFLOW<br />{labels.readingRoom}</span>
        <span className="book-dialog__cover-title">{book.title}</span>
        <span className="book-dialog__cover-author">{book.authors?.[0] || labels.library}</span>
      </div>
      <div className="book-dialog__content">
        <span className="section-kicker">{labels.detailsHeading}</span>
        <h2 id="book-dialog-title">{book.title}</h2>
        <p className="book-dialog__authors">{book.authors?.length ? book.authors.join(', ') : labels.authorUnknown}</p>
        <div className="book-dialog__tabs" role="tablist" aria-label={labels.bookInfoTabs} onKeyDown={handleTabKeyDown}>
          <button
            id="book-details-tab"
            className={`book-dialog__tab${activeTab === 'details' ? ' is-active' : ''}`}
            type="button"
            role="tab"
            aria-selected={activeTab === 'details'}
            aria-controls="book-details-panel"
            tabIndex={activeTab === 'details' ? 0 : -1}
            onClick={() => setActiveTab('details')}
          >
            {labels.detailsTab}
          </button>
          <button
            id="book-rules-tab"
            className={`book-dialog__tab${activeTab === 'rules' ? ' is-active' : ''}`}
            type="button"
            role="tab"
            aria-selected={activeTab === 'rules'}
            aria-controls="book-details-panel"
            tabIndex={activeTab === 'rules' ? 0 : -1}
            onClick={() => setActiveTab('rules')}
          >
            {labels.rulesTab}
          </button>
        </div>
        <div id="book-details-panel" className="book-dialog__tab-panel" role="tabpanel" tabIndex={0} aria-labelledby={`book-${activeTab}-tab`}>
          {activeTab === 'details' ? (
            <dl className="book-dialog__facts">
              <div><dt>{labels.category}</dt><dd>{categoryName(book.categoryName) || labels.unknown}</dd></div>
              <div><dt>{labels.publisher}</dt><dd>{book.publisherName || labels.unknown}</dd></div>
              <div><dt>{labels.year}</dt><dd>{book.publishYear || labels.unknown}</dd></div>
              <div><dt>{labels.isbn}</dt><dd>{book.isbn || labels.unknown}</dd></div>
              <div><dt>{labels.totalCopies}</dt><dd>{labels.copiesCount(book.totalCopies ?? 0)}</dd></div>
              <div><dt>{labels.availableCopies}</dt><dd>{labels.copiesCount(book.availableCopies ?? 0)}</dd></div>
            </dl>
          ) : <LibraryRules compact />}
        </div>
        {borrowState.status === 'success' && (
          <div className="book-dialog__notice book-dialog__notice--success" role="status">
            <strong>{labels.borrowed}</strong>
            {borrowState.loan?.loanCode && <span>{labels.loanCode}: {borrowState.loan.loanCode}</span>}
            {formattedDueDate && <span>{labels.dueDate}: {formattedDueDate}</span>}
          </div>
        )}
        {borrowState.status === 'error' && (
          <div className="book-dialog__notice book-dialog__notice--error" role="alert">{borrowState.message}</div>
        )}
        {reservationState.status === 'success' && (
          <div className="book-dialog__notice book-dialog__notice--success" role="status">
            <strong>{labels.reserved}</strong>
            {reservationState.reservation?.id && <span>{labels.reservationNumber}: {reservationState.reservation.id}</span>}
            {reservationState.reservation?.queuePosition && (
              <span>{labels.queuePosition}: {reservationState.reservation.queuePosition}</span>
            )}
          </div>
        )}
        {reservationState.status === 'error' && (
          <div className="book-dialog__notice book-dialog__notice--error" role="alert">{reservationState.message}</div>
        )}
        {memberActionPending && (
          <div className="book-dialog__agreement">
            <label htmlFor="accept-library-rules" className="book-dialog__agreement-label">
              <input
                id="accept-library-rules"
                type="checkbox"
                checked={termsAccepted}
                onChange={(event) => setTermsAccepted(event.target.checked)}
              />
              <span>{labels.acceptRules}</span>
            </label>
            <button className="book-dialog__read-rules" type="button" onClick={() => setActiveTab('rules')}>
              {labels.readRules}
            </button>
          </div>
        )}
        {user?.role === 'MEMBER' && availableCopies > 0 && borrowState.status !== 'success' && (
          <button
            className="button button--primary book-dialog__borrow"
            type="button"
            onClick={onBorrow}
            disabled={borrowState.status === 'loading' || !termsAccepted}
            aria-busy={borrowState.status === 'loading'}
          >
            {borrowState.status === 'loading' ? labels.borrowing : labels.borrow}
          </button>
        )}
        {user?.role === 'MEMBER' && availableCopies === 0 && borrowState.status !== 'success' && reservationState.status !== 'success' && (
          <button
            className="button button--primary book-dialog__borrow"
            type="button"
            onClick={onReserve}
            disabled={reservationState.status === 'loading' || !termsAccepted}
            aria-busy={reservationState.status === 'loading'}
          >
            {reservationState.status === 'loading' ? labels.reserving : labels.reserve}
          </button>
        )}
        {!user && (
          <div className="book-dialog__member-prompt">
            <p>{labels.memberOnly}</p>
            <Link className="button button--primary" to="/login">
              {availableCopies > 0 ? labels.signInToBorrow : labels.signInToReserve}
            </Link>
          </div>
        )}
        {user && user.role !== 'MEMBER' && (
          <div className="book-dialog__member-prompt">
            <p>{labels.staffBorrowHint}</p>
            <Link className="button button--secondary" to="/admin/loans">{labels.openCirculation}</Link>
          </div>
        )}
        {borrowState.status === 'success' && <Link className="button button--primary book-dialog__borrow" to="/profile">{labels.viewLoans}</Link>}
        {reservationState.status === 'success' && <Link className="button button--primary book-dialog__borrow" to="/profile">{labels.viewReservations}</Link>}
        <button className="button button--secondary book-dialog__done" type="button" onClick={() => dialogRef.current?.close()}>{labels.closeDetails}</button>
      </div>
    </dialog>
  );
}
