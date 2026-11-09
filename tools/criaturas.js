// Textures of the creatures added after the Saci and the Curupira. Loaded by gerar_texturas.js,
// which passes in its drawing helpers. Each UV offset here matches the texOffs in the model class
// of the same creature (client/...Model.java).
'use strict';

module.exports = function (h) {
  const { imagem, pintar, ler, caixa, grade, tom, hex, LUZ, FOGO } = h;
  const liso = cor => face => tom(cor, LUZ[face]);
  /** Fur or scales: a base colour broken by a second tone in a regular pattern. */
  const pelo = (base, outro, passo = 5) => (face, x, y) => tom(((x * 7 + y * 3) % passo === 0) ? outro : base, LUZ[face]);
  const fogo = (face, x, y, fw, fh) => {
    if (face === 'top') return hex(FOGO[4]);
    if (face === 'bottom') return hex(FOGO[0]);
    const k = Math.round((1 - y / Math.max(1, fh - 1)) * 3 + ((x + y) % 2) * 0.6);
    return hex(FOGO[Math.max(0, Math.min(4, k + 1))]);
  };
  /** Copies every opaque pixel of a region of `de` into `para` (for the glow layers). */
  const copiar = (de, para, u, v, w, hh) => {
    for (let y = v; y < v + hh; y++) for (let x = u; x < u + w; x++) { const c = ler(de, x, y); if (c[3]) pintar(para, x, y, c); }
  };

  // ================================================================ BOITATÁ (64x64)
  function boitata() {
    const img = imagem(64, 64);
    const escama = (face, x, y) => {
      if (face === 'bottom') return hex(FOGO[1]);
      if (face === 'top') return hex((x + y) % 2 ? FOGO[3] : FOGO[4]);
      return hex(FOGO[((x + y * 2) % 3) + 1]);
    };
    caixa(img, 0, 0, 6, 5, 8, escama);                         // head
    grade(img, 8, 8, ['......', '......', '.n..n.', 'mmmmmm', '......'], { n: '#3a0a00', m: '#a3231a' }); // nostrils, mouth
    caixa(img, 28, 0, 2, 3, 3, (face, x, y) => (y === 1 && x === 1 && face !== 'top' && face !== 'bottom') ? hex('#1a0800') : hex('#fff6c0'));
    caixa(img, 38, 5, 1, 0, 3, () => hex('#ff3b3b'));            // tongue
    caixa(img, 0, 13, 5, 5, 6, escama);                         // body segment
    caixa(img, 22, 13, 3, 3, 5, escama);                        // tail
    caixa(img, 38, 0, 1, 3, 2, fogo);                           // flame spikes
    return [img, img];                                          // all of him glows
  }

  // ================================================================ MULA SEM CABEÇA (64x64)
  function mula() {
    const img = imagem(64, 64), brilho = imagem(64, 64);
    const PELO = '#3b2a20', PELO2 = '#33241b', BARRIGA = '#5a4434';
    caixa(img, 0, 0, 8, 8, 18, (face, x, y, fw, fh) => {
      if (face === 'bottom') return tom(BARRIGA, 0.8);
      if ((face === 'left' || face === 'right') && y >= fh - 2) return tom(BARRIGA, LUZ[face]);
      return pelo(PELO, PELO2, 6)(face, x, y);
    });
    caixa(img, 0, 26, 5, 7, 5, (face, x, y) => {
      if (face === 'top') return hex((x + y) % 2 ? '#ff7a1c' : '#ffb22e');   // the burning stump
      if (y === 0) return hex('#d8421c');
      return pelo(PELO, PELO2, 6)(face, x, y);
    });
    caixa(img, 0, 38, 6, 1, 6, (face, x) => tom(x % 3 === 0 ? '#c0c0c0' : '#7a7a7a', LUZ[face])); // iron bridle
    caixa(img, 20, 26, 2, 5, 2, fogo);
    caixa(img, 28, 26, 3, 7, 3, fogo);
    caixa(img, 40, 26, 3, 10, 3, (face, x, y, fw, fh) => {
      if (face === 'bottom' || y >= fh - 2) return tom(y === fh - 1 || face === 'bottom' ? '#9a9a9a' : '#6a6a6a', LUZ[face]); // iron shoes
      return pelo(PELO, PELO2, 6)(face, x, y);
    });
    caixa(img, 52, 26, 2, 8, 2, (face, x, y) => tom(y % 3 === 0 ? '#2a2a2a' : '#1a1a1a', LUZ[face]));
    copiar(img, brilho, 20, 26, 20, 10);
    copiar(img, brilho, 5, 26, 5, 5);                           // top face of the neck
    return [img, brilho];
  }

  // ================================================================ IARA (64x64)
  function iara() {
    const img = imagem(64, 64);
    const PELE = '#a8754e', CAB = '#1c1410', CAB2 = '#2e2018';
    const FOLHA = '#3f7a2c', FOLHA2 = '#5f9c3c';
    const ESC = ['#1f6b60', '#2a8a7a', '#3aa595'];
    // Head: skin, hair on top/back/sides and a fringe over the forehead.
    caixa(img, 0, 0, 8, 8, 8, (face, x, y) => {
      if (face === 'top' || face === 'back') return tom((x + y) % 4 === 0 ? CAB2 : CAB, LUZ[face]);
      if ((face === 'right' || face === 'left') && (y < 6)) return tom(CAB, LUZ[face]);
      if (face === 'front' && y < 2) return tom(CAB, 1);
      if (face === 'bottom') return tom(PELE, 0.8);
      return tom(PELE, LUZ[face]);
    });
    grade(img, 8, 8, ['........', '........', '.k....k.', '.wb..bw.', '........', '...nn...', '..llll..', '........'],
      { k: CAB, w: '#f2ede2', b: '#3a2010', n: '#8f5f3e', l: '#8a4a3a' });
    caixa(img, 32, 0, 9, 14, 3, (face, x, y) => tom((x * 3 + y) % 5 === 0 ? CAB2 : CAB, LUZ[face]));  // long hair
    caixa(img, 0, 16, 8, 10, 4, (face, x, y) => {
      if (face === 'top') return tom(PELE, 1.1);
      if (y === 0 && (face === 'front' || face === 'back') && x % 2 === 0) return hex('#f2ede2');      // shell necklace
      if (y >= 2 && y <= 4) return tom((x + y) % 2 ? FOLHA : FOLHA2, LUZ[face]);                       // leaf top
      if (y >= 8) return tom(ESC[(x + y) % 3], LUZ[face]);                                              // scales begin at the hips
      return tom(PELE, LUZ[face]);
    });
    caixa(img, 24, 16, 3, 10, 3, (face, x, y, fw, fh) => y === 5 ? tom(FOLHA, LUZ[face]) : tom(PELE, LUZ[face]));
    const escamas = (face, x, y) => face === 'bottom' || face === 'top' ? tom(ESC[1], LUZ[face]) : tom(ESC[(x + y * 2) % 3], LUZ[face]);
    caixa(img, 36, 17, 7, 6, 4, escamas);
    caixa(img, 0, 30, 5, 5, 3, escamas);
    caixa(img, 16, 30, 8, 4, 1, (face, x, y) => [127, 211, 196, 255].map((v, i) => i < 3 ? Math.round(v * LUZ[face] * (y % 2 ? 0.92 : 1)) : v));
    return img;
  }

  // ================================================================ BOTO (128x64)
  function boto() {
    const img = imagem(128, 64);
    const ROSA = '#e89aa8', ROSA_E = '#d47f90', ROSA_C = '#f6c4cc';
    const golf = (face, x, y, fw, fh) => {
      if (face === 'bottom') return tom(ROSA_C, 0.95);
      if (face === 'top') return tom(ROSA_E, 1.05);
      if ((face === 'left' || face === 'right') && y >= fh - 2) return tom(ROSA_C, LUZ[face]);
      return tom((x * 5 + y) % 9 === 0 ? ROSA_E : ROSA, LUZ[face]);
    };
    caixa(img, 0, 0, 8, 7, 13, golf);
    pintar(img, 13 - 2, 13 + 2, '#2a1a1e');                    // eye, right side (front end)
    pintar(img, 13 + 8 + 1, 13 + 2, '#2a1a1e');                // eye, left side
    caixa(img, 42, 0, 2, 2, 4, golf);
    caixa(img, 52, 20, 1, 3, 4, golf);
    caixa(img, 24, 25, 4, 1, 3, golf);
    caixa(img, 0, 20, 4, 4, 8, golf);
    caixa(img, 24, 20, 10, 1, 4, golf);

    // The young man in white.
    const PELE = '#b98260', TERNO = '#eeeae0', TERNO_E = '#d8d2c4', CAB = '#2a1c14';
    caixa(img, 64, 0, 8, 8, 8, (face, x, y) => {
      if (face === 'top' || face === 'back' || ((face === 'left' || face === 'right') && y < 3)) return tom(CAB, LUZ[face]);
      if (face === 'bottom') return tom(PELE, 0.8);
      return tom(PELE, LUZ[face]);
    });
    grade(img, 72, 8, ['........', '........', '........', '.wb..bw.', '........', '...nn...', '..mmmm..', '........'],
      { w: '#f2ede2', b: '#2a1a10', n: '#9a6a4a', m: '#7a4a3a' });
    caixa(img, 64, 16, 8, 12, 4, (face, x, y) => {
      if (face === 'front') {
        if (y < 3 && (x === 3 || x === 4)) return hex('#f8f8f8');               // shirt
        if (y < 6 && (x === 2 || x === 5)) return tom(TERNO_E, 1);              // lapels
        if (y === 1 && x === 1) return hex('#d8435a');                          // a flower in the lapel
        if (y === 8 && (x === 3 || x === 4)) return hex('#c8a24a');             // belt buckle
      }
      return tom(y === 8 ? TERNO_E : TERNO, LUZ[face]);
    });
    caixa(img, 88, 16, 4, 12, 4, (face, x, y, fw, fh) => y >= fh - 2 || face === 'bottom' ? tom(PELE, LUZ[face]) : tom(TERNO, LUZ[face]));
    caixa(img, 104, 16, 4, 12, 4, (face, x, y, fw, fh) => y >= fh - 2 || face === 'bottom' ? tom('#e0d8c8', LUZ[face] * 0.9) : tom(TERNO, LUZ[face]));
    caixa(img, 64, 32, 10, 1, 10, liso('#f4f2ec'));            // hat brim
    caixa(img, 104, 32, 6, 3, 6, (face, x, y) => y === 2 && face !== 'top' && face !== 'bottom' ? hex('#2a2a2a') : tom('#f4f2ec', LUZ[face]));
    return img;
  }

  // ================================================================ CUCA (64x64)
  function cuca() {
    const img = imagem(64, 64), brilho = imagem(64, 64);
    const VERDE = '#4f7a3a', VERDE_E = '#3a5c2a', VERDE_C = '#6c9a4a', CAB = '#e0c050', CAB2 = '#c8a83a';
    const ROXO = '#5a3a7a', ROXO_E = '#432a5c';
    const couro = (face, x, y) => face === 'bottom' ? tom(VERDE_C, 0.9) : tom(((x + y) % 3 === 0) ? VERDE_E : VERDE, LUZ[face]);
    caixa(img, 0, 0, 7, 6, 7, couro);
    caixa(img, 28, 0, 5, 3, 6, (face, x, y, fw, fh) => {
      if ((face === 'front' || face === 'left' || face === 'right') && y === fh - 1 && x % 2 === 0) return hex('#f4efe2'); // teeth
      if (face === 'front' && y === 0 && (x === 1 || x === 3)) return hex('#1a2a10');                                     // nostrils
      return couro(face, x, y);
    });
    caixa(img, 28, 9, 5, 1, 5, (face, x) => face === 'top' && x % 2 === 0 ? hex('#f4efe2') : couro(face, x, 0));
    caixa(img, 50, 0, 2, 2, 2, (face, x, y) => face === 'front' ? (x === 1 && y === 1 ? hex('#1a1000') : hex('#ffd23a')) : couro(face, x, y));
    caixa(img, 0, 13, 8, 8, 5, (face, x, y) => tom((x * 3 + y) % 4 === 0 ? CAB2 : CAB, LUZ[face]));                  // straw hair
    caixa(img, 0, 26, 8, 11, 5, (face, x, y) => {
      if (face === 'front' && y < 2 && x > 1 && x < 6) return couro(face, x, y);                                        // neck
      return tom(((x + y) % 5 === 0) ? '#8a6aaa' : (y > 8 ? ROXO_E : ROXO), LUZ[face]);
    });
    caixa(img, 26, 26, 10, 6, 7, (face, x, y, fw, fh) => tom(y === fh - 1 ? ROXO_E : ((x + y) % 5 === 0 ? '#8a6aaa' : ROXO), LUZ[face]));
    caixa(img, 0, 42, 3, 11, 3, (face, x, y, fw, fh) => {
      if (y < 5) return tom(ROXO, LUZ[face]);                                                                          // sleeves
      if (y >= fh - 1 || face === 'bottom') return hex('#e8e0c8');                                                     // claws
      return couro(face, x, y);
    });
    caixa(img, 12, 42, 3, 5, 3, couro);
    caixa(img, 24, 42, 3, 3, 8, (face, x, y) => face === 'top' ? tom(VERDE_E, 1.05) : couro(face, x, y));
    pintar(brilho, 52, 2, '#ffd23a'); pintar(brilho, 53, 2, '#ffd23a'); pintar(brilho, 52, 3, '#ffd23a');            // eyes
    return [img, brilho];
  }

  // ================================================================ LOBISOMEM (64x64)
  function lobisomem() {
    const img = imagem(64, 64), brilho = imagem(64, 64);
    const P = '#4a4038', PE = '#3a312a', PC = '#5e5248', PEITO = '#7a6a5a';
    const pelagem = pelo(P, PE, 4);
    caixa(img, 0, 0, 7, 6, 6, pelagem);
    grade(img, 6, 6, ['.......', '.......', 'BB...BB', '.RE.ER.', '.......', '.......'], { B: PE, R: '#ff3020', E: '#1a0a08' });
    caixa(img, 26, 0, 4, 3, 4, (face, x, y, fw, fh) => {
      if (face === 'front' && y === 0 && x > 0 && x < 3) return hex('#111111');                    // nose
      if (face !== 'top' && face !== 'back' && y === fh - 1 && x % 2 === 0) return hex('#f4efe2'); // fangs
      return tom(PC, LUZ[face]);
    });
    caixa(img, 42, 0, 2, 3, 1, (face) => face === 'front' ? hex('#a87a70') : tom(P, LUZ[face]));
    caixa(img, 0, 12, 10, 11, 6, (face, x, y) => face === 'front' && x > 1 && x < 8 && y > 1 ? tom((x + y) % 3 ? PEITO : PC, 1) : pelagem(face, x, y));
    caixa(img, 32, 12, 3, 15, 3, (face, x, y, fw, fh) => (y >= fh - 1 || face === 'bottom') ? hex('#d8d0c0') : pelagem(face, x, y));
    caixa(img, 44, 12, 4, 6, 4, pelagem);
    caixa(img, 0, 29, 3, 5, 3, pelagem);
    caixa(img, 12, 29, 3, 1, 4, (face, x) => face === 'front' ? hex('#d8d0c0') : tom(PE, LUZ[face]));
    caixa(img, 0, 40, 3, 3, 8, (face, x, y) => tom((x + y) % 3 === 0 ? PC : P, LUZ[face]));
    pintar(brilho, 7, 9, '#ff3020'); pintar(brilho, 11, 9, '#ff3020');
    return [img, brilho];
  }

  // ================================================================ MAPINGUARI (128x64)
  function mapinguari() {
    const img = imagem(128, 64), brilho = imagem(128, 64);
    const P = '#8a3a22', PE = '#6a2a18', PC = '#a84e30';
    const pelagem = (face, x, y) => tom((x * 5 + y * 2) % 7 === 0 ? PC : ((x + y * 3) % 6 === 0 ? PE : P), LUZ[face]);
    caixa(img, 0, 0, 16, 20, 10, pelagem);
    // The mouth in the belly.
    grade(img, 10, 10 + 8, [
      '................',
      '...LLLLLLLLLL...',
      '..LTDTDTDTDTDL..',
      '..LDDDDDDDDDDL..',
      '..LDDDDDDDDDDL..',
      '..LDTDTDTDTDTL..',
      '...LLLLLLLLLL...',
    ], { L: '#5a1a12', D: '#2a0a08', T: '#efe6cc' });
    caixa(img, 52, 0, 8, 8, 8, pelagem);
    grade(img, 60, 8, ['........', '..wwww..', '.wwRRww.', '.wRKKRw.', '.wwRRww.', '..wwww..', '........', '........'],
      { w: '#f2ede2', R: '#c8401a', K: '#120404' });
    caixa(img, 84, 0, 6, 26, 6, pelagem);
    caixa(img, 28, 30, 6, 3, 2, (face, x, y) => tom(y === 2 ? '#bfb69a' : '#e8e0c8', LUZ[face]));
    caixa(img, 0, 30, 7, 14, 7, (face, x, y, fw, fh) => y >= fh - 2 || face === 'bottom' ? tom('#3a1a10', LUZ[face]) : pelagem(face, x, y));
    copiar(img, brilho, 62, 9, 4, 4);
    return [img, brilho];
  }

  // ================================================================ ITEMS
  function olho() {
    const img = imagem(16, 16);
    for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
      const d = Math.hypot(x - 7.5, y - 7.5);
      if (d > 6.8) continue;
      pintar(img, x, y, d > 5.6 ? FOGO[1] : d > 4 ? FOGO[3] : FOGO[4]);
    }
    for (let y = 3; y < 13; y++) { pintar(img, 7, y, '#1a0800'); if (y > 4 && y < 11) pintar(img, 8, y, '#1a0800'); }
    pintar(img, 5, 5, '#ffffff'); pintar(img, 5, 6, '#ffffff');
    return img;
  }
  function freio() {
    const img = imagem(16, 16);
    grade(img, 0, 0, [
      '................',
      '....aaaaaaa.....',
      '...aabbbbbaa....',
      '..aa.......aa...',
      '..ab.......ba...',
      '..aa.......aa...',
      '...aabbbbbaa....',
      '....aabbbaa.....',
      '......b.b.......',
      '.....c...c......',
      '....cc...cc.....',
      '...c.......c....',
      '..c.........c...',
      '..cc.......cc...',
      '................',
      '................',
    ], { a: '#5a5a5a', b: '#c8c8c8', c: '#3a3a3a' });
    return img;
  }
  function escama() {
    const img = imagem(16, 16);
    // A rounded fish scale, pointed at the top, with a shine.
    for (let y = 1; y < 15; y++) for (let x = 1; x < 15; x++) {
      const dx = (x - 7.5) / 6.2, dy = (y - 9) / 5.8;
      const ponta = y < 9 ? Math.abs(x - 7.5) <= (y - 1) * 0.85 : true;
      if (dx * dx + dy * dy > 1 && y >= 9 || !ponta || (y < 9 && dx * dx > 1)) continue;
      const borda = dx * dx + dy * dy > 0.7 && y >= 9;
      pintar(img, x, y, borda ? '#1f6b60' : ((x + y) % 4 === 0 ? '#7fd3c4' : '#2a8a7a'));
    }
    pintar(img, 6, 6, '#e0fff8'); pintar(img, 6, 7, '#e0fff8'); pintar(img, 5, 8, '#e0fff8');
    return img;
  }
  function chapeuBloco() {
    const img = imagem(16, 16);
    for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) pintar(img, x, y, y === 15 ? '#2a2a2a' : ((x + y * 3) % 9 === 0 ? '#e4e0d6' : '#f4f2ec'));
    return img;
  }
  function garra() {
    const img = imagem(16, 16);
    // A big hooked claw with a fur-wrapped grip.
    grade(img, 0, 0, [
      '................',
      '........wwww....',
      '......wwwwWWw...',
      '.....wwwW..Ww...',
      '....wwwW....W...',
      '....wwW.........',
      '...wwwW.........',
      '...wwW..........',
      '...wwW..........',
      '..rRRr..........',
      '..rRRr..........',
      '.rRRr...........',
      '.rRr............',
      'rRr.............',
      'rr..............',
      '................',
    ], { w: '#e8e0c8', W: '#a89e82', r: '#6a2a18', R: '#a84e30' });
    return img;
  }

  return { boitata, mula, iara, boto, cuca, lobisomem, mapinguari, olho, freio, escama, chapeuBloco, garra };
};
