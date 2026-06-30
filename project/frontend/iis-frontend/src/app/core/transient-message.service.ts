import { Injectable } from '@angular/core';

export const SUCCESS_MESSAGE_MS = 3000;
export const ERROR_MESSAGE_MS = 5000;

@Injectable({ providedIn: 'root' })
export class TransientMessageService {
  private readonly timers = new WeakMap<object, Map<string, number>>();

  setField<T extends object, K extends keyof T>(
    owner: T,
    key: K,
    value: string,
    durationMs: number,
    afterChange?: () => void
  ): void {
    this.set(
      owner,
      String(key),
      (nextValue) => {
        owner[key] = nextValue as T[K];
      },
      () => owner[key] as string,
      value,
      durationMs,
      afterChange
    );
  }

  clearField<T extends object, K extends keyof T>(owner: T, key: K, afterChange?: () => void): void {
    this.clear(
      owner,
      String(key),
      () => {
        owner[key] = '' as T[K];
      },
      afterChange
    );
  }

  set(
    owner: object,
    key: string,
    setValue: (value: string) => void,
    getValue: () => string,
    value: string,
    durationMs: number,
    afterChange?: () => void
  ): void {
    this.clearTimer(owner, key);
    setValue(value);
    afterChange?.();

    const timerId = window.setTimeout(() => {
      if (getValue() === value) {
        setValue('');
        afterChange?.();
      }
      this.deleteTimer(owner, key);
    }, durationMs);

    this.storeTimer(owner, key, timerId);
  }

  clear(owner: object, key: string, clearValue?: () => void, afterChange?: () => void): void {
    this.clearTimer(owner, key);
    clearValue?.();
    afterChange?.();
  }

  clearAll(owner: object): void {
    const ownerTimers = this.timers.get(owner);
    if (!ownerTimers) {
      return;
    }

    for (const timerId of ownerTimers.values()) {
      window.clearTimeout(timerId);
    }
    ownerTimers.clear();
    this.timers.delete(owner);
  }

  private storeTimer(owner: object, key: string, timerId: number): void {
    let ownerTimers = this.timers.get(owner);
    if (!ownerTimers) {
      ownerTimers = new Map<string, number>();
      this.timers.set(owner, ownerTimers);
    }
    ownerTimers.set(key, timerId);
  }

  private clearTimer(owner: object, key: string): void {
    const ownerTimers = this.timers.get(owner);
    const timerId = ownerTimers?.get(key);
    if (timerId === undefined) {
      return;
    }

    window.clearTimeout(timerId);
    this.deleteTimer(owner, key);
  }

  private deleteTimer(owner: object, key: string): void {
    const ownerTimers = this.timers.get(owner);
    if (!ownerTimers) {
      return;
    }

    ownerTimers.delete(key);
    if (ownerTimers.size === 0) {
      this.timers.delete(owner);
    }
  }
}
