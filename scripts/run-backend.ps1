$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $root
try {
    mvn -q compile
    java -cp target\classes Main
} finally {
    Pop-Location
}
