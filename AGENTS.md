# Project Instructions

This is a Minecraft NeoForge 1.21.1 Java mod project.

## Environment

- Java 21
- Gradle wrapper
- NeoForge 1.21.1
- IntelliJ IDEA
- Windows PowerShell

## General Rules

- Keep changes small and targeted.
- Do not refactor unrelated systems.
- Follow the existing package structure.
- Follow existing registry, config, networking, and data patterns.
- Prefer server-authoritative logic.
- Keep client-only code out of common/server packages.
- Do not reference Minecraft client classes from server/common code.
- Do not rename public classes unless required.
- Do not change save formats unless explicitly requested.
- Do not add unnecessary comments.
- Do not create duplicate systems if an existing system already handles the feature.

## Before Editing

- Search the existing codebase first.
- Identify the relevant files.
- Explain the current flow briefly.
- Propose the smallest patch.
- Only edit after the relevant files are known.

## Build / Test Commands

Use these commands where relevant:

```powershell
.\gradlew compileJava
.\gradlew build
.\gradlew runClient
.\gradlew runServer