$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
& .\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8080 --spring.datasource.url=jdbc:h2:file:./data/browser-demo --spring.jpa.hibernate.ddl-auto=update'
exit $LASTEXITCODE
