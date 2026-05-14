package entrega4

object Main4 {

  private def section(title: String): Unit =
    println(s"\n${"─" * 56}")
    println(s"  $title")
    println("─" * 56)

  private def showResult(label: String, result: ResolveResult): Unit =
    result match
      case BuildOrder(order) =>
        println(s"  [$label] Build order:")
        order.zipWithIndex.foreach { (pkg, i) =>
          println(f"    ${i + 1}%2d. $pkg")
        }
      case CycleDetected(cycle) =>
        println(s"  [$label] *** CYCLE DETECTED ***")
        println(s"       ${cycle.mkString(" → ")}")

  def main(args: Array[String]): Unit = {

    // ── Scenario 1: Simple package manager ──────────────────────────────────
    // Nodes = packages, edges = "depends on"
    // e.g. "http-client" needs "json-parser" and "tls"
    section("Scenario 1 — Package manager (no cycles)")

    val pkgGraph = Graph.from(
      "app"         -> Set("http-client", "logger", "config"),
      "http-client" -> Set("json-parser", "tls", "logger"),
      "json-parser" -> Set("base64"),
      "tls"         -> Set("crypto", "base64"),
      "config"      -> Set("yaml-parser"),
      "yaml-parser" -> Set.empty,
      "logger"      -> Set.empty,
      "crypto"      -> Set.empty,
      "base64"      -> Set.empty
    )

    println(s"\n  Packages: ${pkgGraph.nodes.toList.sorted.mkString(", ")}")
    println(s"\n  In-degrees (# of dependencies):")
    pkgGraph.inDegree.toList.sortBy(_._1).foreach { (pkg, deg) =>
      println(f"    $pkg%-15s  $deg deps")
    }

    showResult("Kahn/BFS", Resolver.kahn(pkgGraph))
    showResult("DFS",      Resolver.dfs(pkgGraph))

    // ── Scenario 2: University course prerequisites ──────────────────────────
    section("Scenario 2 — Course prerequisites (no cycles)")

    val courseGraph = Graph.from(
      "CC3056"  -> Set("CC2016", "CC2003"),       // Sistemas Operativos
      "CC2016"  -> Set("CC2003", "MA2031"),        // Algoritmos
      "CC2003"  -> Set("CC1010"),                  // POO
      "MA2031"  -> Set("MA1031"),                  // Matemática Discreta
      "CC1010"  -> Set.empty,                      // Intro Programación
      "MA1031"  -> Set.empty                       // Cálculo I
    )

    showResult("Kahn/BFS", Resolver.kahn(courseGraph))
    showResult("DFS",      Resolver.dfs(courseGraph))

    // ── Scenario 3: Cycle — circular dependency ──────────────────────────────
    section("Scenario 3 — Circular dependency (cycle expected)")

    val cyclicGraph = Graph.from(
      "A" -> Set("B"),
      "B" -> Set("C"),
      "C" -> Set("D"),
      "D" -> Set("B")   // D depends back on B → cycle B→C→D→B
    )

    showResult("Kahn/BFS", Resolver.kahn(cyclicGraph))
    showResult("DFS",      Resolver.dfs(cyclicGraph))

    // ── Scenario 4: Build system with many independent subgraphs ────────────
    section("Scenario 4 — Build system (disconnected subgraphs)")

    val buildGraph = Graph.from(
      // Frontend
      "frontend"   -> Set("react", "webpack", "babel"),
      "webpack"    -> Set("node-core"),
      "babel"      -> Set("node-core"),
      "react"      -> Set("node-core"),
      "node-core"  -> Set.empty,
      // Backend (independent from frontend)
      "backend"    -> Set("akka-http", "slick", "logback"),
      "akka-http"  -> Set("akka-core"),
      "slick"      -> Set("jdbc"),
      "akka-core"  -> Set.empty,
      "logback"    -> Set.empty,
      "jdbc"       -> Set.empty,
      // Deploy step depends on both
      "deploy"     -> Set("frontend", "backend", "docker"),
      "docker"     -> Set.empty
    )

    println(s"\n  Total packages: ${buildGraph.nodes.size}")
    showResult("Kahn/BFS", Resolver.kahn(buildGraph))

    // ── Data structure summary ───────────────────────────────────────────────
    section("Data structures used")
    println("""
  Graph.edges  Map[String, Set[String]]   adjacency: node → dependencies
  inDegree     Map[String, Int]           # of deps per node (Kahn init)
  dependents   Map[String, Set[String]]   reverse adjacency (Kahn update)

  Kahn's BFS:
    Queue[String]     immutable FIFO frontier — dequeue/enqueue in O(1)
    Map[String, Int]  in-degree table, updated as nodes resolve
    List[String]      result accumulated in topological order

  DFS sort:
    Set[String]       visited — O(1) lookup, prevents reprocessing
    Set[String]       inStack — O(1) back-edge detection → cycle
    Vector[String]    current path — O(1) append, used to extract cycle
    List[String]      result built by post-order prepend (reverse-free)
    Either[V, State]  propagates a cycle error up the call stack without
                      exceptions — purely functional error handling
    """.stripMargin)
  }
}
