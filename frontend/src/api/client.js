const API_BASE = '/api';

export async function apiGet(path) {
  try {
    const res = await fetch(`${API_BASE}${path}`);
    if (!res.ok) throw new Error(`API error: ${res.status}`);
    return res.json();
  } catch(e) {
    console.warn(`API GET ${path} failed, returning null`, e);
    return null;
  }
}

export async function apiPost(path, data) {
  try {
    const res = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(`API error: ${res.status}`);
    return res.json();
  } catch(e) {
    console.warn(`API POST ${path} failed, returning null`, e);
    return null;
  }
}
