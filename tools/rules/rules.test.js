// firestore.rules, run against the Firestore emulator.
//
// The rules are the only thing standing between a public app and twenty strangers'
// scoreboards, so they are tested rather than trusted. The one that matters most is the
// week: the rules compute it from the server's clock with the same arithmetic as
// Week.index on the phone, and if the two ever disagreed every write would fail with
// nothing to see. That is the first test below.
//
//   cd tools/rules && npm install && npm test

import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import { deleteDoc, doc, getDoc, setDoc, updateDoc } from 'firebase/firestore';

const here = path.dirname(fileURLToPath(import.meta.url));
const rules = fs.readFileSync(path.join(here, '..', '..', 'firestore.rules'), 'utf8');

// Week.index, in JavaScript. Whole days since the epoch, shifted so a week starts on Monday.
const week = (millis = Date.now()) => Math.floor((Math.floor(millis / 86_400_000) + 3) / 7);

const THIS_WEEK = week();
const LAST_WEEK = THIS_WEEK - 1;
const NEXT_WEEK = THIS_WEEK + 1;

const board = (id) => `w${id}-wood-1`;
const row = (over = {}) => ({ name: 'Andrea', totalPoints: 400, weekPoints: 400, ...over });

const env = await initializeTestEnvironment({
  projectId: 'demo-cashfluent',
  firestore: { rules, host: '127.0.0.1', port: 8080 },
});

const tests = [];
const test = (name, body) => tests.push([name, body]);

/** A board that already exists, written past the rules, as the seat-taking transaction would. */
async function existingBoard(weekIndex) {
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, 'leagues', board(weekIndex)), { week: weekIndex, tier: 0, seats: 1 });
  });
  return board(weekIndex);
}

test('the rules count weeks exactly as the phone does', async () => {
  // If this fails, nothing else can pass: every write is judged against the current week.
  const me = env.authenticatedContext('phone-a').firestore();
  await assertSucceeds(setDoc(doc(me, 'lobbies', `w${THIS_WEEK}-wood`), {
    week: THIS_WEEK, tier: 0, league: board(THIS_WEEK), seats: 1, opened: 1,
  }));
});

test('a queue cannot be opened in a week that has been and gone, or one that has not come', async () => {
  const me = env.authenticatedContext('phone-a').firestore();
  for (const other of [LAST_WEEK, NEXT_WEEK]) {
    await assertFails(setDoc(doc(me, 'lobbies', `w${other}-wood`), {
      week: other, tier: 0, league: board(other), seats: 1, opened: 1,
    }));
  }
});

test('nobody who has not signed in can read a board or write a row', async () => {
  const leagueId = await existingBoard(THIS_WEEK);
  const stranger = env.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(stranger, 'leagues', leagueId)));
  await assertFails(setDoc(doc(stranger, 'leagues', leagueId, 'members', 'phone-a'), row()));
});

test('a phone writes its own row, and reads everyone else’s', async () => {
  const leagueId = await existingBoard(THIS_WEEK);
  const me = env.authenticatedContext('phone-a').firestore();
  await assertSucceeds(setDoc(doc(me, 'leagues', leagueId, 'members', 'phone-a'), row()));
  await assertSucceeds(getDoc(doc(me, 'leagues', leagueId, 'members', 'phone-b')));
});

test('a phone cannot write, or delete, somebody else’s row', async () => {
  const leagueId = await existingBoard(THIS_WEEK);
  const me = env.authenticatedContext('phone-a').firestore();
  await assertFails(setDoc(doc(me, 'leagues', leagueId, 'members', 'phone-b'), row()));
  await assertFails(deleteDoc(doc(me, 'leagues', leagueId, 'members', 'phone-b')));
});

test('a score that could not have been played is not a score', async () => {
  const leagueId = await existingBoard(THIS_WEEK);
  const me = env.authenticatedContext('phone-a').firestore();
  const mine = doc(me, 'leagues', leagueId, 'members', 'phone-a');
  await assertFails(setDoc(mine, row({ weekPoints: 500_000 })));          // more than a week holds
  await assertFails(setDoc(mine, row({ weekPoints: -100 })));             // less than nothing
  await assertFails(setDoc(mine, row({ weekPoints: 900, totalPoints: 400 }))); // this week beats all time
  await assertFails(setDoc(mine, row({ totalPoints: 10_000_000 })));      // past the ceiling
  await assertFails(setDoc(mine, row({ weekPoints: '400' })));            // not even a number
});

test('a nickname is twenty characters, and there is nowhere to hide anything else', async () => {
  const leagueId = await existingBoard(THIS_WEEK);
  const me = env.authenticatedContext('phone-a').firestore();
  const mine = doc(me, 'leagues', leagueId, 'members', 'phone-a');
  await assertSucceeds(setDoc(mine, row({ name: 'x'.repeat(20) })));
  await assertFails(setDoc(mine, row({ name: 'x'.repeat(21) })));
  await assertFails(setDoc(mine, row({ email: 'andrea@example.com' })));
  await assertFails(setDoc(mine, { name: 'Andrea' }));
});

test('a board whose week has ended is closed for writing, and still open for reading', async () => {
  const leagueId = await existingBoard(LAST_WEEK);
  const me = env.authenticatedContext('phone-a').firestore();
  await assertFails(setDoc(doc(me, 'leagues', leagueId, 'members', 'phone-a'), row()));
  await assertSucceeds(getDoc(doc(me, 'leagues', leagueId, 'members', 'phone-a')));
  // Last week's board cannot be reopened by moving its week forward either.
  await assertFails(updateDoc(doc(me, 'leagues', leagueId), { week: THIS_WEEK }));
});

test('leaving a board is allowed; deleting one is not', async () => {
  const leagueId = await existingBoard(THIS_WEEK);
  const me = env.authenticatedContext('phone-a').firestore();
  await assertSucceeds(setDoc(doc(me, 'leagues', leagueId, 'members', 'phone-a'), row()));
  await assertSucceeds(deleteDoc(doc(me, 'leagues', leagueId, 'members', 'phone-a')));
  await assertFails(deleteDoc(doc(me, 'leagues', leagueId)));
});

test('nothing exists outside the three collections', async () => {
  const me = env.authenticatedContext('phone-a').firestore();
  await assertFails(setDoc(doc(me, 'users', 'phone-a'), { anything: true }));
  await assertFails(getDoc(doc(me, 'secrets', 'anything')));
});

let failed = 0;
for (const [name, body] of tests) {
  await env.clearFirestore();
  try {
    await body();
    console.log(`  ok   ${name}`);
  } catch (error) {
    failed++;
    console.log(`  FAIL ${name}`);
    console.log(`       ${error.message.split('\n')[0]}`);
  }
}
await env.cleanup();

console.log(`\n${tests.length - failed} of ${tests.length} rules tests passed`);
assert.equal(failed, 0, `${failed} rules test(s) failed`);
