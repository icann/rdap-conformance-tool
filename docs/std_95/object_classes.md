# Object Class Tests

## Domain Lookup Validation

Test group: [stdRdapDomainLookupValidation]  [](){ #id-stdRdapDomainLookupValidation }

The following steps should be used to test that a domain data structure is valid:

1. The _domain_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12200,
  "value": "<domain structure>",
  "message": "The domain structure is not syntactically valid."
}
```
2. The name of every name/value pairs shall be any of: _objectClassName_, _handle_, _ldhName_, _unicodeName_, _variants_, _nameservers_, _secureDNS_, _entities_, _status_, _publicIds_, remarks, links, port43, events, notices or rdapConformance.
``` json
{
  "code": -12201,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: objectClassName, handle, ldhName, unicodeName, variants, nameservers, secureDNS, entities, status, publicIds, remarks, links, port43, events, notices or rdapConformance."
}
```
3. The JSON name/values of _objectClassName_, _handle_, _ldhName_, _unicodeName_, _variants_, nameservers, secureDNS, entities, status, publicIds, remarks, links, port43, events, notices or rdapConformance shall appear only once. 
``` json
{
  "code": -12202,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a domain structure was found more than once."
}
```
4. For the JSON name _objectClassName_, the value shall be "domain".
``` json
{
  "code": -12203,
  "value": "<name/value pair>",
  "message": "The JSON value is not 'domain'."
}
```
5. If the JSON name _handle_ exists, the value shall be a JSON string data type.
``` json
{
  "code": -12204,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
6. If the JSON name _ldhName_, the value shall pass the test LDH name [stdRdapLdhNameValidation] defined in this document.
``` json
{
  "code": -12205,
  "value": "<name/value pair>",
  "message": " The value for the JSON name value does not pass LDH name [stdRdapLdhNameValidation]."
}
```
7. If the JSON name _unicodeName_ exists, the value shall pass the test Unicode name  [stdRdapUnicodeNameValidation] defined in this document.
``` json
{
  "code": -12206,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Unicode name [stdRdapUnicodeNameValidation]."
}
```
8. If the JSON name _variants_ exists, the value shall pass the test Variants validation [stdRdapVariantsValidation] defined in this document.
``` json
{
  "code": -12207,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Variants validation [stdRdapVariantsValidation]."
}
```
9. If the JSON name _nameservers_ exists, the value shall pass the test Nameserver lookup  validation [stdRdapNameserverLookupValidation] defined in this document.
``` json
{
  "code": -12208,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Nameserver lookup validation [stdRdapNameserverLookupValidation]."
}
```
10. If the JSON name _secureDNS_ exists, the value shall pass the test Secure DNS validation [stdRdapSecureDnsValidation] defined in this document.
``` json
{
  "code": -12209,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Secure DNS validation [stdRdapSecureDnsValidation]."
}
```
11. If the JSON name _entities_ exists, the value shall pass the test Entities validation [stdRdapEntitiesValidation] defined in this document.
``` json
{
  "code": -12210,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Entities validation [stdRdapEntitiesValidation]."
}
```
12. If the JSON name _status_ exists, the value shall pass the test Status validation [stdRdapStatusValidation] defined in this document.
``` json
{
  "code": -12211,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Status validation [stdRdapStatusValidation]."
}
```
13. If the JSON name _publicIds_ exists, the value shall pass the test Public IDs validation [stdRdapPublicIdsValidation] defined in this document.
``` json
{
  "code": -12212,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Public IDs validation [stdRdapPublicIdsValidation]."
}
```
14. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12213,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
15. If the JSON name _links_ exists, the value shall pass the test Links validation [stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -12214,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
16. If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation] defined in this document.
``` json
{
  "code": -12215,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation]."
}
```
17. If the JSON name _events_ exists, the value shall pass the test Events Validation [stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12216,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
18. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12217,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
19. If the JSON name _notices_ exists and the domain object is not the topmost JSON object.
``` json
{
  "code": -12218,
  "value": "<name/value pair>",
  "message": "The value for the JSON name notices exists but domain object is not the topmost JSON object."
}
```
20. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [stdRdapConformanceValidation] defined in this document.
``` json
{
  "code": -12219,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```

## Entity lookup validation

Test group: [stdRdapEntityLookupValidation]  [](){ #id-stdRdapEntityLookupValidation }

The following steps should be used to test that an entity data structure is valid:

1. The _entity_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12300,
  "value": "<entity structure>",
  "message": "The entity structure is not syntactically valid."
}
```
2. The name of every name/value pairs shall be any of: _objectClassName_, _handle_, _vcardArray_, _roles_, _publicIds_, _entities_, _remarks_, _links_, _events_, _asEventActor_, _status_, _port43_, _notices_ or _rdapConformance_.
``` json
{
  "code": -12301,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: objectClassName, handle, vcardArray, roles, publicIds, entities, remarks, links, events, asEventActor, status, port43, notices or rdapConformance."
}
```
3. The JSON name/values of _objectClassName_, _handle_, _vcardArray_, _roles_, _publicIds_, _entities_, _remarks_, _links_, _events_, _asEventActor_, _status_, _port43_, _notices_ or _rdapConformance_ shall exist only once.
``` json
{
"code": -12302,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a domain structure was found more than once."
}
```
4. For the JSON name _objectClassName_, the value shall be "entity".
``` json
{
  "code": -12303,
  "value": "<name/value pair>",
  "message": "The JSON value is not "entity"."
}
```
5. If the JSON name _handle_ exists, the value shall be a JSON string data type.
``` json
{
  "code": -12304,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
6. If the JSON name title _vcardArray_ exists, the value shall be syntactically valid.
``` json
{
  "code": -12305,
  "value": "<name/value pair>",
  "message": " The value for the JSON name value is not a syntactically valid vcardArray."
}
```
7. If the JSON name _roles_ exists, the value shall pass the test Roles validation [stdRdapRolesValidation] defined in this document.
``` json
{
  "code": -12306,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Roles validation [stdRdapRolesValidation]."
}
```
8. If the JSON name _publicIds_ exists, the value shall pass the test Public IDs validation [stdRdapPublicIdsValidation] defined in this document.
``` json
{
  "code": -12307,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Public IDs validation [stdRdapPublicIdsValidation]."
}
```
9. If the JSON name _entities_ exists, the value shall pass the test Entities validation [stdRdapEntitiesValidation] defined in this document.
``` json
{
  "code": -12308,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Entities validation [stdRdapEntitiesValidation]."
}
```
10. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12309,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
11. If the JSON name _links_ exists, the value shall pass the test Links validation [stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -12310,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
12. If the JSON name _events_ exists, the value shall pass the test Events Validation [stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12311,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
13. If the JSON name _asEventActor_ exists, the value shall pass the test asEventActor Validation [stdRdapAsEventActorValidation] defined in this document.
``` json
{
  "code": -12312,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass asEventActor Validation [stdRdapAsEventActorValidation]."
}
```
14. If the JSON name _status_ exists, the value shall pass the test Status validation [stdRdapStatusValidation] defined in this document.
``` json
{
  "code": -12313,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Status validation [stdRdapStatusValidation]."
}
```
15. If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation] defined in this document.
``` json
{
  "code": -12314,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation]."
}
```
16. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12315,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
17. If the JSON name _notices_ exists and the entity object is not the topmost JSON object.
``` json
{
  "code": -12316,
  "value": "<name/value pair>",
  "message": "The value for the JSON name notices exists but entity object is not the topmost JSON object."
}
```
18. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [stdRdapConformanceValidation] defined in this document.
``` json
{
  "code": -12317,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```

## Nameserver lookup validation

Test group: [stdRdapNameserverLookupValidation]  [](){ #id-stdRdapNameserverLookupValidation }

The following steps should be used to test that a nameserver data structure is valid:

1. The _nameserver_ data structure must be a syntactically valid JSON object.
``` json
{
    "code": -12400,
    "value": "<nameserver structure>",
    "message": "The nameserver structure is not syntactically valid."
}
```
2. The name of every name/value pairs shall be any of: _objectClassName_, _handle_, _ldhName_, _unicodeName_, _ipAddresses_, _entities_, _status_, _remarks_, _links_, _port43_, _events_, _notices_ or _rdapConformance_.
``` json
{
    "code": -12401,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair is not of: objectClassName, handle, ldhName, unicodeName, ipAddresses, entities, status, remarks, links, port43, events, notices or rdapConformance."
}
```
3. The JSON name/values of _objectClassName_, _handle_, _ldhName_, _unicodeName_, _ipAddresses_, _entities_, _status_, _remarks_, _links_, _port43_, _events_, _notices_ or _rdapConformance_ shall exist only once.
``` json
{
  "code": -12402,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
4. For the JSON name _objectClassName_, the value shall be "nameserver".
``` json
{
  "code": -12403,
  "value": "<name/value pair>",
  "message": "The JSON value is not 'nameserver'."
}
```
5. If the JSON name _handle_ exists, the value shall be a JSON string data type.
``` json
{
  "code": -12404,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
6. If the JSON name _ldhName_ exists, the value shall pass the test LDH name [stdRdapLdhNameValidation] defined in this document.
``` json
{
  "code": -12405,
  "value": "<name/value pair>",
  "message": " The value for the JSON name value does not pass LDH name [stdRdapLdhNameValidation]."
}
```
7. If the JSON name _unicodeName_ exists, the value shall pass the test Unicode name [stdRdapUnicodeNameValidation] defined in this document.
``` json
{
  "code": -12406,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Unicode name [stdRdapUnicodeNameValidation]."
}
```
8. If the JSON name _ipAddresses_ exists, the value shall pass the test IP Addresses Validation [stdRdapIpAddressesValidation] defined in this document.
``` json
{
  "code": -12407,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass IP Addresses Validation [stdRdapIpAddressesValidation]."
}
```
9. If the JSON name _entities_ exists, the value shall pass the test Entities validation [stdRdapEntitiesValidation] defined in this document.
``` json
{
  "code": -12408,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Entities validation [stdRdapEntitiesValidation]."
}
```
10. If the JSON name _status_ exists, the value shall pass the test Status validation [stdRdapStatusValidation] defined in this document.
``` json
{
  "code": -12409,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Status validation [stdRdapStatusValidation]."
}
```
11. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12410,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
12. If the JSON name _links_ exists, the value shall pass the test Links validation [stdRdapLinksValidation] defined in this document.
``` json
{
"code": -12411,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
13. If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation] defined in this document.
``` json
{
  "code": -12412,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation]."
}
```
14. If the JSON name _events_ exists, the value shall pass the test Events Validation [stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12413,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
15. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
"code": -12414,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
16. If the JSON name _notices_ exists and the nameserver object is not the topmost JSON object.
``` json
{
"code": -12415,
"value": "<name/value pair>",
"message": "The value for the JSON name notices exists but nameserver object is not the topmost JSON object."
}
```
17. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [stdRdapConformanceValidation] defined in this document.
``` json
{
  "code": -12416,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```

## Help validation

Test group:[stdRdapHelpValidation]

The following steps should be used to test that a help data structure is valid:

1. The _help_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12500,
  "value": "<help structure>",
  "message": "The help structure is not syntactically valid."
}
```
2. The name of every name/value pairs shall be _notices_ or _rdapConformance_.
``` json
{
  "code": -12501,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: notices or rdapConformance."
}
```
3. The JSON name/values of _notices_ or _rdapConformance_ shall exist only once.
``` json
{
  "code": -12502,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
4. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12503,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
5. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [stdRdapConformanceValidation] defined in this document.
``` json
{
  "code": -12504,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```

## Nameservers search validation 

Test group: [stdRdapNameserversSearchValidation]  [](){ #id-stdRdapNameserversSearchValidation }

The following steps should be used to test that a nameserverSearchResults data
structure is valid:


1. The _nameserverSearchResults_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12600,
  "value": "<nameserver structure>",
  "message": "The nameserver structure is not syntactically valid."
}
```
2. The name of every name/value pairs shall be any of: _nameserverSearchResults_, _remarks_, events, notices or rdapConformance.
``` json
{
  "code": -12601,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: nameserverSearchResults, remarks, events, notices or rdapConformance."
}
```
3. The JSON name/values of _nameserverSearchResults_, _remarks_, _events_, _notices_ or rdapConformance shall exist only once.
``` json
{
  "code": -12602,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
3. The _nameserverSearchResults_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -12603,
  "value": "<nameserverSearchResults structure>",
  "message": "The nameserverSearchResults structure is not syntactically valid."
}
```
4. For every object (i.e. nameserver) of the JSON array, verify that the _nameserverSearchResults_ structure complies with:
    1. The object (i.e. nameserver) shall pass the Nameserver lookup validation [stdRdapNameserverLookupValidation] test.
``` json
{
  "code": -12604,
  "value": "<nameserver object>",
  "message": "The nameserver object does not pass Nameserver lookup validation [stdRdapNameserverLookupValidation]."
}
```
5. If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12605,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
6. If the JSON name _events_ exists, the value shall pass the test Events Validation [stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12606,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
7. If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12607,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
8. If the JSON name _notices_ exists and the object is not the topmost JSON object.
```json
{
  "code": -12608,
  "value": "<name/value pair>",
  "message": "The value for the JSON name notices exists but object is not the topmost JSON object."
} 
```
9. If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [stdRdapConformanceValidation] defined in this document.
```json
{
  "code": -12609,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```
