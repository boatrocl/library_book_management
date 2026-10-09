import { useEffect, useRef } from 'react';

export default function BookDetailsDialog({ book, labels, categoryName, onClose }) {
  const dialogRef = useRef(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (dialog && !dialog.open) dialog.showModal();
    return () => {
      if (dialog?.open) dialog.close();
    };
  }, []);

  return (
    <dialog ref={dialogRef} className="book-dialog" aria-labelledby="book-dialog-title" onClose={onClose}>
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
        <button className="button button--primary book-dialog__done" type="button" onClick={() => dialogRef.current?.close()}>{labels.closeDetails}</button>
      </div>
    </dialog>
  );
}
