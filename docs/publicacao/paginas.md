# Kit de publicação: páginas do mod

## Dados do projeto
| Campo | Valor |
|---|---|
| Nome | Folclore Brasileiro |
| IDs | Modrinth `o9qYlBCN` · CurseForge (a criar) |
| Slug / URL | `folclore-brasileiro` |
| Resumo (até 256 caracteres) | Nine creatures from Brazilian folklore, each with the rules of its legend: the Saci steals your things, the Curupira guards the forest, the Iara sings in the rivers... / Criaturas do folclore brasileiro no Minecraft. |
| Loader | Fabric |
| Versão do jogo | 26.3 |
| Ambiente | Cliente: obrigatório · Servidor: obrigatório |
| Dependências | Fabric API (obrigatória) |
| Licença | Personalizada: `LicenseRef-Folclore`, URL `https://github.com/Dants0/folclore-brasileiro/blob/main/LICENSE` |
| Código-fonte | https://github.com/Dants0/folclore-brasileiro |
| Issues | https://github.com/Dants0/folclore-brasileiro/issues |
| Categorias (Modrinth) | `mobs`, `adventure`, `game-mechanics` |
| Categorias (CurseForge) | Principal: **Mobs** · outras: **Adventure and RPG** |
| Modpacks (CurseForge) | permitir distribuição em modpacks: **sim** (a licença permite) |
| Ícone | `docs/imagens/icone_512.png` |
| Galeria | `docs/imagens/*.png` (títulos e descrições em `docs/imagens/galeria.json`) |

## Descrição
O texto completo (Markdown, inglês + português) está em `docs/publicacao/descricao.md`. O CurseForge aceita o mesmo Markdown: escolha o editor "Markdown" na hora de colar.

## Nova versão
1. Suba `version` em `gradle.properties` e escreva `docs/publicacao/changelog-<versão>.md`.
2. `./gradlew build`
3. `node tools/publicar.js todos` (ou `modrinth` / `curseforge`; `--teste` mostra sem enviar).
Não envie o jar à mão também: o CurseForge recusa arquivo duplicado.
