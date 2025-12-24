import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { accountsApi } from "../api/accounts";
import { getErrorMessage } from "../api/errors";
import { formatCurrency } from "../utils/format";
import { pushToast } from "../store/toastStore";
import { useAccountsStore } from "../store/accountsStore";

const PAGE_SIZE_OPTIONS = [4, 6, 10, 20];

type SortBy = "name" | "number" | "balance";
type SortDirection = "asc" | "desc";

type SearchState = {
  number: string;
  name: string;
  sortBy: SortBy;
  sortDirection: SortDirection;
  page: number;
  pageSize: number;
};

const parseSortBy = (value: string | null) => {
  if (value === "name" || value === "number" || value === "balance") {
    return value;
  }
  return null;
};

const parseSortDirection = (value: string | null) => {
  if (value === "asc" || value === "desc") {
    return value;
  }
  return null;
};

const parsePage = (value: string | null) => {
  if (!value) {
    return null;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
};

const parsePageSize = (value: string | null) => {
  if (!value) {
    return null;
  }
  const parsed = Number(value);
  return PAGE_SIZE_OPTIONS.includes(parsed) ? parsed : null;
};

const readStoredValue = (key: string) => {
  if (typeof window === "undefined") {
    return null;
  }
  return window.localStorage.getItem(key);
};

const getStoredPageSize = () => parsePageSize(readStoredValue("mb-page-size"));
const getStoredSortBy = () => parseSortBy(readStoredValue("mb-sort-by"));
const getStoredSortDirection = () => parseSortDirection(readStoredValue("mb-sort-direction"));

function AccountsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const accounts = useAccountsStore((state) => state.accounts);
  const setAccounts = useAccountsStore((state) => state.setAccounts);
  const [number, setNumber] = useState("");
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [page, setPage] = useState(() => parsePage(searchParams.get("page")) ?? 1);
  const [pageSize, setPageSize] = useState(
    () => parsePageSize(searchParams.get("size")) ?? getStoredPageSize() ?? 6,
  );
  const [sortBy, setSortBy] = useState<SortBy>(
    () => parseSortBy(searchParams.get("sort")) ?? getStoredSortBy() ?? "name",
  );
  const [sortDirection, setSortDirection] = useState<SortDirection>(
    () => parseSortDirection(searchParams.get("dir")) ?? getStoredSortDirection() ?? "asc",
  );

  const [createNumber, setCreateNumber] = useState("");
  const [createName, setCreateName] = useState("");
  const [createBalance, setCreateBalance] = useState("");
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState("");

  const fetchAccounts = async (params: { number?: string; name?: string } = {}) => {
    setLoading(true);
    setError("");
    try {
      const data = await accountsApi.list(params);
      setAccounts(data);
    } catch (err) {
      const message = getErrorMessage(err, "Could not load accounts.");
      setError(message);
      pushToast(message);
    } finally {
      setLoading(false);
    }
  };

  const appliedNumber = searchParams.get("number") ?? "";
  const appliedName = searchParams.get("name") ?? "";

  useEffect(() => {
    const nextNumber = appliedNumber;
    const nextName = appliedName;
    const nextSortBy = parseSortBy(searchParams.get("sort")) ?? getStoredSortBy() ?? "name";
    const nextSortDirection =
      parseSortDirection(searchParams.get("dir")) ?? getStoredSortDirection() ?? "asc";
    const nextPageSize = parsePageSize(searchParams.get("size")) ?? getStoredPageSize() ?? 6;
    const nextPage = parsePage(searchParams.get("page")) ?? 1;

    if (nextNumber !== number) {
      setNumber(nextNumber);
    }
    if (nextName !== name) {
      setName(nextName);
    }
    if (nextSortBy !== sortBy) {
      setSortBy(nextSortBy);
    }
    if (nextSortDirection !== sortDirection) {
      setSortDirection(nextSortDirection);
    }
    if (nextPageSize !== pageSize) {
      setPageSize(nextPageSize);
    }
    if (nextPage !== page) {
      setPage(nextPage);
    }
  }, [
    appliedName,
    appliedNumber,
    name,
    number,
    page,
    pageSize,
    searchParams,
    sortBy,
    sortDirection,
  ]);

  useEffect(() => {
    fetchAccounts({
      number: appliedNumber ? appliedNumber : undefined,
      name: appliedName ? appliedName : undefined,
    });
  }, [appliedName, appliedNumber]);

  const updateSearchParams = (overrides: Partial<SearchState>) => {
    const resolvedNumber = overrides.number ?? appliedNumber;
    const resolvedName = overrides.name ?? appliedName;
    const resolvedSortBy = overrides.sortBy ?? sortBy;
    const resolvedSortDirection = overrides.sortDirection ?? sortDirection;
    const resolvedPage = overrides.page ?? page;
    const resolvedPageSize = overrides.pageSize ?? pageSize;

    const params = new URLSearchParams();
    if (resolvedNumber) {
      params.set("number", resolvedNumber);
    }
    if (resolvedName) {
      params.set("name", resolvedName);
    }
    params.set("sort", resolvedSortBy);
    params.set("dir", resolvedSortDirection);
    params.set("page", String(resolvedPage));
    params.set("size", String(resolvedPageSize));
    setSearchParams(params, { replace: true });
  };

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    updateSearchParams({
      number: number.trim(),
      name: name.trim(),
      page: 1,
    });
  };

  const handleReset = () => {
    setNumber("");
    setName("");
    setSortBy("name");
    setSortDirection("asc");
    setPageSize(6);
    setPage(1);
    window.localStorage.removeItem("mb-page-size");
    window.localStorage.removeItem("mb-sort-by");
    window.localStorage.removeItem("mb-sort-direction");
    updateSearchParams({
      number: "",
      name: "",
      sortBy: "name",
      sortDirection: "asc",
      pageSize: 6,
      page: 1,
    });
  };

  const handleCreate = async (event: React.FormEvent) => {
    event.preventDefault();
    setCreateError("");
    setCreateLoading(true);
    try {
      await accountsApi.create({
        number: createNumber,
        name: createName,
        initialBalance: createBalance ? Number(createBalance) : undefined,
      });
      setCreateNumber("");
      setCreateName("");
      setCreateBalance("");
      fetchAccounts({
        number: appliedNumber ? appliedNumber : undefined,
        name: appliedName ? appliedName : undefined,
      });
    } catch (err) {
      const message = getErrorMessage(err, "Account creation failed.");
      setCreateError(message);
      pushToast(message);
    } finally {
      setCreateLoading(false);
    }
  };

  const totalPages = Math.max(1, Math.ceil(accounts.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const startIndex = (currentPage - 1) * pageSize;
  const sortedAccounts = [...accounts].sort((a, b) => {
    let result = 0;
    if (sortBy === "balance") {
      result = Number(a.balance) - Number(b.balance);
    } else {
      result = a[sortBy].localeCompare(b[sortBy]);
    }
    return sortDirection === "asc" ? result : -result;
  });
  const paginatedAccounts = sortedAccounts.slice(startIndex, startIndex + pageSize);
  const showingFrom = accounts.length === 0 ? 0 : startIndex + 1;
  const showingTo = Math.min(startIndex + pageSize, accounts.length);

  useEffect(() => {
    if (page > totalPages) {
      updateSearchParams({ page: totalPages });
    }
  }, [page, totalPages]);

  const handlePageChange = (nextPage: number) => {
    const target = Math.min(Math.max(1, nextPage), totalPages);
    setPage(target);
    updateSearchParams({ page: target });
  };

  return (
    <section className="page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Overview</p>
          <h1>Accounts</h1>
          <p className="muted">Track balances, rename accounts, or move money instantly.</p>
        </div>
        <div className="header-actions">
          <button
            className="btn btn-ghost"
            onClick={() =>
              fetchAccounts({
                number: appliedNumber ? appliedNumber : undefined,
                name: appliedName ? appliedName : undefined,
              })
            }
          >
            Refresh
          </button>
        </div>
      </header>

      <div className="grid two">
        <div className="panel">
          <h3>Find accounts</h3>
          <form className="form inline" onSubmit={handleSearch}>
            <label className="field">
              <span>Number</span>
              <input value={number} onChange={(event) => setNumber(event.target.value)} />
            </label>
            <label className="field">
              <span>Name</span>
              <input value={name} onChange={(event) => setName(event.target.value)} />
            </label>
            <button className="btn btn-secondary" type="submit">
              Search
            </button>
            <button className="btn btn-ghost" type="button" onClick={handleReset}>
              Reset filters
            </button>
          </form>
          <div className="form inline search-controls">
            <label className="field">
              <span>Sort by</span>
              <select
                value={sortBy}
                onChange={(event) => {
                  const value = event.target.value as SortBy;
                  setSortBy(value);
                  window.localStorage.setItem("mb-sort-by", value);
                  setPage(1);
                  updateSearchParams({ sortBy: value, page: 1 });
                }}
              >
                <option value="name">Name</option>
                <option value="number">Number</option>
                <option value="balance">Balance</option>
              </select>
            </label>
            <label className="field">
              <span>Direction</span>
              <select
                value={sortDirection}
                onChange={(event) => {
                  const value = event.target.value as SortDirection;
                  setSortDirection(value);
                  window.localStorage.setItem("mb-sort-direction", value);
                  setPage(1);
                  updateSearchParams({ sortDirection: value, page: 1 });
                }}
              >
                <option value="asc">Ascending</option>
                <option value="desc">Descending</option>
              </select>
            </label>
            <label className="field">
              <span>Page size</span>
              <select
                value={pageSize}
                onChange={(event) => {
                  const value = Number(event.target.value);
                  setPageSize(value);
                  window.localStorage.setItem("mb-page-size", String(value));
                  setPage(1);
                  updateSearchParams({ pageSize: value, page: 1 });
                }}
              >
                {PAGE_SIZE_OPTIONS.map((size) => (
                  <option key={size} value={size}>
                    {size}
                  </option>
                ))}
              </select>
            </label>
          </div>
          {error ? <p className="error">{error}</p> : null}
          {loading ? <p className="muted">Loading accounts...</p> : null}
          <div className="account-list">
            {paginatedAccounts.map((account) => (
              <div className="account-card" key={account.id}>
                <div>
                  <p className="account-name">{account.name}</p>
                  <p className="muted small">{account.number}</p>
                </div>
                <div className="account-meta">
                  <span>{formatCurrency(account.balance)}</span>
                  <Link className="link" to={`/accounts/${account.id}`}>
                    View
                  </Link>
                </div>
              </div>
            ))}
            {!loading && accounts.length === 0 ? (
              <p className="muted">No accounts match your search.</p>
            ) : null}
          </div>
          {accounts.length > 0 ? (
            <div className="pagination">
              <button
                className="btn btn-ghost"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
              >
                Prev
              </button>
              <div className="pagination-info">
                <span>
                  Showing {showingFrom}-{showingTo} of {accounts.length}
                </span>
                <span>
                  Page {currentPage} of {totalPages}
                </span>
              </div>
              <button
                className="btn btn-ghost"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
              >
                Next
              </button>
            </div>
          ) : null}
        </div>

        <div className="panel">
          <h3>Create account</h3>
          <form className="form" onSubmit={handleCreate}>
            <label className="field">
              <span>Account number</span>
              <input
                value={createNumber}
                onChange={(event) => setCreateNumber(event.target.value)}
                placeholder="ACC-100"
                required
              />
            </label>
            <label className="field">
              <span>Name</span>
              <input
                value={createName}
                onChange={(event) => setCreateName(event.target.value)}
                placeholder="Primary"
                required
              />
            </label>
            <label className="field">
              <span>Initial balance</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={createBalance}
                onChange={(event) => setCreateBalance(event.target.value)}
                placeholder="0.00"
              />
            </label>
            {createError ? <p className="error">{createError}</p> : null}
            <button className="btn btn-primary" type="submit" disabled={createLoading}>
              {createLoading ? "Creating..." : "Create"}
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}

export default AccountsPage;
