package entrega4

import scala.collection.immutable.Queue

// ─── Result ADT ───────────────────────────────────────────────────────────────
sealed trait ResolveResult
case class BuildOrder(order: List[String])          extends ResolveResult
case class CycleDetected(cycle: Vector[String])     extends ResolveResult

// ─── Dependency Resolver ──────────────────────────────────────────────────────
// Two algorithms on the same Graph structure:
//   1. Kahn's algorithm (BFS, iterative) — uses Queue + Map + Set
//   2. DFS topological sort              — uses recursive DFS + Set + List

object Resolver {

  // ── 1. Kahn's algorithm (BFS-based topological sort) ──────────────────────
  // Data structures used:
  //   Queue[String]        — BFS frontier (immutable functional queue)
  //   Map[String, Int]     — current in-degree per node
  //   List[String]         — accumulated build order (result)
  //
  // How it works:
  //   - Start by enqueuing every node with in-degree == 0 (no dependencies)
  //   - Dequeue a node → add to result → decrement in-degree of its dependents
  //   - If a dependent now has in-degree 0, enqueue it
  //   - If the result has fewer nodes than the graph, a cycle exists

  def kahn(graph: Graph): ResolveResult = {
    val dependents = graph.dependents
    val initDegree = graph.inDegree

    // Seed queue with nodes that have no dependencies
    val initQueue: Queue[String] =
      Queue.from(initDegree.filter(_._2 == 0).keys.toList.sorted)

    @annotation.tailrec
    def loop(
      queue:  Queue[String],
      degree: Map[String, Int],
      result: List[String]
    ): ResolveResult =
      if queue.isEmpty then
        if result.length < graph.nodes.size then
          // Not all nodes resolved → cycle exists somewhere
          val unresolved = graph.nodes -- result.toSet
          CycleDetected(unresolved.toVector.sorted)
        else
          BuildOrder(result)
      else
        val (node, rest) = queue.dequeue

        // Reduce in-degree of every node that depends on the current one
        val (newDegree, newQueue) =
          dependents.getOrElse(node, Set.empty).foldLeft((degree, rest)) {
            case ((deg, q), dependent) =>
              val updated = deg(dependent) - 1
              val nextQ   = if updated == 0 then q.enqueue(dependent) else q
              (deg.updated(dependent, updated), nextQ)
          }

        loop(newQueue, newDegree, result :+ node)

    loop(initQueue, initDegree, List.empty)
  }

  // ── 2. DFS-based topological sort ─────────────────────────────────────────
  // Data structures used:
  //   Set[String]   — visited nodes (fully processed)
  //   Set[String]   — in-stack nodes (currently in DFS path, for cycle detection)
  //   Vector[String] — current DFS path (to reconstruct the cycle)
  //   List[String]  — result accumulated by prepend, reversed once at the end
  //
  // Returns the same ResolveResult ADT as kahn().

  def dfs(graph: Graph): ResolveResult = {
    // State threaded through the recursion (immutable — no vars)
    case class State(
      visited: Set[String],
      inStack: Set[String],
      path:    Vector[String],
      result:  List[String]
    )

    def visit(node: String, state: State): Either[Vector[String], State] =
      if state.inStack.contains(node) then
        // Back edge → cycle. Slice the path to show just the cycle.
        val cycleStart = state.path.indexOf(node)
        Left(state.path.drop(cycleStart) :+ node)
      else if state.visited.contains(node) then
        Right(state)  // already fully processed, skip
      else
        val entering = state.copy(
          inStack = state.inStack + node,
          path    = state.path    :+ node
        )
        // Visit all dependencies first (DFS recursive descent)
        val afterDeps: Either[Vector[String], State] =
          graph.depsOf(node).foldLeft(Right(entering): Either[Vector[String], State]) {
            case (Left(cycle), _)    => Left(cycle)   // propagate cycle up
            case (Right(st), dep)    => visit(dep, st)
          }

        afterDeps.map { st =>
          st.copy(
            visited = st.visited + node,
            inStack = st.inStack - node,
            path    = st.path.dropRight(1),
            result  = node :: st.result   // post-order prepend → correct order
          )
        }

    val init = State(Set.empty, Set.empty, Vector.empty, List.empty)

    val finalState: Either[Vector[String], State] =
      graph.nodes.toList.sorted.foldLeft(Right(init): Either[Vector[String], State]) {
        case (Left(cycle), _)  => Left(cycle)
        case (Right(st), node) => visit(node, st)
      }

    finalState match
      case Left(cycle)  => CycleDetected(cycle)
      case Right(state) => BuildOrder(state.result.reverse)
  }
}
