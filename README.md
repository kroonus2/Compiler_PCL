# Emulador de CLP (Controlador Lógico Programável)

## Descrição do Projeto

O projeto consiste em desenvolver um emulador de Controlador Lógico Programável (CLP) que utiliza um sistema de leitura de entradas e escrita de saídas por meio de um módulo de interface ModBus. O emulador deve ser capaz de operar em modos de programação, parado e operação, semelhantes a um CLP convencional. O programa emulador deve suportar funções lógicas básicas (NOT, OR, AND), memórias booleanas locais, temporizadores (ON DELAY e OFF DELAY) e permitir a criação de programas lógicos através de uma linguagem de programação baseada em lista de instruções.

## Instalação

Para instalar as bibliotecas necessárias, execute o seguinte comando no terminal:

```python
pip install -U minimalmodbus PySimplegui
```
```java
JDK21 - https://www.oracle.com/br/java/technologies/downloads/#jdk21-windows
```
## Uso

Para executar o projeto, execute o seguinte comando no terminal:

```python
python simpleGUI.py
```


## Funcionalidades

- **Funções Lógicas Básicas:**
  - NÃO (NOT)
  - OU (OR)
  - E (AND)

- **Memórias:**
  - No mínimo 32 memórias booleanas locais

- **Temporizadores:**
  - No mínimo 32 temporizadores
  - ON DELAY (retardo na ativação)
  - OFF DELAY (retardo na desativação)

- **Modos de Operação:**
  - Modo Programação (PROGRAM): Permite a alteração do programa lógico. Não há leitura ou escrita nas saídas físicas.
  - Modo Parado (STOP): O sistema interrompe a execução do programa lógico.
  - Modo Operação (RUN): O sistema executa o programa lógico criado pelo usuário.

- **Ciclo de Varredura do Sistema:**
  1. Inicializar o sistema
  2. Ler as entradas e armazenar em uma memória imagem
  3. Processar o programa do usuário e salvar as alterações da saída na memória imagem de saída
  4. Atualizar as saídas a partir da memória imagem de saída
  5. Retornar ao passo 2

## Modo de Programação

O emulador permite que o programa lógico seja alterado. Neste modo, não há leitura ou escrita nas saídas físicas.

## Modo Parado

O sistema interrompe a execução do programa lógico, permitindo a análise de variáveis e condições.

## Modo de Operação

O sistema executa o programa lógico criado pelo usuário, realizando o ciclo de varredura do sistema.

## Licença

Este projeto é licenciado sob a [Licença MIT](LICENSE).
