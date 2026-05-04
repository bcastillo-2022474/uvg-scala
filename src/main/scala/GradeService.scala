// ─── Grade Service ────────────────────────────────────────────────────────────
// Pure functions: every function here takes input and returns output with no
// side effects (no printing, no mutation, no I/O). This is the functional core.

object GradeService {

  // ── Basic calculations ──────────────────────────────────────────────────────

  def gpa(grades: Map[String, Double]): Double =
    if grades.isEmpty then 0.0
    else grades.values.sum / grades.size

  // Pattern matching as an expression — each case returns a value
  def letterGrade(score: Double): LetterGrade = score match
    case s if s >= 90 => A
    case s if s >= 80 => B
    case s if s >= 70 => C
    case s if s >= 60 => D
    case _            => F

  def standing(gpa: Double, hasGrades: Boolean): Standing =
    if !hasGrades then Incomplete
    else if gpa >= 90 then Honors
    else if gpa >= 70 then Good
    else Probation

  // Option[T] instead of null: forces callers to handle the "empty" case
  def highestGrade(grades: Map[String, Double]): Option[(String, Double)] =
    if grades.isEmpty then None
    else Some(grades.maxBy(_._2))

  def lowestGrade(grades: Map[String, Double]): Option[(String, Double)] =
    if grades.isEmpty then None
    else Some(grades.minBy(_._2))

  // ── Report generation ───────────────────────────────────────────────────────

  // Higher-order: takes a List and transforms every element with a lambda
  def generateReports(students: List[Student]): List[StudentReport] =
    students.map { s =>
      val avg = gpa(s.grades)
      StudentReport(
        student  = s,
        gpa      = avg,
        letter   = letterGrade(avg),
        standing = standing(avg, s.grades.nonEmpty),
        highest  = highestGrade(s.grades),
        lowest   = lowestGrade(s.grades)
      )
    }

  // ── Filtering / querying ─────────────────────────────────────────────────────

  def onProbation(reports: List[StudentReport]): List[StudentReport] =
    reports.filter(_.standing == Probation)

  def onHonors(reports: List[StudentReport]): List[StudentReport] =
    reports.filter(_.standing == Honors)

  // groupBy returns a Map[K, List[V]] — no loops, no mutable state
  def groupByLetter(reports: List[StudentReport]): Map[LetterGrade, List[StudentReport]] =
    reports.groupBy(_.letter)

  // ── Section statistics ───────────────────────────────────────────────────────

  // Recursive function with accumulator (manual fold to show recursion)
  private def sumAll(nums: List[Double], acc: Double = 0.0): Double =
    nums match
      case Nil          => acc
      case head :: tail => sumAll(tail, acc + head)

  def sectionAverage(reports: List[StudentReport]): Double =
    if reports.isEmpty then 0.0
    else
      val total = sumAll(reports.map(_.gpa))
      total / reports.length

  // flatMap flattens one level: List[Map[...]] → List[(String, Double)]
  def allIndividualGrades(students: List[Student]): List[(String, String, Double)] =
    students.flatMap { s =>
      s.grades.toList.map { (course, score) => (s.name, course, score) }
    }

  // for-comprehension: syntactic sugar for flatMap + map + filter
  def topScores(students: List[Student], threshold: Double): List[(String, String, Double)] =
    for
      student        <- students
      (course, score) <- student.grades
      if score >= threshold
    yield (student.name, course, score)

  // ── Formatting helpers ───────────────────────────────────────────────────────

  def letterToString(l: LetterGrade): String = l match
    case A => "A"
    case B => "B"
    case C => "C"
    case D => "D"
    case F => "F"

  def standingToString(s: Standing): String = s match
    case Honors     => "Honors"
    case Good       => "Good Standing"
    case Probation  => "Probation"
    case Incomplete => "Incomplete"
}
