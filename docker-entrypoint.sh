#!/bin/sh

RCT_VERSION="$(xq -x //rdap-conformance.version pom.xml)"

help() {
  java -jar "tool/target/rdapct-${RCT_VERSION}.jar" --help
  exit 0
}

[ "$1" = "" ] && help

for arg in "$@" ; do
  if [ "--help" = "$arg" ] || [ "-h" = "$arg" ] ; then
    help
  fi
done

java -jar "tool/target/rdapct-${RCT_VERSION}.jar" -c tool/bin/rdapct_config.json --use-local-datasets "$@" 1>&2

STATUS=$?

find results -type f -exec cat {} \; -delete

exit $STATUS
