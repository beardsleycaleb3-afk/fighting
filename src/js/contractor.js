/**
 * Contractor - Data Schema Validation, Interface Contracts & State Assertion
 * Enforces runtime data invariants and integrity for fighters, stages, moves, and events.
 */
export class Contractor {
  constructor() {
    this.contracts = new Map();
  }

  defineContract(name, validatorFn) {
    this.contracts.set(name, validatorFn);
    return this;
  }

  validate(name, data) {
    const validator = this.contracts.get(name);
    if (!validator) {
      return { valid: true, warning: `No contract registered for '${name}'` };
    }

    try {
      const result = validator(data);
      if (typeof result === 'boolean') {
        return { valid: result, errors: result ? [] : ['Contract validation failed'] };
      }
      return result;
    } catch (err) {
      return { valid: false, errors: [err.message] };
    }
  }

  assert(name, data) {
    const res = this.validate(name, data);
    if (!res.valid) {
      throw new Error(`[Contract Assertion Failed] ${name}: ${JSON.stringify(res.errors)}`);
    }
    return true;
  }
}

export const contractor = new Contractor();

// Register Default Engine Contracts
contractor.defineContract('Fighter', (data) => {
  const errors = [];
  if (!data.id) errors.push('Missing fighter id');
  if (!data.name) errors.push('Missing fighter name');
  if (typeof data.maxHp !== 'number' || data.maxHp <= 0) errors.push('Invalid maxHp');
  if (!data.elementalState) errors.push('Missing elementalState');
  if (!data.finalBlowMeleeSmash) errors.push('Missing finalBlowMeleeSmash');
  return { valid: errors.length === 0, errors };
});

contractor.defineContract('Stage', (data) => {
  const errors = [];
  if (!data.id) errors.push('Missing stage id');
  if (!data.name) errors.push('Missing stage name');
  if (!data.era) errors.push('Missing stage era');
  return { valid: errors.length === 0, errors };
});

contractor.defineContract('CombatAction', (data) => {
  const errors = [];
  if (!['punch', 'kick', 'special', 'smash', 'block'].includes(data.type?.toLowerCase())) {
    errors.push(`Invalid action type: ${data.type}`);
  }
  return { valid: errors.length === 0, errors };
});
