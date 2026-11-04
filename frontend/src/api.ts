/**
 * API client module for Family Tree SPA.
 * Provides typed methods to interact with the Spring Boot REST API.
 */

// ---- Types ----

export interface PersonFullResponse {
  id: string;
  firstName: string;
  lastName: string;
  bio: string;
  photoUrl: string | null;
  birthDate: string | null; // ISO date (YYYY-MM-DD)
  deathDate: string | null; // ISO date (YYYY-MM-DD)
  login: string | null;
  isClaimed: boolean;
  fatherId: string | null;
  motherId: string | null;
  spouseId: string | null;
  canEdit: boolean;
}

export interface PersonPublicResponse {
  id: number;
  firstName: string;
  lastName: string; // masked (e.g. "И*****")
  bio: string;
  photoUrl: string | null;
  birthDate: string | null; // year only (e.g. "1990")
  deathDate: string | null; // year only (e.g. "1990")
  login: string | null;
  isClaimed: boolean;
  fatherId: string | null;
  motherId: string | null;
  spouseId: string | null;
}

export interface TreeResponse {
  root: PersonPublicResponse;
  wife: PersonPublicResponse | null;
  children: PersonPublicResponse[];
}

export interface PersonNode {
  person: PersonPublicResponse;
  partner: PersonPublicResponse | null;
  children: PersonPublicResponse[];
}

export interface AuthResponse {
  authenticated: boolean;
  username: string | null;
  roles: string[];
}

export interface PersonUpdateRequest {
  bio: string;
}

// ---- API Methods ----

const API_BASE = '/api';

async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    const error = await response.text().catch(() => null);
    throw new Error(`API error ${response.status}: ${error || response.statusText}`);
  }

  if (response.status === 204) {
    return null as unknown as T;
  }

  return response.json();
}

export const api = {
  /** GET /api/persons — list all persons */
  getPersons: () => request<PersonFullResponse[] | PersonPublicResponse[]>('/persons'),

  /** GET /api/persons/{id} — get single person */
  getPerson: (id: number) => request<PersonFullResponse | PersonPublicResponse>(`/persons/${id}`),

  /** PATCH /api/persons/{id} — update person bio */
  updatePerson: (id: number, data: PersonUpdateRequest) =>
    request<PersonPublicResponse>(`/persons/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  /** GET /api/persons/tree — get family tree */
  getTree: () => request<TreeResponse>('/persons/tree'),

  /** GET /api/auth/me — get current auth status */
  getAuth: () => request<AuthResponse>('/auth/me'),

  /** POST /api/persons/{id}/photo — update photo URL */
  uploadPhoto: (id: number, url: string) =>
    request<PersonPublicResponse>(`/persons/${id}/photo`, {
      method: 'POST',
      body: JSON.stringify({ url }),
    }),
};
