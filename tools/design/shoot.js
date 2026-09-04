// Renders the Cashfluent artboards to PNG at a true 360x800 phone viewport.
// These are renders of the design, not screenshots of the built APK.
const { chromium } = require('playwright-core');
const fs = require('fs');
const path = require('path');

const CANVAS = path.join(__dirname, 'artboards');
const OUT = process.env.SHOTS_OUT || path.join(__dirname, 'out');
// Set CHROME to your own browser if this path does not exist on your machine.
const CHROME = process.env.CHROME || '/opt/pw-browsers/chromium-1194/chrome-linux/chrome';

function build(name) {
  const src = fs.readFileSync(path.join(CANVAS, name + '.dc.html'), 'utf8');
  const inner = src.split('<x-dc>')[1].split('</x-dc>')[0];
  const helmet = inner.match(/<helmet>([\s\S]*?)<\/helmet>/);
  const head = helmet ? helmet[1] : '';
  const body = inner.replace(/<helmet>[\s\S]*?<\/helmet>/, '');
  const page = `<!doctype html><html><head><meta charset="utf-8">${head}` +
    `<style>html,body{margin:0;padding:0;width:360px;height:800px;overflow:hidden}</style>` +
    `</head><body>${body}</body></html>`;
  const file = path.join(OUT, 'tmp', name + '.html');
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, page);
  return file;
}

(async () => {
  const browser = await chromium.launch({ executablePath: CHROME, args: ['--no-sandbox'] });
  const page = await browser.newPage({
    viewport: { width: 360, height: 800 },
    deviceScaleFactor: 2,
  });
  for (const name of process.argv.slice(2)) {
    await page.goto('file://' + build(name), { waitUntil: 'networkidle' });
    await page.evaluate(() => document.fonts.ready);
    const out = path.join(OUT, name + '.png');
    await page.screenshot({ path: out });
    console.log(name + '.png  ' + Math.round(fs.statSync(out).size / 1024) + ' KB');
  }
  await browser.close();
})();
