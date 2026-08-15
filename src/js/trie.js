/**
 * Trie (Prefix Tree) Data Structure
 * Used for:
 * 1. Fast Special Move Command Input Recognition (e.g., ["JAB", "JAB", "KICK"], ["FORWARD", "PUNCH", "SPECIAL"])
 * 2. Instant Fighter Name & Element Keyword Search
 * 3. Dialogue Tag Indexing
 */

export class TrieNode {
  constructor() {
    this.children = new Map();
    this.isEndOfWord = false;
    this.value = null; // Associated command definition or fighter object
  }
}

export class CommandTrie {
  constructor() {
    this.root = new TrieNode();
  }

  /**
   * Insert a command sequence into the Trie
   * @param {Array<string>|string} sequence - Array of action tokens (e.g. ['JAB', 'JAB', 'KICK'])
   * @param {*} value - Special move definition payload
   */
  insert(sequence, value) {
    let current = this.root;
    const tokens = Array.isArray(sequence) ? sequence : sequence.toUpperCase().split('');

    for (const token of tokens) {
      if (!current.children.has(token)) {
        current.children.set(token, new TrieNode());
      }
      current = current.children.get(token);
    }
    current.isEndOfWord = true;
    current.value = value;
  }

  /**
   * Search for exact matching command sequence
   */
  search(sequence) {
    let current = this.root;
    const tokens = Array.isArray(sequence) ? sequence : sequence.toUpperCase().split('');

    for (const token of tokens) {
      if (!current.children.has(token)) {
        return null;
      }
      current = current.children.get(token);
    }
    return current.isEndOfWord ? current.value : null;
  }

  /**
   * Find longest matching suffix/prefix in buffer
   */
  findLongestMatch(sequence) {
    let current = this.root;
    let lastMatch = null;
    const tokens = Array.isArray(sequence) ? sequence : sequence.toUpperCase().split('');

    for (const token of tokens) {
      if (!current.children.has(token)) {
        break;
      }
      current = current.children.get(token);
      if (current.isEndOfWord) {
        lastMatch = current.value;
      }
    }
    return lastMatch;
  }

  /**
   * Auto-complete / prefix suggestions
   */
  getWordsWithPrefix(prefix) {
    let current = this.root;
    const tokens = Array.isArray(prefix) ? prefix : prefix.toUpperCase().split('');

    for (const token of tokens) {
      if (!current.children.has(token)) return [];
      current = current.children.get(token);
    }

    const results = [];
    const traverse = (node, path) => {
      if (node.isEndOfWord) {
        results.push({ path, value: node.value });
      }
      for (const [key, childNode] of node.children.entries()) {
        traverse(childNode, [...path, key]);
      }
    };

    traverse(current, tokens);
    return results;
  }
}
