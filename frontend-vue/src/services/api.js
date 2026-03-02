export async function apiRequest(path, options = {}) {
  let authHeaders = {};
  try {
    const raw = localStorage.getItem("tger.auth");
    if (raw) {
      const parsed = JSON.parse(raw);
      if (parsed?.token) authHeaders = { Authorization: `Bearer ${parsed.token}` };
    }
  } catch {
    // ignore session parsing errors
  }

  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...authHeaders,
      ...(options.headers ?? {})
    },
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }

  if (response.status === 204) return null;
  return response.json();
}
