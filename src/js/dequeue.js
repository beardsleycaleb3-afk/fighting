/**
 * Double-Ended Queue (Deque) Data Structure
 * Optimized O(1) push, pop, shift, and unshift operations.
 * Used for:
 * 1. 60-Frame Input Buffer (rolling window for command recognition)
 * 2. Hit-spark & Particle Object Pooling
 * 3. Match Action History (undo/redo & replay scrubbing)
 */

export class Deque {
  constructor(capacity = Infinity) {
    this.capacity = capacity;
    this.head = 0;
    this.tail = 0;
    this.data = {};
  }

  pushBack(value) {
    if (this.size >= this.capacity) {
      this.popFront();
    }
    this.data[this.tail] = value;
    this.tail++;
    return this;
  }

  pushFront(value) {
    if (this.size >= this.capacity) {
      this.popBack();
    }
    this.head--;
    this.data[this.head] = value;
    return this;
  }

  popFront() {
    if (this.isEmpty()) return null;
    const val = this.data[this.head];
    delete this.data[this.head];
    this.head++;
    return val;
  }

  popBack() {
    if (this.isEmpty()) return null;
    this.tail--;
    const val = this.data[this.tail];
    delete this.data[this.tail];
    return val;
  }

  peekFront() {
    if (this.isEmpty()) return null;
    return this.data[this.head];
  }

  peekBack() {
    if (this.isEmpty()) return null;
    return this.data[this.tail - 1];
  }

  get size() {
    return this.tail - this.head;
  }

  isEmpty() {
    return this.size === 0;
  }

  clear() {
    this.head = 0;
    this.tail = 0;
    this.data = {};
  }

  toArray() {
    const arr = [];
    for (let i = this.head; i < this.tail; i++) {
      arr.push(this.data[i]);
    }
    return arr;
  }

  /**
   * Get last N elements (e.g., last 5 inputs in buffer)
   */
  getLastN(n) {
    const arr = [];
    const start = Math.max(this.head, this.tail - n);
    for (let i = start; i < this.tail; i++) {
      arr.push(this.data[i]);
    }
    return arr;
  }
}
