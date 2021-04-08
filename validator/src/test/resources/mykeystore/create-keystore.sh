#!/usr/bin/env bash

set -euo pipefail
read -r -s -p "Please enter a password for the key & keystore (default: password):" PASSWORD
PASSWORD=${PASSWORD:=password}
rm -rf private out
mkdir -p private out
openssl req -x509 -newkey rsa:2048 -utf8 -days 3650 -nodes -config ca-cert.conf -keyout private/ca-cert.pem -out out/ca-cert.crt
openssl pkcs12 -export -inkey private/ca-cert.pem -in out/ca-cert.crt -out private/ca-cert.p12 -password "pass:$PASSWORD"
keytool -importkeystore -deststorepass "$PASSWORD" -destkeypass "$PASSWORD" -srckeystore private/ca-cert.p12 -srcstorepass "$PASSWORD" -deststoretype jks -destkeystore out/ca-cert.jks
keytool -import -alias ca-cert -file out/ca-cert.crt -keystore out/server.jks -deststorepass "$PASSWORD" -noprompt
rm -rf private
