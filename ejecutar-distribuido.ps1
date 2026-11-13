# Demo del nivel 2: compila, abre el Broker y los 3 proveedores en ventanas separadas
# y ejecuta el cliente de demostración en esta consola.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName

Start-Process java '-cp out broker.distribuido.BrokerServidor'
Start-Sleep 1
Start-Process java '-cp out broker.distribuido.ProveedorServidor calificaciones 6001'
Start-Process java '-cp out broker.distribuido.ProveedorServidor materias 6002'
Start-Process java '-cp out broker.distribuido.ProveedorServidor biblioteca 6003'
Start-Sleep 2

java -cp out broker.distribuido.ClienteRemoto

Write-Host "`nSiguientes pasos (ver README): sustituir la biblioteca, proveedor lento, cerrar ventanas con Ctrl+C."
