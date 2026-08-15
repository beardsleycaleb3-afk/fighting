/**
 * Linked List Data Structures:
 * - SinglyLinkedList (Linear event & action log)
 * - DoublyLinkedList (Undo/redo match frames & bidirectional dialogue navigation)
 * - CircularLinkedList (Cyclic stage rotation & character roster carousel)
 */

export class ListNode {
  constructor(value, next = null, prev = null) {
    this.value = value;
    this.next = next;
    this.prev = prev;
  }
}

/**
 * Singly Linked List
 */
export class SinglyLinkedList {
  constructor() {
    this.head = null;
    this.tail = null;
    this.size = 0;
  }

  append(value) {
    const node = new ListNode(value);
    if (!this.head) {
      this.head = node;
      this.tail = node;
    } else {
      this.tail.next = node;
      this.tail = node;
    }
    this.size++;
    return this;
  }

  prepend(value) {
    const node = new ListNode(value, this.head);
    this.head = node;
    if (!this.tail) {
      this.tail = node;
    }
    this.size++;
    return this;
  }

  toArray() {
    const arr = [];
    let current = this.head;
    while (current) {
      arr.push(current.value);
      current = current.next;
    }
    return arr;
  }
}

/**
 * Doubly Linked List with bidirectional cursor
 */
export class DoublyLinkedList {
  constructor() {
    this.head = null;
    this.tail = null;
    this.cursor = null;
    this.size = 0;
  }

  append(value) {
    const node = new ListNode(value, null, this.tail);
    if (!this.head) {
      this.head = node;
      this.tail = node;
      this.cursor = node;
    } else {
      this.tail.next = node;
      node.prev = this.tail;
      this.tail = node;
    }
    this.size++;
    return this;
  }

  stepForward() {
    if (this.cursor && this.cursor.next) {
      this.cursor = this.cursor.next;
      return this.cursor.value;
    }
    return null;
  }

  stepBackward() {
    if (this.cursor && this.cursor.prev) {
      this.cursor = this.cursor.prev;
      return this.cursor.value;
    }
    return null;
  }

  current() {
    return this.cursor ? this.cursor.value : null;
  }

  resetCursor() {
    this.cursor = this.head;
    return this.current();
  }

  toArray() {
    const arr = [];
    let curr = this.head;
    while (curr) {
      arr.push(curr.value);
      curr = curr.next;
    }
    return arr;
  }
}

/**
 * Circular Linked List for carousel rotation
 */
export class CircularLinkedList {
  constructor(initialItems = []) {
    this.head = null;
    this.tail = null;
    this.current = null;
    this.size = 0;

    if (initialItems.length > 0) {
      initialItems.forEach(item => this.append(item));
    }
  }

  append(value) {
    const node = new ListNode(value);
    if (!this.head) {
      this.head = node;
      this.tail = node;
      node.next = node;
      node.prev = node;
      this.current = node;
    } else {
      node.prev = this.tail;
      node.next = this.head;
      this.tail.next = node;
      this.head.prev = node;
      this.tail = node;
    }
    this.size++;
    return this;
  }

  next() {
    if (!this.current) return null;
    this.current = this.current.next;
    return this.current.value;
  }

  prev() {
    if (!this.current) return null;
    this.current = this.current.prev;
    return this.current.value;
  }

  getCurrent() {
    return this.current ? this.current.value : null;
  }

  find(predicate) {
    if (!this.head) return null;
    let node = this.head;
    for (let i = 0; i < this.size; i++) {
      if (predicate(node.value)) {
        this.current = node;
        return node.value;
      }
      node = node.next;
    }
    return null;
  }
}
