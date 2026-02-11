# Gitignore workspace summary

## Detected projects

- apigateway-main (pom.xml)
- el-almacen-de-peliculas-online/el-almacen-de-peliculas-online (pom.xml)
- el-almacen-de-peliculas-online-rating (pom.xml)
- el-almacen-de-peliculas-online-ventas (pom.xml)
- springboot-sso (pom.xml)
- el-almacen-de-peliculas-online-front-end (package.json)

## Changes by project

### apigateway-main

Updated .gitignore with Java build outputs, logs, IDE metadata, OS junk, secrets, and local overrides.

```ignore
# Java / build artifacts
/target/
/build/
*.class
*.jar
*.war
*.ear

# Logs
*.log
/logs/

# IntelliJ IDEA
.idea/
*.iml
*.ipr
*.iws

# Eclipse
.project
.classpath
.settings/

# VS Code
.vscode/

# OS
.DS_Store
Thumbs.db

# Secrets / certs
*.pem
*.key
*.crt
*.p12
/secrets/

# Maven wrapper
!.mvn/wrapper/maven-wrapper.jar

# Maven shaded
dependency-reduced-pom.xml

# Local overrides
docker-compose.override.yml
*.local.ps1
*.local.sh
```

### el-almacen-de-peliculas-online/el-almacen-de-peliculas-online

Added .gitignore with Java build outputs, logs, IDE metadata, OS junk, secrets, and local overrides.

```ignore
# Java / build artifacts
/target/
/build/
*.class
*.jar
*.war
*.ear

# Logs
*.log
/logs/

# IntelliJ IDEA
.idea/
*.iml
*.ipr
*.iws

# Eclipse
.project
.classpath
.settings/

# VS Code
.vscode/

# OS
.DS_Store
Thumbs.db

# Secrets / certs
*.pem
*.key
*.crt
*.p12
/secrets/

# Maven wrapper
!.mvn/wrapper/maven-wrapper.jar

# Maven shaded
dependency-reduced-pom.xml

# Local overrides
docker-compose.override.yml
*.local.ps1
*.local.sh
```

### el-almacen-de-peliculas-online-rating

Added .gitignore with Java build outputs, logs, IDE metadata, OS junk, secrets, and local overrides.

```ignore
# Java / build artifacts
/target/
/build/
*.class
*.jar
*.war
*.ear

# Logs
*.log
/logs/

# IntelliJ IDEA
.idea/
*.iml
*.ipr
*.iws

# Eclipse
.project
.classpath
.settings/

# VS Code
.vscode/

# OS
.DS_Store
Thumbs.db

# Secrets / certs
*.pem
*.key
*.crt
*.p12
/secrets/

# Maven wrapper
!.mvn/wrapper/maven-wrapper.jar

# Maven shaded
dependency-reduced-pom.xml

# Local overrides
docker-compose.override.yml
*.local.ps1
*.local.sh
```

### el-almacen-de-peliculas-online-ventas

Updated .gitignore with Java build outputs, logs, IDE metadata, OS junk, secrets, and local overrides.

```ignore
# Java / build artifacts
/target/
/build/
*.class
*.jar
*.war
*.ear

# Logs
*.log
/logs/

# IntelliJ IDEA
.idea/
*.iml
*.ipr
*.iws

# Eclipse
.project
.classpath
.settings/

# VS Code
.vscode/

# OS
.DS_Store
Thumbs.db

# Secrets / certs
*.pem
*.key
*.crt
*.p12
/secrets/

# Maven wrapper
!.mvn/wrapper/maven-wrapper.jar

# Maven shaded
dependency-reduced-pom.xml

# Local overrides
docker-compose.override.yml
*.local.ps1
*.local.sh
```

### springboot-sso

Updated .gitignore with Java build outputs, logs, IDE metadata, OS junk, secrets, and local overrides.

```ignore
# Java / build artifacts
/target/
/build/
*.class
*.jar
*.war
*.ear

# Logs
*.log
/logs/

# IntelliJ IDEA
.idea/
*.iml
*.ipr
*.iws

# Eclipse
.project
.classpath
.settings/

# VS Code
.vscode/

# OS
.DS_Store
Thumbs.db

# Secrets / certs
*.pem
*.key
*.crt
*.p12
/secrets/

# Maven wrapper
!.mvn/wrapper/maven-wrapper.jar

# Maven shaded
dependency-reduced-pom.xml

# Local overrides
docker-compose.override.yml
*.local.ps1
*.local.sh
```

### el-almacen-de-peliculas-online-front-end

Updated .gitignore with node deps, Vite outputs/cache, logs, envs, IDE metadata, OS junk, secrets, and local overrides.

```ignore
# Dependencies
node_modules/

# Build outputs
dist/
build/

# Vite cache
.vite/

# Logs
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
*.log

# Env files (local)
.env
.env.local
.env.*.local

# IDE
.idea/
.vscode/

# OS
.DS_Store
Thumbs.db

# Secrets / certs
*.pem
*.key
*.crt
*.p12
/secrets/

# Local overrides
docker-compose.override.yml
*.local.ps1
*.local.sh
```

## Suggested cleanup commands (only if already tracked)

```bash
git rm -r --cached */target */build */logs node_modules dist .vite
```

## Quick verification

```bash
git status
```

```bash
git check-ignore -v path/to/file
```
