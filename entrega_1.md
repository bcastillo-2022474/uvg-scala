---
title: "Entrega 1: Hola Mundo"
nav_order: 2
---

# Entrega 1: Hola Mundo

## Ambiente de desarrollo

Scala requiere una JVM (JDK 17+) y **sbt** como build tool.

**macOS (Homebrew):**
```bash
brew install openjdk
brew install sbt
```

sbt descarga automáticamente la versión de Scala definida en `build.sbt` — no es necesario instalar Scala por separado.

## Programa

```scala
object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}
```

**Ejecutar:**
```bash
sbt run
```

**Salida:**
```
Hello, World!
```

## Notas de sintaxis

| Elemento | Significado |
|----------|-------------|
| `object` | Singleton — como una clase con una sola instancia (reemplaza `static` de Java) |
| `def main(...)` | Punto de entrada del programa |
| `args: Array[String]` | Argumentos de línea de comandos |
| `Unit` | Tipo de retorno vacío (equivale a `void` en Java) |
| `println(...)` | Imprime en consola con salto de línea |

## Entorno en línea

Si no se quiere instalar nada localmente, se puede usar [Scastie](https://scastie.scala-lang.org/) — el playground oficial de Scala, que corre directamente en el navegador.
