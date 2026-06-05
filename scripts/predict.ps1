$ErrorActionPreference = "Stop"
if ($args.Count -lt 1) {
    throw "Usage: .\scripts\predict.ps1 <image-path>"
}
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$python = if (Test-Path (Join-Path $root ".venv\Scripts\python.exe")) {
    Join-Path $root ".venv\Scripts\python.exe"
} elseif ($env:SAMPLE_DETECTION_PYTHON) {
    $env:SAMPLE_DETECTION_PYTHON
} else {
    "python"
}
& $python (Join-Path $root "ml\predict.py") --image $args[0]

