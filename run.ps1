$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$out = Join-Path $root "out"
New-Item -ItemType Directory -Force $out | Out-Null
$files = Get-ChildItem (Join-Path $root "src/main/java") -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d $out $files
java -cp $out com.smartlaundry.Main
