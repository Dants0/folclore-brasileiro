// Generates every texture of the Folclore mod as flat pixel art (no noise, short palette), in the
// same spirit as the Arraial skins.
//
//   entity/saci.png, entity/curupira.png, entity/curupira_brilho.png   (64x64, match the models)
//   item/peneira.png, item/saci_spawn_egg.png, item/curupira_spawn_egg.png
//   block/gorro_saci.png (used by the 3D cap item model), block/pegada.png
//   icon.png (128x128)
//
// Usage: node tools/gerar_texturas.js            (writes into src/main/resources/assets/folclore)
'use strict';
const fs = require('fs'), path = require('path'), zlib = require('zlib');

// ------------------------------------------------------------------ PNG
const CRC = (() => { const t = new Int32Array(256); for (let n = 0; n < 256; n++) { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1; t[n] = c; } return t; })();
function crc32(b) { let c = -1; for (const x of b) c = CRC[(c ^ x) & 255] ^ (c >>> 8); return (c ^ -1) >>> 0; }
function chunk(tipo, dados) {
  const len = Buffer.alloc(4); len.writeUInt32BE(dados.length);
  const td = Buffer.concat([Buffer.from(tipo), dados]); const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td));
  return Buffer.concat([len, td, crc]);
}
function png(img) {
  const { w, h, px } = img;
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4); ihdr[8] = 8; ihdr[9] = 6;
  const raw = Buffer.alloc((w * 4 + 1) * h);
  for (let y = 0; y < h; y++) { raw[y * (w * 4 + 1)] = 0; px.copy(raw, y * (w * 4 + 1) + 1, y * w * 4, (y + 1) * w * 4); }
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]);
}

// ------------------------------------------------------------------ canvas
const hex = h => [parseInt(h.slice(1, 3), 16), parseInt(h.slice(3, 5), 16), parseInt(h.slice(5, 7), 16), 255];
const imagem = (w, h) => ({ w, h, px: Buffer.alloc(w * h * 4) });
function pintar(img, x, y, cor) {
  if (!cor || x < 0 || y < 0 || x >= img.w || y >= img.h) return;
  const c = typeof cor === 'string' ? hex(cor) : cor;
  const i = (y * img.w + x) * 4; img.px[i] = c[0]; img.px[i + 1] = c[1]; img.px[i + 2] = c[2]; img.px[i + 3] = c[3] ?? 255;
}
function ler(img, x, y) { const i = (y * img.w + x) * 4; return [img.px[i], img.px[i + 1], img.px[i + 2], img.px[i + 3]]; }
function salvar(img, arq) { fs.mkdirSync(path.dirname(arq), { recursive: true }); fs.writeFileSync(arq, png(img)); console.log('  ' + arq); }

/**
 * Paints a model box laid out the Minecraft way at texture offset (u, v) for a cube of size
 * w x h x d. `f(face, x, y, fw, fh)` returns the colour of each pixel of each face, where face is
 * top, bottom, right, front, left or back, and (x, y) is measured inside the face.
 */
function caixa(img, u, v, w, h, d, f) {
  const faces = [
    ['top', u + d, v, w, d], ['bottom', u + d + w, v, w, d],
    ['right', u, v + d, d, h], ['front', u + d, v + d, w, h], ['left', u + d + w, v + d, d, h], ['back', u + d + w + d, v + d, w, h],
  ];
  for (const [nome, fx, fy, fw, fh] of faces)
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) pintar(img, fx + x, fy + y, f(nome, x, y, fw, fh));
}
/** Pixel-art from rows of characters and a palette (space or '.' = keep). */
function grade(img, x0, y0, linhas, pal) {
  linhas.forEach((l, y) => [...l].forEach((ch, x) => { if (ch !== ' ' && ch !== '.') pintar(img, x0 + x, y0 + y, pal[ch]); }));
}
/** Light per face: tops lit, bottoms and backs in shade. */
const LUZ = { top: 1.12, bottom: 0.72, right: 0.88, front: 1.0, left: 0.88, back: 0.8 };
function tom(cor, f) { const c = hex(cor); return [0, 1, 2].map(i => Math.max(0, Math.min(255, Math.round(c[i] * f)))).concat(255); }

// ================================================================== SACI
function saci() {
  const img = imagem(64, 64);
  const PELE = '#5b3521', PELE_C = '#6f4429', PELE_E = '#43261a';
  const VERM = '#c62828', VERM_E = '#962020', VERM_C = '#e04848';
  const lisa = (cor) => (face) => tom(cor, LUZ[face]);

  // Head 6x6x6 at (0,0): the face is drawn on top of plain skin.
  caixa(img, 0, 0, 6, 6, 6, lisa(PELE));
  grade(img, 6, 6, [
    '......',
    'EE..EE',
    'BP..BP',
    '..NN..',
    'MDDDDM',
    '.MMMM.',
  ], { E: PELE_E, B: '#f2ede2', P: '#1b1210', N: PELE_E, M: '#2a1410', D: '#f4efe2' });
  // Ears on the sides.
  pintar(img, 2, 9, PELE_C); pintar(img, 15, 9, PELE_C);

  // Body 6x7x3 at (0,12): bare chest, red shorts with a darker waistband.
  caixa(img, 0, 12, 6, 7, 3, (face, x, y, fw, fh) => {
    if (face === 'top') return tom(PELE, 1.1);
    if (face === 'bottom') return tom(VERM, 0.7);
    if (y === 4) return tom(VERM_E, LUZ[face]);
    if (y > 4) return tom(VERM, LUZ[face]);
    if (face === 'front' && y === 1 && (x === 1 || x === 4)) return tom(PELE_E, 1); // chest
    if (face === 'front' && y === 3 && x === 3) return tom(PELE_E, 1); // navel
    return tom(PELE, LUZ[face]);
  });

  // The one leg 3x9x3 at (24,12): shorts leg on top, bare skin below, dusty foot.
  caixa(img, 24, 12, 3, 9, 3, (face, x, y, fw, fh) => {
    if (face === 'top') return tom(VERM, 1.0);
    if (face === 'bottom') return tom('#3a2216', 1);
    if (y < 2) return tom(VERM, LUZ[face]);
    if (y === 2) return tom(VERM_E, LUZ[face]);
    if (y === fh - 1) return tom('#4a3020', LUZ[face]);
    return tom(PELE, LUZ[face]);
  });

  // Arms 2x7x2 at (40,12) and (48,12).
  for (const u of [40, 48])
    caixa(img, u, 12, 2, 7, 2, (face, x, y, fw, fh) => face === 'bottom' || y === fh - 1 ? tom(PELE_C, LUZ[face]) : tom(PELE, LUZ[face]));

  // Cap: brim 7x2x7 (0,24), middle 5x2x5 (28,24), tip 3x3x3 (48,24).
  caixa(img, 0, 24, 7, 2, 7, (face, x, y) => face === 'bottom' ? tom(VERM_E, 0.8) : y === 1 ? tom(VERM_E, LUZ[face]) : tom(VERM, LUZ[face]));
  caixa(img, 28, 24, 5, 2, 5, (face) => tom(VERM, LUZ[face]));
  caixa(img, 48, 24, 3, 3, 3, (face, x, y) => face === 'top' ? tom(VERM_C, 1) : tom(VERM, LUZ[face] * (y === 0 ? 1.1 : 1)));

  // Clay pipe: stem 1x1x3 (0,34), bowl 2x2x2 (8,34) with a glowing ember on top.
  caixa(img, 0, 34, 1, 1, 3, (face) => tom('#7a4a24', LUZ[face]));
  caixa(img, 8, 34, 2, 2, 2, (face) => face === 'top' ? hex('#ff8a1e') : tom('#5c3518', LUZ[face]));
  return img;
}

// ================================================================== CURUPIRA
const FOGO = ['#a3231a', '#d8421c', '#f47a1c', '#ffb22e', '#ffe066'];
function curupira() {
  const img = imagem(64, 64), brilho = imagem(64, 64);
  const PELE = '#8a5a3a', PELE_C = '#a06c46', PELE_E = '#6a422a';
  const FOLHA = '#3f7a2c', FOLHA_E = '#2c5a20', FOLHA_C = '#5f9c3c';
  const lisa = (cor) => (face) => tom(cor, LUZ[face]);
  /** Fire: dark at the root (bottom of each face), bright at the tips. */
  const fogo = (face, x, y, fw, fh) => {
    if (face === 'top') return hex(FOGO[4]);
    if (face === 'bottom') return hex(FOGO[0]);
    const k = Math.round((1 - y / Math.max(1, fh - 1)) * 3 + ((x + y) % 2) * 0.6);
    return hex(FOGO[Math.max(0, Math.min(4, k + 1))]);
  };

  // Head 7x7x7 at (0,0).
  caixa(img, 0, 0, 7, 7, 7, lisa(PELE));
  grade(img, 7, 7, [
    '.......',
    '.......',
    'BB...BB',
    '.YP.PY.',
    '...N...',
    '..MWM..',
    '.......',
  ], { B: '#c8401a', Y: '#ffcc33', P: '#1b1210', N: PELE_E, M: '#3a1c12', W: '#f4efe2' });
  // Hair cap 8x3x8 at (28,0): fire, with a jagged fringe over the forehead.
  caixa(img, 28, 0, 8, 3, 8, (face, x, y, fw, fh) => {
    if ((face === 'front' || face === 'right' || face === 'left') && y === fh - 1 && x % 2 === 1) return null;
    return fogo(face, x, y, fw, fh);
  });
  // Body 7x8x4 at (0,14): bare, a band of leaves across the chest.
  caixa(img, 0, 14, 7, 8, 4, (face, x, y) => {
    if (face === 'top') return tom(PELE, 1.1);
    if (face === 'bottom') return tom(PELE_E, 1);
    if (y === 1 && (face === 'front' || face === 'back')) return tom((x % 2) ? FOLHA : FOLHA_C, LUZ[face]);
    return tom(PELE, LUZ[face]);
  });
  // Leaf skirt 8x3x5 at (0,40): overlapping leaves, pointed at the bottom edge.
  caixa(img, 0, 40, 8, 3, 5, (face, x, y, fw, fh) => {
    if (face === 'top' || face === 'bottom') return tom(FOLHA_E, 1);
    if (y === fh - 1 && x % 2 === 0) return null;
    return tom([FOLHA, FOLHA_C, FOLHA_E][(x + y * 2) % 3], LUZ[face]);
  });
  // Legs 3x10x3 at (22,14) and (34,14), with leaf bands at the ankles.
  for (const u of [22, 34])
    caixa(img, u, 14, 3, 10, 3, (face, x, y, fw, fh) => {
      if (face === 'top' || face === 'bottom') return tom(PELE_E, 1);
      if (y === 7) return tom(FOLHA, LUZ[face]);
      return tom(PELE, LUZ[face]);
    });
  // Arms 3x9x3 at (46,14) and (0,28), leaf bands at the wrists.
  for (const [u, v] of [[46, 14], [0, 28]])
    caixa(img, u, v, 3, 9, 3, (face, x, y, fw, fh) => {
      if (face === 'top') return tom(PELE, 1.1);
      if (face === 'bottom') return tom(PELE_C, 1);
      if (y === 6) return tom(FOLHA, LUZ[face]);
      if (y >= 7) return tom(PELE_C, LUZ[face]);
      return tom(PELE, LUZ[face]);
    });
  // Backward feet 3x1x3 at (12,28): the toes are on the back face.
  caixa(img, 12, 28, 3, 1, 3, (face, x) => {
    if (face === 'back' && x !== 1) return tom(PELE_E, 1);
    if (face === 'bottom') return hex('#3a2418');
    return tom(PELE_C, LUZ[face]);
  });
  // Flame tufts 2x5x2 (24,28) and the big one 3x6x3 (32,28).
  caixa(img, 24, 28, 2, 5, 2, fogo);
  caixa(img, 32, 28, 3, 6, 3, fogo);

  // Glow layer: hair and eyes only.
  const copiar = (u, v, w, h) => { for (let y = v; y < v + h; y++) for (let x = u; x < u + w; x++) { const c = ler(img, x, y); if (c[3]) pintar(brilho, x, y, c); } };
  copiar(28, 0, 32, 11); copiar(24, 28, 8, 7); copiar(32, 28, 12, 9);
  for (const [x, y] of [[8, 10], [12, 10]]) pintar(brilho, x, y, '#ffcc33');
  return [img, brilho];
}

// ================================================================== ITEMS AND BLOCKS
function peneira() {
  const img = imagem(16, 16);
  const A = '#c9a35a', B = '#9c7a3a', C = '#6e5226';
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const d = Math.hypot(x - 7.5, y - 7.5);
    if (d > 7.6) continue;
    if (d > 6.2) pintar(img, x, y, d > 7.0 ? C : B);           // wooden hoop
    else pintar(img, x, y, (x + y) % 2 ? A : B);               // woven taquara
  }
  return img;
}
function garrafa() {
  const img = imagem(16, 16);
  // Glass bottle with a cork, and the little Saci inside (red cap, dark face, white eyes).
  grade(img, 0, 0, [
    '................',
    '......kkkk......',
    '......kkkk......',
    '.......gg.......',
    '......gGGg......',
    '.....gGrrGg.....',
    '....gGrrrrGg....',
    '...gGGrrrrrGg...',
    '...gGGsssshGg...',
    '...gGGswswhGg...',
    '...gGGssssGGg...',
    '...gGGGsssGGg...',
    '...gGGGsGGGGg...',
    '...ggGGsGGGgg...',
    '....gggggggg....',
    '................',
  ], { k: '#9c6b3a', g: [170, 215, 230, 230], G: [200, 235, 245, 110], r: '#c62828', s: '#5b3521', w: '#f2ede2', h: [235, 250, 255, 200] });
  return img;
}
function gorroBloco() {
  const img = imagem(16, 16);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++)
    pintar(img, x, y, y > 13 ? '#962020' : (x + y * 3) % 7 === 0 ? '#b52424' : '#c62828');
  return img;
}
function ovo(base, mancha, pontos) {
  const img = imagem(16, 16);
  for (let y = 1; y < 16; y++) for (let x = 0; x < 16; x++) {
    const ry = y < 9 ? 7.2 : 6.2, dy = (y - 9) / ry, dx = (x - 7.5) / 5.6;
    const d = dx * dx + dy * dy;
    if (d > 1) continue;
    pintar(img, x, y, d > 0.8 ? tom(base, 0.7) : base);
  }
  for (const [x, y] of pontos) if (ler(img, x, y)[3]) pintar(img, x, y, mancha);
  return img;
}
function pegada() {
  const img = imagem(16, 16);
  const P = [70, 46, 28, 210], Q = [52, 34, 20, 230];
  // One bare foot, toes towards the top edge (north).
  grade(img, 0, 0, [
    '................',
    '.....q.q.q......',
    '....q.q.q.q.....',
    '................',
    '.....pppp.......',
    '....pppppp......',
    '....pppppp......',
    '....ppppp.......',
    '.....pppp.......',
    '.....pppp.......',
    '.....ppppp......',
    '.....ppppp......',
    '.....ppppp......',
    '......ppp.......',
    '................',
    '................',
  ], { p: P, q: Q });
  return img;
}
function icone(saciImg) {
  // The Saci's face (head front + cap) blown up on a forest green background.
  const img = imagem(128, 128);
  for (let y = 0; y < 128; y++) for (let x = 0; x < 128; x++) pintar(img, x, y, (x + y) % 32 < 16 ? '#2e5e2a' : '#336a2e');
  const E = 14, x0 = 64 - 3 * E, y0 = 40;
  for (let y = 0; y < 6; y++) for (let x = 0; x < 6; x++) {
    const c = ler(saciImg, 6 + x, 6 + y);
    for (let a = 0; a < E; a++) for (let b = 0; b < E; b++) pintar(img, x0 + x * E + b, y0 + y * E + a, c);
  }
  const cap = [[-1, 0, 8], [0, -1, 6], [1, -2, 4], [3, -3, 2]];
  for (const [dx, dy, w] of cap) for (let i = 0; i < w; i++)
    for (let a = 0; a < E; a++) for (let b = 0; b < E; b++)
      pintar(img, x0 + (dx + i) * E + b, y0 + (dy - 1) * E + a, (i + dy) % 3 === 0 ? '#962020' : '#c62828');
  return img;
}

// ================================================================== write
const raiz = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'folclore');
const t = path.join(raiz, 'textures');
const s = saci(); const [c, cb] = curupira();
salvar(s, path.join(t, 'entity', 'saci.png'));
salvar(c, path.join(t, 'entity', 'curupira.png'));
salvar(cb, path.join(t, 'entity', 'curupira_brilho.png'));
salvar(peneira(), path.join(t, 'item', 'peneira.png'));
salvar(ovo('#4a2a1a', '#c62828', [[5, 4], [9, 6], [6, 9], [10, 11], [7, 13], [4, 11]]), path.join(t, 'item', 'saci_spawn_egg.png'));
salvar(ovo('#3f7a2c', '#f47a1c', [[6, 3], [9, 5], [5, 8], [10, 9], [7, 12], [9, 13]]), path.join(t, 'item', 'curupira_spawn_egg.png'));
salvar(garrafa(), path.join(t, 'item', 'saci_garrafa.png'));
salvar(gorroBloco(), path.join(t, 'block', 'gorro_saci.png'));
salvar(pegada(), path.join(t, 'block', 'pegada.png'));
salvar(icone(s), path.join(raiz, 'icon.png'));

// ---- The other creatures (tools/criaturas.js)
const C = require('./criaturas.js')({ imagem, pintar, ler, caixa, grade, tom, hex, LUZ, FOGO });
for (const nome of ['boitata', 'mula', 'cuca', 'lobisomem', 'mapinguari']) {
  const [img, brilho] = C[nome]();
  const arq = nome === 'mula' ? 'mula_sem_cabeca' : nome;
  salvar(img, path.join(t, 'entity', arq + '.png'));
  salvar(brilho, path.join(t, 'entity', arq + '_brilho.png'));
}
salvar(C.iara(), path.join(t, 'entity', 'iara.png'));
salvar(C.boto(), path.join(t, 'entity', 'boto.png'));
salvar(C.olho(), path.join(t, 'item', 'olho_boitata.png'));
salvar(C.freio(), path.join(t, 'item', 'freio_mula.png'));
salvar(C.escama(), path.join(t, 'item', 'escama_iara.png'));
salvar(C.garra(), path.join(t, 'item', 'garra_mapinguari.png'));
salvar(C.chapeuBloco(), path.join(t, 'block', 'chapeu_boto.png'));
const OVOS = {
  boitata: ['#d8421c', '#ffe066'], mula_sem_cabeca: ['#3b2a20', '#f47a1c'], iara: ['#2a8a7a', '#1c1410'],
  boto: ['#e89aa8', '#f4f2ec'], cuca: ['#4f7a3a', '#5a3a7a'], lobisomem: ['#4a4038', '#ff3020'], mapinguari: ['#8a3a22', '#2a0a08'],
};
const PONTOS = [[5, 4], [9, 6], [6, 9], [10, 11], [7, 13], [4, 11]];
for (const [nome, [base, mancha]] of Object.entries(OVOS)) salvar(ovo(base, mancha, PONTOS), path.join(t, 'item', nome + '_spawn_egg.png'));
