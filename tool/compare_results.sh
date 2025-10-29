#!/usr/bin/env bash
# Compare two RDAP conformance results JSON files for equality of:
# - the multiset of "code" values (counts per code)
# - the multiset of "value" strings (counts per value and content)
#
# Usage: compare_results.sh file1.json file2.json
# Exit codes: 0 = match, 1 = differences, 2 = usage/error
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $(basename "$0") <resultsA.json> <resultsB.json>" >&2
  exit 2
fi

A="$1"
B="$2"

if [[ ! -f "$A" ]]; then
  echo "File not found: $A" >&2
  exit 2
fi
if [[ ! -f "$B" ]]; then
  echo "File not found: $B" >&2
  exit 2
fi

# Create a temp dir for intermediates
TMPDIR=$(mktemp -d -t compare-rdap-XXXXXXXX) || {
  echo "Failed to create temp directory" >&2
  exit 2
}
cleanup() { rm -rf "$TMPDIR"; }
trap cleanup EXIT

# Use perl to robustly extract JSON fields even if minified; avoid external deps like jq
# Extract codes: prints one code per line (e.g., -10612)
extract_codes() {
  local f="$1"
  perl -0777 -ne 'while(/"code"\s*:\s*(-?\d+)/g){print "$1\n"}' "$f" || true
}

# Extract values: prints one value string (unescaped JSON string content) per line
# We unescape common JSON escapes to compare semantic equality better.
unescape_json() {
  # Reads from stdin; uses perl to unescape JSON string escapes
  perl -pe '
    s/\\\"/\x22/g;          # escaped quotes
    s/\\\\/\\/g;            # escaped backslash
    s/\\\//\//g;             # escaped slash
    s/\\b/\x08/g;            # backspace
    s/\\f/\x0c/g;            # formfeed
    s/\\n/\n/g;               # newline
    s/\\r/\r/g;               # carriage return
    s/\\t/\t/g;               # tab
    s/\\u([0-9a-fA-F]{4})/chr(hex($1))/eg;  # unicode
  '
}

extract_values_raw() {
  local f="$1"
  perl -0777 -ne 'while(/"value"\s*:\s*"((?:[^"\\]|\\.)*)"/g){print "$1\n"}' "$f" || true
}

extract_values() {
  local f="$1"
  extract_values_raw "$f" | unescape_json
}

# Count multiset: read items on stdin; output as TSV: key<TAB>count, sorted by key
count_multiset() {
  LC_ALL=C sort | uniq -c | awk '{c=$1; $1=""; sub(/^ +/, ""); printf "%s\t%d\n", $0, c}' | LC_ALL=C sort -t $'\t' -k1,1
}

# Compare two TSVs of key<TAB>count. Output a joined TSV: key<TAB>countA<TAB>countB
join_counts() {
  local fa="$1" fb="$2"
  LC_ALL=C join -t $'\t' -a1 -a2 -e 0 -o '0,1.2,2.2' "$fa" "$fb" | LC_ALL=C sort -t $'\t' -k1,1
}

# Prepare code counts
extract_codes "$A" >"$TMPDIR/a.codes"
extract_codes "$B" >"$TMPDIR/b.codes"
wc -l <"$TMPDIR/a.codes" | tr -d ' ' >"$TMPDIR/a.codes.total"
wc -l <"$TMPDIR/b.codes" | tr -d ' ' >"$TMPDIR/b.codes.total"
count_multiset <"$TMPDIR/a.codes" >"$TMPDIR/a.codes.counts"
count_multiset <"$TMPDIR/b.codes" >"$TMPDIR/b.codes.counts"
join_counts "$TMPDIR/a.codes.counts" "$TMPDIR/b.codes.counts" >"$TMPDIR/codes.join"

# Prepare value counts
extract_values "$A" >"$TMPDIR/a.values"
extract_values "$B" >"$TMPDIR/b.values"
wc -l <"$TMPDIR/a.values" | tr -d ' ' >"$TMPDIR/a.values.total"
wc -l <"$TMPDIR/b.values" | tr -d ' ' >"$TMPDIR/b.values.total"
count_multiset <"$TMPDIR/a.values" >"$TMPDIR/a.values.counts"
count_multiset <"$TMPDIR/b.values" >"$TMPDIR/b.values.counts"
join_counts "$TMPDIR/a.values.counts" "$TMPDIR/b.values.counts" >"$TMPDIR/values.join"

# Evaluate differences
DIFF=0

# Codes: total count check
AC=$(cat "$TMPDIR/a.codes.total")
BC=$(cat "$TMPDIR/b.codes.total")
if [[ "$AC" != "$BC" ]]; then
  echo "Codes: total count differs: A=$AC B=$BC"
  DIFF=1
fi

# Codes: per-code counts
CODE_DIFFS=$(awk -F $'\t' '($2 != $3){print $0}' "$TMPDIR/codes.join" || true)
if [[ -n "$CODE_DIFFS" ]]; then
  echo "Codes: per-code count differences (code\tA\tB):"
  echo "$CODE_DIFFS" | sed 's/^/  /'
  DIFF=1
fi

# Values: total count check
AV=$(cat "$TMPDIR/a.values.total")
BV=$(cat "$TMPDIR/b.values.total")
if [[ "$AV" != "$BV" ]]; then
  echo "Values: total count differs: A=$AV B=$BV"
  DIFF=1
fi

# Values: per-value counts and content equality (multiset compare)
VALUE_DIFFS=$(awk -F $'\t' '($2 != $3){print $0}' "$TMPDIR/values.join" || true)
if [[ -n "$VALUE_DIFFS" ]]; then
  echo "Values: differences in content or counts (value\tA\tB):"
  echo "$VALUE_DIFFS" | sed 's/^/  /'
  DIFF=1
fi

if [[ $DIFF -eq 0 ]]; then
  echo "MATCH: code counts and value content/counts are identical between files."
  exit 0
else
  echo "DIFFER: one or more mismatches found."
  exit 1
fi

