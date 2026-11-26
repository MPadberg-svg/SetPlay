#!/usr/bin/env python3
"""
Surgical in-place CI Gradle normalizer for SetPlay.

When running outside the Google AI Studio Build container (e.g., GitHub Actions
ubuntu-latest runners), maps AI Studio internal container Gradle/AGP versions
(agp = "9.1.1", compileSdk 36.1) to Public Maven Central / Google Maven
equivalents (AGP 8.7.3 / Kotlin 2.0.21 / SDK 35) WITHOUT ever destructively
overwriting build files or losing buildConfigField declarations, dependencies,
signingConfigs, manifestPlaceholders, or versionCode/versionName properties.
"""

from pathlib import Path
import re
import sys

# Map AI Studio internal [versions] strings to Public Maven Central / Google Maven equivalents.
VERSION_REPLACEMENTS: dict[str, tuple[str, str]] = {
    "agp": ("9.1.1", "8.7.3"),
    "kotlin": ("2.2.10", "2.0.21"),
    "googleDevtoolsKsp": ("2.3.5", "2.0.21-1.0.28"),
    "coreKtx": ("1.18.0", "1.15.0"),
    "activityCompose": ("1.10.1", "1.9.3"),
    "roomRuntime": ("2.7.0", "2.6.1"),
    "roomKtx": ("2.7.0", "2.6.1"),
    "roomCompiler": ("2.7.0", "2.6.1"),
    "navigationCompose": ("2.8.9", "2.8.5"),
    "robolectric": ("4.16.1", "4.14.1"),
    "retrofit": ("2.12.0", "2.11.0"),
    "converterMoshi": ("2.12.0", "2.11.0"),
    "kotlinxCoroutinesTest": ("1.10.2", "1.9.0"),
    "kotlinxCoroutinesAndroid": ("1.10.2", "1.9.0"),
    "kotlinxCoroutinesCore": ("1.10.2", "1.9.0"),
    "moshiKotlin": ("1.15.2", "1.15.1"),
    "moshiKotlinCodegen": ("1.15.2", "1.15.1"),
    "junitVersion": ("1.3.0", "1.2.1"),
    "espressoCore": ("3.7.0", "3.6.1"),
    "okhttp": ("4.10.0", "4.12.0"),
    "loggingInterceptor": ("4.10.0", "4.12.0"),
}

KOTLIN_ANDROID_PLUGIN_TOML = (
    'kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }'
)


def parse_root_arg(argv: list[str]) -> Path:
    root = "."
    i = 1
    while i < len(argv):
        arg = argv[i]
        if arg == "--root" and i + 1 < len(argv):
            root = argv[i + 1]
            i += 2
        elif arg.startswith("--root="):
            root = arg.split("=", 1)[1]
            i += 1
        else:
            sys.stderr.write(f"Usage: {argv[0]} [--root <dir>]\n")
            sys.exit(2)
    return Path(root).resolve()


def normalize_versions_toml(path: Path) -> None:
    if not path.is_file():
        return
    original = path.read_text(encoding="utf-8")
    lines = original.splitlines(keepends=True)
    in_versions = False
    in_plugins = False
    has_kotlin_android_plugin = any(
        re.match(r"^\s*kotlin-android\s*=", line) for line in lines
    )
    out: list[str] = []

    for line in lines:
        stripped = line.strip()
        if stripped.startswith("[") and stripped.endswith("]"):
            in_versions = stripped == "[versions]"
            in_plugins = stripped == "[plugins]"
            out.append(line)
            continue

        if in_versions:
            m = re.match(r'^(\s*)([A-Za-z0-9_.-]+)(\s*=\s*)"([^"]+)"(.*)$', line)
            if m:
                indent, key, eq, val, rest = m.groups()
                if key in VERSION_REPLACEMENTS:
                    old_val, new_val = VERSION_REPLACEMENTS[key]
                    if val == old_val:
                        newline = "\n" if line.endswith("\n") else ""
                        rest_clean = rest.rstrip("\r\n")
                        line = f'{indent}{key}{eq}"{new_val}"{rest_clean}{newline}'
            out.append(line)
            continue

        if in_plugins and not has_kotlin_android_plugin:
            out.append(line)
            if re.match(r"^\s*android-application\s*=", line):
                out.append(f"{KOTLIN_ANDROID_PLUGIN_TOML}\n")
                has_kotlin_android_plugin = True
            continue

        out.append(line)

    if not has_kotlin_android_plugin:
        if out and not out[-1].endswith("\n"):
            out[-1] = out[-1] + "\n"
        out.append(f"\n[plugins]\n{KOTLIN_ANDROID_PLUGIN_TOML}\n")

    updated = "".join(out)
    if updated != original:
        path.write_text(updated, encoding="utf-8")


def normalize_settings_gradle(path: Path) -> None:
    if not path.is_file():
        return
    original = path.read_text(encoding="utf-8")
    out = [
        line
        for line in original.splitlines(keepends=True)
        if "foojay-resolver-convention" not in line
    ]
    updated = "".join(out)
    if updated != original:
        path.write_text(updated, encoding="utf-8")


def normalize_root_build_gradle(path: Path) -> None:
    if not path.is_file():
        return
    original = path.read_text(encoding="utf-8")
    lines = original.splitlines(keepends=True)
    has_kotlin_android = any("libs.plugins.kotlin.android" in line for line in lines)
    out: list[str] = []

    for line in lines:
        if "libs.plugins.google.services" in line:
            continue
        out.append(line)
        if not has_kotlin_android and "libs.plugins.android.application" in line:
            indent = re.match(r"^(\s*)", line).group(1)
            out.append(f"{indent}alias(libs.plugins.kotlin.android) apply false\n")
            has_kotlin_android = True

    updated = "".join(out)
    if updated != original:
        path.write_text(updated, encoding="utf-8")


def normalize_app_build_gradle(path: Path) -> None:
    if not path.is_file():
        return
    original = path.read_text(encoding="utf-8")
    lines = original.splitlines(keepends=True)
    has_kotlin_android = any("libs.plugins.kotlin.android" in line for line in lines)
    has_kotlin_options = any(
        re.search(r"\bkotlinOptions\s*\{", line) for line in lines
    )

    out: list[str] = []
    in_compile_options = False
    compile_options_indent = "  "

    for line in lines:
        # Remove Google Services plugin import, alias, and configuration line
        if re.match(r"^\s*import\s+com\.google\.gms\.googleservices\.", line):
            continue
        if "libs.plugins.google.services" in line:
            continue
        if re.match(r"^\s*googleServices\s*\{", line):
            continue

        # Normalize AI Studio compileSdk 36.1 / targetSdk 36 to Public SDK 35
        if re.search(r"compileSdk\s*\{[^}]*release\s*\(\s*36\s*\)[^}]*\}", line):
            indent = re.match(r"^(\s*)", line).group(1)
            out.append(f"{indent}compileSdk = 35\n")
            continue
        if re.match(r"^\s*targetSdk\s*=\s*36\b", line):
            line = re.sub(r"targetSdk\s*=\s*36\b", "targetSdk = 35", line, count=1)

        out.append(line)

        # Insert alias(libs.plugins.kotlin.android) right after android.application
        if not has_kotlin_android and "libs.plugins.android.application" in line:
            indent = re.match(r"^(\s*)", line).group(1)
            out.append(f"{indent}alias(libs.plugins.kotlin.android)\n")
            has_kotlin_android = True

        # Insert kotlinOptions { jvmTarget = "11" } right after compileOptions { ... }
        if not has_kotlin_options:
            if re.match(r"^(\s*)compileOptions\s*\{", line):
                in_compile_options = True
                compile_options_indent = re.match(r"^(\s*)", line).group(1)
            elif in_compile_options and re.match(r"^\s*\}\s*$", line):
                in_compile_options = False
                out.append(
                    f'{compile_options_indent}kotlinOptions {{\n'
                    f'{compile_options_indent}  jvmTarget = "11"\n'
                    f'{compile_options_indent}}}\n'
                )
                has_kotlin_options = True

    updated = "".join(out)
    if updated != original:
        path.write_text(updated, encoding="utf-8")


def normalize_gradle_properties(path: Path) -> None:
    if not path.is_file():
        return
    original = path.read_text(encoding="utf-8")
    out: list[str] = []
    has_use_androidx = False

    for line in original.splitlines(keepends=True):
        stripped = line.strip()
        if stripped.startswith("kotlin.compiler.execution.strategy"):
            continue
        if stripped.startswith("org.gradle.configuration-cache"):
            continue
        if stripped == "android.useAndroidX=true":
            has_use_androidx = True
        out.append(line)

    if not has_use_androidx:
        if out and not out[-1].endswith("\n"):
            out[-1] = out[-1] + "\n"
        out.append("android.useAndroidX=true\n")

    updated = "".join(out)
    if updated != original:
        path.write_text(updated, encoding="utf-8")


def normalize_project(root: Path) -> bool:
    toml_path = root / "gradle" / "libs.versions.toml"
    if not toml_path.is_file():
        print(f">>> [normalize_ci_gradle] Skipping: {toml_path} not found.")
        return False

    toml_text = toml_path.read_text(encoding="utf-8")
    if 'agp = "9.1.1"' not in toml_text:
        print('>>> [normalize_ci_gradle] Already normalized (agp = "9.1.1" not present); no changes made.')
        return False

    normalize_versions_toml(toml_path)
    normalize_settings_gradle(root / "settings.gradle.kts")
    normalize_root_build_gradle(root / "build.gradle.kts")
    normalize_app_build_gradle(root / "app" / "build.gradle.kts")
    normalize_gradle_properties(root / "gradle.properties")
    print(f">>> [normalize_ci_gradle] Surgically normalized Gradle configuration under {root} for AGP 8.7.3 / SDK 35.")
    return True


def main(argv: list[str]) -> int:
    root = parse_root_arg(argv)
    normalize_project(root)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
