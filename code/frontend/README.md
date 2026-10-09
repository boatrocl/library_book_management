# LibraFlow Frontend

React single-page application for the LibraFlow library system. It uses Vite, Axios,
Tailwind CSS, the shared Thai/English language setting, and the Loma font included in
`public/fonts/`.

## Local development

Run these commands from `code/frontend`:

```bash
npm ci
npm run dev
```

Vite serves the app at `http://localhost:5173`. The backend API defaults to
`http://localhost:8080`; set `VITE_API_URL` in a local `.env` file to override it.

## Verification

```bash
npm run lint
npm run build
```

GitHub Actions runs both checks for pushes to the configured project branches and pull
requests targeting `develop` or `main` using `.github/workflows/frontend-ci.yml`.

## Main flows

- Browse and filter the public book catalog, then open a responsive book details dialog.
- Signed-in members can borrow an available copy. The API selects and locks a copy, then
  returns the loan code and due date for confirmation.
- Members can review their loan history and fines from the profile page.
- Librarians and administrators use the management pages for circulation and catalog work.
