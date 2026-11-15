/** Matches backend PageResponse envelope. */
export interface ApiPage<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Safely unwrap paginated or legacy array responses. */
export function pageContent<T>(page: ApiPage<T> | T[] | null | undefined): T[] {
  if (!page) return [];
  if (Array.isArray(page)) return page;
  return Array.isArray(page.content) ? page.content : [];
}

export function pageTotal(page: ApiPage<unknown> | null | undefined, fallback = 0): number {
  if (!page || Array.isArray(page)) return fallback;
  const total = Number(page.totalElements);
  return Number.isFinite(total) ? total : fallback;
}
