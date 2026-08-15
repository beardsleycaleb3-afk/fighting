/**
 * Tree Data Structures for Tournament Brackets, Skill Trees & AI Decisions
 * - TournamentTree (Single-elimination championship bracket)
 * - SkillTree (Elemental talent nodes & spirit upgrades)
 * - DecisionTreeNode (State-machine AI behavior evaluation)
 */

export class TreeNode {
  constructor(key, value = null) {
    this.key = key;
    this.value = value;
    this.children = [];
    this.parent = null;
  }

  addChild(node) {
    node.parent = this;
    this.children.push(node);
    return node;
  }
}

/**
 * Binary Tournament Bracket Tree
 */
export class TournamentMatchNode {
  constructor(roundName, matchId, p1 = null, p2 = null) {
    this.roundName = roundName;
    this.matchId = matchId;
    this.p1 = p1;
    this.p2 = p2;
    this.winner = null;
    this.leftMatch = null;
    this.rightMatch = null;
  }
}

export class TournamentTree {
  constructor(fighters = []) {
    this.fighters = fighters;
    this.root = null;
    if (fighters.length >= 2) {
      this.buildBracket(fighters);
    }
  }

  buildBracket(fighters) {
    // Generate an 8-man or 4-man single-elimination tournament tree
    const numFighters = Math.min(8, Math.pow(2, Math.floor(Math.log2(fighters.length))));
    const roster = fighters.slice(0, numFighters);

    const quarterFinals = [];
    for (let i = 0; i < numFighters; i += 2) {
      const match = new TournamentMatchNode('QUARTER-FINALS', `QF_${i / 2 + 1}`, roster[i], roster[i + 1]);
      quarterFinals.push(match);
    }

    const semiFinals = [];
    for (let i = 0; i < quarterFinals.length; i += 2) {
      const match = new TournamentMatchNode('SEMI-FINALS', `SF_${i / 2 + 1}`);
      match.leftMatch = quarterFinals[i];
      match.rightMatch = quarterFinals[i + 1];
      semiFinals.push(match);
    }

    const finals = new TournamentMatchNode('GRAND FINALS', 'FINAL_1');
    finals.leftMatch = semiFinals[0];
    finals.rightMatch = semiFinals[1];

    this.root = finals;
    return finals;
  }

  simulateMatch(node, winnerFighter) {
    node.winner = winnerFighter;
    return node.winner;
  }
}

/**
 * Elemental Spirit Skill Tree
 */
export class SkillTreeNode {
  constructor(id, name, level, description, statBoost, isUnlocked = false) {
    this.id = id;
    this.name = name;
    this.level = level;
    this.description = description;
    this.statBoost = statBoost; // e.g. { power: 0.05, meterGain: 0.1 }
    this.isUnlocked = isUnlocked;
    this.children = [];
  }

  unlock() {
    this.isUnlocked = true;
  }
}
