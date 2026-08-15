/**
 * High-Performance FIFO Queue & Priority Queue
 * Used for Combat Action dispatching, Sound sequencing, Particle spawns,
 * and Typewriter Dialogue line processing.
 */

export class Queue {
  constructor(initialItems = []) {
    this.items = [...initialItems];
    this.head = 0;
  }

  enqueue(element) {
    this.items.push(element);
    return this;
  }

  dequeue() {
    if (this.isEmpty()) return null;
    const item = this.items[this.head];
    this.head++;

    // Periodic memory cleanup when gap exceeds 50 items
    if (this.head > 50 && this.head > this.items.length / 2) {
      this.items = this.items.slice(this.head);
      this.head = 0;
    }
    return item;
  }

  peek() {
    if (this.isEmpty()) return null;
    return this.items[this.head];
  }

  isEmpty() {
    return this.size === 0;
  }

  get size() {
    return this.items.length - this.head;
  }

  clear() {
    this.items = [];
    this.head = 0;
  }

  toArray() {
    return this.items.slice(this.head);
  }
}

/**
 * Priority Queue (Min/Max priority heap)
 */
export class PriorityQueue {
  constructor(compareFn = (a, b) => b.priority - a.priority) {
    this.heap = [];
    this.compare = compareFn;
  }

  enqueue(item, priority = 0) {
    const entry = typeof item === 'object' && item.priority !== undefined ? item : { value: item, priority };
    this.heap.push(entry);
    this.bubbleUp(this.heap.length - 1);
  }

  dequeue() {
    if (this.heap.length === 0) return null;
    const top = this.heap[0];
    const bottom = this.heap.pop();
    if (this.heap.length > 0) {
      this.heap[0] = bottom;
      this.sinkDown(0);
    }
    return top.value !== undefined ? top.value : top;
  }

  bubbleUp(index) {
    while (index > 0) {
      const parentIdx = (index - 1) >>> 1;
      if (this.compare(this.heap[index], this.heap[parentIdx]) > 0) {
        [this.heap[index], this.heap[parentIdx]] = [this.heap[parentIdx], this.heap[index]];
        index = parentIdx;
      } else {
        break;
      }
    }
  }

  sinkDown(index) {
    const length = this.heap.length;
    while (true) {
      const left = (index << 1) + 1;
      const right = left + 1;
      let swap = null;

      if (left < length && this.compare(this.heap[left], this.heap[index]) > 0) {
        swap = left;
      }
      if (right < length && (swap === null ? this.compare(this.heap[right], this.heap[index]) > 0 : this.compare(this.heap[right], this.heap[left]) > 0)) {
        swap = right;
      }
      if (swap === null) break;

      [this.heap[index], this.heap[swap]] = [this.heap[swap], this.heap[index]];
      index = swap;
    }
  }

  isEmpty() {
    return this.heap.length === 0;
  }

  get size() {
    return this.heap.length;
  }
}
