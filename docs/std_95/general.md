# General Tests

## IPv4 address validation 

Test group: [[ipv4Validation]](#id-ipv4Validation){ #id-ipv4Validation } 

The following steps should be used to test that an IPv4 address is valid:

1. Test case [-10100](#id-testCase-10100){ #id-testCase-10100 }: The IPv4 address is syntactically valid in dot-decimal notation.
``` json
{
  "code": -10100,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is not syntactically valid in dot-decimal notation."
}
```
2. Test case [-10101](#id-testCase-10101){ #id-testCase-10101 }: The IPv4 address MUST be part of a prefix categorized as "ALLOCATED" or "LEGACY" in  the "Status" field in the ipv4AddressSpace.
``` json
{
  "code": -10101,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is not included in a prefix categorized as ALLOCATED or LEGACY in the IANA IPv4 Address Space Registry. Dataset: ipv4AddressSpace"
}
```
3. Test case [-10102](#id-testCase-10102){ #id-testCase-10102 }: The IPv4 address MUST NOT be part of the **specialIPv4Addresses**.
``` json
{
  "code": -10102,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is included in the IANA IPv4 Special-Purpose Address Registry. Dataset: specialIPv4Addresses"
}
```

## IPv6 address validation

Test group: [[ipv6Validation]](#id-ipv6Validation){ #id-ipv6Validation }

The following steps should be used to test that an IPv6 address is valid:

1. Test case [-10200](#id-testCase-10200){ #id-testCase-10200 }: The IPv6 address is in canonical textual representation format.
``` json
{
  "code": -10200,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is not syntactically valid."
}
```
2. Test case [-10201](#id-testCase-10201){ #id-testCase-10201 }: The IPv6 address MUST be part of an allocation of type "Global Unicast" in the **ipv6AddressSpace**.
``` json
{
  "code": -10201,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is not included in a prefix categorized as Global Unicast in the Internet Protocol Version 6 Address Space. Dataset: ipv6AddressSpace"
}
```
3. Test case [-10202](#id-testCase-10202){ #id-testCase-10202 }: The IPv6 address MUST NOT be part of the **specialIPv6Addresses**.
``` json
{
  "code": -10202,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is included in the IANA IPv6 Special-Purpose Address Registry. Dataset: specialIPv6Addresses"
}
```


## Domain Name validation 

Test group: [[domainNameValidation]](#id-domainNameValidation){ #id-domainNameValidation }

The following steps should be used to test that a domain name is valid:

1. Test case [-10300](#id-testCase-10300){ #id-testCase-10300 }: The length of each label is between 1 and 63.
``` json
{
  "code": -10300,
  "value": "<domain name>",
  "message": "A DNS label with length not between 1 and 63 was found."
}
```
2. Test case [-10301](#id-testCase-10301){ #id-testCase-10301 }: A maximum total length of 253 characters not including the last ".".
``` json
{
  "code": -10301,
  "value": "<domain name>",
  "message": "A domain name of more than 253 characters was found. "
}
```
3. Test case [-10302](#id-testCase-10302){ #id-testCase-10302 }: At least two labels shall exist in the domain name. See,
RDAP_Technical_Implementation_Guide_2_1 section 1.10.
``` json
{
  "code": -10302,
  "value": "<domain name>",
  "message": "A domain name with less than two labels was found."
}
```
4. Test case [-10303](#id-testCase-10303){ #id-testCase-10303 }: Each label of the domain name is a valid "A-label", "U-label", or "NR-LDH label".
``` json
{
  "code": -10303,
  "value": "<domain name>",
  "message": "A DNS label not being a valid 'A-label', 'U-label', or 'NR-LDH label' was found."
}
```

Note: the latest version of the IANA IDNA Rules and Derived Property Values shall be used. 
See <https://www.iana.org/assignments/idna-tables-11.0.0/idna-tables-11.0.0.xml>.

## Web URI validation

Test group: [[webUriValidation]](#id-webUriValidation){ #id-webUriValidation }

The following steps should be used to test that a Web URI is valid:

1. Test case [-10400](#id-testCase-10400){ #id-testCase-10400 }: The URI shall be syntactically valid according to RFC3986.
``` json
{
  "code": -10400,
  "value": "<URI>",
  "message": "The URI is not syntactically valid according to RFC3986."
}
```
2. Test case [-10401](#id-testCase-10401){ #id-testCase-10401 }: The scheme of the URI shall be "http" or "https".
``` json
{
  "code": -10401,
  "value": "<URI>",
  "message": "The scheme of the URI is not 'http' nor 'https'"
}
```
3. Test case [-10402](#id-testCase-10402){ #id-testCase-10402 }: The host of the URI shall pass the test Domain Name validation [[domainNameValidation]][id-domainNameValidation], IPv4 address validation [[ipv4Validation]][id-ipv4Validation] or IPv6 address validation [[ipv6Validation]][id-ipv6Validation].
``` json
{
  "code": -10402,
  "value": "<URI>",
  "message": "The host does not pass Domain Name validation [domainNameValidation], IPv4 address validation [ipv4Validation] nor IPv6 address validation [ipv6Validation]"
}
```


## Domain label case folding validation 

Test group: [[domainCaseFoldingValidation]](#id-domainCaseFoldingValidation){ #id-domainCaseFoldingValidation }

The following steps should be used to test that an RDAP server is processing label case conversion correctly for domain name lookups:

1. Test case [-10403](#id-testCase-10403){ #id-testCase-10403 }: A subsequent RDAP lookup may be performed in the case of a domain name lookup to validate correct support for case insensitive label matching:
      1. For any "NR-LDH label" or "A-label" present, the RDAP response must match the response of a subsequent request converting any "NR-LDH label" or "A-label" alternating uppercase and lowercase characters (e.g., if the domain is "test.example" the RDAP response must match also for converted domain name "tEsT.ExAmPlE").
      1. For any "U-Label" present, in case that any of the code points support case folding, the u-label should be case-folded for the subsequent request. (e.g., if the domain is "CAFÉ.EXAMPLE" the RDAP response must match also for converted domain name "café.ExAmPlE").
          - In case that the domain name in the query contains all u-labels and none of the labels can be case-folded (i.e., the script or code points do not support case folding) a subsequent query is not required.
          - In case that the domain name in the query contains all u-labels and the resulting domain name to query after case-folding is the same as the original, a subsequent query is not required.

``` json
{
  "code": -10403,
  "value": "<converted domain name>",
  "message": "RDAP responses do not match when handling domain label case folding."
}
```
