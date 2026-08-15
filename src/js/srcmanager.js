/**
 * SrcManager - Dynamic Source Script, Module Registry & Dependency Graph Resolver
 * Coordinates runtime module loading, versioning, and script dependency chains.
 */
export class SrcManager {
  constructor() {
    this.registry = new Map();
    this.loadedModules = new Map();
    this.dependencies = new Map();
    this.status = 'idle'; // 'idle', 'loading', 'ready', 'error'
  }

  register(moduleName, moduleInstance, deps = []) {
    this.registry.set(moduleName, {
      instance: moduleInstance,
      dependencies: deps,
      registeredAt: performance.now()
    });
    this.dependencies.set(moduleName, deps);
    return this;
  }

  get(moduleName) {
    const entry = this.registry.get(moduleName);
    return entry ? entry.instance : null;
  }

  has(moduleName) {
    return this.registry.has(moduleName);
  }

  async loadDynamicScript(src, id = null) {
    return new Promise((resolve, reject) => {
      if (id && document.getElementById(id)) {
        resolve(document.getElementById(id));
        return;
      }
      const script = document.createElement('script');
      if (id) script.id = id;
      script.src = src;
      script.async = true;
      script.onload = () => {
        this.loadedModules.set(id || src, { loaded: true, time: performance.now() });
        resolve(script);
      };
      script.onerror = (err) => reject(new Error(`Failed to load source: ${src}`));
      document.head.appendChild(script);
    });
  }

  resolveDependencyOrder() {
    const visited = new Set();
    const result = [];
    const visiting = new Set();

    const visit = (node) => {
      if (visiting.has(node)) throw new Error(`Cyclic dependency detected: ${node}`);
      if (!visited.has(node)) {
        visiting.add(node);
        const deps = this.dependencies.get(node) || [];
        for (const dep of deps) {
          if (this.registry.has(dep)) {
            visit(dep);
          }
        }
        visiting.delete(node);
        visited.add(node);
        result.push(node);
      }
    };

    for (const key of this.registry.keys()) {
      visit(key);
    }
    return result;
  }

  getManifest() {
    const manifest = {};
    for (const [key, val] of this.registry.entries()) {
      manifest[key] = {
        deps: val.dependencies,
        registeredAt: val.registeredAt
      };
    }
    return manifest;
  }
}

export const srcManager = new SrcManager();
