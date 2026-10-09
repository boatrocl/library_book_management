import { useEffect, useRef } from 'react';

export default function BookDetailsDialog({ book, onClose }) {
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
      <button className="book-dialog__close" type="button" onClick={() => dialogRef.current?.close()} aria-label="ปิดรายละเอียด">×</button>
      <div className="book-dialog__cover" aria-hidden="true">
        <span className="book-dialog__cover-label">LIBRAFLOW<br />READING ROOM</span>
        <span className="book-dialog__cover-title">{book.title}</span>
        <span className="book-dialog__cover-author">{book.authors?.[0] || 'LIBRAFLOW LIBRARY'}</span>
      </div>
      <div className="book-dialog__content">
        <span className="section-kicker">รายละเอียดหนังสือ</span>
        <h2 id="book-dialog-title">{book.title}</h2>
        <p className="book-dialog__authors">{book.authors?.length ? book.authors.join(', ') : 'ไม่ระบุผู้แต่ง'}</p>
        <dl className="book-dialog__facts">
          <div><dt>หมวดหมู่</dt><dd>{book.categoryName || 'ไม่ระบุ'}</dd></div>
          <div><dt>สำนักพิมพ์</dt><dd>{book.publisherName || 'ไม่ระบุ'}</dd></div>
          <div><dt>ปีที่พิมพ์</dt><dd>{book.publishYear || 'ไม่ระบุ'}</dd></div>
          <div><dt>ISBN</dt><dd>{book.isbn || 'ไม่ระบุ'}</dd></div>
          <div><dt>จำนวนตัวเล่ม</dt><dd>{book.totalCopies ?? 0} เล่ม</dd></div>
          <div><dt>พร้อมให้ยืม</dt><dd>{book.availableCopies ?? 0} เล่ม</dd></div>
        </dl>
        <button className="button button--primary book-dialog__done" type="button" onClick={() => dialogRef.current?.close()}>ปิดรายละเอียด</button>
      </div>
    </dialog>
  );
}
