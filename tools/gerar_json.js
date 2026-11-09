// Writes the item definitions/models of the creature items and eggs, and merges the creature
// texts into both lang files. Usage: node tools/gerar_json.js
'use strict';
const fs = require('fs'), path = require('path');
const A = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'folclore');
const escrever = (rel, obj) => fs.writeFileSync(path.join(A, rel), JSON.stringify(obj, null, '\t') + '\n');

const planos = ['olho_boitata', 'freio_mula', 'escama_iara', 'garra_mapinguari', 'boitata_spawn_egg', 'mula_sem_cabeca_spawn_egg',
  'iara_spawn_egg', 'boto_spawn_egg', 'cuca_spawn_egg', 'lobisomem_spawn_egg', 'mapinguari_spawn_egg'];
for (const i of planos) {
  escrever('items/' + i + '.json', { model: { type: 'minecraft:model', model: 'folclore:item/' + i } });
  escrever('models/item/' + i + '.json', {
    parent: i === 'garra_mapinguari' ? 'minecraft:item/handheld' : 'minecraft:item/generated',
    textures: { layer0: 'folclore:item/' + i },
  });
}

// The Boto's hat: a 3D model, so it sits right on the head.
escrever('items/chapeu_boto.json', { model: { type: 'minecraft:model', model: 'folclore:item/chapeu_boto' } });
const f = uv => ({ uv, texture: '#c' });
escrever('models/item/chapeu_boto.json', {
  textures: { c: 'folclore:block/chapeu_boto', particle: 'folclore:block/chapeu_boto' },
  elements: [
    { name: 'aba', from: [3, 0, 3], to: [13, 1, 13], faces: { north: f([3, 14, 13, 15]), south: f([3, 14, 13, 15]),
      east: f([3, 14, 13, 15]), west: f([3, 14, 13, 15]), up: f([3, 3, 13, 13]), down: f([3, 3, 13, 13]) } },
    { name: 'copa', from: [5, 1, 5], to: [11, 4, 11], faces: { north: f([5, 13, 11, 16]), south: f([5, 13, 11, 16]),
      east: f([5, 13, 11, 16]), west: f([5, 13, 11, 16]), up: f([5, 5, 11, 11]) } },
  ],
  display: {
    gui: { rotation: [25, 135, 0], translation: [0, 3, 0], scale: [1.1, 1.1, 1.1] },
    ground: { translation: [0, 3, 0], scale: [0.6, 0.6, 0.6] },
    fixed: { translation: [0, 3, 0], scale: [1, 1, 1] },
    head: { rotation: [0, 180, 0], translation: [0, 14.5, 0], scale: [1.15, 1.15, 1.15] },
    thirdperson_righthand: { rotation: [75, 45, 0], translation: [0, 2.5, 1], scale: [0.6, 0.6, 0.6] },
    firstperson_righthand: { rotation: [0, 45, 0], translation: [0, 4, 0], scale: [0.7, 0.7, 0.7] },
    firstperson_lefthand: { rotation: [0, 225, 0], translation: [0, 4, 0], scale: [0.7, 0.7, 0.7] },
  },
});

const TEXTOS = {
  pt_br: {
    'entity.folclore.boitata': 'Boitatá', 'entity.folclore.mula_sem_cabeca': 'Mula sem Cabeça', 'entity.folclore.iara': 'Iara',
    'entity.folclore.boto': 'Boto', 'entity.folclore.cuca': 'Cuca', 'entity.folclore.lobisomem': 'Lobisomem', 'entity.folclore.mapinguari': 'Mapinguari',
    'item.folclore.olho_boitata': 'Olho de Boitatá', 'item.folclore.freio_mula': 'Freio da Mula', 'item.folclore.escama_iara': 'Escama da Iara',
    'item.folclore.chapeu_boto': 'Chapéu do Boto', 'item.folclore.garra_mapinguari': 'Garra do Mapinguari',
    'item.folclore.boitata_spawn_egg': 'Ovo Gerador de Boitatá', 'item.folclore.mula_sem_cabeca_spawn_egg': 'Ovo Gerador de Mula sem Cabeça',
    'item.folclore.iara_spawn_egg': 'Ovo Gerador de Iara', 'item.folclore.boto_spawn_egg': 'Ovo Gerador de Boto',
    'item.folclore.cuca_spawn_egg': 'Ovo Gerador de Cuca', 'item.folclore.lobisomem_spawn_egg': 'Ovo Gerador de Lobisomem',
    'item.folclore.mapinguari_spawn_egg': 'Ovo Gerador de Mapinguari',
    'folclore.boitata.chegou': 'Você pôs fogo perto da mata... dois olhos enormes acendem no escuro. É o Boitatá!',
    'folclore.boitata.visto': 'Uma luz de fogo serpenteia lá longe, no campo...',
    'folclore.mula.chegou': 'Um relincho de gelar o sangue e um galope de ferraduras... é a Mula sem Cabeça!',
    'folclore.mula.cansada': 'A Mula está cansada! Clique nela com a mão vazia para arrancar o freio.',
    'folclore.mula.desencantada': 'Você arrancou o freio de ferro: o encanto se quebrou e a Mula sumiu numa fumaça.',
    'folclore.mula.freio_usado': 'Com o freio da Mula, esse animal agora galopa feito assombração!',
    'folclore.iara.chegou': 'Uma voz doce vem do rio...',
    'folclore.iara.encanto': 'O canto da Iara te chama para a água... desvie o olhar!',
    'folclore.boto.presente': 'O moço de chapéu branco agradece o peixe e te dá um presente.',
    'folclore.boto.revelado': 'O chapéu caiu... e lá estava o furo do boto na cabeça! Ele correu de volta para o rio.',
    'folclore.boto.salvou': 'Um boto cor-de-rosa te empurrou para a superfície!',
    'folclore.cuca.chegou': 'Dorme, nenê, que a Cuca vem pegar... faz noites demais que você não dorme!',
    'folclore.cuca.sono': 'Um sono pesado cai sobre você...',
    'folclore.cuca.desistiu': 'Você dormiu... e a Cuca foi embora resmungando.',
    'folclore.lobisomem.chegou': 'Lua cheia... um uivo longo vem da encruzilhada.',
    'folclore.lobisomem.desencantado': 'O encanto quebrou! No lugar do Lobisomem estava %s, que não lembra de nada.',
    'folclore.mapinguari.chegou': 'Um fedor horrível toma conta da mata... e um rugido faz o chão tremer. Mapinguari!',
    'folclore.mapinguari.fedor': 'Que fedor! Você está ficando enjoado...',
    'folclore.mapinguari.couro': 'O couro do Mapinguari é duro demais! Acerte a boca na barriga dele.',
    'folclore.mapinguari.barriga': 'Bem na boca da barriga!',
  },
  en_us: {
    'entity.folclore.boitata': 'Boitatá', 'entity.folclore.mula_sem_cabeca': 'Headless Mule', 'entity.folclore.iara': 'Iara',
    'entity.folclore.boto': 'Boto', 'entity.folclore.cuca': 'Cuca', 'entity.folclore.lobisomem': 'Lobisomem', 'entity.folclore.mapinguari': 'Mapinguari',
    'item.folclore.olho_boitata': 'Boitatá Eye', 'item.folclore.freio_mula': "Mule's Iron Bridle", 'item.folclore.escama_iara': 'Iara Scale',
    'item.folclore.chapeu_boto': "Boto's Hat", 'item.folclore.garra_mapinguari': 'Mapinguari Claw',
    'item.folclore.boitata_spawn_egg': 'Boitatá Spawn Egg', 'item.folclore.mula_sem_cabeca_spawn_egg': 'Headless Mule Spawn Egg',
    'item.folclore.iara_spawn_egg': 'Iara Spawn Egg', 'item.folclore.boto_spawn_egg': 'Boto Spawn Egg',
    'item.folclore.cuca_spawn_egg': 'Cuca Spawn Egg', 'item.folclore.lobisomem_spawn_egg': 'Lobisomem Spawn Egg',
    'item.folclore.mapinguari_spawn_egg': 'Mapinguari Spawn Egg',
    'folclore.boitata.chegou': 'You set fire near the forest... two huge eyes light up in the dark. It is the Boitatá!',
    'folclore.boitata.visto': 'A light of fire snakes across the fields far away...',
    'folclore.mula.chegou': 'A bloodcurdling neigh and the gallop of iron shoes... the Headless Mule!',
    'folclore.mula.cansada': 'The Mule is worn out! Use an empty hand on her to pull off the bridle.',
    'folclore.mula.desencantada': 'You pulled off the iron bridle: the curse is broken and the Mule vanished in smoke.',
    'folclore.mula.freio_usado': "With the Mule's bridle, this animal now gallops like a ghost!",
    'folclore.iara.chegou': 'A sweet voice comes from the river...',
    'folclore.iara.encanto': "The Iara's song calls you into the water... look away!",
    'folclore.boto.presente': 'The young man in the white hat thanks you for the fish and gives you a present.',
    'folclore.boto.revelado': "The hat fell off... revealing a dolphin's blowhole! He ran back to the river.",
    'folclore.boto.salvou': 'A pink river dolphin pushed you up to the surface!',
    'folclore.cuca.chegou': 'Sleep, little one, or the Cuca will come... you have not slept for too many nights!',
    'folclore.cuca.sono': 'A heavy sleep falls upon you...',
    'folclore.cuca.desistiu': 'You went to sleep... and the Cuca left, grumbling.',
    'folclore.lobisomem.chegou': 'Full moon... a long howl comes from the crossroads.',
    'folclore.lobisomem.desencantado': 'The curse is broken! The Lobisomem was %s all along, and he remembers nothing.',
    'folclore.mapinguari.chegou': 'A horrible stench fills the jungle... and a roar shakes the ground. Mapinguari!',
    'folclore.mapinguari.fedor': 'What a stench! You are feeling sick...',
    'folclore.mapinguari.couro': "The Mapinguari's hide is too tough! Strike the mouth on its belly.",
    'folclore.mapinguari.barriga': 'Right in the belly mouth!',
  },
};
for (const [lingua, textos] of Object.entries(TEXTOS)) {
  const arq = path.join(A, 'lang', lingua + '.json');
  const j = JSON.parse(fs.readFileSync(arq, 'utf8'));
  Object.assign(j, textos);
  fs.writeFileSync(arq, JSON.stringify(j, null, '\t') + '\n');
}
console.log('ok');
