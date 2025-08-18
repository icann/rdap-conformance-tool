# Domain Tests

## RP Section 2.1 

Test group: [[rdapResponseProfile_2_1_Validation]](#id-rdapResponseProfile_2_1_Validation){ #id-rdapResponseProfile_2_1_Validation }

The following steps should be used to test the RDAP protocol section 2.1 of the RDAP Response Profile 2.1.

1. Test case [-46100](#id-testCase-46100){ #id-testCase-46100 }: If domain/<domain name> in the RDAP Query URI contains only A-label or NR-LDH labels, the topmost domain object shall contain a ldhName.
``` json
{
  "code": -46100,
  "value": "<domain object>",
  "message": "The RDAP Query URI contains only A-label or NR-LDH labels, the topmost domain object does not contain a ldhName member. See section 2.1 of the RDAP_Response_Profile_2_1."
}
```
2. Test case [-46101](#id-testCase-46101){ #id-testCase-46101 }: If domain/<domain name> in the RDAP Query URI contains one or more U-label, the topmost domain object shall contain an unicodeName.
``` json
{
  "code": -46101,
  "value": "<domain object>",
  "message": " The RDAP Query URI contains one or more U-label, the topmost domain object does not contain a unicodeName member. See section 2.1 of the RDAP_Response_Profile_2_1."
}
```

## RP Section 2.2 

Test group: [[rdapResponseProfile_2_2_Validation]](#id-rdapResponseProfile_2_2_Validation){ #id-rdapResponseProfile_2_2_Validation }

The following steps should be used to test the RDAP protocol section 2.2 of the RDAP Response Profile 2.1:

1. Test case [-46200](#id-testCase-46200){ #id-testCase-46200 }: The handle in the topmost _domain_ object shall comply with the following format specified in RFC5730: "(\w|_){1,80}-\w{1,8}".
``` json
{
  "code": -46200,
  "value": "<domain object>",
  "message": "The handle in the domain object does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730."
}
```
2. Test case [-46201](#id-testCase-46201){ #id-testCase-46201 }: If the handle in the topmost _domain_ object comply with the format: "(\w|_){1,80}-\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is registered in EPPROID.
``` json
{
  "code": -46201,
  "value": "<domain object>",
  "message": "The globally unique identifier in the domain object handle is not registered in EPPROID."
}
```
3. Test case [-46205](#id-testCase-46205){ #id-testCase-46205 }: If the handle in the topmost _domain_ object comply with the format: "(\w|_){1,80}-\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is not “ICANNRST”.
``` json
{
  "code": -46205,
  "value": "<domain object>",
  "message": "The globally unique identifier in the domain object handle is using an EPPROID reserved for testing by ICANN."
}
```


## RP Section 2.3.1.1 

Test group: [[rdapResponseProfile_2_3_1_1_Validation]](#id-rdapResponseProfile_2_3_1_1_Validation){ #id-rdapResponseProfile_2_3_1_1_Validation }

The following steps should be used to test the RDAP protocol section 2.3.1.1 of the RDAP Response Profile 2.1:

1. Test case [-46300](#id-testCase-46300){ #id-testCase-46300 }: Validate that an _eventAction_ of type "registration" exists in the topmost events structure.
```json
{
  "code": -46300,
  "value": "<events data structure>",
  "message": "An eventAction of type registration does not exists in the topmost events data structure. See section 2.3.1.1 of the RDAP_Response_Profile_2_1."
}
```

## RP Section 2.3.1.2 

Test group: [[rdapResponseProfile_2_3_1_2_Validation]](#id-rdapResponseProfile_2_3_1_2_Validation){ #id-rdapResponseProfile_2_3_1_2_Validation }

The following steps should be used to test the RDAP protocol section 2.3.1.2 of the  RDAP Response Profile 2.1:

1. Test case [-46400](#id-testCase-46400){ #id-testCase-46400 }: Validate that an _eventAction_ type "expiration" exists in the topmost events structure.
```json
{
  "code": -46400,
  "value": "<events data structure>",
  "message": "An eventAction of type expiration does not exists in the topmost events data structure. See section 2.3.1.2 of the RDAP_Response_Profile_2_1."
}
```

## Required Notices

Test group: [[rdapResponseProfile_notices_included_Validation]](#id-rdapResponseProfile_notices_included_Validation){ #id-rdapResponseProfile_notices_included_Validation }

The following steps should be used to test that a notices member appear in the RDAP  response:

1. Test case [-46500](#id-testCase-46500){ #id-testCase-46500 }: Validate that a _notices_ member appears in the RDAP response.
```json
{
  "code": -46500,
  "value": "<RDAP response>",
  "message": "A notices members does not appear in the RDAP response."
}
```

## RP Section 2.6.3

Test group: [[rdapResponseProfile_2_6_3_Validation]](#id-rdapResponseProfile_2_6_3_Validation){ #id-rdapResponseProfile_2_6_3_Validation }

The following steps should be used to test the RDAP protocol section 2.6.3 of the  RDAP Response Profile 2.1:

1. Test case [-46600](#id-testCase-46600){ #id-testCase-46600 }: Validate that the _notices_ member contains an element in the JSON array with a title
“Status Codes”, a description containing the string “For more information on domain
status codes, please visit https://icann.org/epp” and a links member with an href
containing "https://icann.org/epp".
```json
{
  "code": -46600,
  "value": "<notices structure>",
  "message": "The notice for https://icann.org/epp was not found."
}
```
See [Notice of EPP Status Codes][notice-of-epp-status-codes] in the conformance considerations for more information.

## RP Section 2.11 

Test group: [[rdapResponseProfile_2_11_Validation]](#id-rdapResponseProfile_2_11_Validation){ #id-rdapResponseProfile_2_11_Validation }

The following steps should be used to test the RDAP protocol section 2.11 of the  RDAP Response Profile 2.1:

1. Test case [-46700](#id-testCase-46700){ #id-testCase-46700 }: Validate that the _notices_ member contains an element in the JSON array with a title
“RDDS Inaccuracy Complaint Form”, a description containing the string “URL of the
ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf” and a links member
with an href containing "https://icann.org/wicf".
``` json
{
  "code": -46700,
  "value": "<notices structure>",
  "message": "The notice for https://icann.org/wicf was not found."
}
```
See [Notice of RDDS Inaccuracy Report][notice-of-rdds-inaccuracy-report] in the conformance considerations for more information.
2. Test case [-65300](#id-testCase-65300){ #id-testCase-65300 }: Validate that the query “/domain/not-a-domain.invalid” yields an HTTP status code of 404.
``` json
{
  "code": -65300,
  "value": "<http status code>",
  "message": "A query for an invalid domain name did not yield a 404 response."
}
```


## RP Section 2.10 

Test group: [[rdapResponseProfile_2_10_Validation]](#id-rdapResponseProfile_2_10_Validation){ #id-rdapResponseProfile_2_10_Validation }

The following steps should be used to test the RDAP protocol section 2.10 of the  RDAP Response Profile 2.1:

1. Test case [-46800](#id-testCase-46800){ #id-testCase-46800 }: Validate that a _secureDNS_ member is included in the domain object.
```json
{
  "code": -46800,
  "value": "<domain object>",
  "message": "A secureDNS member does not appear in the domain object."
}
```
2. Test case [-46801](#id-testCase-46801){ #id-testCase-46801 }: The JSON name _delegationSigned_ shall appear.
```json
{
    "code": -46801,
    "value": "<secureDNS structure>",
    "message": "The delegationSigned element does not exist."
}
```
3. Test case [-46802](#id-testCase-46802){ #id-testCase-46802 }: If delegationSigned has a value of true, one dsData name/values or one keyData name/value shall appear.
```json
{
  "code": -46802,
  "value": "<secureDNS structure>",
  "message": "delegationSigned value is true, but no dsData nor keyData name/value pair exists."
}
```

## RFC 5731 Validation 

Test group: [[rdapResponseProfile_rfc5731_Validation]](#id-rdapResponseProfile_rfc5731_Validation){ #id-rdapResponseProfile_rfc5731_Validation }

The following steps should be used to test that the status values comply with  RFC5731:

1. Test case [-46900](#id-testCase-46900){ #id-testCase-46900 }: Validate that the values of the _status_ member in the topmost object comply with the following:
    1. "active" status MUST NOT be combined with any other status.
    2. "pending delete" status MUST NOT be combined with either "client delete prohibited" or "server delete prohibited" status.
    3. "pending renew" status MUST NOT be combined with either "client renew prohibited" or "server renew prohibited" status.
    4. "pending transfer" status MUST NOT be combined with either "client transfer prohibited" or "server transfer prohibited" status.
    5. "pending update" status MUST NOT be combined with either "client update prohibited" or "server update prohibited" status.
    6. The pending create, pending delete, pending renew, pending transfer, and pending update status values MUST NOT be combined with each other.
```json
{
  "code": -46900,
  "value": "<status data structure>",
  "message": "The values of the status data structure does not comply with RFC5731."
}
```

## RFC 3915 Validation 

Test group: [[rdapResponseProfile_rfc3915_Validation]](#id-rdapResponseProfile_rfc3915_Validation){ #id-rdapResponseProfile_rfc3915_Validation }

The following steps should be used to test that the status values comply with RFC3915:

1. Test case [-47000](#id-testCase-47000){ #id-testCase-47000 }: Validate that the values of the _status_ member in the topmost object comply with the following:
    1. "redemption period" status MUST only be combined with "pending delete".
    2. "pending restore" status MUST only be combined with "pending delete".
```json
{
  "code": -47000,
  "value": "<status data structure>",
  "message": "The values of the status data structure does not comply with RFC3915."
}
```
2. Test case [-47001](#id-testCase-47001){ #id-testCase-47001 }: If the topmost object is a domain, validate that if the “status” member has the value “redemption period” only if it also has the value “pending delete”.
```json
{
  "code": -47001,
  "value": "<status data structure>",
  "message": "'redemption period' is only valid with a status of 'pending delete'"
}
```
3. Test case [-47002](#id-testCase-47002){ #id-testCase-47002 }: If the topmost object is a domain, validate that if the “status” member has the value “pending restore” only if it also has the value “pending delete”.
```json
{
  "code": -47002,
  "value": "<status data structure>",
  "message": "'pending restore' is only valid with a status of 'pending delete'"
}
```


## RP Section 2.6.1 

Test group: [[rdapResponseProfile_2_6_1_Validation]](#id-rdapResponseProfile_2_6_1_Validation){ #id-rdapResponseProfile_2_6_1_Validation }

The following steps should be used to test the RDAP protocol section 2.6.1 of the RDAP Response Profile 2.1:

1. Test case [-47100](#id-testCase-47100){ #id-testCase-47100 }: Validate that a _status_ member in the topmost object contain at least one value.
```json
{
  "code": -47100,
  "value": "<status data structure>",
  "message": "The status member does not contain at least one value."
}
```

## RP Section 2.9.1 and 2.9.2

Test group: [[rdapResponseProfile_2_9_1_and_2_9_2_Validation]](#id-rdapResponseProfile_2_9_1_and_2_9_2_Validation){ #id-rdapResponseProfile_2_9_1_and_2_9_2_Validation }

The following steps should be used to test the RDAP protocol section 2.9.1 and 2.9.2 of the RDAP Response Profile 2.1:

1. Test case [-47200](#id-testCase-47200){ #id-testCase-47200 }: If the _nameservers_ member is included within the _domain_ object, validate that all nameserver objects contain the ldhName element.
``` json
{
  "code": -47200,
  "value": "<nameservers data structure>",
  "message": "A nameserver object without ldhName was found."
}
```
2. Test case [-47201](#id-testCase-47201){ #id-testCase-47201 }: If the _nameservers_ member is included within the _domain_ object, validate that all handles in the nameserver objects comply with the following format specified in RFC5730: "(\w|_){1,80}-\w{1,8}".
```json
{
  "code": -47201,
  "value": "<nameserver object>",
  "message": "The handle in the nameserver object does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730."
}
```
3. Test case [-47202](#id-testCase-47202){ #id-testCase-47202 }: If the _nameservers_ member is included within the _domain_ object, validate that the string followed by a hyphen ("-", ASCII value 0x002D) is registered in **EPPROID** for all the handles that comply with the format "(\w|_){1,80}-\w{1,8}".
```json
{
  "code": -47202,
  "value": "<nameserver object>",
  "message": "The globally unique identifier in the nameserver object handle is not registered in EPPROID."
}
```
4. Test case [-47203](#id-testCase-47203){ #id-testCase-47203 }: If the _nameservers_ member is included within the _domain_ object AND THE QUERY IS TO A GLTD REGISTRAR (i.e. --gtld-registrar option) and at least one nameserver object contains a _handle_ or a _status_ element, validate that all nameserver objects include a handle and a status element.
```json
{
  "code": -47203,
  "value": "<nameserver object>",
  "message": "The handle or status in the nameserver object is not included."
}
```
5. Test case [-47204](#id-testCase-47204){ #id-testCase-47204 }: If the _nameservers_ member is included within the domain object, validate that all _status_ elements included in the nameserver objects comply with the following:
    1. "active" status MAY only be combined with "associated" status.
    2. "associated" status MAY be combined with any status.
    3. "pending delete" status MUST NOT be combined with either "client delete prohibited" or "server delete prohibited" status.
    4. "pending update" status MUST NOT be combined with either "client update prohibited" or "server update prohibited" status.
    5. The pending create, pending delete, pending renew, pending transfer, and pending update status values MUST NOT be combined with each other.
```json
{
  "code": -47204,
  "value": "<status data structure>",
  "message": "The values of the status data structure does not comply with RFC5732."
}
```
6. Test case [-47205](#id-testCase-47205){ #id-testCase-47205 }: If the nameservers member is included within the domain object, validate that the handle is a string followed by a hyphen ("-", ASCII value 0x002D) is not “ICANNRST”.
```json
{
  "code": -47205,
  "value": "<nameserver object>",
  "message": "The globally unique identifier in the nameserver object handle is using an EPPROID reserved for testing by ICANN."
}
```


## RP Section 2.4.1 

Test group: [[rdapResponseProfile_2_4_1_Validation]](#id-rdapResponseProfile_2_4_1_Validation){ #id-rdapResponseProfile_2_4_1_Validation }

The following steps should be used to test the RDAP protocol section 2. 4 .1 of the  RDAP Response Profile 2.1:

1. Test case [-47300](#id-testCase-47300){ #id-testCase-47300 }: An _entity_ with the registrar role within the topmost domain object shall exist.
```json
{
  "code": -47300,
  "value": "<domain object data structure>",
  "message": "An entity with the registrar role was not found in the domain topmost object."
}
```
2. Test case [-47301](#id-testCase-47301){ #id-testCase-47301 }: Only one _entity_ with the registrar role within the topmost domain object shall exist.
```json
{
  "code": -47301,
  "value": "<domain object data structure>",
  "message": "More than one entities with the registrar role were found in the domain topmost object."
}
```
3. Test case [-47302](#id-testCase-47302){ #id-testCase-47302 }: For the _entity_ with the registrar role within the topmost domain object, validate that a fn member is included in all of the vcard objects.
```json
{
"code": -47302,
"value": "<entity data structure>",
"message": "An fn member was not found in one or more vcard objects of the entity with the registrar role."
}
```

## RP Section 2.4.2 and 2.4.3 

Test group: [[rdapResponseProfile_2_4_2_and_2_4_3_Validation]](#id-rdapResponseProfile_2_4_2_and_2_4_3_Validation){ #id-rdapResponseProfile_2_4_2_and_2_4_3_Validation }

The following steps should be used to test the RDAP protocol section 2. 4. 2 and 2.4.3  of the RDAP Response Profile 2.1:

1. Test case [-47400](#id-testCase-47400){ #id-testCase-47400 }: For the _entity_ with the registrar role within the topmost object, validate that a _publicIds_ member is included.
```json
{
  "code": -47400,
  "value": "<entity data structure>",
  "message": "A publicIds member is not included in the entity with the registrar role."
}
```
2. Test case [-47401](#id-testCase-47401){ #id-testCase-47401 }: For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is included, validate that the identifier member is a positive integer.
```json
{
  "code": -47401,
  "value": "<publicIds data structure>",
  "message": "The identifier of the publicIds member of the entity with the registrar role is not a positive integer."
}
```
3. Test case [-47402](#id-testCase-47402){ #id-testCase-47402 }: For the _entity_ with the registrar role within the domain object, validate that the _handle_ member is a positive integer.
```json
{
  "code": -47402,
  "value": "<publicIds data structure>",
  "message": "The handle of the entity with the registrar role is not a positive integer."
}
```
4. Test case [-47403](#id-testCase-47403){ #id-testCase-47403 }: For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is included, validate that the _identifier_ member equals the _handle_ member.
```json
{
  "code": -47403,
  "value": "<entity data structure>",
  "message": "The identifier of the publicIds member of the entity with the registrar role is not equal to the handle member."
}
```
5. Test case [-47404](#id-testCase-47404){ #id-testCase-47404 }: For the _entity_ with the registrar role within the domain object, validate that the value of the _handle_ member exists in the **registrarId**.
```json
{
  "code": -47404,
  "value": "<handle> + \n\n + <registrarId>",
  "message": "The handle references an IANA Registrar ID that does not exist in the **registrarId** ."
}
```

## RP Section 2.4.5 

Test group: [[rdapResponseProfile_2_4_5_Validation]](#id-rdapResponseProfile_2_4_5_Validation){ #id-rdapResponseProfile_2_4_5_Validation }

1. Test case [-47500](#id-testCase-47500){ #id-testCase-47500 }: For the _entity_ with the registrar role within the _domain_ object, validate that an _entity_
with the abuse role is included, and the entity with the abuse role includes a tel and
email members in all the vcard objects.
```json
{
  "code": -47500,
  "value": "<entity data structure>",
  "message": "Tel and email members were not found for the entity within the entity with the abuse role in the topmost domain object."
}
```
