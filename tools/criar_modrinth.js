// One-time setup of the Modrinth project: creates it as a draft with icon and description, then
// uploads the gallery. Prints the project id (put it in tools/publicar.js).
//
// Usage: node tools/criar_modrinth.js [--teste]            create the draft project
//        node tools/criar_modrinth.js galeria <project id>  upload docs/imagens/galeria.json
//        node tools/criar_modrinth.js enviar <project id>   submit for review (after a version exists)
'use strict';
const fs = require('fs'), path = require('path'), os = require('os');

const raiz = path.join(__dirname, '..');
const AGENTE = 'Dants0/folclore-brasileiro-publicacao';
const API = 'https://api.modrinth.com/v2';
const token = () => fs.readFileSync(path.join(os.homedir(), '.modrinth_token.txt'), 'utf8').replace(/^﻿/, '').trim();
const [, , acao, id] = process.argv;

async function criar(teste) {
  const data = {
    slug: 'folclore-brasileiro',
    title: 'Folclore Brasileiro',
    description: 'Nine creatures from Brazilian folklore, each with the rules of its legend: the Saci steals your things, the Curupira guards the forest, the Iara sings in the rivers... / Criaturas do folclore brasileiro no Minecraft.',
    body: fs.readFileSync(path.join(raiz, 'docs', 'publicacao', 'descricao.md'), 'utf8'),
    project_type: 'mod',
    categories: ['mobs', 'adventure', 'game-mechanics'],
    additional_categories: [],
    client_side: 'required',
    server_side: 'required',
    license_id: 'LicenseRef-Folclore',
    license_url: 'https://github.com/Dants0/folclore-brasileiro/blob/main/LICENSE',
    source_url: 'https://github.com/Dants0/folclore-brasileiro',
    issues_url: 'https://github.com/Dants0/folclore-brasileiro/issues',
    donation_urls: [],
    initial_versions: [],
    is_draft: true,
  };
  if (teste) return console.log({ ...data, body: data.body.length + ' caracteres' });
  const form = new FormData();
  form.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
  form.append('icon', new Blob([fs.readFileSync(path.join(raiz, 'docs', 'imagens', 'icone_512.png'))], { type: 'image/png' }), 'icon.png');
  const r = await fetch(API + '/project', { method: 'POST', headers: { Authorization: token(), 'User-Agent': AGENTE }, body: form });
  const j = await r.json();
  if (!r.ok) throw new Error(r.status + ' ' + JSON.stringify(j));
  console.log('projeto criado: id ' + j.id + ' slug ' + j.slug);
}

async function galeria() {
  const lista = JSON.parse(fs.readFileSync(path.join(raiz, 'docs', 'imagens', 'galeria.json'), 'utf8'));
  for (const g of lista) {
    const q = new URLSearchParams({ ext: 'png', featured: String(!!g.destaque), title: g.titulo, description: g.descricao });
    const r = await fetch(`${API}/project/${id}/gallery?${q}`, {
      method: 'POST', headers: { Authorization: token(), 'User-Agent': AGENTE, 'Content-Type': 'image/png' },
      body: fs.readFileSync(path.join(raiz, 'docs', 'imagens', g.arquivo)),
    });
    console.log(g.arquivo, r.status === 204 ? 'ok' : 'falhou ' + r.status + ' ' + await r.text());
  }
}

async function enviar() {
  const r = await fetch(`${API}/project/${id}`, {
    method: 'PATCH', headers: { Authorization: token(), 'User-Agent': AGENTE, 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: 'processing' }),
  });
  console.log('enviado para revisão:', r.status === 204 ? 'ok' : r.status + ' ' + await r.text());
}

(async () => {
  if (acao === 'galeria') return galeria();
  if (acao === 'enviar') return enviar();
  return criar(acao === '--teste');
})().catch(e => { console.error(e.message); process.exit(1); });
