# Folclore Brasileiro: TODO

Mod de criaturas do folclore brasileiro (Fabric 26.3). Cada criatura nova = uma atualização = um vídeo.

## 0.1.0: Saci e Curupira (feito, falta teste em jogo pelo usuário)
- [x] **Saci**: chega num redemoinho (1 chance em 20 por minuto, de dia, a céu aberto), rouba um stack do inventário e foge com ele na mão.
  - Não leva dano (todo golpe faz ele sumir no redemoinho e rir). Só `/kill` mata.
  - Para de vez em quando para gargalhar: é a chance de pegar ele.
  - Pegadinhas: apaga tochas e trança a crina dos cavalos (lentidão).
  - **Peneira** (graveto + linha): mira no Saci a até 9 blocos e clica, e ele devolve o roubo e larga o gorro.
  - **Gorro do Saci**: +20% de velocidade, pulo maior, e nenhum Saci te rouba.
- [x] **Curupira**: pontos de desmatamento por jogador (tronco +1, animal selvagem +2, muda plantada −3, −1 a cada 20 s).
  - 24 pontos: assobio na mata, pegadas ao contrário perto de você.
  - 48 pontos: ele aparece e caça você (náusea, escuridão, assobio). Deixa pegadas viradas ao contrário.
  - Foge para a mata e volta curado 2 vezes. Se morrer: fraqueza, lentidão e azar.
  - **Oferenda de mel** (clicar com garrafa de mel): perdoa tudo, deixa 4 mudas e vai embora.
- [x] `/folclore saci|curupira|desmatamento [valor]` para testes e gravação.
- [x] **Saci na Garrafa**: pegar o Saci com a peneira tendo uma garrafa de vidro na mão secundária. 5 favores (redemoinho que traz todos os itens e XP soltos num raio de 16 blocos, espera de 20 s); no último ele estoura a rolha e foge.
- [x] Pontos de desmatamento salvos no jogador (attachment persistente, sobrevivem à morte).
- [x] `config/folclore.json`: saciAtivo, saciChance, curupiraAtivo, curupiraAviso, curupiraFuria.

## Próximos
- [ ] Teste em jogo pelo usuário (T)
- [ ] Sons próprios: gargalhada do Saci, assobio do Curupira (gravar ou gerar .ogg)
- [ ] Modelos em Blockbench (.bbmodel) para o usuário retocar
- [ ] Publicação: GitHub, Modrinth, CurseForge (como no Arraial: tools/publicar.js)

## 0.1.0 (cont.): todas as criaturas
- [x] **Boitatá**: cobra de fogo que rasteja pelo chão (noite, céu aberto). Apaga todo fogo num raio de 8 blocos. Quem acende fogo (isqueiro, carga de fogo) perto de troncos/folhas chama o Boitatá, que persegue e queima. Larga o **Olho de Boitatá** (visão noturna 3 min + resistência a fogo 1 min).
- [x] **Mula sem Cabeça**: noite, céu aberto; dobro de chance na noite de quinta para sexta. Galopa, atropela e põe fogo. Abaixo de 50% de vida fica **cansada**: mão vazia nela arranca o **Freio da Mula** (quebra o encanto). O freio deixa um cavalo domado 30% mais rápido.
- [x] **Iara**: noite, perto d'água. Canta; quem **olha** para ela é puxado para a água (náusea); perto dela na água, é puxado para o fundo. Desviar o olhar quebra o encanto. Ferida, mergulha e foge. Larga **Escama da Iara** (respirar e nadar 3 min).
- [x] **Boto**: golfinho rosa que salva quem está se afogando (respiração aquática + empurrão). À noite vira o moço de chapéu branco que dança na margem; dá presente em troca de peixe. Bater nele derruba o **Chapéu do Boto** (nado rápido + fôlego) e ele volta para o rio.
- [x] **Cuca**: vem à noite para quem não dorme há 3 noites. Joga poções, lança "sono pesado" (escuridão, lentidão, cansaço). Se o jogador dormir, ela desiste. Larga 2 poções boas.
- [x] **Lobisomem**: noites de lua cheia. Rápido, salta, caça jogadores e animais. Com menos de 25% de vida (ou ao amanhecer) o encanto quebra e vira um aldeão com nome.
- [x] **Mapinguari**: selva, de dia. Gigante, fedor (náusea + veneno). Só leva 20% do dano, a não ser que acerte de frente (a boca na barriga: 150%). Larga **Garra do Mapinguari** (arma) e couro.
- [x] Chances de cada criatura em `config/folclore.json` (0 desliga), e `/folclore boitata|mula|iara|boto|cuca|lobisomem|mapinguari`. Chamadas por comando/ovo não somem com o dia (bom para gravar).

## Ideias para depois
- [ ] Sons próprios (gargalhada, assobio, canto da Iara, uivo)
- [ ] Mais lendas: Caipora (prima do Curupira), Negrinho do Pastoreio, Corpo-Seco, Pisadeira, Bicho-Papão
