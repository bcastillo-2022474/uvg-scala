object DataTypes {
  def main(args: Array[String]): Unit = {

    // ─────────────────────────────────────────────
    // 1. TIPOS PRIMITIVOS (AnyVal)
    // ─────────────────────────────────────────────
    val entero: Int       = 42
    val largo: Long       = 9_000_000_000L
    val decimal: Double   = 3.14159
    val flotante: Float   = 2.71f
    val byte: Byte        = 127
    val corto: Short      = 32_000
    val caracter: Char    = 'S'
    val bandera: Boolean  = true

    println("=== Tipos primitivos (AnyVal) ===")
    println(s"Int:     $entero  (${entero.getClass.getSimpleName})")
    println(s"Long:    $largo  (${largo.getClass.getSimpleName})")
    println(s"Double:  $decimal  (${decimal.getClass.getSimpleName})")
    println(s"Float:   $flotante  (${flotante.getClass.getSimpleName})")
    println(s"Byte:    $byte  (${byte.getClass.getSimpleName})")
    println(s"Short:   $corto  (${corto.getClass.getSimpleName})")
    println(s"Char:    $caracter  (${caracter.getClass.getSimpleName})")
    println(s"Boolean: $bandera  (${bandera.getClass.getSimpleName})")

    // ─────────────────────────────────────────────
    // 2. TIPOS DE REFERENCIA (AnyRef)
    // ─────────────────────────────────────────────
    val texto: String = "Hola Scala"
    println("\n=== Tipos de referencia (AnyRef) ===")
    println(s"String: \"$texto\"  longitud=${texto.length}")

    // ─────────────────────────────────────────────
    // 3. INFERENCIA DE TIPOS Y val vs var
    // ─────────────────────────────────────────────
    val inferido = 100          // el compilador deduce Int
    var cambiable = "cambiable"  // var permite reasignación
    cambiable = "nuevo valor"

    println("\n=== Inferencia de tipos y val/var ===")
    println(s"inferido  = $inferido  tipo inferido: ${inferido.getClass.getSimpleName}")
    println(s"cambiable = $cambiable")

    // Tipado fuerte: esto no compilaría —
    // val x: Int = "texto"   // ERROR en tiempo de compilación

    // ─────────────────────────────────────────────
    // 4. List (inmutable, lista enlazada)
    // ─────────────────────────────────────────────
    val numeros: List[Int] = List(10, 20, 30, 40, 50)
    val conPrepend         = 5 :: numeros   // :: agrega al frente — O(1)
    val concatenada        = numeros ::: List(60, 70)

    println("\n=== List (inmutable) ===")
    println(s"numeros:    $numeros")
    println(s"con prepend: $conPrepend")
    println(s"concatenada: $concatenada")
    println(s"head: ${numeros.head}  tail: ${numeros.tail}")
    println(s"filtrar pares: ${numeros.filter(_ % 20 == 0)}")
    println(s"map x2:        ${numeros.map(_ * 2)}")

    // ─────────────────────────────────────────────
    // 5. Vector (inmutable, acceso indexado eficiente)
    // ─────────────────────────────────────────────
    val vec = Vector("a", "b", "c", "d")
    println("\n=== Vector (inmutable, O(log n) acceso) ===")
    println(s"vector: $vec")
    println(s"vec(2): ${vec(2)}")
    println(s"updated: ${vec.updated(1, "Z")}")  // devuelve nuevo vector

    // ─────────────────────────────────────────────
    // 6. Array (mutable, equivale al array de JVM)
    // ─────────────────────────────────────────────
    val arreglo = Array(1, 2, 3, 4, 5)
    arreglo(0) = 99  // mutación directa

    println("\n=== Array (mutable, JVM array) ===")
    println(s"arreglo: ${arreglo.mkString(", ")}")
    println(s"longitud: ${arreglo.length}")

    // ─────────────────────────────────────────────
    // 7. Set (sin duplicados, inmutable por defecto)
    // ─────────────────────────────────────────────
    val conjunto = Set(1, 2, 3, 2, 1)  // duplicados ignorados
    val conNuevo = conjunto + 4
    val sinUno   = conjunto - 1

    println("\n=== Set (sin duplicados) ===")
    println(s"conjunto: $conjunto")
    println(s"+ 4:      $conNuevo")
    println(s"- 1:      $sinUno")
    println(s"contiene 2: ${conjunto.contains(2)}")

    // ─────────────────────────────────────────────
    // 8. Map (clave → valor, inmutable por defecto)
    // ─────────────────────────────────────────────
    val edades: Map[String, Int] = Map("Ana" -> 20, "Luis" -> 25, "Mia" -> 22)
    val actualizado = edades + ("Carlos" -> 30)

    println("\n=== Map (clave → valor) ===")
    println(s"edades: $edades")
    println(s"Ana tiene: ${edades("Ana")} años")
    println(s"Acceso seguro (clave inexistente): ${edades.get("Pedro")}")  // Option
    println(s"con Carlos: $actualizado")
    println(s"claves: ${edades.keys.toList}")

    // ─────────────────────────────────────────────
    // 9. Tuple (tipos heterogéneos, hasta 22 elementos)
    // ─────────────────────────────────────────────
    val persona: (String, Int, Boolean) = ("João", 21, true)

    println("\n=== Tuple ===")
    println(s"persona: $persona")
    println(s"nombre: ${persona._1}  edad: ${persona._2}  activo: ${persona._3}")

    // Desestructuración (más idiomático)
    val (nombre, edad, activo) = persona
    println(s"desestructurado → nombre=$nombre, edad=$edad, activo=$activo")

    // ─────────────────────────────────────────────
    // 10. Option (alternativa segura a null)
    // ─────────────────────────────────────────────
    val encontrado: Option[String]    = Some("resultado")
    val noEncontrado: Option[String]  = None

    println("\n=== Option (evita NullPointerException) ===")
    println(s"encontrado:   $encontrado")
    println(s"noEncontrado: $noEncontrado")
    println(s"getOrElse:    ${noEncontrado.getOrElse("valor por defecto")}")

    // Uso práctico: buscar en un Map sin riesgo de excepción
    val capital = Map("Guatemala" -> "Ciudad de Guatemala", "México" -> "CDMX")
    val buscar  = List("Guatemala", "Honduras", "México")
    buscar.foreach { pais =>
      capital.get(pais) match {
        case Some(c) => println(s"  $pais → $c")
        case None    => println(s"  $pais → no encontrado")
      }
    }

    // ─────────────────────────────────────────────
    // 11. Colecciones mutables (scala.collection.mutable)
    // ─────────────────────────────────────────────
    import scala.collection.mutable

    val listaM = mutable.ListBuffer(1, 2, 3)
    listaM += 4
    listaM -= 1

    val mapaM = mutable.Map("x" -> 10)
    mapaM("y") = 20

    println("\n=== Colecciones mutables ===")
    println(s"ListBuffer: $listaM")
    println(s"mutable.Map: $mapaM")
  }
}
