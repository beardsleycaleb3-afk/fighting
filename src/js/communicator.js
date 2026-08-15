/**
 * Communicator - Event-Driven Message Bus & Inter-Module IPC Broker
 * Enables decoupled communication between combat systems, UI, audio, clocks, and controllers.
 */
export class Communicator {
  constructor() {
    this.channels = new Map();
    this.history = [];
    this.maxHistory = 100;
  }

  on(event, handler, priority = 0) {
    if (!this.channels.has(event)) {
      this.channels.set(event, []);
    }
    const handlers = this.channels.get(event);
    handlers.push({ handler, priority });
    handlers.sort((a, b) => b.priority - a.priority);

    return () => this.off(event, handler);
  }

  once(event, handler) {
    const unsubscribe = this.on(event, (data) => {
      unsubscribe();
      handler(data);
    });
    return unsubscribe;
  }

  off(event, handler) {
    if (!this.channels.has(event)) return;
    const handlers = this.channels.get(event).filter(h => h.handler !== handler);
    this.channels.set(event, handlers);
  }

  emit(event, data = null) {
    const timestamp = performance.now();
    const eventRecord = { event, data, timestamp };

    this.history.push(eventRecord);
    if (this.history.length > this.maxHistory) {
      this.history.shift();
    }

    if (this.channels.has(event)) {
      const handlers = [...this.channels.get(event)];
      for (const entry of handlers) {
        try {
          entry.handler(data, eventRecord);
        } catch (err) {
          console.error(`Communicator error in handler for [${event}]:`, err);
        }
      }
    }
  }

  getEventHistory(filterEvent = null) {
    if (!filterEvent) return [...this.history];
    return this.history.filter(h => h.event === filterEvent);
  }

  clear() {
    this.channels.clear();
    this.history = [];
  }
}

export const communicator = new Communicator();
