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

java -jar tool/bin/rdapct-1.0.jar -c config.json --use-local-datasets "$@" 1>&2

find results -type f -exec cat {} \; -delete
