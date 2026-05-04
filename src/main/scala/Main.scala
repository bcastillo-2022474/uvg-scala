// ─── Main ─────────────────────────────────────────────────────────────────────
// Entry point. Builds the dataset, calls GradeService (pure functions),
// and renders results to the console.

object Main {

  // Pretty-prints a separator line
  private def section(title: String): Unit =
    println(s"\n${"─" * 50}")
    println(s"  $title")
    println("─" * 50)

  // Formats a Double to 2 decimal places
  private def fmt(d: Double): String = f"$d%.2f"

  def main(args: Array[String]): Unit = {

    // ── Dataset ────────────────────────────────────────────────────────────────
    val courses = List(
      Course("CC2016", "Algoritmos y Estructura de Datos", 4),
      Course("CC2003", "Programación Orientada a Objetos",  4),
      Course("MA2031", "Matemática Discreta",               3),
      Course("CC3056", "Sistemas Operativos",               4)
    )

    val students = List(
      Student("22-001", "Ana López",
        Map("CC2016" -> 85.0, "CC2003" -> 90.0, "MA2031" -> 78.0, "CC3056" -> 92.0)),
      Student("22-002", "Luis Pérez",
        Map("CC2016" -> 55.0, "CC2003" -> 60.0, "MA2031" -> 58.0, "CC3056" -> 45.0)),
      Student("22-003", "Mía García",
        Map("CC2016" -> 95.0, "CC2003" -> 98.0, "MA2031" -> 100.0, "CC3056" -> 97.0)),
      Student("22-004", "Carlos Ruiz",
        Map("CC2016" -> 70.0, "CC2003" -> 65.0, "MA2031" -> 72.0, "CC3056" -> 68.0)),
      Student("22-005", "Sofia Torres",
        Map("CC2016" -> 40.0, "CC2003" -> 50.0, "MA2031" -> 35.0, "CC3056" -> 55.0)),
      Student("22-006", "David Chen",
        Map("CC2016" -> 88.0, "CC2003" -> 84.0, "MA2031" -> 91.0, "CC3056" -> 87.0)),
      Student("22-007", "Valeria Soto",
        Map("CC2016" -> 62.0, "CC2003" -> 67.0, "MA2031" -> 64.0, "CC3056" -> 60.0))
    )

    // ── Generate reports (pure transformation, no side effects) ────────────────
    val reports = GradeService.generateReports(students)

    // ── 1. Individual reports ──────────────────────────────────────────────────
    section("REPORTE INDIVIDUAL")
    reports.foreach { r =>
      val best  = r.highest.map { (c, s) => s"$c ${fmt(s)}" }.getOrElse("N/A")
      val worst = r.lowest.map  { (c, s) => s"$c ${fmt(s)}" }.getOrElse("N/A")
      println(
        f"  ${r.student.name}%-16s" +
        f"  GPA=${fmt(r.gpa)}%6s" +
        f"  [${GradeService.letterToString(r.letter)}]" +
        f"  ${GradeService.standingToString(r.standing)}%-15s" +
        f"  best=$best  worst=$worst"
      )
    }

    // ── 2. Students on probation ───────────────────────────────────────────────
    section("ESTUDIANTES EN PROBATORIA")
    val probation = GradeService.onProbation(reports)
    if probation.isEmpty then println("  Ninguno")
    else probation.foreach(r => println(s"  ${r.student.name}  GPA=${fmt(r.gpa)}"))

    // ── 3. Honor roll ──────────────────────────────────────────────────────────
    section("CUADRO DE HONOR")
    val honors = GradeService.onHonors(reports)
    if honors.isEmpty then println("  Ninguno")
    else honors.foreach(r => println(s"  ${r.student.name}  GPA=${fmt(r.gpa)}"))

    // ── 4. Distribution by letter grade ───────────────────────────────────────
    section("DISTRIBUCIÓN POR LETRA")
    val grouped = GradeService.groupByLetter(reports)
    List(A, B, C, D, F).foreach { letter =>
      val label = GradeService.letterToString(letter)
      val group = grouped.getOrElse(letter, Nil)
      val names = if group.isEmpty then "—" else group.map(_.student.name).mkString(", ")
      println(s"  $label: $names")
    }

    // ── 5. Section average ─────────────────────────────────────────────────────
    section("ESTADÍSTICAS DE SECCIÓN")
    val avg = GradeService.sectionAverage(reports)
    println(s"  Promedio general de la sección: ${fmt(avg)}")
    println(s"  Total de estudiantes           : ${students.length}")
    println(s"  Aprobados (>= 61)              : ${reports.count(_.gpa >= 61)}")
    println(s"  Reprobados (< 61)              : ${reports.count(_.gpa < 61)}")

    // ── 6. Top scores across all courses (for-comprehension) ──────────────────
    section("NOTAS INDIVIDUALES >= 90")
    val topScores = GradeService.topScores(students, 90.0)
    if topScores.isEmpty then println("  Ninguna")
    else topScores
      .sortBy { (_, _, score) => -score }   // sort descending by score
      .foreach { (name, course, score) =>
        println(f"  $name%-16s  $course  ${fmt(score)}")
      }

    // ── 7. Per-course averages using groupBy + map ─────────────────────────────
    section("PROMEDIO POR CURSO")
    val allGrades = GradeService.allIndividualGrades(students)
    val byCourse: Map[String, List[Double]] =
      allGrades
        .groupBy(_._2)                     // group by course code
        .map { (course, rows) =>
          course -> rows.map(_._3)         // keep only the scores
        }

    courses.foreach { c =>
      val scores  = byCourse.getOrElse(c.code, Nil)
      val average = if scores.isEmpty then 0.0 else scores.sum / scores.length
      println(f"  ${c.code}  ${c.title}%-40s  avg=${fmt(average)}")
    }

    println()
  }
}
