<!-- Auto-generated guidance for AI coding agents working on MARS-MASHUP -->
# Copilot instructions for MARS-MASHUP

This file gives repository-specific guidance for AI coding agents so you can be productive immediately.

Summary
- This is a Java-based educational MIPS IDE (MARS) fork with an extension to let students add custom assembly languages.
- Key packages live under `mars/` (e.g. `mars.assembler`, `mars.mips`, `mars.simulator`, `mars.tools`).

What matters (big-picture)
- Entry point: top-level `Mars.java` which forwards to `mars.MarsLaunch`.
- Assembler code is under `mars/assembler/` (look at `Assembler.java`, `Tokenizer.java`, `SymbolTable.java`).
- MIPS instruction implementations live under `mars/mips/` and subfolders. Custom language plugins (user-supplied instruction sets) are expected in `mars/mips/instructions/customlangs/` as JAR files.
- Simulator components live under `mars/simulator/` (e.g. `Simulator.java`, `Exceptions.java`, `ProgramArgumentList.java`).
- UI/help/docs are in `docs/` and `help/` (auto-generated Javadoc HTML useful for discovering public APIs and behavior).

Build / run / test notes (discovered from repo)
- There is no Maven/Gradle build file; builds are performed using `javac`/`jar` in repo scripts or helpers.
- To run the IDE locally from the repository root (simple run):
  - compile: `javac Mars.java` (or compile all sources into an output dir) 
  - run: `java Mars`
- To build a custom language JAR for `mars/mips/instructions/customlangs/`, use the provided helper `BuildCustomLang.java`:
  - Example: `java BuildCustomLang ExampleCustomAssembly.java` (the helper uses `javac` and `jar`, then places `ExampleCustomAssembly.jar` into `mars/mips/instructions/customlangs/`).
  - The helper ensures `Mars.java` is compiled, produces a temporary `last_customlang_out` class directory, builds the jar, then moves it into the `customlangs` folder.

Repository conventions and patterns
- Package layout must match filesystem layout under `mars/` — do not move files outside their packages.
- New instructions or languages: implement classes under `mars/mips/instructions/` or `.../customlangs/` and (for custom languages) produce a JAR placed in `mars/mips/instructions/customlangs/`.
- Configuration is driven by text files at repo root: `Settings.properties`, `Config.properties`, `Syscall.properties`, `PseudoOps.txt`. These are read at runtime — when changing behavior, prefer updating these files rather than hardcoding values.
- Javadoc-derived docs in `docs/` are authoritative for public API behavior; consult `docs/mars/*.html` when uncertain about how to use a class.

Patterns to follow when proposing code changes
- Keep public APIs in `mars.*` stable — many tools/docs expect those classes to exist with their current signatures.
- Tests are not present in this repo; verify changes by running the IDE (`java Mars`) and/or using small sample `.asm` files in the repo (e.g. `mips1.asm`, `mipsPlusPlus.asm`, `testfile.asm`).
- When adding new files, follow the existing style: one top-level public class per `.java` file, packages declared at the top matching the path.

Integration points and cross-component communication
- Assembler -> Simulator: the assembler produces `MIPSprogram` objects (see `mars/MIPSprogram.java`) consumed by `simulator/Simulator.java`.
- Instruction implementations live close to the assembler and simulator layers; changes in instruction semantics often need paired updates to assembler token/operand parsing (`mars/assembler/OperandFormat.java`, `TokenTypes.java`) and simulator behavior.
- Resource files (images, help files) live in `images/`, `help/`, `resources/` — updates here affect the UI and runtime help.

Files to consult first (quick map)
- `Mars.java` — repo entry point
- `BuildCustomLang.java` — helper for building custom language JARs
- `mars/assembler/Assembler.java`, `Tokenizer.java`, `SymbolTable.java` — assembler internals
- `mars/mips/instructions/` and `mars/mips/instructions/customlangs/` — instruction implementations and plugin jars
- `mars/simulator/Simulator.java`, `Exceptions.java` — simulator core
- `docs/` and `help/` — generated API docs and user help

Examples (do this when making changes)
- To add a new custom language class named `FooLang.java`: place source in `mars/mips/instructions/customlangs/`, then run `java BuildCustomLang FooLang.java` from repo root. That will compile and move `FooLang.jar` into the `customlangs` folder.
- To validate a change to an instruction implementation: run `java Mars`, open an example `.asm` file from repo root, assemble and run it in the IDE, and check simulator output.

What not to change without human approval
- Large refactors of package structure or renaming public classes in `mars.*` (breaks students' expectations and docs).
- Removing or repurposing configuration files (`Settings.properties`, `Syscall.properties`, `PseudoOps.txt`) used at runtime.

If anything here is unclear or you want this tailored (for example, to include CI build commands, sample inputs, or a more detailed developer runbook), tell me which areas to expand and I'll iterate.

<!-- end -->
