# Mini Bank UI

## Overview

Mini Bank UI is the React front end for the Mini Bank API. It provides authentication,
account management, transfers, and transaction history with a clean, responsive layout.

## Highlights

- Login and registration screens
- Account list with search, sorting, and client-side pagination
- URL query params for search/sort/page state
- Account detail view with update/delete
- Money transfer with success/failure feedback
- Transaction history per account
- Zustand state for auth, accounts, and transactions
- Toast notifications for API errors

## Tech Stack

- React 19 + TypeScript
- Vite
- React Router
- Zustand
- Axios

## Getting Started (Fresh Clone)

1. Clone the repository and enter the UI directory:

```bash
git clone <your-repo-url>
cd <repo-root>/mini-bank-ui
```

2. Install dependencies:

```bash
npm install
```

3. Start the dev server:

```bash
npm run dev
```

4. Open the app:
   `http://localhost:5173`

## Configuration

The UI expects the backend API at `http://localhost:8080` by default.
Override with `VITE_API_URL` in a `.env` file:

```bash
VITE_API_URL=http://localhost:8080
```

## Useful Scripts

- `npm run dev` - start development server
- `npm run build` - typecheck and build production assets
- `npm run preview` - preview the production build
- `npm run lint` - run ESLint

## Routes

- `/login`
- `/register`
- `/accounts`
- `/accounts/:id`
- `/accounts/:id/history`
- `/transfer`

## Notes

- Client-side sorting and pagination keep the backend minimal.
- Query params preserve search and view state for easy sharing.
