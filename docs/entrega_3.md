# Entrega 3: Programa de Ejemplo

**Universidad del Valle de Guatemala**  
**CC2016 – Algoritmos y Estructura de Datos**  
**Lenguaje investigado: Scala 3**

---

## 1. Objetivo

Demostrar el paradigma **funcional** de Scala a través de una aplicación real dividida en múltiples archivos. El programa implementa un sistema de seguimiento de calificaciones estudiantiles y explora los siguientes conceptos:

- **Case classes y sealed traits** (tipos de datos algebraicos)
- **Pattern matching** como expresión
- **Funciones de orden superior** (`map`, `filter`, `flatMap`, `groupBy`)
- **for-comprehensions**
- **Recursión** con acumulador
- **`Option[T]`** para manejar ausencia de valor sin `null`
- **Inmutabilidad** como estilo de programación

---

## 2. Estructura del proyecto

```
src/main/scala/
├── models.scala        ← tipos de datos (case classes + ADTs)
├── GradeService.scala  ← lógica pura: funciones sin efectos secundarios
└── Main.scala          ← punto de entrada, renderiza resultados
```

Esta separación sigue el principio de **arquitectura funcional**: el núcleo del programa es puro (sin I/O ni mutación), y los efectos (imprimir en pantalla) quedan confinados al borde exterior (`Main`).

---

## 3. `models.scala` — El modelo de datos

```scala
case class Student(id: String, name: String, grades: Map[String, Double])

case class Course(code: String, title: String, credits: Int)

sealed trait LetterGrade
case object A  extends LetterGrade
case object B  extends LetterGrade
case object C  extends LetterGrade
case object D  extends LetterGrade
case object F  extends LetterGrade

sealed trait Standing
case object Honors      extends Standing
case object Good        extends Standing
case object Probation   extends Standing
case object Incomplete  extends Standing

case class StudentReport(
  student: Student, gpa: Double, letter: LetterGrade,
  standing: Standing, highest: Option[(String, Double)],
  lowest: Option[(String, Double)]
)
```

### Case classes

Una `case class` es una clase inmutable cuyo compilador genera automáticamente:

- `equals` y `hashCode` basados en el contenido (no en la referencia)
- `toString` legible
- Método `copy` para producir versiones modificadas sin mutar el original

```scala
val a = Student("22-001", "Ana", Map("CC2016" -> 85.0))
val b = a.copy(name = "Ana López")  // nuevo objeto, a no cambia
```

### Sealed traits como ADTs

Un **ADT (Algebraic Data Type)** es un tipo cuyos subtipos son cerrados y conocidos en tiempo de compilación. `sealed` garantiza que solo los casos definidos en el mismo archivo existen. El compilador puede entonces advertir si un `match` no es exhaustivo:

```scala
sealed trait LetterGrade
case object A extends LetterGrade
// ...

def show(l: LetterGrade): String = l match
  case A => "A"
  case B => "B"
  // Si olvidamos C, D, F → el compilador advierte "match may not be exhaustive"
```

**Comparación con Java:** Java 17+ tiene `sealed classes` con la misma idea. Antes de Java 17, modelar ADTs requería enums o jerarquías de clases con boilerplate considerable.

---

## 4. `GradeService.scala` — La lógica pura

### 4.1 Funciones básicas

```scala
def gpa(grades: Map[String, Double]): Double =
  if grades.isEmpty then 0.0
  else grades.values.sum / grades.size

def letterGrade(score: Double): LetterGrade = score match
  case s if s >= 90 => A
  case s if s >= 80 => B
  case s if s >= 70 => C
  case s if s >= 60 => D
  case _            => F
```

Las **guards** (`if s >= 90`) en los cases permiten condiciones arbitrarias dentro del pattern match, haciendo innecesaria la clásica cadena de `if / else if`.

### 4.2 Option en lugar de null

```scala
def highestGrade(grades: Map[String, Double]): Option[(String, Double)] =
  if grades.isEmpty then None
  else Some(grades.maxBy(_._2))
```

`Option[T]` obliga al llamador a manejar ambos casos (`Some` / `None`), eliminando la posibilidad de un `NullPointerException` silencioso. El compilador verifica que no se use el valor sin antes desempacarlo.

### 4.3 Funciones de orden superior

```scala
def generateReports(students: List[Student]): List[StudentReport] =
  students.map { s =>
    val avg = gpa(s.grades)
    StudentReport(s, avg, letterGrade(avg), standing(avg, s.grades.nonEmpty),
                  highestGrade(s.grades), lowestGrade(s.grades))
  }

def onProbation(reports: List[StudentReport]): List[StudentReport] =
  reports.filter(_.standing == Probation)
```

`map` y `filter` reciben **lambdas** (funciones anónimas). El símbolo `_` es un atajo para el parámetro cuando se usa solo una vez.

### 4.4 for-comprehension

```scala
def topScores(students: List[Student], threshold: Double): List[(String, String, Double)] =
  for
    student          <- students
    (course, score)  <- student.grades
    if score >= threshold
  yield (student.name, course, score)
```

Un `for` en Scala **no es un loop imperativo** — es azúcar sintáctica sobre llamadas encadenadas a `flatMap`, `map` y `filter`. El compilador lo transforma automáticamente. El resultado es una nueva lista, sin mutar nada.

**Equivalente sin for-comprehension:**
```scala
students.flatMap { s =>
  s.grades
    .filter { (_, score) => score >= threshold }
    .map    { (course, score) => (s.name, course, score) }
}
```

### 4.5 Recursión con acumulador

```scala
private def sumAll(nums: List[Double], acc: Double = 0.0): Double =
  nums match
    case Nil          => acc
    case head :: tail => sumAll(tail, acc + head)
```

Scala no tiene `for` loops como Java, pero la recursión sobre listas usando el patrón `head :: tail` es idiomática. El acumulador `acc` convierte la recursión en **tail-recursive** (el compilador la optimiza en un loop sin riesgo de stack overflow).

**Equivalente en Java:**
```java
double sum = 0;
for (double n : nums) sum += n;
```

Ambos hacen lo mismo; la versión Scala evita la variable mutable y expresa la intención como una transformación.

---

## 5. `Main.scala` — Punto de entrada

`Main` es el único lugar donde ocurren efectos (I/O). Construye los datos, delega la lógica a `GradeService`, y renderiza los resultados.

### Ejemplo: distribución por letra con `groupBy`

```scala
val grouped: Map[LetterGrade, List[StudentReport]] =
  GradeService.groupByLetter(reports)

List(A, B, C, D, F).foreach { letter =>
  val group = grouped.getOrElse(letter, Nil)
  val names = if group.isEmpty then "—" else group.map(_.student.name).mkString(", ")
  println(s"  ${GradeService.letterToString(letter)}: $names")
}
```

`groupBy` retorna un `Map` donde cada clave agrupa los elementos que comparten ese valor. `getOrElse` maneja el caso en que una letra no tiene estudiantes, devolviendo una lista vacía sin lanzar excepción.

---

## 6. Ejecutar el programa

```bash
sbt "runMain Main"
```

### Salida

```
──────────────────────────────────────────────────
  REPORTE INDIVIDUAL
──────────────────────────────────────────────────
  Ana López         GPA= 86.25  [B]  Good Standing    best=CC3056 92.00  worst=MA2031 78.00
  Luis Pérez        GPA= 54.50  [F]  Probation        best=CC2003 60.00  worst=CC3056 45.00
  Mía García        GPA= 97.50  [A]  Honors           best=MA2031 100.00  worst=CC2016 95.00
  Carlos Ruiz       GPA= 68.75  [D]  Probation        best=MA2031 72.00  worst=CC2003 65.00
  Sofia Torres      GPA= 45.00  [F]  Probation        best=CC3056 55.00  worst=MA2031 35.00
  David Chen        GPA= 87.50  [B]  Good Standing    best=MA2031 91.00  worst=CC2003 84.00
  Valeria Soto      GPA= 63.25  [D]  Probation        best=CC2003 67.00  worst=CC3056 60.00

──────────────────────────────────────────────────
  ESTUDIANTES EN PROBATORIA
──────────────────────────────────────────────────
  Luis Pérez  GPA=54.50
  Carlos Ruiz  GPA=68.75
  Sofia Torres  GPA=45.00
  Valeria Soto  GPA=63.25

──────────────────────────────────────────────────
  CUADRO DE HONOR
──────────────────────────────────────────────────
  Mía García  GPA=97.50

──────────────────────────────────────────────────
  DISTRIBUCIÓN POR LETRA
──────────────────────────────────────────────────
  A: Mía García
  B: Ana López, David Chen
  C: —
  D: Carlos Ruiz, Valeria Soto
  F: Luis Pérez, Sofia Torres

──────────────────────────────────────────────────
  ESTADÍSTICAS DE SECCIÓN
──────────────────────────────────────────────────
  Promedio general de la sección: 71.82
  Total de estudiantes           : 7
  Aprobados (>= 61)              : 5
  Reprobados (< 61)              : 2

──────────────────────────────────────────────────
  NOTAS INDIVIDUALES >= 90
──────────────────────────────────────────────────
  Mía García        MA2031  100.00
  Mía García        CC2003  98.00
  ...

──────────────────────────────────────────────────
  PROMEDIO POR CURSO
──────────────────────────────────────────────────
  CC2016  Algoritmos y Estructura de Datos          avg=70.71
  CC2003  Programación Orientada a Objetos          avg=73.43
  MA2031  Matemática Discreta                       avg=71.14
  CC3056  Sistemas Operativos                       avg=72.00
```

---

## 7. Conceptos clave demostrados

| Concepto | Dónde se ve | Qué resuelve |
|---|---|---|
| Case classes | `models.scala` | Tipos de datos inmutables sin boilerplate |
| Sealed traits (ADT) | `LetterGrade`, `Standing` | Pattern matching exhaustivo y seguro |
| Pattern matching | `letterGrade`, `standing`, `sumAll` | Alternativa expresiva a `if/else` y `switch` |
| `Option[T]` | `highestGrade`, `lowestGrade` | Elimina `NullPointerException` |
| `map` / `filter` | `generateReports`, `onProbation` | Transformar/filtrar colecciones sin loops |
| `flatMap` | `allIndividualGrades` | Aplanar colecciones anidadas |
| `groupBy` | `groupByLetter`, per-course avg | Agrupar sin tablas hash manuales |
| `for`-comprehension | `topScores` | Queries sobre colecciones con sintaxis declarativa |
| Recursión con acumulador | `sumAll` | Reemplaza loops con variables mutables |
| Funciones de orden superior | `filtrarPor`, `topScores` | Comportamiento parametrizable sin herencia |

---

## 8. Paradigma funcional vs imperativo

El mismo resultado de `topScores` en **Java imperativo**:

```java
List<Object[]> result = new ArrayList<>();
for (Student s : students) {
    for (Map.Entry<String, Double> e : s.getGrades().entrySet()) {
        if (e.getValue() >= threshold) {
            result.add(new Object[]{s.getName(), e.getKey(), e.getValue()});
        }
    }
}
```

En **Scala funcional**:

```scala
for
  s            <- students
  (course, sc) <- s.grades
  if sc >= threshold
yield (s.name, course, sc)
```

Ambos producen el mismo output. La versión Scala:
- No tiene variables mutables (`result`, `e`)
- No tiene efectos secundarios dentro del cuerpo
- Es más directa de leer como especificación: "de cada estudiante, de cada nota, si ≥ umbral, devuelve la tupla"

---

## 9. Recursos

- Scala 3 Book — Pattern Matching: https://docs.scala-lang.org/scala3/book/control-structures.html#match-expressions
- Scala 3 Book — Case Classes: https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html
- Tour of Scala — Higher-Order Functions: https://docs.scala-lang.org/tour/higher-order-functions.html
- Scastie (entorno online): https://scastie.scala-lang.org/
