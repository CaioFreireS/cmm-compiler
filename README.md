# C-- Compiler

Analisador Léxico com IDE integrada para a linguagem C--.

**Trabalho de Compiladores**  
**Prof. Rodrigo Freitas Silva**  
**Ciência da Computação**

## Descrição

Compilador educacional implementado em Java que realiza análise léxica de um subconjunto da linguagem C denominado C--. O projeto inclui uma interface gráfica (IDE) para desenvolvimento e compilação de programas.

## Funcionalidades

- Análise Léxica completa com reconhecimento de tokens
- Editor de código com syntax highlighting
- Tabela dinâmica de tokens
- Detecção de erros léxicos em tempo real
- Remoção automática de comentários (`//` e `/* */`)
- Indicação precisa de linha e coluna para erros

## Requisitos

- Java 25+
- Maven 3.8+

## Dependências

```xml
<!-- Syntax Highlighting e Editor -->
<dependency>
    <groupId>com.fifesoft</groupId>
    <artifactId>rsyntaxtextarea</artifactId>
    <version>3.4.0</version>
</dependency>

<!-- Testes -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

## Como Executar

### Opção 1: Maven (Recomendado)
```bash
cd cmm-compiler
mvn clean compile
mvn exec:java
```

### Opção 2: NetBeans
1. Abra o projeto em NetBeans
2. Clique em "Run" (F6)

### Opção 3: IDE (Jar)
```bash
mvn clean package
java -jar target/cmm-compiler-1.0-SNAPSHOT.jar
```

## Estrutura do Projeto

```
cmm-compiler/
├── src/
│   ├── main/java/com/mycompany/cmm/compiler/
│   │   ├── CmmCompiler.java                    # Ponto de entrada
│   │   ├── lexer/
│   │   │   ├── Lexer.java                     # Analisador Léxico
│   │   │   └── LexicalError.java              # Exceções léxicas
│   │   ├── model/
│   │   │   ├── Token.java                     # Representação de token
│   │   │   ├── TokenType.java                 # Tipos de tokens
│   │   │   ├── SymbolTable.java               # Tabela de símbolos
│   │   │   ├── SymbolInfo.java                # Info de símbolo
│   │   │   └── SemanticAnalyzer.java          # Análise semântica
│   │   └── view/
│   │       ├── CMMCompilerFrame.java          # Interface Gráfica
│   │       ├── CMMCompilerFrame.form          # Definição visual
│   │       └── LexerParser.java               # Parser IDE
│   └── test/java/                             # Testes unitários
├── pom.xml                                     # Configuração Maven
├── nbactions.xml                               # Configuração NetBeans
├── README.md
├── STATUS_PROJETO.md                           # Análise completa
└── arq/testes/                                 # Exemplos de teste
```

## Linguagem C--

Subconjunto simplificado de C com suporte a:

- **Tipos**: `void`, `char`, `int`, `float`, `double`, `short`, `long`, `signed`, `unsigned`
- **Especificadores**: `auto`, `static`, `extern`, `const`
- **Estruturas de Controle**: `if`, `else`, `while`, `for`, `break`, `return`
- **Funções de I/O**: `printf()`, `scanf()`
- **Comentários**: `//` e `/* */`

## Exemplo

```c
int quadrado(int x) {
    return x * x;
}

int main() {
    int a = 10;
    
    if (a > 5) {
        printf(a);
    }
    
    return 0;
}
```

## Usando a IDE

1. **Abrir arquivo**: Menu `Arquivo` → `Abrir`
2. **Salvar arquivo**: Menu `Arquivo` → `Salvar`
3. **Compilar**: Menu `Compilação` → `Compilar Agora`
4. **Auto-compilar**: Menu `Compilação` → `Auto-compilar` (análise em tempo real)

Os erros léxicos aparecem sublinhados em vermelho com indicação de linha e coluna.

## Gramática

A gramática completa da linguagem C-- encontra-se em `STATUS_PROJETO.md`.

---

**Última atualização**: Janeiro 2026
