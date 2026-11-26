#!/usr/bin/env bash
set -euo pipefail

keytool="${JAVA_HOME:?JAVA_HOME must point to the application JDK}/bin/keytool"
certificate="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)/pnet-ca.crt"
alias="pnet-dev-pnet-ca"

"$keytool" -printcert -file "$certificate" > /dev/null

if sudo "$keytool" -list -cacerts -storepass changeit -alias "$alias" > /dev/null 2>&1; then
    sudo "$keytool" -delete -cacerts -storepass changeit -alias "$alias"
fi

sudo "$keytool" -importcert -cacerts -storepass changeit -noprompt \
    -alias "$alias" -file "$certificate"
