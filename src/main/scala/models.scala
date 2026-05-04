// ─── Data Model ──────────────────────────────────────────────────────────────
// Case classes are Scala's primary tool for modeling immutable data.
// The compiler auto-generates equals, hashCode, toString, and a copy method.

case class Student(id: String, name: String, grades: Map[String, Double])

case class Course(code: String, title: String, credits: Int)

// Sealed trait = closed ADT: the compiler knows every possible subtype,
// enabling exhaustive pattern matching (warning if a case is missing).
sealed trait LetterGrade
case object A  extends LetterGrade  // 90-100
case object B  extends LetterGrade  // 80-89
case object C  extends LetterGrade  // 70-79
case object D  extends LetterGrade  // 60-69
case object F  extends LetterGrade  // < 60

sealed trait Standing
case object Honors      extends Standing   // GPA >= 90
case object Good        extends Standing   // GPA >= 70
case object Probation   extends Standing   // GPA < 70
case object Incomplete  extends Standing   // no courses registered

// Report aggregates computed results — produced by GradeService, not mutated
case class StudentReport(
  student:  Student,
  gpa:      Double,
  letter:   LetterGrade,
  standing: Standing,
  highest:  Option[(String, Double)],  // (course, score)
  lowest:   Option[(String, Double)]
)
