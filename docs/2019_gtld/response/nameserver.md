# Nameserver Tests

#### RP Section 4.1 

Test group: [[rdapResponseProfile_4_1_Validation]](#id-rdapResponseProfile_4_1_Validation){ #id-rdapResponseProfile_4_1_Validation }

The following steps should be used to test the RDAP protocol section 4 .1 of the RDAP Response Profile 2.1:

1. If nameserver/<nameserver name> in the RDAP Query URI contains only A-label or NR-
LDH labels, the topmost domain object shall contain a ldhName.
```json
{
  "code": -49100,
  "value": "<nameserver object>",
  "message": "The RDAP Query URI contains only A-label or NR-LDH labels, the topmost nameserver object does not contain a ldhName member. See section 2.1 of the RDAP_Response_Profile_2_1."
}
```
2. If nameserver/<nameserver name> in the RDAP Query URI contains one or more U-
label, the topmost domain object shall contain an unicodeName.
```json
{
  "code": -49101,
  "value": "<nameserver object>",
  "message": " The RDAP Query URI contains one or more U-label, the topmost nameserver object does not contain a unicodeName member. See section 2.1 of the RDAP_Response_Profile_2_1."
}
```
3. The handle in the topmost _nameserver_ object shall comply with the following format
specified in RFC5730: "(\w|_){1,80}-\w{1,8}".
```json
{
  "code": -49102,
  "value": "<nameserver object>",
  "message": "The handle in the nameserver object does not comply with the format (\w|_){1,80}-\w{1,8} specified in RFC5730"."
}
```
4. If the handle in the topmost _nameserver_ object comply with the format: "(\w|_){1,80}-
\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is
registered in EPPROID.
```json
{
  "code": -49103,
  "value": "<nameserver object>",
  "message": "The globally unique identifier in the nameserver object handle is not registered in EPPROID."
}
```

## RP Section 4.3 

Test group: [[rdapResponseProfile_4_3_Validation]](#id-rdapResponseProfile_4_3_Validation){ #id-rdapResponseProfile_4_3_Validation }

The following steps should be used to test the RDAP protocol section 4. 3 of the  RDAP Response Profile 2.1.

The following steps shall only be executed if an entity with the registrar role exists within the topmost object, and the handle is different from "not applicable":

1. For the _entity_ with the registrar role within the topmost object, validate that a _publicIds_ member is included.
```json
{
  "code": -49200,
  "value": "<entity data structure>",
  "message": "A publicIds member is not included in the entity with the registrar role."
}
```
2. For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is included, validate that the identifier member is a positive integer.
```json
{
  "code": -49201,
  "value": "<publicIds data structure>",
  "message": "The identifier of the publicIds member of the entity with the registrar role is not a positive integer."
}
```
3. For the _entity_ with the registrar role within the domain object, validate that the _handle_
member is a positive integer.
```json
{
  "code": -49202,
  "value": "<publicIds data structure>",
  "message": "The handle of the entity with the registrar role is not a positive integer."
}
```
4. For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is
included, validate that the identifier member equals the handle member.
```json
{
  "code": -49203,
  "value": "<entity data structure>",
  "message": "The identifier of the publicIds member of the entity with the registrar role is not equal to the handle member."
}
```
5. For the _entity_ with the registrar role within the domain object, validate that the value of
the handle member exists in the registrarId.
```json
{
  "code": -49204,
  "value": "<handle> + "\n/\n" + <registrarId>",
  "message": "The handle references an IANA Registrar ID that does not exist in the registrarId ."
}
```

The following steps shall only be executed if an entity with the registrar role exists
within the topmost object, and the handle is "not applicable":

6. For the _entity_ with the registrar role within the topmost object, validate that a _publicIds_
member is not included.
```json
{
"code": -49205,
"value": "<entity data structure>",
"message": "A publicIds member is included in the entity with the registrar role."
}
```
## Nameserver Status 

Test group: [[nameserver_status]](#id-nameserver_status){ #id-nameserver_status }

1. If a _status_ element is included in the nameserver object, validate that it complies with
the following:
    1. "active" status MAY only be combined with "associated" status.
    2. "associated" status MAY be combined with any status.
    3. "pending delete" status MUST NOT be combined with either "client delete prohibited" or "server delete prohibited" status.
    4. "pending update" status MUST NOT be combined with either "client update prohibited" or "server update prohibited" status.
    5. The pending create, pending delete, pending renew, pending transfer, and pending update status values MUST NOT be combined with each other.
```json
{
  "code": -49300,
  "value": "<status data structure>",
  "message": "The values of the status data structure does not comply with RFC5732."
}
```

