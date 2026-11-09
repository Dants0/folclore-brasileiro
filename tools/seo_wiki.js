// SEO pass on docs/wiki: rich <head> (title, description, canonical, hreflang, Open Graph,
// Twitter, JSON-LD for the mod and the FAQ), image alt/size/lazy, robots.txt and sitemap.xml.
// Idempotent: run it again after editing the wiki. Usage: node tools/seo_wiki.js
'use strict';
const fs = require('fs'), path = require('path');
const dir = path.join(__dirname, '..', 'docs', 'wiki');
const arq = path.join(dir, 'index.html');
const URL_SITE = 'https://folclorebr.dantas-dev.com/';
const hoje = new Date().toISOString().slice(0, 10);
let html = fs.readFileSync(arq, 'utf8');

// ---------------------------------------------------------------- <head>
const titulo = 'Folclore Brasileiro: mod de Minecraft com Saci, Curupira, Boitatá, Iara e mais | Wiki oficial';
const descricao = 'Wiki oficial do mod Folclore Brasileiro para Minecraft (Fabric 26.3): Saci, Curupira, Boitatá, Mula sem Cabeça, Iara, Boto, Cuca, Lobisomem e Mapinguari. Como encontrar, como vencer, itens, comandos e configuração.';
const descricaoEn = 'Official wiki of the Brazilian Folklore Minecraft mod (Fabric 26.3): Saci, Curupira, Boitatá, Headless Mule, Iara, Boto, Cuca, Lobisomem and Mapinguari. How to find them, how to beat them, items, commands and config.';

const faq = [
  ['O Saci roubou meu item e sumiu. Perdi?', 'Não. Enquanto o Saci estiver com o seu item, ele não some do mundo: fica fugindo por perto. Faça uma peneira (gravetos em cruz com linha no meio), mire nele e clique com o botão direito quando ele parar para rir.'],
  ['Como matar o Saci no Minecraft?', 'Nenhuma arma acerta o Saci: todo golpe só faz ele sumir no redemoinho. A única forma de pegá-lo é a Peneira. Com uma garrafa de vidro na mão secundária, ele fica preso na garrafa e passa a te dever favores.'],
  ['Como acalmar o Curupira?', 'Clique nele com uma garrafa de mel. Ele perdoa todo o desmatamento, deixa 4 mudas e vai embora. Plantar mudas também diminui seus pontos de desmatamento.'],
  ['Como quebrar o encanto da Mula sem Cabeça?', 'Deixe a Mula com menos da metade da vida até ela ficar cansada e clique nela com a mão vazia para arrancar o freio de ferro. O freio deixa um cavalo domado 30% mais rápido.'],
  ['Qual versão do Minecraft o mod Folclore Brasileiro suporta?', 'Minecraft 26.3 com Fabric Loader e Fabric API. O mod precisa estar no cliente e no servidor e funciona em mundos já existentes.'],
  ['Posso usar o mod em servidor, modpack ou vídeo?', 'Pode, desde que o mod seja baixado das páginas oficiais (Modrinth ou CurseForge). Vídeos e lives, inclusive monetizados, estão liberados.'],
];
const jsonld = [
  {
    '@context': 'https://schema.org',
    '@type': 'SoftwareApplication',
    name: 'Folclore Brasileiro',
    alternateName: ['Brazilian Folklore', 'Folclore Brasileiro mod'],
    description: descricao,
    url: URL_SITE,
    image: URL_SITE + 'img/og.jpg',
    applicationCategory: 'GameApplication',
    applicationSubCategory: 'Minecraft mod',
    operatingSystem: 'Windows, macOS, Linux (Minecraft Java Edition 26.3 + Fabric)',
    softwareVersion: '1.0.0-beta.1',
    inLanguage: ['pt-BR', 'en'],
    isAccessibleForFree: true,
    offers: { '@type': 'Offer', price: '0', priceCurrency: 'BRL' },
    author: { '@type': 'Person', name: 'Guilherme Goes', url: 'https://dantas-dev.com' },
    downloadUrl: 'https://modrinth.com/mod/folclore-brasileiro',
    sameAs: ['https://modrinth.com/mod/folclore-brasileiro', 'https://www.curseforge.com/projects/1794254', 'https://github.com/Dants0/folclore-brasileiro'],
    keywords: 'minecraft, mod, folclore brasileiro, saci, curupira, boitatá, mula sem cabeça, iara, boto, cuca, lobisomem, mapinguari, fabric',
  },
  {
    '@context': 'https://schema.org',
    '@type': 'FAQPage',
    mainEntity: faq.map(([q, a]) => ({ '@type': 'Question', name: q, acceptedAnswer: { '@type': 'Answer', text: a } })),
  },
];

const head = `<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${titulo}</title>
<meta name="description" content="${descricao}">
<meta name="keywords" content="mod folclore brasileiro minecraft, saci minecraft, curupira minecraft, boitatá, mula sem cabeça, iara, boto, cuca, lobisomem, mapinguari, mod brasileiro minecraft, fabric 26.3, brazilian folklore minecraft mod">
<meta name="author" content="Guilherme Goes">
<meta name="robots" content="index, follow, max-image-preview:large">
<meta name="theme-color" content="#10170f">
<link rel="canonical" href="${URL_SITE}">
<link rel="alternate" hreflang="pt-BR" href="${URL_SITE}">
<link rel="alternate" hreflang="en" href="${URL_SITE}">
<link rel="alternate" hreflang="x-default" href="${URL_SITE}">
<link rel="icon" type="image/png" sizes="128x128" href="img/icone.png">
<link rel="icon" type="image/png" sizes="512x512" href="img/icone_512.png">
<link rel="apple-touch-icon" href="img/icone_512.png">
<link rel="sitemap" type="application/xml" href="/sitemap.xml">

<meta property="og:type" content="website">
<meta property="og:site_name" content="Folclore Brasileiro">
<meta property="og:url" content="${URL_SITE}">
<meta property="og:title" content="Folclore Brasileiro: Saci, Curupira, Boitatá e mais no Minecraft">
<meta property="og:description" content="Nove criaturas do folclore brasileiro no Minecraft, cada uma com as regras da sua lenda. / ${descricaoEn}">
<meta property="og:image" content="${URL_SITE}img/og.jpg">
<meta property="og:image:width" content="1200">
<meta property="og:image:height" content="630">
<meta property="og:image:alt" content="Mapinguari, o gigante de um olho só, no mod Folclore Brasileiro">
<meta property="og:locale" content="pt_BR">
<meta property="og:locale:alternate" content="en_US">
<meta name="twitter:card" content="summary_large_image">
<meta name="twitter:title" content="Folclore Brasileiro: mod de Minecraft">
<meta name="twitter:description" content="Saci, Curupira, Boitatá, Mula sem Cabeça, Iara, Boto, Cuca, Lobisomem e Mapinguari no Minecraft.">
<meta name="twitter:image" content="${URL_SITE}img/og.jpg">

<script type="application/ld+json">
${JSON.stringify(jsonld, null, 1)}
</script>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&family=Pixelify+Sans:wght@500;700&display=swap">
<style>`;
html = html.replace(/<head>[\s\S]*?<style>/, head);

// ---------------------------------------------------------------- images: alt text, size, lazy
const ALT = {
  'saci.jpg': 'Saci no Minecraft: menino de uma perna só, gorro vermelho e cachimbo, com um diamante roubado na mão',
  'curupira.jpg': 'Curupira no Minecraft: guardião da mata com cabelo de fogo e pés virados para trás',
  'curupira_noite.jpg': 'Curupira com o cabelo de fogo brilhando à noite no Minecraft',
  'mapinguari.jpg': 'Mapinguari no Minecraft: gigante de pelo vermelho, um olho só e boca na barriga',
  'boitata.jpg': 'Boitatá, a cobra de fogo, voando sobre o campo no Minecraft',
  'boitata_noite.jpg': 'Boitatá brilhando no escuro à noite no Minecraft',
  'mula_sem_cabeca.jpg': 'Mula sem Cabeça com fogo saindo do pescoço no Minecraft',
  'iara.jpg': 'Iara, a mãe-d\'água com cauda de peixe, no Minecraft',
  'boto.jpg': 'Boto cor-de-rosa no Minecraft',
  'boto_homem.jpg': 'Boto transformado no moço de terno e chapéu branco no Minecraft',
  'cuca.jpg': 'Cuca, a bruxa com cabeça de jacaré, no Minecraft',
  'cuca_noite.jpg': 'Cuca com os olhos brilhando à noite no Minecraft',
  'lobisomem.jpg': 'Lobisomem em noite de lua cheia no Minecraft',
};
let primeira = true;
html = html.replace(/<img ([^>]*?)src="img\/([a-z_]+\.jpg)"([^>]*?)>/g, (m, antes, arq, depois) => {
  let attrs = (antes + depois).replace(/\s*alt="[^"]*"/, '').replace(/\s*(width|height|loading|decoding|fetchpriority)="[^"]*"/g, '');
  const lazy = primeira ? ' fetchpriority="high"' : ' loading="lazy"';
  primeira = false;
  return `<img ${attrs.trim()} src="img/${arq}" alt="${ALT[arq] || ''}" width="854" height="480" decoding="async"${lazy}>`.replace(/<img\s+src/, '<img src').replace(/\s{2,}/g, ' ');
});
// Pixel icons below the fold load lazily too.
html = html.replace(/<img class="pixel" src="img\/itens\/([a-z_]+)\.png" alt="">/g,
  (m, n) => `<img class="pixel" src="img/itens/${n}.png" alt="" width="16" height="16" loading="lazy">`);

// The FAQ section gets the questions people search for (kept in sync with the JSON-LD).
fs.writeFileSync(arq, html);

// ---------------------------------------------------------------- robots + sitemap
fs.writeFileSync(path.join(dir, 'robots.txt'), `User-agent: *\nAllow: /\n\nSitemap: ${URL_SITE}sitemap.xml\n`);
fs.writeFileSync(path.join(dir, 'sitemap.xml'), `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:image="http://www.google.com/schemas/sitemap-image/1.1" xmlns:xhtml="http://www.w3.org/1999/xhtml">
  <url>
    <loc>${URL_SITE}</loc>
    <lastmod>${hoje}</lastmod>
    <changefreq>weekly</changefreq>
    <priority>1.0</priority>
    <xhtml:link rel="alternate" hreflang="pt-BR" href="${URL_SITE}"/>
    <xhtml:link rel="alternate" hreflang="en" href="${URL_SITE}"/>
${Object.entries(ALT).map(([a, t]) => `    <image:image><image:loc>${URL_SITE}img/${a}</image:loc><image:title>${t.replace(/&/g, '&amp;').replace(/'/g, '&apos;')}</image:title></image:image>`).join('\n')}
  </url>
</urlset>
`);
console.log('seo ok');
