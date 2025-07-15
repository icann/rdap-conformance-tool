# General Tests

## TIG Section 1.2

Test group: [tigSection_1_2_Validation]  [](){ #id-tigSection_1_2_Validation }

The following steps should be used to test the RDAP protocol section 1.2 of the  RDAP_Technical_Implementation_Guide_2_1:

1. If the scheme of the URI to be tested is "http":
``` json
{
  "code": -20100,
  "value": "<URI>",
  "message": "The URL is HTTP, per section 1.2 of the RDAP_Technical_Implementation_Guide_2_1 shall be HTTPS only."
}
```
2. If the scheme of the URI to be tested is "https", perform the same RDAP query using "http". If the HTTP URI provides a response (other than redirect)::
``` json
{
  "code": -20101,
  "value": "<RDAP response provided over HTTP> + '\n/\n' + <RDAP response provided over HTTPS>",
  "message": "The RDAP response was provided over HTTP, per section 1.2 of the RDAP_Technical_Implementation_Guide_2_1 shall be HTTPS only."
}
```

Note: If redirects are present, the test [tigSection_1_2_Validation] shall be performed  on the URL on the last HTTP redirect.

## TIG Section 1.3

Test group: [tigSection_1_3_Validation]  [](){ #id-tigSection_1_3_Validation }

The following steps should be used to test the RDAP protocol section 1.3 of the  RDAP_Technical_Implementation_Guide_2_1:

1. If the scheme of the URI to be tested is "https", verify that SSLv2 and SSLv3 are not offered by the RDAP server.
``` json
{
  "code": -20200,
  "value": "<URI>",
  "message": "The RDAP server is offering SSLv2 and/or SSLv3."
}
```

Note: the test [tigSection_1_3_Validation] shall be performed on the URL on every HTTP  redirect.

## TIG Section 1.6

Test group: [tigSection_1_6_Validation]  [](){ #id-tigSection_1_6_Validation }

The following steps should be used to test the RDAP protocol section 1.6 of the RDAP_Technical_Implementation_Guide_2_1:

1. The tool shall use the HTTP HEAD method on the URI to be tested. If the HTTP Status code is different from the status code obtained when doing the GET method:
``` json
{
  "code": -20300,
  "value": "<HTTP Status code when using the GET method> + '\n/\n' + <HTTP Status code when using the HEAD method>",
  "message": "The HTTP Status code obtained when using the HEAD method is different from the GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

## TIG Section 1.8

Test group: [tigSection_1_8_Validation]  [](){ #id-tigSection_1_8_Validation }

The following steps should be used to test the RDAP protocol section 1.8 of the  RDAP_Technical_Implementation_Guide_2_1:

1. Obtain the Resource Record for the A QTYPE for the host in the URI. Validate that the status of the DNS response is not NOERROR. Validate that all IPv4 addresses in the RDATA pass IPv4 address validation [ipv4Validation]:
``` json
{
  "code": -20400,
  "value": "<IPv4 addresses>",
  "message": "The RDAP service is not provided over IPv4. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
2. Obtain the Resource Record for the AAAA QTYPE for the host in the URI. Validate that the status of the DNS response is not NOERROR. Validate that all IPv6 addresses in the RDATA pass IPv6 address validation [ipv6Validation]:
``` json
{
  "code": -20401,
  "value": "<IPv6 addresses>",
  "message": "The RDAP service is not provided over IPv6. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

Note: the test [tigSection_1_8_Validation] shall be performed on the URL on every HTTP redirect.

## TIG Section 1.13

Test group: [tigSection_1_13_Validation]  [](){ #id-tigSection_1_13_Validation }

The following steps should be used to test the RDAP protocol section 1.13 of the RDAP_Technical_Implementation_Guide_2_1:

1. Validate that the HTTP header "Access-Control-Allow-Origin: *" is included in the RDAP response.
``` json
{
  "code": -20500,
  "value": "<HTTP headers>",
  "message": "The HTTP header 'Access-Control-Allow-Origin: *' is not included in the HTTP headers. See section 1.13 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

Note: the test [tigSection_1_13_Validation] shall be performed on the URL on every HTTP redirect.

## TIG Section 1.14

Test group: [tigSection_1_14_Validation]  [](){ #id-tigSection_1_14_Validation }

The following steps should be used to test the RDAP protocol section 1.14 of the RDAP_Technical_Implementation_Guide_2_1:

1. Validate that the JSON string value "icann_rdap_technical_implementation_guide_0" is included in the RDAP Conformance data structure.
``` json
{
  "code": -20600,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

## TIG Section 3.3 and 3.4

Test group: [tigSection_3_3_and_3_4_Validation]  [](){ #id-tigSection_3_3_and_3_4_Validation }

The following steps should be used to test the RDAP protocol section 3.3 and 3.4 of the RDAP_Technical_Implementation_Guide_2_1:

1. Validate that at least one links data structure exists within the notices object in the topmost object.
``` json
{
  "code": -20700,
  "value": "<notices data structure>",
  "message": "A links object was not found in the notices object in the topmost object. See section 3.3 and 3.4 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

## TIG Section 4.1

Test group: [tigSection_4_1_Validation]  [](){ #id-tigSection_4_1_Validation }

The following steps should be used to test the RDAP protocol section 4.1 of the TIG:

1. Validate that all the _entities_ in the RDAP Response contain structured address. If a street address has more than one line, it MUST be structured as an array of strings.
``` json
{
  "code": -20800,
  "value": "<entity data structure>",
  "message": "An entity with a non-structured address was found. See section 4.1 of the TIG."
}
```

## TIG Section 7.1 and 7.2

Test group: [tigSection_7_1_and_7_2_Validation]  [](){ #id-tigSection_7_1_and_7_2_Validation }

The following steps should be used to test the RDAP protocol section 7. 1 and 7. 2 of the TIG:

1. Validate that at all the _tel_ properties in the _entities_ in the RDAP Response contain voice or fax as type parameter.
``` json
{
  "code": -20900,
  "value": "<entity data structure>",
  "message": "An entity with a tel property without a voice or fax type was found. See section 7.1 and 7.2 of the TIG."
}
```

