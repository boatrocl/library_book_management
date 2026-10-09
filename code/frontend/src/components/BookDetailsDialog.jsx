import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';

export default function BookDetailsDialog({ book, labels, categoryName, user, borrowState, reservationState, onBorrow, onReserve, onClose }) {
  const dialogRef = useRef(null);
  const availableCopies = Number(book.availableCopies || 0);
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
        <dl className="book-dialog__facts">
          <div><dt>{labels.category}</dt><dd>{categoryName(book.categoryName) || labels.unknown}</dd></div>
          <div><dt>{labels.publisher}</dt><dd>{book.publisherName || labels.unknown}</dd></div>
          <div><dt>{labels.year}</dt><dd>{book.publishYear || labels.unknown}</dd></div>
          <div><dt>{labels.isbn}</dt><dd>{book.isbn || labels.unknown}</dd></div>
          <div><dt>{labels.totalCopies}</dt><dd>{labels.copiesCount(book.totalCopies ?? 0)}</dd></div>
          <div><dt>{labels.availableCopies}</dt><dd>{labels.copiesCount(book.availableCopies ?? 0)}</dd></div>
        </dl>
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
        {user?.role === 'MEMBER' && availableCopies > 0 && borrowState.status !== 'success' && (
          <button
            className="button button--primary book-dialog__borrow"
            type="button"
            onClick={onBorrow}
            disabled={borrowState.status === 'loading'}
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
            disabled={reservationState.status === 'loading'}
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
