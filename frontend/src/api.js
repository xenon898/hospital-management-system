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
    if (response.status === 401) {
      throw new Error("Your session has expired or is invalid. Please sign in again.");
    }
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

export function minimumDateTime() {
  const now = new Date(Date.now() + 5 * 60 * 1000);
  const offset = now.getTimezoneOffset() * 60000;
  return new Date(now.getTime() - offset).toISOString().slice(0, 16);
}

export function phoneValidationMessage(phone) {
  const value = String(phone || "").trim();
  if (!value) return "";
  if (/^\d{10}$/.test(value) && new Set(value).size === 1) {
    return "Phone number is too predictable. Enter a real patient mobile number.";
  }
  if (!/^[6-9]\d{9}$/.test(value)) {
    return "Phone must be exactly 10 digits and start with 6, 7, 8, or 9.";
  }
  if (value === "1234567890") {
    return "Phone number is too predictable. Enter a real patient mobile number.";
  }
  return "";
}

export function ageValidationMessage(age) {
  if (age === "" || age === null || age === undefined) return "";
  const value = Number(age);
  if (!Number.isFinite(value) || value < 1) {
    return "Age must be greater than 0.";
  }
  if (value > 120) {
    return "Invalid age entered.";
  }
  return "";
}
