# Fase 2: Revisión inicial de Tipos de datos y Estructuras de datos

**Universidad del Valle de Guatemala**  
**CC2016 – Algoritmos y Estructura de Datos**  
**Lenguaje investigado: Scala 3**

---

## 1. Introducción

Scala corre sobre la JVM (Java Virtual Machine), lo que le permite interoperar directamente con código Java. Sin embargo, su sistema de tipos va considerablemente más allá del de Java: **todo es un objeto**, incluyendo los tipos que Java trata como primitivos (`int`, `boolean`, etc.). Esta decisión de diseño simplifica el modelo mental del lenguaje y habilita capacidades del paradigma funcional que en Java serían mucho más verbosas.

---

## 2. Jerarquía de tipos

En Scala existe una jerarquía de tipos unificada. A diferencia de Java, no hay separación entre tipos primitivos y tipos de referencia a nivel del lenguaje:

```
Any
├── AnyVal  (tipos de valor — compilados como primitivos JVM)
│   ├── Int, Long, Short, Byte
│   ├── Double, Float
│   ├── Char, Boolean
│   └── Unit  (equivalente a void)
└── AnyRef  (tipos de referencia — equivale a java.lang.Object)
    ├── String
    ├── List, Array, Map, ...
    └── (cualquier clase definida por el usuario)

Nothing  // subtipo de todo — representa ausencia de valor (ej: excepciones)
Null     // subtipo de AnyRef — solo existe por compatibilidad con Java
```

**`Any`** es la raíz de toda la jerarquía. Cualquier valor en Scala es instancia de `Any`.  
**`Nothing`** es útil en tipos de retorno cuando una función nunca regresa normalmente (lanza excepción o entra en loop infinito).

**Comparación con Java:** en Java, `int` y `Integer` son cosas distintas (boxing/unboxing explícito o implícito). En Scala, `Int` es siempre `Int` — el compilador decide si usar el primitivo JVM o el wrapper de forma transparente.

---

## 3. Tipos de datos primitivos (AnyVal)

| Tipo      | Tamaño   | Rango / descripción                  | Equivalente Java |
|-----------|----------|--------------------------------------|-----------------|
| `Int`     | 32 bits  | −2,147,483,648 a 2,147,483,647       | `int`           |
| `Long`    | 64 bits  | ±9.2 × 10¹⁸                          | `long`          |
| `Short`   | 16 bits  | −32,768 a 32,767                     | `short`         |
| `Byte`    | 8 bits   | −128 a 127                           | `byte`          |
| `Double`  | 64 bits  | punto flotante doble precisión       | `double`        |
| `Float`   | 32 bits  | punto flotante simple precisión      | `float`         |
| `Char`    | 16 bits  | caracter Unicode (U+0000 a U+FFFF)   | `char`          |
| `Boolean` | —        | `true` / `false`                     | `boolean`       |
| `Unit`    | —        | un solo valor `()`, sin información  | `void`          |

```scala
val entero: Int      = 42
val largo: Long      = 9_000_000_000L   // L al final = literal Long
val decimal: Double  = 3.14159
val flotante: Float  = 2.71f            // f al final = literal Float
val byte: Byte       = 127
val corto: Short     = 32_000
val caracter: Char   = 'S'
val bandera: Boolean = true
```

---

## 4. Tipado del lenguaje

### 4.1 Tipado fuerte (strongly typed)

Scala es un lenguaje **fuertemente tipado**: cada valor tiene un tipo definido en tiempo de compilación y no se realizan conversiones implícitas entre tipos incompatibles. Esto significa que errores de tipo se detectan **antes de ejecutar** el programa.

```scala
val x: Int = "hola"   // ERROR en compilación — String no es Int
val y: Int = 3.14     // ERROR en compilación — Double no es Int
```

En Java también hay tipado fuerte, pero Scala lo lleva más lejos porque no permite el widening automático entre numericales sin cast explícito en todos los contextos.

### 4.2 Inferencia de tipos

Aunque el tipado es fuerte, Scala tiene **inferencia de tipos**: el compilador puede deducir el tipo de una variable por su valor inicial, por lo que no siempre es necesario escribirlo:

```scala
val inferido = 100          // el compilador deduce: Int
val nombre   = "Scala"      // el compilador deduce: String
val lista    = List(1, 2)   // el compilador deduce: List[Int]
```

Esto **no** es tipado dinámico. El tipo sigue siendo verificado en compilación; simplemente no tienes que escribirlo explícitamente.

**Comparación con Python:** Python es dinámicamente tipado — el tipo de una variable puede cambiar en tiempo de ejecución. En Scala, una vez que el compilador infirió `Int`, no puedes asignar un `String` a esa variable.

### 4.3 `val` vs `var`

| Keyword | Mutabilidad | Equivalente en Java |
|---------|-------------|---------------------|
| `val`   | inmutable (no puede reasignarse) | `final` |
| `var`   | mutable (puede reasignarse) | variable normal |

```scala
val pi = 3.14159
// pi = 3.0   // ERROR — no se puede reasignar un val

var contador = 0
contador = contador + 1  // OK
```

La convención en Scala idiomático es preferir `val` siempre que sea posible, ya que facilita el razonamiento sobre el código (programación funcional).

---

## 5. Estructuras de datos principales

Scala tiene una librería de colecciones muy completa. La distinción más importante es **inmutable vs mutable**:

- `scala.collection.immutable` — importado por defecto. Las operaciones devuelven **nuevas** colecciones en lugar de modificar la original.
- `scala.collection.mutable` — requiere import explícito. Permite modificar la colección en su lugar.

### 5.1 List — lista enlazada inmutable

Lista ordenada de elementos del mismo tipo. Internamente es una lista enlazada singly-linked. Óptima para operaciones en la cabeza (`head`/`::`).

```scala
val numeros: List[Int] = List(10, 20, 30, 40, 50)

val conPrepend = 5 :: numeros          // agrega al frente — O(1)
val concatenada = numeros ::: List(60) // concatena dos listas

numeros.head            // 10
numeros.tail            // List(20, 30, 40, 50)
numeros.filter(_ > 25)  // List(30, 40, 50)
numeros.map(_ * 2)      // List(20, 40, 60, 80, 100)
```

**vs Java:** equivale a `List<Integer>` inmutable. En Java no existe una lista inmutable de primera clase en la librería estándar (sin `Collections.unmodifiableList`).  
**vs Python:** las listas de Python son mutables por defecto. `List` en Scala es inmutable — para mutar se usa `ListBuffer`.

### 5.2 Vector — secuencia indexada inmutable

Acceso por índice eficiente (`O(log₃₂ n)` ≈ constante en práctica). Reemplaza a `List` cuando se necesita acceso aleatorio frecuente.

```scala
val vec = Vector("a", "b", "c", "d")

vec(2)                    // "c"  — acceso por índice
vec.updated(1, "Z")       // Vector("a", "Z", "c", "d") — nueva copia
vec :+ "e"                // Vector("a", "b", "c", "d", "e") — append
```

### 5.3 Array — arreglo mutable (JVM array)

Equivalente directo al array de Java. Mutable, tamaño fijo, acceso `O(1)` por índice. Útil cuando se necesita interoperabilidad con Java o rendimiento máximo.

```scala
val arreglo = Array(1, 2, 3, 4, 5)
arreglo(0) = 99             // mutación directa — arreglo es ahora [99, 2, 3, 4, 5]
arreglo.length              // 5
arreglo.mkString(", ")      // "99, 2, 3, 4, 5"
```

**vs Java:** `int[]` — prácticamente idéntico a nivel JVM.

### 5.4 Set — conjunto sin duplicados

Colección que no permite elementos repetidos. El orden no está garantizado (a diferencia de `SortedSet`).

```scala
val conjunto = Set(1, 2, 3, 2, 1)   // Set(1, 2, 3) — duplicados eliminados

conjunto + 4          // Set(1, 2, 3, 4)  — nuevo Set
conjunto - 1          // Set(2, 3)        — nuevo Set
conjunto.contains(2)  // true
```

### 5.5 Map — tabla clave → valor

Almacena pares clave-valor. Inmutable por defecto. La sintaxis `->` crea una tupla `(clave, valor)`.

```scala
val edades: Map[String, Int] = Map("Ana" -> 20, "Luis" -> 25)

edades("Ana")          // 20  — lanza excepción si la clave no existe
edades.get("Pedro")    // None — seguro, devuelve Option
edades + ("Mia" -> 22) // nuevo Map con la entrada agregada
edades.keys            // Iterable con las claves
edades.values          // Iterable con los valores
```

**vs Java:** equivale a `Map<String, Integer>` de Java, pero inmutable y con acceso seguro vía `Option`.

### 5.6 Tuple — colección heterogénea

Agrupa un número fijo de valores que pueden tener tipos distintos. Útil para retornar múltiples valores de una función sin crear una clase.

```scala
val persona: (String, Int, Boolean) = ("João", 21, true)

persona._1   // "João"  — acceso por posición (base 1)
persona._2   // 21
persona._3   // true

// Desestructuración (más legible)
val (nombre, edad, activo) = persona
```

Scala soporta hasta `Tuple22`. En Scala 3 se puede usar `*:` para tuplas genéricas.

**vs Java:** Java no tiene tuplas nativas. Se usan `Pair<A,B>` de librerías externas o clases propias.  
**vs Python:** Python tiene `tuple` nativo con sintaxis muy similar `(a, b, c)`.

### 5.7 Option — alternativa segura a `null`

`Option[A]` es un contenedor que puede tener:
- `Some(valor)` — hay un valor presente
- `None` — no hay valor (equivalente semántico de `null`, pero type-safe)

Esto elimina la posibilidad de `NullPointerException` cuando se usa correctamente.

```scala
val capital = Map("Guatemala" -> "Ciudad de Guatemala", "México" -> "CDMX")

capital.get("Guatemala")   // Some("Ciudad de Guatemala")
capital.get("Honduras")    // None

// Patrón match — la forma idiomática de manejar Option
capital.get("Honduras") match {
  case Some(c) => println(s"Capital: $c")
  case None    => println("País no encontrado")
}

// O directamente con getOrElse
capital.get("Honduras").getOrElse("desconocida")   // "desconocida"
```

**vs Java:** Java tiene `Optional<T>` desde Java 8, con la misma idea. Scala lo usa de forma mucho más ubicua en toda su librería estándar.

---

## 6. Colecciones mutables

Cuando se necesita mutabilidad explícita, se importa `scala.collection.mutable`:

```scala
import scala.collection.mutable

val lista = mutable.ListBuffer(1, 2, 3)
lista += 4   // agrega
lista -= 1   // elimina
// lista: ListBuffer(2, 3, 4)

val mapa = mutable.Map("x" -> 10)
mapa("y") = 20
// mapa: HashMap(x -> 10, y -> 20)
```

La recomendación general en Scala es iniciar con colecciones inmutables y solo recurrir a las mutables cuando hay una razón de rendimiento comprobada.

---

## 7. Programa de ejemplo

El siguiente programa ejecutable (`src/main/scala/DataTypes.scala`) demuestra todos los conceptos anteriores en un solo archivo:

```bash
sbt "runMain DataTypes"
```

Salida:
```
=== Tipos primitivos (AnyVal) ===
Int:     42  (int)
Long:    9000000000  (long)
Double:  3.14159  (double)
...
=== List (inmutable) ===
numeros:    List(10, 20, 30, 40, 50)
filtrar pares: List(20, 40)
map x2:        List(20, 40, 60, 80, 100)
...
=== Option (evita NullPointerException) ===
  Guatemala → Ciudad de Guatemala
  Honduras → no encontrado
  México → CDMX
```

---

## 8. Comparación Scala vs Java vs Python

| Aspecto | Scala | Java | Python |
|---|---|---|---|
| Tipado | Fuerte + estático + inferido | Fuerte + estático (verbose) | Fuerte + dinámico |
| Primitivos | No existen en el lenguaje (`Int` = objeto) | Sí (`int`, `double`, etc.) | No existen (todo es objeto) |
| Inferencia de tipos | Sí (`val x = 5`) | Desde Java 10 (`var x = 5`) | Innecesaria (tipado dinámico) |
| Nulabilidad | `Option[T]` evita null | `null` + `Optional<T>` (Java 8+) | `None` + errores en runtime |
| Listas | Inmutables por defecto | Mutables por defecto | Mutables por defecto |
| Colecciones inmutables | Primera clase en stdlib | Wrappers (`Collections.unmodifiableX`) | `tuple`, `frozenset` (limitado) |
| Tuplas | `(A, B, C)` nativas | No nativas (librerías externas) | `(a, b, c)` nativas |
| Interop JVM | Nativa | — | No |

---

## 9. Recursos

- Documentación oficial de tipos: https://docs.scala-lang.org/tour/unified-types.html  
- Colecciones de Scala: https://docs.scala-lang.org/overviews/collections-2.13/introduction.html  
- Tour de Scala: https://docs.scala-lang.org/tour/tour-of-scala.html  
- Entorno en línea (Scastie): https://scastie.scala-lang.org/
