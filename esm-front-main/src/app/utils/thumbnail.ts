export function normalizeThumbnailUrl(raw: string | undefined | null): string | undefined {
  if (!raw || typeof raw !== 'string') return undefined;
  // Absolute URLs (http/https) are safe to use directly
  if (raw.startsWith('http://') || raw.startsWith('https://')) return raw;
  // Relative uploads path from backend
  if (raw.startsWith('/uploads/')) return raw;
  return raw;
}
