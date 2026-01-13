# Common Data Structures Tests

## RDAP Conformance validation 

Test group: [[stdRdapConformanceValidation]](#id-stdRdapConformanceValidation){ #id-stdRdapConformanceValidation }

The following steps should be used to test that an RDAP Conformance data structure is valid:

1. Test case [-10500](#id-testCase-10500){ #id-testCase-10500 }: The _RDAP Conformance_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10500,
  "value": "<rdapConformance structure>",
  "message": "The RDAP Conformance structure is not syntactically valid."
}
```
2. Test case [-10501](#id-testCase-10501){ #id-testCase-10501 }: Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -10501,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
3. Test case [-10502](#id-testCase-10502){ #id-testCase-10502 }: Each of the JSON string values in the JSON array, with the exception of "rdap_level_0", shall be included as an Extension Identifier in **RDAPExtensions**.
``` json
{
  "code": -10502,
  "value": "<JSON string>",
  "message": "The JSON string is not included as an Extension Identifier in RDAPExtensions."
}
```
4. Test case [-10503](#id-testCase-10503){ #id-testCase-10503 }: The JSON string value "rdap_level_0" is not included in the _RDAP Conformance_ data structure.
``` json
{
  "code": -10503,
  "value": "<rdapConformance>",
  "message": "The RDAP Conformance data structure does not include rdap_level_0."
}
```
5. Test case [-10504](#id-testCase-10504){ #id-testCase-10504 }: The rdapConformance array must exist in all RDAP responses.
``` json
{
  "code": -10504,
  "value": "<response>",
  "message": "RFC 9083 requires all RDAP responses to have an rdapConformance array."
}
```
6. Test case [-10505](#id-testCase-10505){ #id-testCase-10505 }: If the rdapConformance array exists in an object that is embedded in another object, then this is an error because rdapConformance must only appear in the top-most part of an RDAP response.
``` json
{
  "code": -10505,
  "value": "<RDAP object>",
  "message": "The rdapConformance array must appear only in the top-most of the RDAP response."
}
```


## Links validation

Test group: [[stdRdapLinksValidation]](#id-stdRdapLinksValidation){ #id-stdRdapLinksValidation }

The following steps should be used to test that a links data structure is valid:

1. Test case [-10600](#id-testCase-10600){ #id-testCase-10600 }: The _links_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10600,
  "value": "<links structure>",
  "message": "The links structure is not syntactically valid."
}
```
2. For every object (i.e. link) of the JSON array, verify that the _link_ structure complies with:
    1. Test case [-10601](#id-testCase-10601){ #id-testCase-10601 }: The name of every name/value pair shall be value, rel, href, hreflang, title, media or type.
``` json
{
  "code": -10601,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: value, rel, href, hreflang, title, media or type."
}
```
    2. Test case [-10602](#id-testCase-10602){ #id-testCase-10602 }: The JSON name/value pairs of _rel_, _href_, _hreflang_, _title_, _media_ and _type_ shall appear only once.
``` json
{
  "code": -10602,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
    3. Test case [-10603](#id-testCase-10603){ #id-testCase-10603 }: If the JSON name _media_ exists, the allowed values are: screen, tty, tv, projection, handheld, print, braille, embossed, speech, and all.
``` json
{
  "code": -10603,
  "value": "<name/value pair>",
  "message": "The value for the JSON name media is not of: screen, tty, tv, projection, handheld, print, braille, embossed, speech, or all."
}
```
    4. Test case [-10604](#id-testCase-10604){ #id-testCase-10604 }: If the JSON name _rel_ exists, the value shall be included as a "Relation Name" in linkRelations.
``` json
{
  "code": -10604,
  "value": "<name/value pair>",
  "message": "The JSON value is not included as a Relation Name in linkRelations."
}
```
    5. Test case [-10605](#id-testCase-10605){ #id-testCase-10605 }: If the JSON name _type_ exists, the value shall be included as a "Type Name/Subtype Name" as registered in mediaTypes.
``` json
{
  "code": -10605,
  "value": "<name/value pair>",
  "message": "The JSON value is not included as a Name in mediaTypes."
}
```
    6. Test case [-10606](#id-testCase-10606){ #id-testCase-10606 }: If the JSON name title exists, the value shall be a JSON string data type.
``` json
{
  "code": -10606,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    7. Test case [-10607](#id-testCase-10607){ #id-testCase-10607 }: If the JSON name hreflang exists, the value shall be a JSON string data type or a valid JSON array where every value is a JSON string data type.
``` json
{
  "code": -10607,
  "value": "<name/value pair>",
  "message": "The value for the JSON name hreflang is not a JSON string data type or a valid JSON array where every value is a JSON string data type."
}
```
    8. Test case [-10608](#id-testCase-10608){ #id-testCase-10608 }: If the JSON name _hreflang_ exists, every one of the _JSON string_ data values shall conform to the Language-Tag syntax.
``` json
{
  "code": -10608,
  "value": "<name/value pair>",
  "message": "The value of the JSON string data in the hreflang does not conform to Language-Tag syntax."
}
```
    9. Test case [-10609](#id-testCase-10609){ #id-testCase-10609 }: If the JSON name _value_ exists, the value shall pass the test Web URI validation [[webUriValidation]][id-webUriValidation] defined in this document.
``` json
{
  "code": -10609,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Web URI validation [webUriValidation]."
}
```
    10. Test case [-10610](#id-testCase-10610){ #id-testCase-10610 }: The JSON name href shall exist.
``` json
{
  "code": -10610,
  "value": "<links structure>",
  "message": "The href element does not exist."
}
```
    11. Test case [-10611](#id-testCase-10611){ #id-testCase-10611 }: For the JSON name href, the value shall pass the test Web URI validation [[webUriValidation]][id-webUriValidation] defined in this document.
``` json
{
  "code": -10611,
  "value": "<name/value pair>",
  "message": "The value for the JSON name href does not pass Web URI validation [webUriValidation]."
}
```
    12. Test case [-10612](#id-testCase-10612){ #id-testCase-10612 }: The JSON name value shall exist:
``` json
{
  "code": -10612,
  "value": "<link structure>",
  "message": "A 'value' propert does not exist in the link object."
}
```
    13. Test case [-10613](#id-testCase-10613){ #id-testCase-10613 }: The JSON name “rel” shall exist:
``` json
{
  "code": -10613,
  "value": "<link structure>",
  "message": "A 'rel' property does not exist in the link object."
}
```


## Notices and Remarks Validation 

Test group: [[stdRdapNoticesRemarksValidation]](#id-stdRdapNoticesRemarksValidation){ #id-stdRdapNoticesRemarksValidation }

The following steps should be used to test that a notices or remarks data structure is  valid:

1. Test case [-10700](#id-testCase-10700){ #id-testCase-10700 }: The _notices_ or _remarks_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10700,
  "value": "<notices or remarks structure>",
  "message": "The notices or remarks structure is not syntactically valid."
}
```
2. For every object of the JSON array, verify that the structure complies with:
    1. Test case [-10701](#id-testCase-10701){ #id-testCase-10701 }: The name of every name/value pair shall be title, type, description or links.
``` json
{
  "code": -10701,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: title, type, description or links."
}
```
    2. Test case [-10702](#id-testCase-10702){ #id-testCase-10702 }: The JSON name/values of title, type, description and links shall exist only once.
``` json
{
  "code": -10702,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
    3. Test case [-10703](#id-testCase-10703){ #id-testCase-10703 }: If the JSON name title exists, the value shall be a JSON string data type.
``` json
{
  "code": -10703,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    4. Test case [-10704](#id-testCase-10704){ #id-testCase-10704 }: If the JSON name links exists, the value shall pass the test Links validation [[stdRdapLinksValidation]][id-stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -10704,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
    5. Test case [-10705](#id-testCase-10705){ #id-testCase-10705 }: If the JSON name type exists, the value shall be a JSON string data type.
``` json
{
  "code": -10705,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    6. Test case [-10706](#id-testCase-10706){ #id-testCase-10706 }: If the JSON name type exists, the value shall be included in the RDAPJSONValues with Type="notice and remark type".
``` json
{
  "code": -10706,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='notice and remark type' in the RDAPJSONValues dataset."
}
```
    7. Test case [-10707](#id-testCase-10707){ #id-testCase-10707 }: The JSON name description shall exist.
``` json
{
  "code": -10707,
  "value": "<notices or remarks structure>",
  "message": "The description element does not exist."
}
```
    8. Test case [-10708](#id-testCase-10708){ #id-testCase-10708 }: The description data structure must be a syntactically valid JSON array.
``` json
{
  "code": -10708,
  "value": "<description structure>",
  "message": "The description structure is not syntactically valid."
}
```
    9. Test case [-10709](#id-testCase-10709){ #id-testCase-10709 }: Every value of the JSON array of the _description_ data structure shall be a JSON string data type.
``` json
{
  "code": -10709,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```

## Language Identifier Validation 

Test group: [[stdRdapLanguageIdentifierValidation]](#id-stdRdapLanguageIdentifierValidation){ #id-stdRdapLanguageIdentifierValidation }

The following steps should be used to test that a lang data structure is valid:

1. Test case [-10800](#id-testCase-10800){ #id-testCase-10800 }: For the JSON name _lang_, the value shall conform to the Language-Tag syntax.
``` json 
{
  "code": -10800,
  "value": "<name/value pair>",
  "message": "The value of the JSON string data in lang does not conform to Language-Tag syntax."
}
```

## Events Validation

Test group: [[stdRdapEventsValidation]](#id-stdRdapEventsValidation){ #id-stdRdapEventsValidation }

The following steps should be used to test that a events data structure is valid:

1. Test case [-10900](#id-testCase-10900){ #id-testCase-10900 }: The _events_ data structure must be a syntactically valid JSON array.
``` json 
{
  "code": -10900,
  "value": "<events structure>",
  "message": "The events structure is not syntactically valid."
}
```
2. For every object of the JSON array, verify that the structure complies with:
    1. Test case [-10901](#id-testCase-10901){ #id-testCase-10901 }: The name of every name/value pair shall be any of: eventAction, eventActor, eventDate or links.
``` json
{
  "code": -10901,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: eventAction, eventActor, eventDate or links."
}
```
    2. Test case [-10902](#id-testCase-10902){ #id-testCase-10902 }: The JSON name/value pairs of _eventAction_, _eventActor_, _eventDate_ and _links_ shall exist only once.
``` json
{
  "code": -10902,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once. "
}
```
    3. Test case [-10903](#id-testCase-10903){ #id-testCase-10903 }: The JSON name eventAction shall exist.
``` json
{
  "code": -10903,
  "value": "<links structure>",
  "message": "The eventAction element does not exist."
}
```
    4. Test case [-10904](#id-testCase-10904){ #id-testCase-10904 }: For the JSON name eventAction, the value shall be a JSON string data type.
``` json
{
  "code": -10904,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    5. For the JSON name _eventAction_, the value shall be included in the RDAPJSONValues with Type="event action".
``` json
{
  "code": -10905,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='event action' in the RDAPJSONValues data set.
}
```
    6. Test case [-10906](#id-testCase-10906){ #id-testCase-10906 }: The JSON name eventDate shall exist.
``` json
{
  "code": -10906,
  "value": "<links structure>",
  "message": "The eventDate element does not exist."
}
```
    7. Test case [-10907](#id-testCase-10907){ #id-testCase-10907 }: For the JSON name eventDate, the value shall be a JSON string data type.
``` json
{
  "code": -10907,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    8. Test case [-10908](#id-testCase-10908){ #id-testCase-10908 }: For the JSON name eventDate, the value shall be syntactically valid time and date according to RFC3339.
``` json
{
"code": -10908,
"value": "<name/value pair>",
"message": "The JSON value shall be a syntactically valid time and date according to RFC3339."
}
```
    9. Test case [-10909](#id-testCase-10909){ #id-testCase-10909 }: If the JSON name eventActor exists, the value shall be a JSON string data type.
``` json
{
  "code": -10909,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    10. Test case [-10910](#id-testCase-10910){ #id-testCase-10910 }: If the JSON name links exists, the JSON name eventActor shall also exist.
``` json
{
  "code": -10910,
  "value": "<events structure>",
  "message": "A links structure was found but an eventActor was not."
}
```
    11. Test case [-10911](#id-testCase-10911){ #id-testCase-10911 }: If the JSON name _links_ exists, the value shall pass the test Links validation [[stdRdapLinksValidation]][id-stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -10911,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
3. Test case [-10912](#id-testCase-10912){ #id-testCase-10912 }: An _eventAction_ value shall only appears once within the events array.
``` json
{
  "code": -10912,
  "value": "<events structure>",
  "message": "An eventAction value exists more than once within the events array."
}
```

## Status validation 

Test group: [[stdRdapStatusValidation]](#id-stdRdapStatusValidation){ #id-stdRdapStatusValidation }

The following steps should be used to test that a status data structure is valid:

1. Test case [-11000](#id-testCase-11000){ #id-testCase-11000 }: The _status_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11000,
  "value": "<status structure>",
  "message": "The status structure is not syntactically valid."
}
```
2. Test case [-11001](#id-testCase-11001){ #id-testCase-11001 }: Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11001,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
3. Test case [-11002](#id-testCase-11002){ #id-testCase-11002 }: Each of the JSON string values in the JSON array shall be included in the **RDAPJSONValues** with Type="status".
``` json
{
  "code": -11002,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='status'."
}
```
4. Test case [-11003](#id-testCase-11003){ #id-testCase-11003 }: A string in the status array shall only appear once:
``` json
{
  "code": -11003,
  "value": "<JSON string>",
  "message": "A status value exists more than once in the status array."
}
```


## Port 43 WHOIS Server 

Test group: [[stdRdapPort43WhoisServerValidation]](#id-stdRdapPort43WhoisServerValidation){ #id-stdRdapPort43WhoisServerValidation }

The following steps should be used to test that a port43 data structure is valid:

1. Test case [-11100](#id-testCase-11100){ #id-testCase-11100 }: For the JSON name _port43_, the value shall pass the test [IPv4Validation], [IPv6Validation] or [DomainNameValidation] defined in this document.
``` json
{
"code": -11100,
"value": "<name/value pair>",
"message": "The value for the JSON name port43 does not pass [IPv4Validation], [IPv6Validation] or [DomainNameValidation]."
}
```

## Public IDs validation 

Test group: [[stdRdapPublicIdsValidation]](#id-stdRdapPublicIdsValidation){ #id-stdRdapPublicIdsValidation }

The following steps should be used to test that a publicIds data structure is valid:

1. Test case [-11200](#id-testCase-11200){ #id-testCase-11200 }: The _publicIds_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11200,
  "value": "<publicIds structure>",
  "message": "The publicIds structure is not syntactically valid."
}
```
2. For every object of the JSON array, verify that the structure complies with:
    1. Test case [-11201](#id-testCase-11201){ #id-testCase-11201 }: The name of every name/value pairs shall be type or identifier.
``` json
{
  "code": -11201,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: type or identifier."
}
```
    2. Test case [-11202](#id-testCase-11202){ #id-testCase-11202 }: The JSON name/values of type or identifier shall appear only once.
``` json
{
  "code": -11202,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a domain structure was found more than once."
}
```
    3. Test case [-11203](#id-testCase-11203){ #id-testCase-11203 }: The JSON name/values of type and identifier shall appear.
``` json
{
  "code": -11203,
  "value": "<publicIds structure>",
  "message": "The following name/values shall exist: type or identifier."
}
```
    4. Test case [-11204](#id-testCase-11204){ #id-testCase-11204 }: For the JSON name type, the value shall be a JSON string data type.
``` json
{
  "code": -11204,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    5. Test case [-11205](#id-testCase-11205){ #id-testCase-11205 }: For the JSON name identifier, the value shall be a JSON string data type.
``` json
{
  "code": -11205,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```

## asEventActor Validation 

Test group: [[stdRdapAsEventActorValidation]](#id-stdRdapAsEventActorValidation){ #id-stdRdapAsEventActorValidation }

The following steps should be used to test that an asEventActor data structure is valid:

1. Test case [-11300](#id-testCase-11300){ #id-testCase-11300 }: The _asEventActor_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11300,
  "value": "<asEventActor structure>",
  "message": "The asEventActor structure is not syntactically valid."
}
```
2. Test case [-11301](#id-testCase-11301){ #id-testCase-11301 }: The _asEventActor_ data structure must be embedded within an entity object, and the entity object must be embedded within another object.
``` json
{
  "code": -11301,
  "value": "<asEventActor structure>",
  "message": "The asEventActor structure is not embedded within an entity object and the entity object is not embedded within another object."
}
```
3. For every object of the JSON array, verify that the structure complies with:
    1. Test case [-11302](#id-testCase-11302){ #id-testCase-11302 }: The name of every name/value pair shall be any of: eventAction or eventDate.
``` json
{
  "code": -11302,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: eventAction and eventDate."
}
```
    2. Test case [-11303](#id-testCase-11303){ #id-testCase-11303 }: The JSON name/values of eventAction or eventDate shall appear only once.
``` json
{
  "code": -11303,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
    3. Test case [-11304](#id-testCase-11304){ #id-testCase-11304 }: The JSON name eventAction shall exist.
``` json
{
  "code": -11304,
  "value": "<links structure>",
  "message": "The eventAction element does not exist."
}
```
    4. Test case [-11305](#id-testCase-11305){ #id-testCase-11305 }: For the JSON name eventAction, the value shall be a JSON string data type.
``` json
{
  "code": -11305,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    5. Test case [-11306](#id-testCase-11306){ #id-testCase-11306 }: For the JSON name eventAction, the value shall be included in the RDAPJSONValues with Type="event action".
``` json
{
  "code": -11306,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='event action' in the RDAPJSONValues dataset."
}
```
    6. Test case [-11307](#id-testCase-11307){ #id-testCase-11307 }: The JSON name eventDate shall exist.
``` json
{
  "code": -11307,
  "value": "<links structure>",
  "message": "The eventDate element does not exist."
}
```
    7. Test case [-11308](#id-testCase-11308){ #id-testCase-11308 }: For the JSON name eventDate, the value shall be a JSON string data type.
``` json
{
  "code": -11308,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    8. Test case [-11309](#id-testCase-11309){ #id-testCase-11309 }: For the JSON name _eventDate_, the value shall be syntactically valid time and date according to RFC3339.
``` json
{
  "code": -11309,
  "value": "<name/value pair>",
  "message": "The JSON value shall be a syntactically valid time and date according to RFC3339."
}
```
4. Test case [-11310](#id-testCase-11310){ #id-testCase-11310 }: An _eventAction_ shall only appear once within the events array.
``` json
{
  "code": -11310,
  "value": "<events structure>",
  "message": "An _eventAction_ exists more than once within the events array."
}
```

## IP Addresses Validation 

Test group: [[stdRdapIpAddressesValidation]](#id-stdRdapIpAddressesValidation){ #id-stdRdapIpAddressesValidation }

The following steps should be used to test that an ipAddresses data structure is valid:

1. Test case [-11400](#id-testCase-11400){ #id-testCase-11400 }: The _ipAddresses_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -11400,
  "value": "<ipAddresses structure>",
  "message": "The ipAddresses structure is not syntactically valid."
}
```
2. Test case [-11401](#id-testCase-11401){ #id-testCase-11401 }: The name of every name/value pair shall be any of: _v4_ or _v6_.
``` json
{
  "code": -11401,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: v4 or v6."
}
```
3. Test case [-11402](#id-testCase-11402){ #id-testCase-11402 }: The JSON name/values of _v4_ and _v6_ shall appear only once.
``` json
{
  "code": -11402,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of an ipAddresses structure was found more than once."
}
```
4. Test case [-11403](#id-testCase-11403){ #id-testCase-11403 }: One _v4_ name/values or one _v6_ name/value shall appear.
``` json
{
  "code": -11403,
  "value": "<name/value pair>",
  "message": "v4 nor v6 name/value pair exists."
}
```
5. If the JSON name _v4_ exists, the value shall pass the following:
    1. Test case [-11404](#id-testCase-11404){ #id-testCase-11404 }: The v4 data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11404,
  "value": "<v4 structure>",
  "message": "The v4 structure is not syntactically valid."
}
```
    2. For every object of the JSON array, verify that the structure complies with:
        1. Test case [-11405](#id-testCase-11405){ #id-testCase-11405 }: Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11405,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
        2. Test case [-11406](#id-testCase-11406){ #id-testCase-11406 }: The IPv4 address is syntactically valid in dot-decimal notation.
``` json
{
  "code": -11406,
  "value": "<IPv4 address string>",
  "message": "The IPv4 address is not syntactically valid in dot-decimal notation."
}
```
6. If the JSON name _v6_ exists, the value shall pass the following:
    1. Test case [-11407](#id-testCase-11407){ #id-testCase-11407 }: The v6 data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11407,
  "value": "<v6 structure>",
  "message": "The v6 structure is not syntactically valid."
}
```
    2. For every object of the JSON array, verify that the structure complies with:
        1. Test case [-11408](#id-testCase-11408){ #id-testCase-11408 }: Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11408,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
        2. Test case [-11409](#id-testCase-11409){ #id-testCase-11409 }: The IPv6 address is syntactically valid.
``` json
{
  "code": -11409,
  "value": "<IPv6 address string>",
  "message": "The IPv6 address is not syntactically valid."
}
```

## Variants validation 

Test group: [[stdRdapVariantsValidation]](#id-stdRdapVariantsValidation){ #id-stdRdapVariantsValidation }

The following steps should be used to test that a variants data structure is valid:

1. Test case [-11500](#id-testCase-11500){ #id-testCase-11500 }: The _variants_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11500,
  "value": "<variants structure>",
  "message": "The variants structure is not syntactically valid."
}
```
2. For every object of the JSON array, verify that the structure complies with:
    1. Test case [-11501](#id-testCase-11501){ #id-testCase-11501 }: The name of every name/value pair shall be relation, idnTable or variantNames.
``` json
{
  "code": -11501,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: relation, idnTable or variantNames."
}
```
    2. Test case [-11502](#id-testCase-11502){ #id-testCase-11502 }: The JSON name/value pairs of relation, idnTable and variantNames shall appear
only once.
``` json
{
  "code": -11502,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
    3. Test case [-11503](#id-testCase-11503){ #id-testCase-11503 }: The RDAP relation data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11503,
  "value": "<relation structure>",
  "message": "The RDAP Conformance structure is not syntactically valid."
}
```
    4. For every object of the JSON array, verify that the structure complies with:

      1. Test case [-11504](#id-testCase-11504){ #id-testCase-11504 }: Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11504,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
      2. Test case [-11505](#id-testCase-11505){ #id-testCase-11505 }: Each of the JSON string values in the JSON array shall be included in the
RDAPJSONValues with Type="domain variant relation".
``` json
{
  "code": -11505,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='domain variant relation '."
}
```
    5. Test case [-11506](#id-testCase-11506){ #id-testCase-11506 }: If the JSON name idnTable exists, the value shall be a JSON string data type.
``` json
{
  "code": -11506,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
    6. Test case [-11507](#id-testCase-11507){ #id-testCase-11507 }: The variantNames data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11507,
  "value": "<variantNames structure>",
  "message": "The variantNames structure is not syntactically valid."
}
```
    7. For every object of the JSON array, verify that the structure complies with:
        1. Test case [-11508](#id-testCase-11508){ #id-testCase-11508 }: The name of every name/value pair shall be any of: ldhName or unicodeName
``` json
{
  "code": -11508,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: ldhName or unicodeName."
}
```
        2. Test case [-11509](#id-testCase-11509){ #id-testCase-11509 }: The JSON name/value pairs of ldhName or unicodeName shall exist only once.
``` json
{
  "code": -11509,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
        3. Test case [-11510](#id-testCase-11510){ #id-testCase-11510 }: If the JSON name title ldhName exists, the value shall pass the test LDH name [[stdRdapLdhNameValidation]][id-stdRdapLdhNameValidation] defined in this document.
``` json
{
  "code": -11510,
  "value": "<name/value pair>",
  "message": " The value for the JSON name value does not pass LDH name [stdRdapLdhNameValidation]."
}
```
        4. Test case [-11511](#id-testCase-11511){ #id-testCase-11511 }: If the JSON name unicodeName exists, the value shall pass the test Unicode name [[stdRdapUnicodeNameValidation]][id-stdRdapUnicodeNameValidation] defined in this document.
``` json
{
  "code": -11511,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Unicode name [stdRdapUnicodeNameValidation]."
}
```

## Unicode name 

Test group: [[stdRdapUnicodeNameValidation]](#id-stdRdapUnicodeNameValidation){ #id-stdRdapUnicodeNameValidation }

The following steps should be used to test that a unicodeName is valid:

1. Test case [-11600](#id-testCase-11600){ #id-testCase-11600 }: The length of each label is between 1 and 63.
``` json
{
  "code": -11600,
  "value": "<domain name>",
  "message": "A DNS label with length not between 1 and 63 was found."
}
```
2. Test case [-11601](#id-testCase-11601){ #id-testCase-11601 }: A maximum total length of 253 characters not including the last ".".
``` json
{
  "code": -11601,
  "value": "<domain name>",
  "message": "A domain name of more than 253 characters was found."
}
```
3. Test case [-11602](#id-testCase-11602){ #id-testCase-11602 }: At least two labels shall exist in the domain name. See, RDAP_Technical_Implementation_Guide_2_1 section 1.10.
``` json
{
  "code": -11602,
  "value": "<domain name>",
  "message": "A domain name with less than two labels was found. See RDAP_Technical_Implementation_Guide_2_1 section 1.10"
}
```
4. Test case [-11603](#id-testCase-11603){ #id-testCase-11603 }: Each label of the domain name is a valid "U-label or "NR-LDH label".
``` json
{
  "code": -11603,
  "value": "<domain name>",
  "message": "A label not being a valid 'U-label' or 'NR-LDH label' was found."
}
```

Note: the latest version of the IANA IDNA Rules and Derived Property Values shall be used. See, https://www.iana.org/assignments/idna-tables-11.0.0/idna-tables-11.0.0.xml

Note: some legacy gTLDs may fail this test, because they have a few domain name registrations that comply with IDNA2003 but not IDNA2018. Such names are not recommended to be used when testing an RDAP response with this tool.


## LDH name 

Test group: [[stdRdapLdhNameValidation]](#id-stdRdapLdhNameValidation){ #id-stdRdapLdhNameValidation }

The following steps should be used to test that a ldhName is valid:

1. Test case [-11700](#id-testCase-11700){ #id-testCase-11700 }: The length of each label is between 1 and 63.
``` json
{
  "code": -11700,
  "value": "<domain name>",
  "message": "A DNS label with length not between 1 and 63 was found."
}
```
2. Test case [-11701](#id-testCase-11701){ #id-testCase-11701 }: A maximum total length of 253 characters not including the last ".".
``` json
{
  "code": -11701,
  "value": "<domain name>",
  "message": "A domain name of more than 253 characters was found."
}
```
3. Test case [-11702](#id-testCase-11702){ #id-testCase-11702 }: At least two labels shall exist in the domain name. See, RDAP_Technical_Implementation_Guide_2_1 section 1.10.
``` json
{
  "code": -11702,
  "value": "<domain name>",
  "message": "A domain name with less than two labels was found. See RDAP_Technical_Implementation_Guide_2_1 section 1.10"
}
```
4. Test case [-11703](#id-testCase-11703){ #id-testCase-11703 }: Each label of the domain name is a valid "A-label or "NR-LDH label".
``` json
{
  "code": -11703,
  "value": "<domain name>",
  "message": "A label not being a valid 'A-label' or 'NR-LDH label' was found."
}
```

Note: the latest version of the IANA IDNA Rules and Derived Property Values
shall be used. See, https://www.iana.org/assignments/idna-tables-11.0.0/idna-tables-11.0.0.xml

Note: some legacy gTLDs may fail this test, because they have a few domain
name registrations that comply with IDNA2003 but not IDNA2018. Such names
are not recommended to be used when testing an RDAP response with this tool.


## Roles validation 

Test group: [[stdRdapRolesValidation]](#id-stdRdapRolesValidation){ #id-stdRdapRolesValidation }

The following steps should be used to test that a roles data structure is valid:

1. Test case [-11800](#id-testCase-11800){ #id-testCase-11800 }: The _roles_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -11800,
  "value": "<roles structure>",
  "message": "The roles structure is not syntactically valid."
}
```
2. Test case [-11801](#id-testCase-11801){ #id-testCase-11801 }: Every value of the JSON array shall be a JSON string data type.
``` json
{
  "code": -11801,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
3. Test case [-11802](#id-testCase-11802){ #id-testCase-11802 }: Each of the JSON string values in the JSON array shall be included in the **RDAPJSONValues** with Type="role".
``` json
{
  "code": -11802,
  "value": "<JSON string>",
  "message": "The JSON string is not included as a Value with Type='role'."
}
```
4. Test case [-11803](#id-testCase-11803){ #id-testCase-11803 }: The role value shall only appear once in the JSON array.
``` json
{
  "code": -11803,
  "value": "<roles structure>",
  "message": "A role value appeared more than once."
}
```


## Entities validation 

Test group: [[stdRdapEntitiesValidation]](#id-stdRdapEntitiesValidation){ #id-stdRdapEntitiesValidation }

The following steps should be used to test that an entities data structure is valid:

1. Test case [-11900](#id-testCase-11900){ #id-testCase-11900 }: The _entities_ data structure must be a syntactically valid JSON array.
``` json
{
    "code": -11900,
    "value": "<entities structure>",
    "message": "The entities structure is not syntactically valid."
}
```
2. Test case [-11901](#id-testCase-11901){ #id-testCase-11901 }: Every value of the JSON array shall pass the test Entity lookup validation [[stdRdapEntityLookupValidation]][id-stdRdapEntityLookupValidation] defined in this document.
``` json
{
    "code": -11901,
    "value": "<JSON value>",
    "message": "The JSON value does not pass Entity lookup validation [stdRdapEntityLookupValidation]."
}
```


## Secure DNS validation 

Test group: [[stdRdapSecureDnsValidation]](#id-stdRdapSecureDnsValidation){ #id-stdRdapSecureDnsValidation}

The following steps should be used to test that a secureDNS data structure is valid:

1. Test case [-12000](#id-testCase-12000){ #id-testCase-12000 }: The _secureDNS_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12000,
  "value": "<secureDNS structure>",
  "message": "The domain structure is not syntactically valid."
}
```
2. Test case [-12001](#id-testCase-12001){ #id-testCase-12001 }: The name of every name/value pairs shall be _zoneSigned_, _delegationSigned_, _maxSigLife_, _dsData_ or _keyData_.
``` json
{
  "code": -12001,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: zoneSigned, delegationSigned, maxSigLife, dsData or keyData."
}
```
3. Test case [-12002](#id-testCase-12002){ #id-testCase-12002 }: The JSON name/values of _zoneSigned_, _delegationSigned_, _maxSigLife_, _dsData_ and keyData shall appear only once.
``` json
{
  "code": -12002,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a domain structure was found more than once."
}
```
4. Test case [-12003](#id-testCase-12003){ #id-testCase-12003 }: If the JSON name _zoneSigned_ appears, the value shall be a JSON boolean data type.
``` json
{
  "code": -12003,
  "value": "<name/value pair>",
  "message": "The JSON value is not a boolean."
}
```
5. Test case [-12005](#id-testCase-12005){ #id-testCase-12005 }: If the JSON name _delegationSigned_ appears, the value shall be a JSON boolean data type.
``` json
{
  "code": -12005,
  "value": "<name/value pair>",
  "message": "The JSON value is not a boolean."
}
```
6. Test case [-12006](#id-testCase-12006){ #id-testCase-12006 }: If the JSON name _maxSigLife_ exists, the value shall be a JSON number data type between 1 and 2147483647.
``` json
{
  "code": -12006,
  "value": "<name/value pair>",
  "message": "The JSON value is not a number between 1 and 2147483647."
}
```
7. If the JSON name _dsData_ appears, the value shall pass the following:
    1. Test case [-12008](#id-testCase-12008){ #id-testCase-12008 }: The dsData data structure must be a syntactically valid array of JSON objects.
``` json
{
  "code": -12008,
  "value": "<dsData structure>",
  "message": "The dsData structure is not syntactically valid."
}
```
    2. Test case [-12009](#id-testCase-12009){ #id-testCase-12009 }: The name of every name/value pair shall be any of: _keyTag_, _algorithm_, _digest_, digestType, events or links.
``` json
{
  "code": -12009,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: keyTag, algorithm, digest, digestType, events or links."
}
```
    3. Test case [-12010](#id-testCase-12010){ #id-testCase-12010 }: The JSON name/values of keyTag, algorithm, digest, digestType, events or links shall appear only once.
``` json
{
  "code": -12010,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a dsData structure was found more than once."
}
```
    4. Test case [-12011](#id-testCase-12011){ #id-testCase-12011 }: The JSON name/values of keyTag, algorithm, digest and digestType shall appear.
``` json
{
  "code": -12011,
  "value": "<dsData structure>",
  "message": "The following name/values shall exist: keyTag, algorithm, digest and digestType."
}
```
    5. Test case [-12012](#id-testCase-12012){ #id-testCase-12012 }: For the JSON name _keyTag_, the value shall be a JSON number data type between 1 and 65535.
``` json
{
  "code": -12012,
  "value": "<name/value pair>",
  "message": "The JSON value is not a number between 1 and 65535."
}
```
    6. Test case [-12013](#id-testCase-12013){ #id-testCase-12013 }: For the JSON name _algorithm_, the value shall be a JSON number listed with Zone Signing=Y in dnsSecAlgNumbers. The values 253 and 254 are not valid for this test.
``` json
{
  "code": -12013,
  "value": "<name/value pair>",
  "message": "The JSON value is not listed with Zone Signing=Y in dnsSecAlgNumbers, or it's 253 or 254."
}
```
    7. Test case [-12014](#id-testCase-12014){ #id-testCase-12014 }: For the JSON name digest, the value shall be a JSON string of case-insensitive hexadecimal digits. Whitespace is allowed within the hexadecimal text.
``` json
{
  "code": -12014,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string of case-insensitive hexadecimal digits. Whitespace is allowed within the hexadecimal test."
}
```
    8. Test case [-12015](#id-testCase-12015){ #id-testCase-12015 }: For the JSON name _digestType_, the value shall be a JSON number assigned in dsRrTypes.
``` json
{
  "code": -12015,
  "value": "<name/value pair>",
  "message": "The JSON value is not assigned in dsRrTypes."
}
```
    9. Test case [-12016](#id-testCase-12016){ #id-testCase-12016 }: If the JSON name _events_ exists, the value shall pass the test Events Validation [[stdRdapEventsValidation]][id-stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12016,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
    10. Test case [-12017](#id-testCase-12017){ #id-testCase-12017 }: If the JSON name links exists, the value shall pass the test Links validation [[stdRdapLinksValidation]][id-stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -12017,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
8. If the JSON name _keyData_ exists, the value shall pass the following:
    1. Test case [-12018](#id-testCase-12018){ #id-testCase-12018 }: The keyData data structure must be a syntactically valid array of JSON objects.
``` json
{
  "code": -12018,
  "value": "<keyData structure>",
  "message": "The keyData structure is not syntactically valid."
}
```
    2. Test case [-12019](#id-testCase-12019){ #id-testCase-12019 }: The name of every name/value pair shall be _flags_, _protocol_, _publicKey_, _algorithm_, events or links.
``` json
{
  "code": -12019,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: flags, protocol, publicKey, algorithm, events or links."
}
```
    3. Test case [-12020](#id-testCase-12020){ #id-testCase-12020 }: The JSON name/values of _flags_, _protocol_, _publicKey_, _algorithm_, _events_ or _links_ shall appear only once.
``` json
{
  "code": -12020,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a keyData structure was found more than once."
}
```
    4. Test case [-12021](#id-testCase-12021){ #id-testCase-12021 }: The JSON name/values of _flags_, _protocol_, _publicKey_ and _algorithm_ shall appear.
``` json
{
  "code": -12021,
  "value": "<dsData structure>",
  "message": "The following name/values shall exist: flags, protocol, publicKey and algorithm."
}
```
    5. Test case [-12022](#id-testCase-12022){ #id-testCase-12022 }: For the JSON name _flags_, the value shall be a JSON number data type with values:  256 or 257.
``` json
{
  "code": -12022,
  "value": "<name/value pair>",
  "message": "The JSON value is not 256 or 257."
}
```
    6. Test case [-12023](#id-testCase-12023){ #id-testCase-12023 }: For the JSON name _protocol_, the value shall be a JSON number data type with  value: 3.
``` json
{
  "code": -12023,
  "value": "<name/value pair>",
  "message": "The JSON value is not 3."
}
```
    7. Test case [-12024](#id-testCase-12024){ #id-testCase-12024 }: For the JSON name _publicKey_, the value shall be a JSON string, and the key is  represented as a Base64. Whitespace is allowed within the text.
``` json
{
  "code": -12024,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string of case-insensitive hexadecimal digits. Whitespace is allowed within the hexadecimal text."
}
```
    8. Test case [-12025](#id-testCase-12025){ #id-testCase-12025 }: For the JSON name _algorithm_, the value shall be a JSON number listed with Zone Signing=Y in **dnsSecAlgNumbers**. The values 253 and 254 are not valid for this test.
``` json
{
  "code": -12025,
  "value": "<name/value pair>",
  "message": "The JSON value is not listed with Zone Signing=Y in dnsSecAlgNumbers, or it's 253 or 254."
}
```
    9. Test case [-12026](#id-testCase-12026){ #id-testCase-12026 }: If the JSON name events exists, the value shall pass the test Events Validation [[stdRdapEventsValidation]][id-stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12026,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
    10. Test case [-12027](#id-testCase-12027){ #id-testCase-12027 }: If the JSON name _links_ exists, the value shall pass the test Links validation [[stdRdapLinksValidation]][id-stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -12027,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```

Codes -12004 and -12007 are intentionally omitted.

## Error Response Body

Test group: [[stdRdapErrorResponseBodyValidation]](#id-stdRdapErrorResponseBodyValidation){ #id-stdRdapErrorResponseBodyValidation }

The following steps should be used to test that an error data structure is valid:

1. Test case [-12100](#id-testCase-12100){ #id-testCase-12100 }: The _error_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12100,
  "value": "<error structure>",
  "message": "The error structure is not syntactically valid."
}
```
2. Test case [-12101](#id-testCase-12101){ #id-testCase-12101 }: At least the _errorCode_ name/value pairs shall appear.
``` json
{
  "code": -12101,
  "value": "<error structure>",
  "message": "The errorCode element does not exist."
}
```
3. Test case [-12102](#id-testCase-12102){ #id-testCase-12102 }: The JSON name/values of _errorCode_, _title_, and _description_ shall appear only once.
``` json
{
  "code": -12102,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of an error structure was found more than once."
}
```
4. Test case [-12103](#id-testCase-12103){ #id-testCase-12103 }: For the JSON name _errorCode_, the value shall be a JSON number data type.
``` json
{
  "code": -12103,
  "value": "<name/value pair>",
  "message": "The JSON value is not a number."
}
```
5. Test case [-12104](#id-testCase-12104){ #id-testCase-12104 }: For the JSON name _title_, the value shall be a JSON string data type.
``` json
{
  "code": -12104,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
6. Test case [-12105](#id-testCase-12105){ #id-testCase-12105 }: The _description_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -12105,
  "value": "<description structure>",
  "message": "The description structure is not syntactically valid."
}
```
7. Test case [-12106](#id-testCase-12106){ #id-testCase-12106 }: Every value of the JSON array of the _description_ data structure shall be a JSON string data type.
``` json
{
  "code": -12106,
  "value": "<JSON value>",
  "message": "The JSON value is not a string."
}
```
8. Test case [-12107](#id-testCase-12107){ #id-testCase-12107 }: "errorCode" is required in an error response.
``` json
{
  "code": -12107,
  "value": "<error structure>",
  "message": "The errorCode value is required in an error response."
}
```
9. Test case [-12108](#id-testCase-12108){ #id-testCase-12108 }: The value of "errorCode" must match the value of the HTTP status code.
``` json
{
  "code": -12108,
  "value": "<error structure>",
  "message": "The errorCode value does not match the HTTP status code."
}
```
