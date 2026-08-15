/**
 * Advanced Array & Vector Processing Utilities for Game Engine
 * Includes shuffle, partition, chunking, binary search, interpolation,
 * 2D grid matrix math, and statistical roll aggregations.
 */

export const ArrayUtils = {
  /**
   * Fisher-Yates pure in-place/copy shuffle
   */
  shuffle(arr, copy = true) {
    const target = copy ? [...arr] : arr;
    for (let i = target.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [target[i], target[j]] = [target[j], target[i]];
    }
    return target;
  },

  /**
   * Pick random sample of k unique items
   */
  sample(arr, count = 1) {
    if (count >= arr.length) return this.shuffle(arr, true);
    const shuffled = this.shuffle(arr, true);
    return shuffled.slice(0, count);
  },

  /**
   * Divide array into chunks of size n
   */
  chunk(arr, size) {
    if (size <= 0) return [arr];
    const results = [];
    for (let i = 0; i < arr.length; i += size) {
      results.push(arr.slice(i, i + size));
    }
    return results;
  },

  /**
   * Partition array by predicate function [matches, nonMatches]
   */
  partition(arr, predicate) {
    const truthy = [];
    const falsy = [];
    for (let i = 0; i < arr.length; i++) {
      if (predicate(arr[i], i, arr)) {
        truthy.push(arr[i]);
      } else {
        falsy.push(arr[i]);
      }
    }
    return [truthy, falsy];
  },

  /**
   * Binary search for sorted numeric or mapped values
   */
  binarySearch(arr, target, keyFn = x => x) {
    let low = 0;
    let high = arr.length - 1;

    while (low <= high) {
      const mid = (low + high) >>> 1;
      const midVal = keyFn(arr[mid]);

      if (midVal === target) return mid;
      if (midVal < target) low = mid + 1;
      else high = mid - 1;
    }
    return -(low + 1); // Not found insertion index
  },

  /**
   * Sliding window map for rolling smoothing / DPS calculation
   */
  slidingWindow(arr, windowSize, reducer = (acc, val) => acc + val) {
    const result = [];
    for (let i = 0; i <= arr.length - windowSize; i++) {
      const slice = arr.slice(i, i + windowSize);
      result.push(slice.reduce(reducer));
    }
    return result;
  },

  /**
   * Linear interpolation between two numeric vectors [x1, y1] -> [x2, y2]
   */
  lerpVector2D(v1, v2, t) {
    const clampedT = Math.max(0, Math.min(1, t));
    return [
      v1[0] + (v2[0] - v1[0]) * clampedT,
      v1[1] + (v2[1] - v1[1]) * clampedT
    ];
  },

  /**
   * Weighted random selection
   */
  weightedChoice(items, weights) {
    const totalWeight = weights.reduce((sum, w) => sum + w, 0);
    let randomVal = Math.random() * totalWeight;

    for (let i = 0; i < items.length; i++) {
      randomVal -= weights[i];
      if (randomVal <= 0) return items[i];
    }
    return items[items.length - 1];
  },

  /**
   * Generate sequential range [start..end] with step
   */
  range(start, end, step = 1) {
    const result = [];
    for (let i = start; step > 0 ? i <= end : i >= end; i += step) {
      result.push(i);
    }
    return result;
  }
};
