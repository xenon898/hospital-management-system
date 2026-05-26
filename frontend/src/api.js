const API_ROOT = "/api";

export async function request(path, { token, body, method = "GET" } = {}) {
  const headers = {};
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_ROOT}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  });

  const text = await response.text();
  let result = null;
  if (text) {
    try {
      result = JSON.parse(text);
    } catch {
      result = text;
    }
  }

  if (!response.ok) {
    const message =
      result?.error ||
      result?.message ||
      (typeof result === "string" && result) ||
      `Request failed (${response.status})`;
    throw new Error(message);
  }

  return result;
}

export function futureDateTime() {
  const future = new Date(Date.now() + 24 * 60 * 60 * 1000);
  future.setMinutes(0, 0, 0);
  const offset = future.getTimezoneOffset() * 60000;
  return new Date(future.getTime() - offset).toISOString().slice(0, 16);
}
