/**
 * JSManager - JavaScript Runtime Lifecycle, Plugin Registry & Syntax Verifier
 * Supervises engine plugins, sandbox evaluation, and global context injection.
 */
export class JSManager {
  constructor() {
    this.plugins = new Map();
    this.runtimeContext = {
      version: '2.4.0',
      environment: 'tcl_mobile_arcade',
      viewport: { width: 350, height: 550, resolution: '720x1440' }
    };
    this.executionHooks = {
      beforeInit: [],
      afterInit: [],
      beforeTick: [],
      afterTick: []
    };
  }

  registerPlugin(name, pluginInstance) {
    if (typeof pluginInstance.init === 'function') {
      pluginInstance.init(this.runtimeContext);
    }
    this.plugins.set(name, pluginInstance);
    return this;
  }

  getPlugin(name) {
    return this.plugins.get(name) || null;
  }

  addHook(hookType, callback) {
    if (this.executionHooks[hookType]) {
      this.executionHooks[hookType].push(callback);
    }
    return this;
  }

  runHook(hookType, payload = null) {
    const list = this.executionHooks[hookType] || [];
    for (const fn of list) {
      try {
        fn(payload, this.runtimeContext);
      } catch (err) {
        console.error(`JSManager Hook Error [${hookType}]:`, err);
      }
    }
  }

  verifySyntax(codeString) {
    try {
      new Function(codeString);
      return { valid: true, error: null };
    } catch (e) {
      return { valid: false, error: e.message };
    }
  }

  getSystemInfo() {
    return {
      runtime: this.runtimeContext,
      pluginsCount: this.plugins.size,
      registeredPlugins: Array.from(this.plugins.keys())
    };
  }
}

export const jsManager = new JSManager();
