# STD 95 Tests

## General tests

### IPv4 address validation 

Test group: [ipv4Validation]

The following steps should be used to test that an IPv4 address is valid:

1. The IPv4 address is syntactically valid in dot-decimal notation.
``` json
{
  "code": -10100 ,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is not syntactically valid in dot-decimal notation."
}
```
2. The IPv4 address MUST be part of a prefix categorized as "ALLOCATED" or "LEGACY" in  the "Status" field in the ipv4AddressSpace.
``` json
{
  "code": -10101 ,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is not included in a prefix categorized as ALLOCATED or LEGACY in the IANA IPv4 Address Space Registry. Dataset: ipv4AddressSpace"
}
```
3. The IPv4 address MUST NOT be part of the **specialIPv4Addresses**.
``` json
{
  "code": -10102 ,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is included in the IANA IPv4 Special-Purpose Address Registry. Dataset: specialIPv4Addresses"
}
```

### IPv6 address validation

Test group: [ipv6Validation]

The following steps should be used to test that an IPv6 address is valid:

1. The IPv6 address is in canonical textual representation format.
``` json
{
  "code": -10200 ,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is not syntactically valid."
}
```
2. The IPv6 address MUST be part of an allocation of type "Global Unicast" in the **ipv6AddressSpace**.
``` json
{
  "code": -10201 ,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is not included in a prefix categorized as Global Unicast in the Internet Protocol Version 6 Address Space. Dataset: ipv6AddressSpace"
}
```
3. The IPv6 address MUST NOT be part of the **specialIPv6Addresses**.
``` json
{
  "code": -10202 ,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is included in the IANA IPv6 Special-Purpose Address Registry. Dataset: specialIPv6Addresses"
}
```


### Domain Name validation 

Test group: [domainNameValidation]

The following steps should be used to test that a domain name is valid:

1. The length of each label is between 1 and 63.
``` json
{
  "code": -10300 ,
  "value": "<domain name>",
  "message": "A DNS label with length not between 1 and 63 was found."
}
```
2. A maximum total length of 253 characters not including the last ".".
``` json
{
  "code": -10301 ,
  "value": "<domain name>",
  "message": "A domain name of more than 253 characters was found. "
}
```
3. At least two labels shall exist in the domain name. See,
RDAP_Technical_Implementation_Guide_2_1 section 1.10.
``` json
{
  "code": -10302 ,
  "value": "<domain name>",
  "message": "A domain name with less than two labels was found. See
}
```
4. Each label of the domain name is a valid "A-label", "U-label", or "NR-LDH label".
``` json
{
  "code": -10303 ,
  "value": "<domain name>",
  "message": "A DNS label not being a valid 'A-label', 'U-label', or 'NR-LDH label' was found."
}
```

Note: the latest version of the IANA IDNA Rules and Derived Property Values shall be used. 
See <https://www.iana.org/assignments/idna-tables-11.0.0/idna-tables-11.0.0.xml>.

### Web URI validation

Test group: [webUriValidation]

The following steps should be used to test that a Web URI is valid:

1. The URI shall be syntactically valid according to RFC3986.
``` json
{
  "code": -10400 ,
  "value": "<URI>",
  "message": "The URI is not syntactically valid according to RFC3986."
}
```
2. The scheme of the URI shall be "http" or "https".
``` json
{
  "code": -10401 ,
  "value": "<URI>",
  "message": "The scheme of the URI is not 'http' nor 'https'".
}
```
3. The host of the URI shall pass the test Domain Name validation [domainNameValidation], IPv4 address validation [ipv4Validation] or IPv6 address validation [ipv6Validation].
``` json
{
  "code": -10402 ,
  "value": "<URI>",
  "message": "The host does not pass Domain Name validation [domainNameValidation], IPv4 address validation [ipv4Validation] nor IPv6 address validation [ipv6Validation]".
}
```


### Domain label case folding validation 

Test group: [domainCaseFoldingValidation]

The following steps should be used to test that an RDAP server is processing label case conversion correctly for domain name lookups:

1. A subsequent RDAP lookup may be performed in the case of a domain name lookup to validate correct support for case insensitive label matching:
      1. For any "NR-LDH label" or "A-label" present, the RDAP response must match the response of a subsequent request converting any "NR-LDH label" or "A- label" alternating uppercase and lowercase characters (e.g., if the domain is "test.example" the RDAP response must match also for converted domain name "tEsT.ExAmPlE").
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

## Standard RDAP Common Data Structures Validations

### RDAP Conformance validation 

Test group: [stdRdapConformanceValidation]

The following steps should be used to test that an RDAP Conformance data structure is valid:

1. The _RDAP Conformance_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10500 ,
  "value": "<rdapConformance structure>",
  "message": "The RDAP Conformance structure is not syntactically valid."
}
```
2. Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -10501 ,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
3. Each of the JSON string values in the JSON array, with the exception of "rdap_level_0", shall be included as an Extension Identifier in **RDAPExtensions**.
``` json
{
  "code": -10502 ,
  "value": "<JSON string>",
  "message": "The JSON string is not included as an Extension Identifier in RDAPExtensions."
}
```
4. The JSON string value "rdap_level_0" is not included in the _RDAP Conformance_ data structure.
``` json
{
  "code": -10503 ,
  "value": "<rdapConformance>",
  "message": "The RDAP Conformance data structure does not include rdap_level_0."
}
```


### Links validation

Test group: [stdRdapLinksValidation]

The following steps should be used to test that a links data structure is valid:

1. The _links_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10600,
  "value": "<links structure>",
  "message": "The links structure is not syntactically valid."
}
```
2. For every object (i.e. link) of the JSON array, verify that the _link_ structure complies with:
    1. The name of every name/value pair shall be value , rel , href , hreflang , title , media or type.
``` json
{
  "code": - 10601,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: value, rel, href, hreflang, title, media or type."
}
```
    2. The JSON name/value pairs of _rel_ , _href_ , _hreflang_ , _title_ , _media_ and _type_ shall appear only once.
``` json
{
  "code": -10602,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
    3. If the JSON name _media_ exists, the allowed values are: screen, tty, tv, projection, handheld, print, braille, embossed, speech, and all.
``` json
{
  "code": -10603,
  "value": "<name/value pair>",
  "message": "The value for the JSON name media is not of: screen, tty, tv, projection, handheld, print, braille, embossed, speech, or all."
}
```
    4. If the JSON name _rel_ exists, the value shall be included as a "Relation Name" in linkRelations.
``` json
{
  "code": -10604,
  "value": "<name/value pair>",
  "message": "The JSON value is not included as a Relation Name in linkRelations."
}
```
    5. If the JSON name _type_ exists, the value shall be included as a "Type Name/Subtype Name" as registered in mediaTypes.
``` json
{
  "code": -10605,
  "value": "<name/value pair>",
  "message": "The JSON value is not included as a Name in mediaTypes."
}
```
    6. If the JSON name title exists, the value shall be a JSON string data type.
``` json
{
  "code": -10606,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    7. If the JSON name hreflang exists, the value shall be a JSON string data type or a valid JSON array where every value is a JSON string data type.
``` json
{
  "code": -10607,
  "value": "<name/value pair>",
  "message": "The value for the JSON name hreflang is not a JSON string data type or a valid JSON array where every value is a JSON string data type."
}
```
    8. If the JSON name _hreflang_ exists, every one of the _JSON string_ data values shall conform to the Language-Tag syntax.
``` json
{
  "code": -10608,
  "value": "<name/value pair>",
  "message": "The value of the JSON string data in the hreflang does not conform to Language-Tag syntax."
}
```
    9. If the JSON name _value_ exists, the value shall pass the test Web URI validation [webUriValidation] defined in this document.
``` json
{
  "code": -10609,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Web URI validation [webUriValidation]."
}
```
    10. The JSON name href shall exist.
``` json
{
  "code": -10610,
  "value": "<links structure>",
  "message": "The href element does not exist."
}
```
    11. For the JSON name href , the value shall pass the test Web URI validation [webUriValidation] defined in this document.
``` json
{
  "code": -10611,
  "value": "<name/value pair>",
  "message": "The value for the JSON name href does not pass Web URI validation [webUriValidation]."
}
```

### Notices and Remarks Validation 

Test group: [stdRdapNoticesRemarksValidation]

The following steps should be used to test that a notices or remarks data structure is  valid:

1. The _notices_ or _remarks_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10700,
  "value": "<notices or remarks structure>",
  "message": "The notices or remarks structure is not syntactically valid."
}
```
2. For every object of the JSON array, verify that the structure complies with:
    1. The name of every name/value pair shall be title , type , description or links.
``` json
{
  "code": -10701,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: title, type, description or links."
}
```
    2. The JSON name/values of title , type , description and links shall exist only once.
``` json
{
  "code": -10702,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
    3. If the JSON name title exists, the value shall be a JSON string data type.
``` json
{
  "code": -10703,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    4. If the JSON name links exists, the value shall pass the test Links validation [stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -10704,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
    5. If the JSON name type exists, the value shall be a JSON string data type.
``` json
{
  "code": -10705,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    6. If the JSON name type exists, the value shall be included in the RDAPJSONValues with Type="notice and remark type".
``` json
{
  "code": -10706,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='notice and remark type' in the RDAPJSONValues dataset."
}
```
    7. The JSON name description shall exist.
``` json
{
  "code": -10707,
  "value": "<notices or remarks structure>",
  "message": "The description element does not exist."
}
```
    8. The description data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10708,
  "value": "<description structure>",
  "message": "The description structure is not syntactically valid."
}
```
    9. Every value of the JSON array of the _description_ data structure shall be a JSON string data type.
``` json
{
  "code": -10709,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```

### Language Identifier Validation 

Test group: [stdRdapLanguageIdentifierValidation]

The following steps should be used to test that a lang data structure is valid:

1. For the JSON name _lang_ , the value shall conform to the Language-Tag syntax.
``` json 
{
  "code": -10800,
  "value": "<name/value pair>",
  "message": "The value of the JSON string data in lang does not conform to Language-Tag syntax."
}
```

### Events Validation

Test group: [stdRdapEventsValidation]

The following steps should be used to test that a events data structure is valid:

1. The _events_ data structure must be a syntactically valid JSON array.
``` json 
{
  "code": -10900,
  "value": "<events structure>",
  "message": "The events structure is not syntactically valid."
}
```
2. For every object of the JSON array, verify that the structure complies with:
    1. The name of every name/value pair shall be any of: eventAction , eventActor , eventDate or links.
``` json
{
  "code": -10901,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: eventAction, eventActor, eventDate or links."
}
```
    2. The JSON name/value pairs of _eventAction_ , _eventActor_ , _eventDate_ and _links_ shall exist only once.
``` json
{
  "code": -10902,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once. "
}
```
    3. The JSON name eventAction shall exist.
``` json
{
  "code": -10903,
  "value": "<links structure>",
  "message": "The eventAction element does not exist."
}
```
    4. For the JSON name eventAction , the value shall be a JSON string data type.
``` json
{
  "code": -10904,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    5. For the JSON name _eventAction_ , the value shall be included in the RDAPJSONValues with Type="event action".
``` json
{
  "code": -10905,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type="event action" in the RDAPJSONValues data set.
}
```
    6. The JSON name eventDate shall exist.
``` json
{
  "code": -10906,
  "value": "<links structure>",
  "message": "The eventDate element does not exist."
}
```
    7. For the JSON name eventDate , the value shall be a JSON string data type.
``` json
{
  "code": -10907,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    8. For the JSON name eventDate , the value shall be syntactically valid time and date according to RFC3339.
``` json
{
"code": -10908,
"value": "<name/value pair>",
"message": "The JSON value shall be a syntactically valid time and date according to RFC3339."
}
```
    9. If the JSON name eventActor exists, the value shall be a JSON string data type.
``` json
{
  "code": -10909,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    10. If the JSON name links exists, the JSON name eventActor shall also exist.
``` json
{
  "code": -10910,
  "value": "<events structure>",
  "message": "A links structure was found but an eventActor was not."
}
```
    11. If the JSON name _links_ exists, the value shall pass the test Links validation [stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -10911,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
3. An _eventAction_ value shall only appears once within the events array.
``` json
{
  "code": -10912,
  "value": "<events structure>",
  "message": "An eventAction value exists more than once within the events array."
}
```

### Status validation 

Test group: [stdRdapStatusValidation]

The following steps should be used to test that a status data structure is valid:

1. The _status_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11000,
  "value": "<status structure>",
  "message": "The status structure is not syntactically valid."
}
```
2. Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11001,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
3. Each of the JSON string values in the JSON array shall be included in the **RDAPJSONValues** with Type="status".
``` json
{
  "code": - 11002 ,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='status'."
}
```

### Port 43 WHOIS Server 

Test group: [stdRdapPort43WhoisServerValidation]

The following steps should be used to test that a port43 data structure is valid:

1. For the JSON name _port43_ , the value shall pass the test [IPv4Validation], [IPv6Validation] or [DomainNameValidation] defined in this document.
``` json
{
"code": -11100,
"value": "<name/value pair>",
"message": "The value for the JSON name port43 does not pass [IPv4Validation], [IPv6Validation] or [DomainNameValidation]."
}
```

### Public IDs validation 

Test group: [stdRdapPublicIdsValidation]

The following steps should be used to test that a publicIds data structure is valid:

1. The _publicIds_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11200,
  "value": "<publicIds structure>",
  "message": "The publicIds structure is not syntactically valid."
}
```
2. For every object of the JSON array, verify that the structure complies with:
    1. The name of every name/value pairs shall be type or identifier.
``` json
{
  "code": -11201,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: type or identifier."
}
```
    2. The JSON name/values of type or identifier shall appear only once.
``` json
{
  "code": -11202,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a domain structure was found more than once."
}
```
    3. The JSON name/values of type and identifier shall appear.
``` json
{
  "code": -11203,
  "value": "<publicIds structure>",
  "message": "The following name/values shall exist: type or identifier."
}
```
    4. For the JSON name type , the value shall be a JSON string data type.
``` json
{
  "code": -11204,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    5. For the JSON name identifier , the value shall be a JSON string data type.
``` json
{
  "code": -11205,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```

### asEventActor Validation 

Test group: [stdRdapAsEventActorValidation]

The following steps should be used to test that an asEventActor data structure is valid:

1. The _asEventActor_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11300,
  "value": "<asEventActor structure>",
  "message": "The asEventActor structure is not syntactically valid."
}
```
2. The _asEventActor_ data structure must be embedded within an entity object, and the entity object must be embedded within another object.
``` json
{
  "code": -11301,
  "value": "<asEventActor structure>",
  "message": "The asEventActor structure is not embedded within an entity object and the entity object is not embedded within another object."
}
```
3. For every object of the JSON array, verify that the structure complies with:
    1. The name of every name/value pair shall be any of: eventAction or eventDate.
``` json
{
  "code": -11302,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: eventAction and eventDate."
}
```
    2. The JSON name/values of eventAction or eventDate shall appear only once.
``` json
{
  "code": -11303,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
    3. The JSON name eventAction shall exist.
``` json
{
"code": -11304,
"value": "<links structure>",
"message": "The eventAction element does not exist."
}
```
    4. For the JSON name eventAction , the value shall be a JSON string data type.
``` json
{
"code": -11305,
"value": "<name/value pair>",
"message": "The JSON value is not a string."
}
```
    5. For the JSON name eventAction , the value shall be included in the RDAPJSONValues with Type="event action".
``` json
{
"code": -11306,
"value": "<JSON string>",
"message": "The JSON string is not included as a Value with Type='event action' in the RDAPJSONValues dataset."
}
```
    6. The JSON name eventDate shall exist.
``` json
{
  "code": -11307,
  "value": "<links structure>",
  "message": "The eventDate element does not exist."
}
```
    7. For the JSON name eventDate, the value shall be a JSON string data type.
``` json
{
  "code": -11308,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    8. For the JSON name _eventDate_ , the value shall be syntactically valid time and date according to RFC3339.
``` json
{
  "code": -11309,
  "value": "<name/value pair>",
  "message": "The JSON value shall be a syntactically valid time and date according to RFC3339."
}
```
4. An _eventAction_ shall only appear once within the events array.
``` json
{
  "code": -11310,
  "value": "<events structure>",
  "message": "An _eventAction_ exists more than once within the events array."
}
```

### IP Addresses Validation 

Test group: [stdRdapIpAddressesValidation]

The following steps should be used to test that an ipAddresses data structure is valid:

1. The _ipAddresses_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -11400,
  "value": "<ipAddresses structure>",
  "message": "The ipAddresses structure is not syntactically valid."
}
```
2. The name of every name/value pair shall be any of: _v4_ or _v6_.
``` json
{
  "code": -11401,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: v4 or v6."
}
```
3. The JSON name/values of _v4_ and _v6_ shall appear only once.
``` json
{
  "code": -11402,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of an ipAddresses structure was found more than once."
}
```
4. One _v4_ name/values or one _v6_ name/value shall appear.
``` json
{
  "code": -11403,
  "value": "<name/value pair>",
  "message": "v4 nor v6 name/value pair exists."
}
```
5. If the JSON name _v4_ exists, the value shall pass the following:
    1. The v4 data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11404,
  "value": "<v4 structure>",
  "message": "The v4 structure is not syntactically valid."
}
```
    2. For every object of the JSON array, verify that the structure complies with:
        1. Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11405,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
        2. The IPv4 address is syntactically valid in dot-decimal notation.
``` json
{
  "code": -11406,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is not syntactically valid in dot-decimal notation."
}
```
6. If the JSON name _v6_ exists, the value shall pass the following:
    1. The v6 data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11407,
  "value": "<v6 structure>",
  "message": "The v6 structure is not syntactically valid."
}
```
    2. For every object of the JSON array, verify that the structure complies with:
        1. Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11408,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
        2. The IPv6 address is syntactically valid.
``` json
{
  "code": -11409,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is not syntactically valid."
}
```

### Variants validation [stdRdapVariantsValidation]

The following steps should be used to test that a variants data structure is valid:

1. The _variants_ data structure must be a syntactically valid JSON array.
    {
    "code": - 11500 ,
    "value": "<variants structure>",
    "message": "The variants structure is not syntactically valid."
    }
2. For every object of the JSON array, verify that the structure complies with:

2.1. The name of every name/value pair shall be relation , idnTable or variantNames.
```
{
"code": - 11501 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair is not of: relation, idnTable
or variantNames."
}
```
2.2. The JSON name/value pairs of relation , idnTable and variantNames shall appear
only once.
```
{
"code": - 11502 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a link structure was found
more than once."
}
```
2.3. The RDAP relation data structure must be a syntactically valid JSON array.
```
{
"code": - 11503 ,
"value": "<relation structure>",
"message": "The RDAP Conformance structure is not syntactically valid."
}
```
2.4. For every object of the JSON array, verify that the structure complies with:

2.4.1. Every value of the JSON array shall be a JSON string data type.
```
{
"code": - 11504 ,
"value": "<JSON value>",
"message": "The JSON value is not a string."
}
```
2.4.2. Each of the JSON string values in the JSON array shall be included in the
RDAPJSONValues with Type="domain variant relation".
```
{
"code": - 11505 ,
"value": "<JSON string>",
"message": "The JSON string is not included as a Value with
Type="domain variant relation "."
}
```
2.5. If the JSON name idnTable exists, the value shall be a JSON string data type.
```
{
"code": - 11506 ,
"value": "<name/value pair>",
"message": "The JSON value is not a string."
}
```
2.6. The variantNames data structure must be a syntactically valid JSON array.
```
{
"code": - 11507 ,
"value": "<variantNames structure>",
"message": "The variantNames structure is not syntactically valid."
}
```
2.7. For every object of the JSON array, verify that the structure complies with:
2.7.1. The name of every name/value pair shall be any of: ldhName or
unicodeName

```
{
"code": - 11508 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair is not of: ldhName or
unicodeName."
}
```
2.7.2. The JSON name/value pairs of ldhName or unicodeName shall exist only
once.
```
{
"code": - 11509 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a link structure was
found more than once."
}
```
2.7.3. If the JSON name title ldhName exists, the value shall pass the test LDH
name [stdRdapLdhNameValidation] defined in this document.
```
{
"code": - 11510 ,
"value": "<name/value pair>",
"message": " The value for the JSON name value does not pass LDH name
[stdRdapLdhNameValidation]."
}
```
2.7.4. If the JSON name unicodeName exists, the value shall pass the test Unicode
name [stdRdapUnicodeNameValidation] defined in this document.
```
{
"code": - 11511 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Unicode
name [stdRdapUnicodeNameValidation]."
}
```

### Unicode name [stdRdapUnicodeNameValidation]

The following steps should be used to test that a unicodeName is valid:

1. The length of each label is between 1 and 63.
    {
    "code": - 11600 ,
    "value": "<domain name>",
    "message": "A DNS label with length not between 1 and 63 was found."
    }
2. A maximum total length of 253 characters not including the last ".".
    {
    "code": - 11601 ,
    "value": "<domain name>",
    "message": "A domain name of more than 253 characters was found."
    }
3. At least two labels shall exist in the domain name. See,
    RDAP_Technical_Implementation_Guide_2_1 section 1.10.
    {
    "code": - 11602 ,
    "value": "<domain name>",
    "message": "A domain name with less than two labels was found. See RDAP_Technical_Implementation_Guide_2_1 section 1.10"
}

4. Each label of the domain name is a valid "U-label or "NR-LDH label".
    {
    "code": - 11603 ,
    "value": "<domain name>",
    "message": "A label not being a valid "U-label" or "NR-LDH label" was
    found."
    }

Note: the latest version of the IANA IDNA Rules and Derived Property Values shall be used. See, https://www.iana.org/assignments/idna-tables-11.0.0/idna-tables-11.0.0.xml

Note: some legacy gTLDs may fail this test, because they have a few domain name registrations that comply with IDNA2003 but not IDNA2018. Such names are not recommended to be used when testing an RDAP response with this tool.


### LDH name [stdRdapLdhNameValidation]

The following steps should be used to test that a ldhName is valid:

1. The length of each label is between 1 and 63.
    {
    "code": - 11700 ,
    "value": "<domain name>",
    "message": "A DNS label with length not between 1 and 63 was found."
    }
2. A maximum total length of 253 characters not including the last ".".
    {
    "code": - 11701 ,
    "value": "<domain name>",
    "message": "A domain name of more than 253 characters was found."
    }
3. At least two labels shall exist in the domain name. See,
    RDAP_Technical_Implementation_Guide_2_1 section 1.10.
    {
    "code": - 11702 ,
    "value": "<domain name>",
    "message": "A domain name with less than two labels was found. See RDAP_Technical_Implementation_Guide_2_1 section 1.10"
}

4. Each label of the domain name is a valid "A-label or "NR-LDH label".
    {
    "code": - 11703 ,
    "value": "<domain name>",
    "message": "A label not being a valid "A-label" or "NR-LDH label" was
    found."
    }

Note: the latest version of the IANA IDNA Rules and Derived Property Values
shall be used. See, https://www.iana.org/assignments/idna-tables-11.0.0/idna-tables-11.0.0.xml

Note: some legacy gTLDs may fail this test, because they have a few domain
name registrations that comply with IDNA2003 but not IDNA2018. Such names
are not recommended to be used when testing an RDAP response with this tool.


### Roles validation [stdRdapRolesValidation]

The following steps should be used to test that a roles data structure is valid:

1. The _roles_ data structure must be a syntactically valid JSON array.
    {
    "code": - 11800 ,
    "value": "<roles structure>",
    "message": "The roles structure is not syntactically valid."
    }
2. Every value of the JSON array shall be a JSON string data type.
    {
    "code": - 11801 ,
    "value": "<JSON value>",
    "message": "The JSON value is not a string."
    }
3. Each of the JSON string values in the JSON array shall be included in the
    **RDAPJSONValues** with Type="role".
    {
    "code": - 11802 ,
    "value": "<JSON string>",
    "message": "The JSON string is not included as a Value with Type="role"."
    }
4. The role value shall only appear once in the JSON array.
    {
    "code": - 11803 ,
    "value": "<roles structure>",
    "message": "A role value appeared more than once."
    }


### Entities validation [stdRdapEntitiesValidation]

The following steps should be used to test that an entities data structure is valid:

1. The _entities_ data structure must be a syntactically valid JSON array.
    {
    "code": - 11900 ,
    "value": "<entities structure>",
    "message": "The entities structure is not syntactically valid."
    }
2. Every value of the JSON array shall pass the test Entity lookup validation
    [stdRdapEntityLookupValidation] defined in this document.
    {
    "code": - 11901 ,
    "value": "<JSON value>",
    "message": "The JSON value does not pass Entity lookup validation
    [stdRdapEntityLookupValidation]."
    }


### Secure DNS validation [stdRdapSecureDnsValidation]

The following steps should be used to test that a secureDNS data structure is valid:

1. The _secureDNS_ data structure must be a syntactically valid JSON object.
    {
    "code": - 12000 ,
    "value": "<secureDNS structure>",
    "message": "The domain structure is not syntactically valid."
    }
2. The name of every name/value pairs shall be _zoneSigned_ , _delegationSigned_ , _maxSigLife_ ,
    _dsData_ or _keyData_.
    {
    "code": - 12001 ,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair is not of: zoneSigned,
    delegationSigned, maxSigLife, dsData or keyData."
    }
3. The JSON name/values of _zoneSigned_ , _delegationSigned_ , _maxSigLife_ , _dsData_ and
keyData shall appear only once.

```
{
"code": - 12002 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a domain structure was
found more than once."
}
```
4. If the JSON name _zoneSigned_ appears, the value shall be a JSON boolean data type.
    {
    "code": - 12003 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a boolean."
    }

<!-- - 12004 -->

5. If the JSON name _delegationSigned_ appears, the value shall be a JSON boolean data
    type.
    {
    "code": - 12005 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a boolean."
    }
6. If the JSON name _maxSigLife_ exists, the value shall be a JSON number data type
    between 1 and 2147483647.
    {
    "code": - 12006 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a number between 1 and 2147483647."
    }


<!-- - 12007 -->

7. If the JSON name _dsData_ appears, the value shall pass the following:

```
7.1. The dsData data structure must be a syntactically valid array of JSON objects.
{
"code": - 12008 ,
"value": "<dsData structure>",
"message": "The dsData structure is not syntactically valid."
}
```
7.2. The name of every name/value pair shall be any of: _keyTag_ , _algorithm_ , _digest_ ,
digestType , events or links.

```
{
"code": - 12009 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair is not of: keyTag, algorithm,
digest, digestType, events or links."
}
```
7.3. The JSON name/values of keyTag , algorithm , digest , digestType , events or links
shall appear only once.
```
{
"code": - 12010 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a dsData structure was
found more than once."
}
```
7.4. The JSON name/values of keyTag , algorithm , digest and digestType shall appear.
```
{
"code": - 12011 ,
"value": "<dsData structure>",
"message": "The following name/values shall exist: keyTag, algorithm,
digest and digestType."
}
```
7.5. For the JSON name _keyTag_ , the value shall be a JSON number data type between 1
and 65535.

```
{
"code": - 12012 ,
"value": "<name/value pair>",
"message": "The JSON value is not a number between 1 and 65535."
}
```
7.6. For the JSON name _algorithm_ , the value shall be a JSON number listed with Zone
Signing=Y in dnsSecAlgNumbers. The values 253 and 254 are not valid for this test.

```
{
"code": - 12013 ,
"value": "<name/value pair>",
"message": "The JSON value is not listed with Zone Signing=Y in
dnsSecAlgNumbers, or it's 253 or 254."
}
```
7.7. For the JSON name digest , the value shall be a JSON string of case-insensitive
hexadecimal digits. Whitespace is allowed within the hexadecimal text.

```
{
"code": - 12014 ,
"value": "<name/value pair>",
"message": "The JSON value is not a string of case-insensitive hexadecimal
digits. Whitespace is allowed within the hexadecimal test."
}
```
7.8. For the JSON name _digestType_ , the value shall be a JSON number assigned in
dsRrTypes.

```
{
"code": - 12015 ,
"value": "<name/value pair>",
"message": "The JSON value is not assigned in dsRrTypes."
}
```
7.9. If the JSON name _events_ exists, the value shall pass the test Events Validation
[stdRdapEventsValidation] defined in this document.

```
{
"code": - 12016 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Events
Validation [stdRdapEventsValidation]."
}
```
7.10. If the JSON name links exists, the value shall pass the test Links validation
[stdRdapLinksValidation] defined in this document.
```
{
"code": - 12017 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Links
validation [stdRdapLinksValidation]."
}
```
8. If the JSON name _keyData_ exists, the value shall pass the following:
8.1. The keyData data structure must be a syntactically valid array of JSON objects.

```
{
"code": - 12018 ,
"value": "<keyData structure>",
"message": "The keyData structure is not syntactically valid."
}
```
8.2. The name of every name/value pair shall be _flags_ , _protocol_ , _publicKey_ , _algorithm_ ,
events or links.

```
{
"code": - 12019 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair is not of: flags, protocol,
publicKey, algorithm, events or links."
}
```

8.3. The JSON name/values of _flags_ , _protocol_ , _publicKey_ , _algorithm_ , _events_ or _links_ shall

appear only once.
{
"code": - 12020 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a keyData structure was
found more than once."
}

8.4. The JSON name/values of _flags_ , _protocol_ , _publicKey_ and _algorithm_ shall appear.
{
"code": - 12021 ,
"value": "<dsData structure>",
"message": "The following name/values shall exist: flags, protocol,
publicKey and algorithm."
}

8.5. For the JSON name _flags_ , the value shall be a JSON number data type with values:

256 or 257.
{
"code": - 12022 ,
"value": "<name/value pair>",
"message": "The JSON value is not 256 or 257."
}

8.6. For the JSON name _protocol_ , the value shall be a JSON number data type with

value: 3.
{
"code": - 12023 ,
"value": "<name/value pair>",
"message": "The JSON value is not 3."
}

8.7. For the JSON name _publicKey_ , the value shall be a JSON string, and the key is

represented as a Base64. Whitespace is allowed within the text.
{
"code": - 12024 ,
"value": "<name/value pair>",
"message": "The JSON value is not a string of case-insensitive hexadecimal
digits. Whitespace is allowed within the hexadecimal text."
}

8.8. For the JSON name _algorithm_ , the value shall be a JSON number listed with Zone

Signing=Y in **dnsSecAlgNumbers**. The values 253 and 254 are not valid for this test.
{
"code": - 12025 ,
"value": "<name/value pair>",
"message": "The JSON value is not listed with Zone Signing=Y in
dnsSecAlgNumbers, or it's 253 or 254."
}

8.9. If the JSON name events exists, the value shall pass the test Events Validation
[stdRdapEventsValidation] defined in this document.

```
{
"code": - 12026 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Events
Validation [stdRdapEventsValidation]."
}
```
8.10. If the JSON name _links_ exists, the value shall pass the test Links validation

```
[stdRdapLinksValidation] defined in this document.
{
"code": - 12027 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Links
validation [stdRdapLinksValidation]."
}
```

### Error Response Body [stdRdapErrorResponseBodyValidation]

The following steps should be used to test that an error data structure is valid:

1. The _error_ data structure must be a syntactically valid JSON object.
    {
    "code": - 12100 ,
    "value": "<error structure>",
    "message": "The error structure is not syntactically valid."
    }
2. At least the following name/value pairs shall appear: _errorCode_ , _title_ and _description_.
    {
    "code": - 12101 ,
    "value": "<name/value pair>",
    "message": "At least the following name/value pairs shall exist:
    errorCode, title and description."
    }
3. The JSON name/values of _errorCode_ , _title_ , and _description_ shall appear only once.
    {
    "code": - 12102 ,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair of an error structure was
    found more than once."
    }
4. For the JSON name _errorCode_ , the value shall be a JSON number data type.
    {
    "code": - 12103 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a number."
    }
5. For the JSON name _title_ , the value shall be a JSON string data type.
    {
    "code": - 12104 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a string."
    }
6. The _description_ data structure must be a syntactically valid JSON array.
    {
    "code": - 12105 ,
    "value": "<description structure>",
    "message": "The description structure is not syntactically valid."
    }
7. Every value of the JSON array of the _description_ data structure shall be a JSON string
    data type.
    {
    "code": - 12106 ,
    "value": "<JSON value>",
    "message": "The JSON value is not a string."
    }



## Standard RDAP Object Classes Validations

### Domain Lookup Validation [stdRdapDomainLookupValidation]

The following steps should be used to test that a domain data structure is valid:

1. The _domain_ data structure must be a syntactically valid JSON object.
    {
    "code": - 12200 ,
    "value": "<domain structure>",
    "message": "The domain structure is not syntactically valid."
    }
2. The name of every name/value pairs shall be any of: _objectClassName_ , _handle_ ,
    _ldhName_ , _unicodeName_ , _variants_ , _nameservers_ , _secureDNS_ , _entities_ , _status_ , _publicIds_ ,
remarks , links , port43 , events , notices or rdapConformance.
```
{
"code": - 12201 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair is not of: objectClassName, handle,
ldhName, unicodeName, variants, nameservers, secureDNS, entities, status,
publicIds, remarks, links, port43, events, notices or rdapConformance."
}
```
3. The JSON name/values of _objectClassName_ , _handle_ , _ldhName_ , _unicodeName_ , _variants_ ,
nameservers , secureDNS , entities , status , publicIds , remarks , links , port43 , events ,
notices or rdapConformance shall appear only once.

```
{
"code": - 12202 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a domain structure was
found more than once."
}
```
4. For the JSON name _objectClassName_ , the value shall be "domain".
    {
    "code": - 12203 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not "domain"."
    }
5. If the JSON name _handle_ exists, the value shall be a JSON string data type.
    {
    "code": - 12204 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a string."
    }
6. If the JSON name _ldhName_ , the value shall pass the test LDH name
    [stdRdapLdhNameValidation] defined in this document.
    {
    "code": - 12205 ,
    "value": "<name/value pair>",
    "message": " The value for the JSON name value does not pass LDH name
    [stdRdapLdhNameValidation]."
    }


7. If the JSON name _unicodeName_ exists, the value shall pass the test Unicode name

[stdRdapUnicodeNameValidation] defined in this document.
```
{
"code": - 12206 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Unicode name
[stdRdapUnicodeNameValidation]."
}
```
8. If the JSON name _variants_ exists, the value shall pass the test Variants validation
[stdRdapVariantsValidation] defined in this document.

```
{
"code": - 12207 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Variants
validation [stdRdapVariantsValidation]."
}
```
9. If the JSON name _nameservers_ exists, the value shall pass the test Nameserver lookup

validation [stdRdapNameserverLookupValidation] defined in this document.
```
{
"code": - 12208 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Nameserver
lookup validation [stdRdapNameserverLookupValidation]."
}
```
10. If the JSON name _secureDNS_ exists, the value shall pass the test Secure DNS validation
[stdRdapSecureDnsValidation] defined in this document.

```
{
"code": - 12209 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Secure DNS
validation [stdRdapSecureDnsValidation]."
}
```
11. If the JSON name _entities_ exists, the value shall pass the test Entities validation
[stdRdapEntitiesValidation] defined in this document.

```
{
"code": - 12210 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Entities
validation [stdRdapEntitiesValidation]."
}
```
12. If the JSON name _status_ exists, the value shall pass the test Status validation
[stdRdapStatusValidation] defined in this document.

```
{
"code": - 12211 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Status
validation [stdRdapStatusValidation]."
}
```

13. If the JSON name _publicIds_ exists, the value shall pass the test Public IDs validation
[stdRdapPublicIdsValidation] defined in this document.

```
{
"code": - 12212 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Public IDs
validation [stdRdapPublicIdsValidation]."
}
```
14. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12213 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
15. If the JSON name _links_ exists, the value shall pass the test Links validation
[stdRdapLinksValidation] defined in this document.

```
{
"code": - 12214 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Links
validation [stdRdapLinksValidation]."
}
```
16. If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server
[stdRdapPort43WhoisServerValidation] defined in this document.

```
{
"code": - 12215 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Port 43 WHOIS
Server [stdRdapPort43WhoisServerValidation]."
}
```
17. If the JSON name _events_ exists, the value shall pass the test Events Validation
[stdRdapEventsValidation] defined in this document.

```
{
"code": - 12216 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Events
Validation [stdRdapEventsValidation]."
}
```
18. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12217 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```

19. If the JSON name _notices_ exists and the domain object is not the topmost JSON object.
    {
    "code": - 12218 ,
    "value": "<name/value pair>",
    "message": "The value for the JSON name notices exists but domain object
    is not the topmost JSON object."
    }
20. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP
    Conformance validation [stdRdapConformanceValidation] defined in this document.
    {
    "code": - 12219 ,
    "value": "<name/value pair>",
    "message": "The value for the JSON name value does not pass RDAP
    Conformance validation [stdRdapConformanceValidation]."
    }

### Entity lookup validation [stdRdapEntityLookupValidation]

The following steps should be used to test that an entity data structure is valid:

1. The _entity_ data structure must be a syntactically valid JSON object.
    {
    "code": - 12300 ,
    "value": "<entity structure>",
    "message": "The entity structure is not syntactically valid."
    }
2. The name of every name/value pairs shall be any of: _objectClassName_ , _handle_ ,
    _vcardArray_ , _roles_ , _publicIds_ , _entities_ , _remarks_ , _links_ , _events_ , _asEventActor_ , _status_ , _port43_ ,
    _notices_ or _rdapConformance_.
    {
    "code": - 12301 ,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair is not of: objectClassName, handle,
    vcardArray, roles, publicIds, entities, remarks, links, events, asEventActor,
    status, port43, notices or rdapConformance."
    }
3. The JSON name/values of _objectClassName_ , _handle_ , _vcardArray_ , _roles_ , _publicIds_ , _entities_ ,
    _remarks_ , _links_ , _events_ , _asEventActor_ , _status_ , _port43_ , _notices_ or _rdapConformance_ shall

exist only once.
```
{
"code": - 12302 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a domain structure was
found more than once."
}
```
4. For the JSON name _objectClassName_ , the value shall be "entity".
    {
    "code": - 12303 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not "entity"."
    }
5. If the JSON name _handle_ exists, the value shall be a JSON string data type.
    {
    "code": - 12304 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a string."
    }
6. If the JSON name title _vcardArray_ exists, the value shall be syntactically valid.
    {
    "code": - 12305 ,
    "value": "<name/value pair>",
    "message": " The value for the JSON name value is not a syntactically
    valid vcardArray."
    }


7. If the JSON name _roles_ exists, the value shall pass the test Roles validation
[stdRdapRolesValidation] defined in this document.

```
{
"code": - 12306 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Roles
validation [stdRdapRolesValidation]."
}
```
8. If the JSON name _publicIds_ exists, the value shall pass the test Public IDs validation
[stdRdapPublicIdsValidation] defined in this document.

```
{
"code": - 12307 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Public IDs
validation [stdRdapPublicIdsValidation]."
}
```
9. If the JSON name _entities_ exists, the value shall pass the test Entities validation
[stdRdapEntitiesValidation] defined in this document.
```
{
"code": - 12308 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Entities
validation [stdRdapEntitiesValidation]."
}
```
10. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12309 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
11. If the JSON name _links_ exists, the value shall pass the test Links validation
[stdRdapLinksValidation] defined in this document.

```
{
"code": - 12310 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Links
validation [stdRdapLinksValidation]."
}
```
12. If the JSON name _events_ exists, the value shall pass the test Events Validation
[stdRdapEventsValidation] defined in this document.

```
{
"code": - 12311 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Events
Validation [stdRdapEventsValidation]."
}
```

13. If the JSON name _asEventActor_ exists, the value shall pass the test asEventActor
Validation [stdRdapAsEventActorValidation] defined in this document.

```
{
"code": - 12312 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass asEventActor
Validation [stdRdapAsEventActorValidation]."
}
```
14. If the JSON name _status_ exists, the value shall pass the test Status validation
[stdRdapStatusValidation] defined in this document.

```
{
"code": - 12313 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Status
validation [stdRdapStatusValidation]."
}
```
15. If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server
[stdRdapPort43WhoisServerValidation] defined in this document.

```
{
"code": - 12314 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Port 43 WHOIS
Server [stdRdapPort43WhoisServerValidation]."
}
```
16. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12315 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
17. If the JSON name _notices_ exists and the entity object is not the topmost JSON object.
    {
    "code": - 12316 ,
    "value": "<name/value pair>",
    "message": "The value for the JSON name notices exists but entity object
    is not the topmost JSON object."
    }
18. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP
Conformance validation [stdRdapConformanceValidation] defined in this document.

```
{
"code": - 12317 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass RDAP
Conformance validation [stdRdapConformanceValidation]."
}
```

### Nameserver lookup validation [stdRdapNameserverLookupValidation]

The following steps should be used to test that a nameserver data structure is valid:

1. The _nameserver_ data structure must be a syntactically valid JSON object.
    {
    "code": - 12400 ,
    "value": "<nameserver structure>",
    "message": "The nameserver structure is not syntactically valid."
    }
2. The name of every name/value pairs shall be any of: _objectClassName_ , _handle_ ,
    _ldhName_ , _unicodeName_ , _ipAddresses_ , _entities_ , _status_ , _remarks_ , _links_ , _port43_ , _events_ ,
    _notices_ or _rdapConformance_.
    {
    "code": - 12401 ,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair is not of: objectClassName, handle,
    ldhName, unicodeName, ipAddresses, entities, status, remarks, links, port43,
    events, notices or rdapConformance."
    }
3. The JSON name/values of _objectClassName_ , _handle_ , _ldhName_ , _unicodeName_ ,
    _ipAddresses_ , _entities_ , _status_ , _remarks_ , _links_ , _port43_ , _events_ , _notices_ or _rdapConformance_
shall exist only once.

```
{
"code": - 12402 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a link structure was found
more than once."
}
```
4. For the JSON name _objectClassName_ , the value shall be "nameserver".
    {
    "code": - 12403 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not "nameserver"."
    }
5. If the JSON name _handle_ exists, the value shall be a JSON string data type.
    {
    "code": - 12404 ,
    "value": "<name/value pair>",
    "message": "The JSON value is not a string."
    }
6. If the JSON name _ldhName_ exists, the value shall pass the test LDH name
    [stdRdapLdhNameValidation] defined in this document.
    {
    "code": - 12405 ,
    "value": "<name/value pair>",
    "message": " The value for the JSON name value does not pass LDH name
    [stdRdapLdhNameValidation]."
    }


7. If the JSON name _unicodeName_ exists, the value shall pass the test Unicode name
[stdRdapUnicodeNameValidation] defined in this document.

```
{
"code": - 12406 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Unicode name
[stdRdapUnicodeNameValidation]."
}
```
8. If the JSON name _ipAddresses_ exists, the value shall pass the test IP Addresses Validation
[stdRdapIpAddressesValidation] defined in this document.

```
{
"code": - 12407 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass IP Addresses
Validation [stdRdapIpAddressesValidation]."
}
```
9. If the JSON name _entities_ exists, the value shall pass the test Entities validation
[stdRdapEntitiesValidation] defined in this document.

```
{
"code": - 12408 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Entities
validation [stdRdapEntitiesValidation]."
}
```
10. If the JSON name _status_ exists, the value shall pass the test Status validation
[stdRdapStatusValidation] defined in this document.

```
{
"code": - 12409 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Status
validation [stdRdapStatusValidation]."
}
```
11. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12410 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
12. If the JSON name _links_ exists, the value shall pass the test Links validation
[stdRdapLinksValidation] defined in this document.

```
{
"code": - 12411 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Links
validation [stdRdapLinksValidation]."
}
```

13. If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server
    [stdRdapPort43WhoisServerValidation] defined in this document.
    {
    "code": - 12412 ,
    "value": "<name/value pair>",
    "message": "The value for the JSON name value does not pass Port 43 WHOIS
    Server [stdRdapPort43WhoisServerValidation]."
    }
14. If the JSON name _events_ exists, the value shall pass the test Events Validation
[stdRdapEventsValidation] defined in this document.

```
{
"code": - 12413 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Events
Validation [stdRdapEventsValidation]."
}
```
15. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12414 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
16. If the JSON name _notices_ exists and the nameserver object is not the topmost JSON
object.

```
{
"code": - 12415 ,
"value": "<name/value pair>",
"message": "The value for the JSON name notices exists but nameserver
object is not the topmost JSON object."
}
```
17. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP
    Conformance validation [stdRdapConformanceValidation] defined in this document.
    {
    "code": - 12416 ,
    "value": "<name/value pair>",
    "message": "The value for the JSON name value does not pass RDAP
    Conformance validation [stdRdapConformanceValidation]."
    }

### Help validation [stdRdapHelpValidation]

The following steps should be used to test that a help data structure is valid:

1. The _help_ data structure must be a syntactically valid JSON object.
    {
    "code": - 12500 ,
    "value": "<help structure>",
    "message": "The help structure is not syntactically valid."
    }
2. The name of every name/value pairs shall be _notices_ or _rdapConformance_.
    {
    "code": - 12501 ,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair is not of: notices or
    rdapConformance."
    }
3. The JSON name/values of _notices_ or _rdapConformance_ shall exist only once.
    {
    "code": - 12502 ,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair of a link structure was found
    more than once."
    }
4. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12503 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
5. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP
    Conformance validation [stdRdapConformanceValidation] defined in this document.
    {
    "code": - 12504 ,
    "value": "<name/value pair>",
    "message": "The value for the JSON name value does not pass RDAP
    Conformance validation [stdRdapConformanceValidation]."
    }

### Nameservers search validation [stdRdapNameserversSearchValidation]

The following steps should be used to test that a nameserverSearchResults data
structure is valid:


1. The _nameserverSearchResults_ data structure must be a syntactically valid JSON object.
    {
    "code": - 12600 ,
    "value": "<nameserver structure>",
    "message": "The nameserver structure is not syntactically valid."
    }
2. The name of every name/value pairs shall be any of: _nameserverSearchResults_ , _remarks_ ,
events , notices or rdapConformance.

```
{
"code": - 12601 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair is not of: nameserverSearchResults,
remarks, events, notices or rdapConformance."
}
```
3. The JSON name/values of _nameserverSearchResults_ , _remarks_ , _events_ , _notices_ or
rdapConformance shall exist only once.

```
{
"code": - 12602 ,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a link structure was found
more than once."
}
```
3. The _nameserverSearchResults_ data structure must be a syntactically valid JSON array.
    {
    "code": - 12603 ,
    "value": "<nameserverSearchResults structure>",
    "message": "The nameserverSearchResults structure is not syntactically
    valid."
    }
4. For every object (i.e. nameserver) of the JSON array, verify that the _nameserverSearchResults_ structure complies with:

4.1. The object (i.e. nameserver) shall pass the Nameserver lookup validation
[stdRdapNameserverLookupValidation] test.

```
{
"code": - 12604 ,
"value": "<nameserver object>",
"message": "The nameserver object does not pass Nameserver lookup
validation [stdRdapNameserverLookupValidation]."
}
```
5. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12605 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
6. If the JSON name _events_ exists, the value shall pass the test Events Validation
[stdRdapEventsValidation] defined in this document.

```
{
"code": - 12606 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Events
Validation [stdRdapEventsValidation]."
}
```
7. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks
Validation [stdRdapNoticesRemarksValidation] defined in this document.

```
{
"code": - 12607 ,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and
Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
8. If the JSON name _notices_ exists and the object is not the topmost JSON object.
```json
{
  "code": -12608 ,
  "value": "<name/value pair>",
  "message": "The value for the JSON name notices exists but object is not the topmost JSON object."
}
```
9. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [stdRdapConformanceValidation] defined in this document.
```json
{
  "code": -12609 ,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```
