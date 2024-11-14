#!/bin/sh

help() {
    cat << END
Arguments:
  [--timeout=<timeout>]
  [[--use-rdap-profile-february-2019]
  ([--gtld-registrar] | [--gtld-registry [--thin]])]
  RDAP_URI
END
  exit 0
}

[ "$1" = "" ] && help

for arg in "$@" ; do
  if [ "--help" = "$arg" ] || [ "-h" = "$arg" ] ; then
    help
  fi
done

RCT_VERSION="$(xq -x //rdap-conformance.version pom.xml)"

java -jar "tool/target/rdapct-${RCT_VERSION}.jar" -c tool/bin/rdapct_config.json --use-local-datasets "$@" 1>&2

find results -type f -exec cat {} \; -delete
