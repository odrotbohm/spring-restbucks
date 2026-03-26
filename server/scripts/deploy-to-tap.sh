#!/usr/bin/env bash
set -euo pipefail

# Ensure direnv config exists (for Artifactory credentials)
if [ ! -f .envrc ]; then
  echo "ERROR: Missing .envrc file"
  echo "       Create it from the template (cp .envrc-template .envrc)"
  echo "       Then follow the instructions in the file to set Artifactory credentials"
  echo "       Then run direnv allow"
  exit 1
fi

# Clean the Maven repository
# rm -rf ~/.m2/repository/com/vmware/tanzu/spring/

# Build the application
./mvnw -s ./m2-settings.xml clean package -DskipTests

# Push the application
cf push -p target/restbucks-1.0.0-SNAPSHOT.jar --random-route

# If not already insalled, install plugins
cf plugins | awk 'NR>1{print $1}' | grep -Fxq metric-registrar || cf install-plugin -r CF-Community metric-registrar -f
cf plugins | awk 'NR>1{print $1}' | grep -Fxq log-cache        || cf install-plugin -r CF-Community log-cache -f

# Register the metrics endpoint
cf register-metrics-endpoint restbucks /actuator/prometheusjvm --internal-port 8090 --insecure

# Verify that the metrics are being collected
cf tail -f --envelope-class=metrics restbucks