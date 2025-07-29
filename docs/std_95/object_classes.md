# Object Class Tests

## Domain Lookup Validation

Test group: [[stdRdapDomainLookupValidation]](#id-stdRdapDomainLookupValidation){ #id-stdRdapDomainLookupValidation }

The following steps should be used to test that a domain data structure is valid:

1. Test case [-12200](#id-testCase-12200){ #id-testCase-12200 }: The _domain_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12200,
  "value": "<domain structure>",
  "message": "The domain structure is not syntactically valid."
}
```
2. Test case [-12201](#id-testCase-12201){ #id-testCase-12201 }: The name of every name/value pairs shall be any of: _objectClassName_, _handle_, _ldhName_, _unicodeName_, _variants_, _nameservers_, _secureDNS_, _entities_, _status_, _publicIds_, remarks, links, port43, events, notices or rdapConformance.
``` json
{
  "code": -12201,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: objectClassName, handle, ldhName, unicodeName, variants, nameservers, secureDNS, entities, status, publicIds, remarks, links, port43, events, notices or rdapConformance."
}
```
3. Test case [-12202](#id-testCase-12202){ #id-testCase-12202 }: The JSON name/values of _objectClassName_, _handle_, _ldhName_, _unicodeName_, _variants_, nameservers, secureDNS, entities, status, publicIds, remarks, links, port43, events, notices or rdapConformance shall appear only once. 
``` json
{
  "code": -12202,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a domain structure was found more than once."
}
```
4. Test case [-12203](#id-testCase-12203){ #id-testCase-12203 }: For the JSON name _objectClassName_, the value shall be "domain".
``` json
{
  "code": -12203,
  "value": "<name/value pair>",
  "message": "The JSON value is not 'domain'."
}
```
5. Test case [-12204](#id-testCase-12204){ #id-testCase-12204 }: If the JSON name _handle_ exists, the value shall be a JSON string data type.
``` json
{
  "code": -12204,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
6. Test case [-12205](#id-testCase-12205){ #id-testCase-12205 }: If the JSON name _ldhName_, the value shall pass the test LDH name [[stdRdapLdhNameValidation]][id-stdRdapLdhNameValidation] defined in this document.
``` json
{
  "code": -12205,
  "value": "<name/value pair>",
  "message": " The value for the JSON name value does not pass LDH name [stdRdapLdhNameValidation]."
}
```
7. Test case [-12206](#id-testCase-12206){ #id-testCase-12206 }: If the JSON name _unicodeName_ exists, the value shall pass the test Unicode name  [[stdRdapUnicodeNameValidation]][id-stdRdapUnicodeNameValidation] defined in this document.
``` json
{
  "code": -12206,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Unicode name [stdRdapUnicodeNameValidation]."
}
```
8. Test case [-12207](#id-testCase-12207){ #id-testCase-12207 }: If the JSON name _variants_ exists, the value shall pass the test Variants validation [[stdRdapVariantsValidation]][id-stdRdapVariantsValidation] defined in this document.
``` json
{
  "code": -12207,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Variants validation [stdRdapVariantsValidation]."
}
```
9. Test case [-12208](#id-testCase-12208){ #id-testCase-12208 }: If the JSON name _nameservers_ exists, the value shall pass the test Nameserver lookup  validation [[stdRdapNameserverLookupValidation]][id-stdRdapNameserverLookupValidation] defined in this document.
``` json
{
  "code": -12208,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Nameserver lookup validation [stdRdapNameserverLookupValidation]."
}
```
10. Test case [-12209](#id-testCase-12209){ #id-testCase-12209 }: If the JSON name _secureDNS_ exists, the value shall pass the test Secure DNS validation [[stdRdapSecureDnsValidation]][id-stdRdapSecureDnsValidation] defined in this document.
``` json
{
  "code": -12209,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Secure DNS validation [stdRdapSecureDnsValidation]."
}
```
11. Test case [-12210](#id-testCase-12210){ #id-testCase-12210 }: If the JSON name _entities_ exists, the value shall pass the test Entities validation [[stdRdapEntitiesValidation]][id-stdRdapEntitiesValidation] defined in this document.
``` json
{
  "code": -12210,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Entities validation [stdRdapEntitiesValidation]."
}
```
12. Test case [-12211](#id-testCase-12211){ #id-testCase-12211 }: If the JSON name _status_ exists, the value shall pass the test Status validation [[stdRdapStatusValidation]][id-stdRdapStatusValidation] defined in this document.
``` json
{
  "code": -12211,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Status validation [stdRdapStatusValidation]."
}
```
13. Test case [-12212](#id-testCase-12212){ #id-testCase-12212 }: If the JSON name _publicIds_ exists, the value shall pass the test Public IDs validation [[stdRdapPublicIdsValidation]][id-stdRdapPublicIdsValidation] defined in this document.
``` json
{
  "code": -12212,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Public IDs validation [stdRdapPublicIdsValidation]."
}
```
14. Test case [-12213](#id-testCase-12213){ #id-testCase-12213 }: If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12213,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
15. Test case [-12214](#id-testCase-12214){ #id-testCase-12214 }: If the JSON name _links_ exists, the value shall pass the test Links validation [[stdRdapLinksValidation]][id-stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -12214,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
16. Test case [-12215](#id-testCase-12215){ #id-testCase-12215 }: If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server [[stdRdapPort43WhoisServerValidation]][id-stdRdapPort43WhoisServerValidation] defined in this document.
``` json
{
  "code": -12215,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation]."
}
```
17. Test case [-12216](#id-testCase-12216){ #id-testCase-12216 }: If the JSON name _events_ exists, the value shall pass the test Events Validation [[stdRdapEventsValidation]][id-stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12216,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
18. Test case [-12217](#id-testCase-12217){ #id-testCase-12217 }: If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12217,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
19. Test case [-12218](#id-testCase-12218){ #id-testCase-12218 }: If the JSON name _notices_ exists and the domain object is not the topmost JSON object.
``` json
{
  "code": -12218,
  "value": "<name/value pair>",
  "message": "The value for the JSON name notices exists but domain object is not the topmost JSON object."
}
```
20. Test case [-12219](#id-testCase-12219){ #id-testCase-12219 }: If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [[stdRdapConformanceValidation]][id-stdRdapConformanceValidation] defined in this document.
``` json
{
  "code": -12219,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```

## Entity lookup validation

Test group: [[stdRdapEntityLookupValidation]](#id-stdRdapEntityLookupValidation){ #id-stdRdapEntityLookupValidation }

The following steps should be used to test that an entity data structure is valid:

1. Test case [-12300](#id-testCase-12300){ #id-testCase-12300 }: The _entity_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12300,
  "value": "<entity structure>",
  "message": "The entity structure is not syntactically valid."
}
```
2. Test case [-12301](#id-testCase-12301){ #id-testCase-12301 }: The name of every name/value pairs shall be any of: _objectClassName_, _handle_, _vcardArray_, _roles_, _publicIds_, _entities_, _remarks_, _links_, _events_, _asEventActor_, _status_, _port43_, _notices_ or _rdapConformance_.
``` json
{
  "code": -12301,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: objectClassName, handle, vcardArray, roles, publicIds, entities, remarks, links, events, asEventActor, status, port43, notices or rdapConformance."
}
```
3. Test case [-12302](#id-testCase-12302){ #id-testCase-12302 }: The JSON name/values of _objectClassName_, _handle_, _vcardArray_, _roles_, _publicIds_, _entities_, _remarks_, _links_, _events_, _asEventActor_, _status_, _port43_, _notices_ or _rdapConformance_ shall exist only once.
``` json
{
"code": -12302,
"value": "<name/value pair>",
"message": "The name in the name/value pair of a domain structure was found more than once."
}
```
4. Test case [-12303](#id-testCase-12303){ #id-testCase-12303 }: For the JSON name _objectClassName_, the value shall be "entity".
``` json
{
  "code": -12303,
  "value": "<name/value pair>",
  "message": "The JSON value is not 'entity'."
}
```
5. Test case [-12304](#id-testCase-12304){ #id-testCase-12304 }: If the JSON name _handle_ exists, the value shall be a JSON string data type.
``` json
{
  "code": -12304,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
6. Test case [-12305](#id-testCase-12305){ #id-testCase-12305 }: If the JSON name title _vcardArray_ exists, the value shall be syntactically valid.
``` json
{
  "code": -12305,
  "value": "<name/value pair>",
  "message": " The value for the JSON name value is not a syntactically valid vcardArray."
}
```
7. Test case [-12306](#id-testCase-12306){ #id-testCase-12306 }: If the JSON name _roles_ exists, the value shall pass the test Roles validation [[stdRdapRolesValidation]][id-stdRdapRolesValidation] defined in this document.
``` json
{
  "code": -12306,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Roles validation [stdRdapRolesValidation]."
}
```
8. Test case [-12307](#id-testCase-12307){ #id-testCase-12307 }: If the JSON name _publicIds_ exists, the value shall pass the test Public IDs validation [[stdRdapPublicIdsValidation]][id-stdRdapPublicIdsValidation] defined in this document.
``` json
{
  "code": -12307,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Public IDs validation [stdRdapPublicIdsValidation]."
}
```
9. Test case [-12308](#id-testCase-12308){ #id-testCase-12308 }: If the JSON name _entities_ exists, the value shall pass the test Entities validation [[stdRdapEntitiesValidation]][id-stdRdapEntitiesValidation] defined in this document.
``` json
{
  "code": -12308,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Entities validation [stdRdapEntitiesValidation]."
}
```
10. Test case [-12309](#id-testCase-12309){ #id-testCase-12309 }: If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12309,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
11. Test case [-12310](#id-testCase-12310){ #id-testCase-12310 }: If the JSON name _links_ exists, the value shall pass the test Links validation [[stdRdapLinksValidation]][id-stdRdapLinksValidation] defined in this document.
``` json
{
  "code": -12310,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
12. Test case [-12311](#id-testCase-12311){ #id-testCase-12311 }: If the JSON name _events_ exists, the value shall pass the test Events Validation [[stdRdapEventsValidation]][id-stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12311,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
13. Test case [-12312](#id-testCase-12312){ #id-testCase-12312 }: If the JSON name _asEventActor_ exists, the value shall pass the test asEventActor Validation [[stdRdapAsEventActorValidation]][id-stdRdapAsEventActorValidation] defined in this document.
``` json
{
  "code": -12312,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass asEventActor Validation [stdRdapAsEventActorValidation]."
}
```
14. Test case [-12313](#id-testCase-12313){ #id-testCase-12313 }: If the JSON name _status_ exists, the value shall pass the test Status validation [[stdRdapStatusValidation]][id-stdRdapStatusValidation] defined in this document.
``` json
{
  "code": -12313,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Status validation [stdRdapStatusValidation]."
}
```
15. Test case [-12314](#id-testCase-12314){ #id-testCase-12314 }: If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server [[stdRdapPort43WhoisServerValidation]][id-stdRdapPort43WhoisServerValidation] defined in this document.
``` json
{
  "code": -12314,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation]."
}
```
16. Test case [-12315](#id-testCase-12315){ #id-testCase-12315 }: If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12315,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
17. Test case [-12316](#id-testCase-12316){ #id-testCase-12316 }: If the JSON name _notices_ exists and the entity object is not the topmost JSON object.
``` json
{
  "code": -12316,
  "value": "<name/value pair>",
  "message": "The value for the JSON name notices exists but entity object is not the topmost JSON object."
}
```
18. Test case [-12317](#id-testCase-12317){ #id-testCase-12317 }: If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [[stdRdapConformanceValidation]][id-stdRdapConformanceValidation] defined in this document.
``` json
{
  "code": -12317,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```

## Nameserver lookup validation

Test group: [[stdRdapNameserverLookupValidation]](#id-stdRdapNameserverLookupValidation){ #id-stdRdapNameserverLookupValidation }

The following steps should be used to test that a nameserver data structure is valid:

1. Test case [-12400](#id-testCase-12400){ #id-testCase-12400 }: The _nameserver_ data structure must be a syntactically valid JSON object.
``` json
{
    "code": -12400,
    "value": "<nameserver structure>",
    "message": "The nameserver structure is not syntactically valid."
}
```
2. Test case [-12401](#id-testCase-12401){ #id-testCase-12401 }: The name of every name/value pairs shall be any of: _objectClassName_, _handle_, _ldhName_, _unicodeName_, _ipAddresses_, _entities_, _status_, _remarks_, _links_, _port43_, _events_, _notices_ or _rdapConformance_.
``` json
{
    "code": -12401,
    "value": "<name/value pair>",
    "message": "The name in the name/value pair is not of: objectClassName, handle, ldhName, unicodeName, ipAddresses, entities, status, remarks, links, port43, events, notices or rdapConformance."
}
```
3. Test case [-12402](#id-testCase-12402){ #id-testCase-12402 }: The JSON name/values of _objectClassName_, _handle_, _ldhName_, _unicodeName_, _ipAddresses_, _entities_, _status_, _remarks_, _links_, _port43_, _events_, _notices_ or _rdapConformance_ shall exist only once.
``` json
{
  "code": -12402,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
4. Test case [-12403](#id-testCase-12403){ #id-testCase-12403 }: For the JSON name _objectClassName_, the value shall be "nameserver".
``` json
{
  "code": -12403,
  "value": "<name/value pair>",
  "message": "The JSON value is not 'nameserver'."
}
```
5. Test case [-12404](#id-testCase-12404){ #id-testCase-12404 }: If the JSON name _handle_ exists, the value shall be a JSON string data type.
``` json
{
  "code": -12404,
  "value": "<name/value pair>",
  "message": "The JSON value is not a string."
}
```
6. Test case [-12405](#id-testCase-12405){ #id-testCase-12405 }: If the JSON name _ldhName_ exists, the value shall pass the test LDH name [[stdRdapLdhNameValidation]][id-stdRdapLdhNameValidation] defined in this document.
``` json
{
  "code": -12405,
  "value": "<name/value pair>",
  "message": " The value for the JSON name value does not pass LDH name [stdRdapLdhNameValidation]."
}
```
7. Test case [-12406](#id-testCase-12406){ #id-testCase-12406 }: If the JSON name _unicodeName_ exists, the value shall pass the test Unicode name [[stdRdapUnicodeNameValidation]][id-stdRdapUnicodeNameValidation] defined in this document.
``` json
{
  "code": -12406,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Unicode name [stdRdapUnicodeNameValidation]."
}
```
8. Test case [-12407](#id-testCase-12407){ #id-testCase-12407 }: If the JSON name _ipAddresses_ exists, the value shall pass the test IP Addresses Validation [[stdRdapIpAddressesValidation]][id-stdRdapIpAddressesValidation] defined in this document.
``` json
{
  "code": -12407,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass IP Addresses Validation [stdRdapIpAddressesValidation]."
}
```
9. Test case [-12408](#id-testCase-12408){ #id-testCase-12408 }: If the JSON name _entities_ exists, the value shall pass the test Entities validation [[stdRdapEntitiesValidation]][id-stdRdapEntitiesValidation] defined in this document.
``` json
{
  "code": -12408,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Entities validation [stdRdapEntitiesValidation]."
}
```
10. Test case [-12409](#id-testCase-12409){ #id-testCase-12409 }: If the JSON name _status_ exists, the value shall pass the test Status validation [[stdRdapStatusValidation]][id-stdRdapStatusValidation] defined in this document.
``` json
{
  "code": -12409,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Status validation [stdRdapStatusValidation]."
}
```
11. Test case [-12410](#id-testCase-12410){ #id-testCase-12410 }: If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12410,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
12. Test case [-12411](#id-testCase-12411){ #id-testCase-12411 }: If the JSON name _links_ exists, the value shall pass the test Links validation [[stdRdapLinksValidation]][id-stdRdapLinksValidation] defined in this document.
``` json
{
"code": -12411,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Links validation [stdRdapLinksValidation]."
}
```
13. Test case [-12412](#id-testCase-12412){ #id-testCase-12412 }: If the JSON name _port43_ exists, the value shall pass the test Port 43 WHOIS Server [[stdRdapPort43WhoisServerValidation]][id-stdRdapPort43WhoisServerValidation] defined in this document.
``` json
{
  "code": -12412,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Port 43 WHOIS Server [stdRdapPort43WhoisServerValidation]."
}
```
14. Test case [-12413](#id-testCase-12413){ #id-testCase-12413 }: If the JSON name _events_ exists, the value shall pass the test Events Validation [[stdRdapEventsValidation]][id-stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12413,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
15. Test case [-12414](#id-testCase-12414){ #id-testCase-12414 }: If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
"code": -12414,
"value": "<name/value pair>",
"message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
16. Test case [-12415](#id-testCase-12415){ #id-testCase-12415 }: If the JSON name _notices_ exists and the nameserver object is not the topmost JSON object.
``` json
{
"code": -12415,
"value": "<name/value pair>",
"message": "The value for the JSON name notices exists but nameserver object is not the topmost JSON object."
}
```
17. Test case [-12416](#id-testCase-12416){ #id-testCase-12416 }: If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [[stdRdapConformanceValidation]][id-stdRdapConformanceValidation] defined in this document.
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

1. Test case [-12500](#id-testCase-12500){ #id-testCase-12500 }: The _help_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12500,
  "value": "<help structure>",
  "message": "The help structure is not syntactically valid."
}
```
2. Test case [-12501](#id-testCase-12501){ #id-testCase-12501 }: The name of every name/value pairs shall be _notices_ or _rdapConformance_.
``` json
{
  "code": -12501,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: notices or rdapConformance."
}
```
3. Test case [-12502](#id-testCase-12502){ #id-testCase-12502 }: The JSON name/values of _notices_ or _rdapConformance_ shall exist only once.
``` json
{
  "code": -12502,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
4. Test case [-12503](#id-testCase-12503){ #id-testCase-12503 }: If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12503,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
5. Test case [-12504](#id-testCase-12504){ #id-testCase-12504 }: If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [[stdRdapConformanceValidation]][id-stdRdapConformanceValidation] defined in this document.
``` json
{
  "code": -12504,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```

## Nameservers search validation 

Test group: [[stdRdapNameserversSearchValidation]](#id-stdRdapNameserversSearchValidation){ #id-stdRdapNameserversSearchValidation }

The following steps should be used to test that a nameserverSearchResults data
structure is valid:


1. Test case [-12600](#id-testCase-12600){ #id-testCase-12600 }: The _nameserverSearchResults_ data structure must be a syntactically valid JSON object.
``` json
{
  "code": -12600,
  "value": "<nameserver structure>",
  "message": "The nameserver structure is not syntactically valid."
}
```
2. Test case [-12601](#id-testCase-12601){ #id-testCase-12601 }: The name of every name/value pairs shall be any of: _nameserverSearchResults_, _remarks_, events, notices or rdapConformance.
``` json
{
  "code": -12601,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair is not of: nameserverSearchResults, remarks, events, notices or rdapConformance."
}
```
3. Test case [-12602](#id-testCase-12602){ #id-testCase-12602 }: The JSON name/values of _nameserverSearchResults_, _remarks_, _events_, _notices_ or rdapConformance shall exist only once.
``` json
{
  "code": -12602,
  "value": "<name/value pair>",
  "message": "The name in the name/value pair of a link structure was found more than once."
}
```
3. Test case [-12603](#id-testCase-12603){ #id-testCase-12603 }: The _nameserverSearchResults_ data structure must be a syntactically valid JSON array.
``` json
{
  "code": -12603,
  "value": "<nameserverSearchResults structure>",
  "message": "The nameserverSearchResults structure is not syntactically valid."
}
```
4. For every object (i.e. nameserver) of the JSON array, verify that the _nameserverSearchResults_ structure complies with:
    1. Test case [-12604](#id-testCase-12604){ #id-testCase-12604 }: The object (i.e. nameserver) shall pass the Nameserver lookup validation [[stdRdapNameserverLookupValidation]][id-stdRdapNameserverLookupValidation] test.
``` json
{
  "code": -12604,
  "value": "<nameserver object>",
  "message": "The nameserver object does not pass Nameserver lookup validation [stdRdapNameserverLookupValidation]."
}
```
5. Test case [-12605](#id-testCase-12605){ #id-testCase-12605 }: If the JSON name _remarks_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12605,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
6. Test case [-12606](#id-testCase-12606){ #id-testCase-12606 }: If the JSON name _events_ exists, the value shall pass the test Events Validation [[stdRdapEventsValidation]][id-stdRdapEventsValidation] defined in this document.
``` json
{
  "code": -12606,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Events Validation [stdRdapEventsValidation]."
}
```
7. Test case [-12607](#id-testCase-12607){ #id-testCase-12607 }: If the JSON name _notices_ exists, the value shall pass the test Notices and Remarks Validation [[stdRdapNoticesRemarksValidation]][id-stdRdapNoticesRemarksValidation] defined in this document.
``` json
{
  "code": -12607,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass Notices and Remarks Validation [stdRdapNoticesRemarksValidation]."
}
```
8. Test case [-12608](#id-testCase-12608){ #id-testCase-12608 }: If the JSON name _notices_ exists and the object is not the topmost JSON object.
```json
{
  "code": -12608,
  "value": "<name/value pair>",
  "message": "The value for the JSON name notices exists but object is not the topmost JSON object."
} 
```
9. Test case [-12609](#id-testCase-12609){ #id-testCase-12609 }: If the JSON name _rdapConformance_ exists, the value shall pass the test RDAP Conformance validation [[stdRdapConformanceValidation]][id-stdRdapConformanceValidation] defined in this document.
```json
{
  "code": -12609,
  "value": "<name/value pair>",
  "message": "The value for the JSON name value does not pass RDAP Conformance validation [stdRdapConformanceValidation]."
}
```
10. Test case [-12610](#id-testCase-12610){ #id-testCase-12610 }: The nameserverSearchResults value must exist.
```json
{
  "code": -12610,
  "value": "<response>",
  "message": "The nameserverSearchResults structure is required."
}
```