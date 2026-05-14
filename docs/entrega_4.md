# Entrega 4: Estructuras de Datos

**Universidad del Valle de Guatemala**  
**CC2016 – Algoritmos y Estructura de Datos**  
**Lenguaje investigado: Scala 3**

---

## 1. ¿Para qué tipos de problemas es Scala más adecuado?

Scala brilla en situaciones donde se combinan **transformaciones de datos complejas** y **corrección por construcción** (errores detectados en compilación, no en runtime). Los dominios más naturales son:

| Dominio | Por qué Scala encaja |
|---|---|
| Procesamiento de datos / ETL | Colecciones inmutables + `map/filter/flatMap` expresivos |
| Sistemas distribuidos (Akka) | Modelo de actores + tipos que previenen estado compartido |
| Compiladores / intérpretes | ADTs + pattern matching exhaustivo para ASTs |
| Servicios web (Play, http4s) | Tipos para efectos, errores manejados como valores |
| Big Data (Apache Spark) | Spark está escrito en Scala; su API es idiomática Scala |
| Algoritmos sobre grafos | Inmutabilidad facilita razonamiento sobre estado |

---

## 2. Problema: Resolución de dependencias (Topological Sort)

Un **resolvedor de dependencias** determina en qué orden construir/instalar elementos que dependen unos de otros. Es el problema central de herramientas como `npm`, `pip`, `apt`, Maven, o un sistema de build como `sbt` mismo.

El problema se modela como un **grafo dirigido acíclico (DAG)**:
- **Nodos** = paquetes / cursos / tareas
- **Arista A → B** = "B depende de A" (A debe construirse antes)
- **Objetivo** = orden lineal donde cada nodo aparece después de todos sus predecesores

Si el grafo tiene un ciclo (A → B → C → A), no existe orden válido → el resolvedor debe detectarlo.

---

## 3. Estructura del proyecto

```
src/main/scala/entrega4/
├── Graph.scala    ← representación del grafo (Map + Set)
├── Resolver.scala ← dos algoritmos: Kahn (BFS) y DFS
└── Main4.scala    ← cuatro escenarios de prueba
```

---

## 4. `Graph.scala` — El grafo como estructura de datos

```scala
case class Graph(edges: Map[String, Set[String]])
```

El grafo completo vive en un **`Map[String, Set[String]]`**: cada nodo mapea al conjunto de nodos de los que depende. Usar `Set` (no `List`) para las dependencias elimina duplicados automáticamente y da búsqueda O(1).

### Operaciones derivadas

```scala
def nodes: Set[String] =
  edges.keySet ++ edges.values.flatten.toSet

def dependents: Map[String, Set[String]] =
  nodes.foldLeft(Map.empty[String, Set[String]]) { (acc, node) =>
    depsOf(node).foldLeft(acc) { (a, dep) =>
      a.updated(dep, a.getOrElse(dep, Set.empty) + node)
    }
  }

def inDegree: Map[String, Int] =
  nodes.map { n => n -> depsOf(n).size }.toMap
```

`dependents` invierte el grafo (de "quién necesito" a "quién me necesita") usando solo `foldLeft` y `Map.updated` — sin variables mutables. `inDegree` mapea cada nodo a cuántas dependencias tiene directas.

### Estructura de datos en uso

| Estructura | Rol | Complejidad |
|---|---|---|
| `Map[String, Set[String]]` | Grafo de adyacencia | lookup O(1) avg |
| `Set[String]` | Dependencias de un nodo | member O(1), sin dups |
| `Map[String, Int]` | In-degree por nodo | lookup/update O(1) avg |

---

## 5. `Resolver.scala` — Dos algoritmos, mismas estructuras

Ambos algoritmos implementan **topological sort** y retornan el mismo ADT:

```scala
sealed trait ResolveResult
case class BuildOrder(order: List[String])      extends ResolveResult
case class CycleDetected(cycle: Vector[String]) extends ResolveResult
```

Usar un ADT sellado garantiza que el código que consume el resultado **debe** manejar ambos casos — el compilador no deja ignorar `CycleDetected`.

---

### 5.1 Algoritmo de Kahn (BFS)

**Idea:** procesar iterativamente los nodos sin dependencias pendientes, reduciendo el in-degree de sus sucesores.

```
1. Calcular in-degree de todos los nodos
2. Encolar todos los nodos con in-degree == 0
3. Mientras la cola no esté vacía:
   a. Desencolar nodo N → agregarlo al resultado
   b. Para cada nodo M que depende de N: decrementar in-degree(M)
   c. Si in-degree(M) == 0, encolar M
4. Si resultado.length < total nodos → hay un ciclo
```

**Estructuras de datos:**

```scala
// BFS frontier — immutable Queue de Scala
val initQueue: Queue[String] =
  Queue.from(initDegree.filter(_._2 == 0).keys.toList.sorted)

@annotation.tailrec
def loop(
  queue:  Queue[String],   // FIFO frontier
  degree: Map[String, Int], // in-degree actualizado
  result: List[String]      // orden acumulado
): ResolveResult = ...
```

`Queue[String]` de Scala es una cola funcional e inmutable. `dequeue` retorna `(elemento, nuevaCola)` sin mutar nada. El loop es **tail-recursive** (`@tailrec` lo verifica en compilación).

```scala
val (node, rest) = queue.dequeue

val (newDegree, newQueue) =
  dependents.getOrElse(node, Set.empty).foldLeft((degree, rest)) {
    case ((deg, q), dependent) =>
      val updated = deg(dependent) - 1
      val nextQ   = if updated == 0 then q.enqueue(dependent) else q
      (deg.updated(dependent, updated), nextQ)
  }

loop(newQueue, newDegree, result :+ node)
```

`foldLeft` recorre el conjunto de dependientes actualizando el `Map` de grados y la `Queue` de forma completamente inmutable — cada iteración produce nuevos valores, los anteriores no se modifican.

---

### 5.2 DFS topológico

**Idea:** hacer DFS desde cada nodo; agregar un nodo al resultado solo después de haber visitado todos sus predecesores (post-order). Un ciclo se detecta si al visitar un nodo, este ya está en el stack de llamadas actual.

```scala
case class State(
  visited: Set[String],    // nodos completamente procesados
  inStack: Set[String],    // nodos en el camino DFS actual
  path:    Vector[String], // camino actual (para reconstruir ciclo)
  result:  List[String]    // acumulador post-order
)
```

El `State` se pasa explícitamente a través de la recursión — no hay variables globales mutables. `Either[Vector[String], State]` funciona como manejo de errores puramente funcional: `Left(ciclo)` se propaga automáticamente hacia arriba sin excepciones.

```scala
def visit(node: String, state: State): Either[Vector[String], State] =
  if state.inStack.contains(node) then
    val cycleStart = state.path.indexOf(node)
    Left(state.path.drop(cycleStart) :+ node)   // ciclo detectado
  else if state.visited.contains(node) then
    Right(state)                                  // ya procesado
  else
    // Visitar todas las dependencias primero (DFS recursivo)
    val afterDeps = graph.depsOf(node).foldLeft(Right(entering)) {
      case (Left(cycle), _)  => Left(cycle)
      case (Right(st), dep)  => visit(dep, st)
    }
    // Post-order: agregar el nodo al resultado después de sus deps
    afterDeps.map { st =>
      st.copy(result = node :: st.result, ...)
    }
```

**Por qué `Vector` para el path y no `List`:**  
`Vector` tiene `append` (`:+`) en O(1) amortizado e `indexOf` eficiente. `List` tiene `append` en O(n) — para un path que crece elemento por elemento, `Vector` es la elección correcta.

**Por qué dos `Set` distintos (`visited` e `inStack`):**

| Set | Pregunta que responde | Cuándo se agrega | Cuándo se elimina |
|---|---|---|---|
| `visited` | ¿Ya terminé este nodo? | Al salir del DFS (post-order) | Nunca |
| `inStack` | ¿Estoy en medio de este nodo? | Al entrar al DFS | Al salir (post-order) |

Un nodo en `inStack` pero no en `visited` indica que estamos en medio de su procesamiento. Si encontramos ese nodo de nuevo → ciclo.

---

## 6. Salida del programa

```bash
sbt "runMain entrega4.Main4"
```

```
────────────────────────────────────────────────────────
  Scenario 1 — Package manager (no cycles)
────────────────────────────────────────────────────────
  [Kahn/BFS] Build order:
     1. base64       4. yaml-parser   7. config
     2. crypto       5. json-parser   8. http-client
     3. logger       6. tls           9. app
  [DFS] Build order:
     1. base64       4. tls           7. yaml-parser
     2. json-parser  5. logger        8. config
     3. crypto       6. http-client   9. app

────────────────────────────────────────────────────────
  Scenario 2 — Course prerequisites (no cycles)
────────────────────────────────────────────────────────
  [Kahn/BFS] Build order:
     1. CC1010  2. MA1031  3. CC2003  4. MA2031  5. CC2016  6. CC3056
  [DFS] Build order:
     1. CC1010  2. CC2003  3. MA1031  4. MA2031  5. CC2016  6. CC3056

────────────────────────────────────────────────────────
  Scenario 3 — Circular dependency (cycle expected)
────────────────────────────────────────────────────────
  [Kahn/BFS] *** CYCLE DETECTED ***  A → B → C → D
  [DFS]      *** CYCLE DETECTED ***  B → C → D → B

────────────────────────────────────────────────────────
  Scenario 4 — Build system (13 packages, 2 subgraphs)
────────────────────────────────────────────────────────
  [Kahn/BFS] Build order:
     1. akka-core   5. node-core   9. webpack
     2. docker      6. akka-http  10. babel
     3. jdbc        7. slick      11. backend
     4. logback     8. react      12. frontend  13. deploy
```

Ambos algoritmos producen un **orden topológico válido** (todo nodo aparece después de sus dependencias). Pueden diferir en el orden relativo de nodos independientes — ambos son correctos.

---

## 7. Resumen de estructuras de datos

| Estructura | Dónde | Qué resuelve |
|---|---|---|
| `Map[String, Set[String]]` | `Graph.edges` | Grafo de adyacencia compacto |
| `Set[String]` | Dependencias, `visited`, `inStack` | Membresía O(1), sin duplicados |
| `Map[String, Int]` | `inDegree` en Kahn | Contador de dependencias pendientes |
| `Map[String, Set[String]]` | `dependents` en Kahn | Grafo inverso para actualizar in-degrees |
| `Queue[String]` | Frontier BFS en Kahn | FIFO funcional e inmutable |
| `List[String]` | Resultado en ambos algoritmos | Secuencia ordenada acumulada |
| `Vector[String]` | Path DFS | Append O(1) + acceso por índice para ciclos |
| `Either[V, State]` | Retorno de `visit` en DFS | Propagación de error sin excepciones |

---

## 8. Comparación Kahn vs DFS

| Aspecto | Kahn (BFS) | DFS |
|---|---|---|
| Estilo | Iterativo (loop tail-recursive) | Recursivo |
| Detección de ciclo | Implícita: resultado incompleto | Explícita: back-edge en `inStack` |
| Reporte del ciclo | Nodos sin resolver (no el camino exacto) | Camino exacto del ciclo |
| Orden del resultado | BFS-level order | DFS post-order |
| Paralelismo | Fácil: todos los nodos de nivel 0 son independientes | Más complejo |

---

## 9. Recursos

- Scala Docs — Immutable Queue: https://www.scala-lang.org/api/current/scala/collection/immutable/Queue.html
- Scala Docs — Collections overview: https://docs.scala-lang.org/overviews/collections-2.13/introduction.html
- Kahn's algorithm: Kahn, A.B. (1962). *Topological sorting of large networks*. CACM.
- Scastie (entorno online): https://scastie.scala-lang.org/
