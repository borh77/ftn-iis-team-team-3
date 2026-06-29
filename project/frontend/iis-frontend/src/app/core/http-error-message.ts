import { HttpErrorResponse } from '@angular/common/http';

export function extractBackendErrorMessage(error: unknown, fallback = 'Operation failed.'): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error;

    if (typeof body?.error === 'string' && body.error.trim()) {
      return body.error.trim();
    }

    if (typeof body?.message === 'string' && body.message.trim()) {
      return body.message.trim();
    }

    if (typeof body?.detail === 'string' && body.detail.trim()) {
      return body.detail.trim();
    }

    if (body?.errors && typeof body.errors === 'object') {
      const firstFieldErrors = Object.values(body.errors)[0];
      if (Array.isArray(firstFieldErrors) && firstFieldErrors.length > 0) {
        return String(firstFieldErrors[0]);
      }
      if (typeof firstFieldErrors === 'string' && firstFieldErrors.trim()) {
        return firstFieldErrors.trim();
      }
    }

    if (typeof body === 'string' && body.trim()) {
      return body.trim();
    }

    if (error.message?.trim()) {
      return error.message.trim();
    }
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message.trim();
  }

  return fallback;
}
