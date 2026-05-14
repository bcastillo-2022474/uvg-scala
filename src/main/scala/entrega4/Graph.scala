package entrega4

// ─── Graph representation ─────────────────────────────────────────────────────
// A directed graph stored as an adjacency map.
// Map[String, Set[String]]:  node → set of nodes it depends on
//   e.g. "B" -> Set("A")  means B requires A (edge A → B in build order)

case class Graph(edges: Map[String, Set[String]]) {

  // All nodes mentioned anywhere (as keys or as dependencies)
  def nodes: Set[String] =
    edges.keySet ++ edges.values.flatten.toSet

  // Dependencies of a single node (empty Set if the node has none)
  def depsOf(node: String): Set[String] =
    edges.getOrElse(node, Set.empty)

  // Reverse: for each node, who depends on it?
  def dependents: Map[String, Set[String]] =
    nodes.foldLeft(Map.empty[String, Set[String]]) { (acc, node) =>
      depsOf(node).foldLeft(acc) { (a, dep) =>
        a.updated(dep, a.getOrElse(dep, Set.empty) + node)
      }
    }

  // In-degree: how many nodes point TO each node
  def inDegree: Map[String, Int] =
    nodes.map { n => n -> depsOf(n).size }.toMap
}

object Graph {
  // Convenience builder: list of (node, dependencies) pairs
  def from(pairs: (String, Set[String])*): Graph =
    Graph(pairs.toMap)
}
