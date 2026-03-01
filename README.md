# UVG Scala

A Scala project for UVG data structures.

## Environment Setup

### 1. Install Java (JDK 17+)

Scala runs on the JVM, so you need a JDK installed first. JDK 25 works fine.

**macOS (with Homebrew):**
```bash
brew install openjdk
```

Verify:
```bash
java -version
```

### 2. Install sbt (Scala Build Tool)

**macOS (with Homebrew):**
```bash
brew install sbt
```

Verify:
```bash
sbt --version
```

sbt will automatically download the correct version of Scala defined in `build.sbt`.

### 3. (Optional) Install the Scala CLI

Useful for running single `.scala` files quickly. Use the VirtusLab tap:
```bash
brew install VirtusLab/scala-cli/scala-cli
```

---

## Running the Project

### With sbt

```bash
# From the project root directory:
sbt run
```

On the first run, sbt will download dependencies and the Scala compiler — this may take a minute.

### With Scala CLI (single file, no build tool needed)

```bash
scala-cli src/main/scala/HelloWorld.scala
```

---

## Project Structure

```
uvg-scala/
├── build.sbt                        # Project configuration & dependencies
├── README.md
└── src/
    └── main/
        └── scala/
            └── HelloWorld.scala     # Main entry point
```

## IDE Support

[IntelliJ IDEA](https://www.jetbrains.com/idea/) with the **Scala plugin** or
[VS Code](https://code.visualstudio.com/) with the **Metals** extension both
provide full Scala support (autocomplete, type hints, debugging).
