# Build Up Game

Jogo de gerenciamento de inventário e batalhas, desenvolvido em Java. O objetivo é montar uma mochila e derrotar três inimigos aleatórios.

## Interface gráfica

A versão principal utiliza **Java Swing**. A janela mantém a experiência guiada por texto, com uma área de registro da aventura e controles diretos para:

- Adicionar armas físicas/mágicas, artefatos e poções.
- Consultar a mochila e atualizar nome, dano, bônus ou cura dos itens selecionados.
- Remover itens da mochila.
- Usar poções, iniciar batalhas e avançar pelas três etapas.
- Consultar o ranking das dez melhores campanhas.

A mochila mantém o limite de cinco espaços. Artefatos não ocupam espaço e podem ser associados a armas. O jogo continua usando as regras de dano e as categorias aleatórias dos inimigos existentes.

## Persistência e pontuação

O ranking é salvo automaticamente em `data/ranking.csv`, sem dependências externas. Cada campanha registra jogador, pontuação, resultado, inimigos derrotados, vida restante, data e a relação de armas, artefatos e poções utilizados/guardados.

A pontuação é calculada por:

- 1.000 pontos por inimigo derrotado;
- 10 pontos por ponto de vida restante;
- 25 pontos por moeda restante.

O arquivo é criado na primeira campanha concluída (vitória ou derrota), e somente as dez melhores campanhas são mantidas. O CSV usa campos textuais codificados em Base64 URL-safe para preservar acentos, vírgulas e caracteres especiais.

## Como executar

Requisitos: JDK 8 ou superior.

Na raiz do projeto:

```bash
javac -encoding UTF-8 -d out Main.java GameGUI.java src/*.java
java -cp out Main
```

O arquivo `ranking.csv` é criado dentro da pasta `data` no diretório de execução.
