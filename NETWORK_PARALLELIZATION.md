# Aggressive Network Parallelization

## Overview
The RDAP Conformance Tool supports aggressive network parallelization to significantly reduce validation time by running network operations concurrently. This feature is **opt-in** and requires explicit enablement.

## Usage

### Enable Aggressive Network Parallelization
```bash
java -Drdap.parallel.network=true -jar tool/target/rdapct-2.0.1.jar -c tool/bin/rdapct_config.json -v
```

### Default Sequential Behavior
```bash
java -jar tool/target/rdapct-2.0.1.jar -c tool/bin/rdapct_config.json -v
```

## Aggressive Settings When Enabled

When `-Drdap.parallel.network=true` is set, the tool uses:

- **8 concurrent network connections** (vs 1 sequential)
- **50ms delay between operations** (vs sequential blocking)
- **Parallel execution** of all network validations:
  - DNS queries (TigValidation1Dot8)
  - HTTP HEAD requests (TigValidation1Dot6)
  - SSL validation (TigValidation1Dot2, TigValidation1Dot5_2024)
  - Help endpoint queries (ResponseValidationHelp_2024)
  - Invalid domain queries (ResponseValidationDomainInvalid_2024)
  - Redirect testing (ResponseValidationTestInvalidRedirect_2024)

## Performance Impact

**Expected speedup**: 2-4x faster for network-heavy validations

**Before (Sequential)**:
- Each network operation waits for the previous to complete
- Total time = sum of all network latencies
- Susceptible to individual slow responses

**After (Parallel)**:
- Multiple network operations run simultaneously
- Total time = max of individual network latencies
- Resilient to individual slow responses

## Rate Limiting Compliance

The tool **automatically handles 429 responses** and will back off when servers rate limit. The existing rate limiting code in HTTP requests is preserved and will:

- Detect 429 status codes
- Apply appropriate backoff delays
- Retry requests as configured

The parallel execution works **within** these existing rate limiting constraints.

## Important Notes

1. **Opt-in Only**: Must explicitly enable with `-Drdap.parallel.network=true`
2. **Respects 429s**: Existing rate limiting code is untouched
3. **Aggressive by Design**: When enabled, uses 8 concurrent connections for maximum speed
4. **IPv4/IPv6 Compatible**: Maintains full stack switching functionality
5. **Safe Fallback**: Defaults to sequential behavior if not enabled

## Recommendations

- **Use for performance testing**: When you need fastest possible validation times
- **Use for batch processing**: When validating many domains
- **Don't use against rate-limited servers**: If you know the target server is strict about rate limiting
- **Monitor server responses**: Watch for 429s and adjust usage accordingly

## Technical Details

The implementation:
- Separates network validations from non-network validations
- Executes non-network validations sequentially (fast)
- Executes network validations in parallel using a thread pool
- Maintains proper error handling and result aggregation
- Preserves all existing functionality when disabled