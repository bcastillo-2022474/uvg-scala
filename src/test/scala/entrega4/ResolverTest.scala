package entrega4

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ResolverTest extends AnyFunSuite with Matchers {

  // ── helpers ─────────────────────────────────────────────────────────────────

  // Verify that `order` is a valid topological ordering for `graph`:
  // for every node, all its dependencies appear before it.
  private def isValidOrder(order: List[String], graph: Graph): Boolean =
    order.zipWithIndex.forall { (node, idx) =>
      graph.depsOf(node).forall { dep =>
        order.indexOf(dep) < idx
      }
    }

  // ── Graph.nodes ──────────────────────────────────────────────────────────────

  test("nodes includes keys and their dependencies") {
    val g = Graph.from("B" -> Set("A"))
    g.nodes shouldBe Set("A", "B")
  }

  test("nodes on empty graph is empty") {
    Graph(Map.empty).nodes shouldBe empty
  }

  // ── Graph.inDegree ───────────────────────────────────────────────────────────

  test("inDegree is 0 for a leaf node") {
    val g = Graph.from("A" -> Set.empty, "B" -> Set("A"))
    g.inDegree("A") shouldBe 0
  }

  test("inDegree counts direct dependencies only") {
    val g = Graph.from("C" -> Set("A", "B"))
    g.inDegree("C") shouldBe 2
  }

  // ── Kahn — happy path ────────────────────────────────────────────────────────

  test("Kahn resolves a linear chain A←B←C in correct order") {
    val g = Graph.from("B" -> Set("A"), "C" -> Set("B"), "A" -> Set.empty)
    Resolver.kahn(g) match
      case BuildOrder(order) =>
        order shouldBe List("A", "B", "C")
      case other => fail(s"Expected BuildOrder, got $other")
  }

  test("Kahn result is a valid topological order for the package graph") {
    val g = Graph.from(
      "app"         -> Set("http-client", "logger"),
      "http-client" -> Set("json-parser"),
      "json-parser" -> Set.empty,
      "logger"      -> Set.empty
    )
    Resolver.kahn(g) match
      case BuildOrder(order) => isValidOrder(order, g) shouldBe true
      case other             => fail(s"Expected BuildOrder, got $other")
  }

  test("Kahn handles disconnected subgraphs") {
    val g = Graph.from(
      "B" -> Set("A"), "A" -> Set.empty,
      "D" -> Set("C"), "C" -> Set.empty
    )
    Resolver.kahn(g) match
      case BuildOrder(order) =>
        order should have length 4
        isValidOrder(order, g) shouldBe true
      case other => fail(s"Expected BuildOrder, got $other")
  }

  test("Kahn detects a simple cycle A←B←A") {
    val g = Graph.from("A" -> Set("B"), "B" -> Set("A"))
    Resolver.kahn(g) shouldBe a[CycleDetected]
  }

  test("Kahn detects a longer cycle B←C←D←B") {
    val g = Graph.from("B" -> Set.empty, "C" -> Set("B"), "D" -> Set("C"), "B2" -> Set("D", "B"))
    // real cycle: A->B->C->D->B
    val cyclic = Graph.from("A" -> Set("B"), "B" -> Set("C"), "C" -> Set("D"), "D" -> Set("B"))
    Resolver.kahn(cyclic) shouldBe a[CycleDetected]
  }

  test("Kahn on a single node with no deps returns that node") {
    val g = Graph.from("solo" -> Set.empty)
    Resolver.kahn(g) shouldBe BuildOrder(List("solo"))
  }

  // ── DFS — happy path ─────────────────────────────────────────────────────────

  test("DFS resolves a linear chain A←B←C in correct order") {
    val g = Graph.from("B" -> Set("A"), "C" -> Set("B"), "A" -> Set.empty)
    Resolver.dfs(g) match
      case BuildOrder(order) =>
        isValidOrder(order, g) shouldBe true
        order.last shouldBe "C"
      case other => fail(s"Expected BuildOrder, got $other")
  }

  test("DFS result is a valid topological order for the course graph") {
    val g = Graph.from(
      "CC3056" -> Set("CC2016"),
      "CC2016" -> Set("CC2003"),
      "CC2003" -> Set("CC1010"),
      "CC1010" -> Set.empty
    )
    Resolver.dfs(g) match
      case BuildOrder(order) =>
        isValidOrder(order, g) shouldBe true
        order shouldBe List("CC1010", "CC2003", "CC2016", "CC3056")
      case other => fail(s"Expected BuildOrder, got $other")
  }

  test("DFS detects a cycle and reports the cycle path") {
    val g = Graph.from("A" -> Set("B"), "B" -> Set("C"), "C" -> Set("A"))
    Resolver.dfs(g) match
      case CycleDetected(cycle) =>
        cycle.length should be >= 2
        cycle.head shouldBe cycle.last  // cycle wraps back to start
      case other => fail(s"Expected CycleDetected, got $other")
  }

  test("DFS on a single node returns that node") {
    val g = Graph.from("solo" -> Set.empty)
    Resolver.dfs(g) match
      case BuildOrder(order) => order shouldBe List("solo")
      case other             => fail(s"Expected BuildOrder, got $other")
  }

  // ── Both algorithms agree on cycle-free graphs ────────────────────────────

  test("Kahn and DFS both succeed (BuildOrder) on the same acyclic graph") {
    val g = Graph.from(
      "deploy"   -> Set("frontend", "backend"),
      "frontend" -> Set("node-core"),
      "backend"  -> Set("jvm-core"),
      "node-core" -> Set.empty,
      "jvm-core"  -> Set.empty
    )
    val kahnOk = Resolver.kahn(g).isInstanceOf[BuildOrder]
    val dfsOk  = Resolver.dfs(g).isInstanceOf[BuildOrder]
    kahnOk shouldBe true
    dfsOk  shouldBe true
  }

  test("Kahn and DFS both fail (CycleDetected) on the same cyclic graph") {
    val g = Graph.from("X" -> Set("Y"), "Y" -> Set("Z"), "Z" -> Set("X"))
    Resolver.kahn(g) shouldBe a[CycleDetected]
    Resolver.dfs(g)  shouldBe a[CycleDetected]
  }
}
