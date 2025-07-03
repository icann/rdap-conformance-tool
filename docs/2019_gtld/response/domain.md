# Domain Tests

#### 8.6.1. [rdapResponseProfile_2_1_Validation]

#### The following steps should be used to test the RDAP protocol section 2.1 of the

#### RDAP_Response_Profile_2_1:

1. If domain/<domain name> in the RDAP Query URI contains only A-label or NR-LDH
labels, the topmost domain object shall contain a ldhName.
```
{
"code": - 46100 ,
"value": "<domain object>",
"message": "The RDAP Query URI contains only A-label or NR-LDH labels, the
topmost domain object does not contain a ldhName member. See section 2.1 of the
RDAP_Response_Profile_2_1."
}
```
2. If domain/<domain name> in the RDAP Query URI contains one or more U-label, the
topmost domain object shall contain an unicodeName.
```
{
"code": - 46101 ,
"value": "<domain object>",
"message": " The RDAP Query URI contains one or more U-label, the topmost
domain object does not contain a unicodeName member. See section 2.1 of the
RDAP_Response_Profile_2_1."
}
```
#### 8.6.2. [rdapResponseProfile_2_2_Validation]

#### The following steps should be used to test the RDAP protocol section 2.2 of the

#### RDAP_Response_Profile_2_1:

1. The handle in the topmost _domain_ object shall comply with the following format

```
specified in RFC5730: "(\w|_){1,80}-\w{1,8}".
{
"code": - 46200 ,
"value": "<domain object>",
"message": "The handle in the domain object does not comply with the
format (\w|_){1,80}-\w{1,8} specified in RFC5730"."
}
```
2. If the handle in the topmost _domain_ object comply with the format: "(\w|_){1,80}-

```
\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is
registered in EPPROID.
{
"code": - 46201 ,
"value": "<domain object>",
"message": "The globally unique identifier in the domain object handle is
not registered in EPPROID."
}
```

#### 8.6.3. [rdapResponseProfile_2_3_1_1_Validation]

#### The following steps should be used to test the RDAP protocol section 2.3.1.1 of the

#### RDAP_Response_Profile_2_1:

1. Validate that an _eventAction_ of type "registration" exists in the topmost events
    structure.
    {
    "code": - 46300 ,
    "value": "<events data structure>",
    "message": "An eventAction of type registration does not exists in the
    topmost events data structure. See section 2.3.1.1 of the
    RDAP_Response_Profile_2_1."
    }

#### 8.6.4. [rdapResponseProfile_2_3_1_2_Validation]

#### The following steps should be used to test the RDAP protocol section 2.3.1.2 of the

#### RDAP_Response_Profile_2_1:

1. Validate that an _eventAction_ type "expiration" exists in the topmost events structure.
    {
    "code": - 46400 ,
    "value": "<events data structure>",
    "message": "An eventAction of type expiration does not exists in the
    topmost events data structure. See section 2.3.1.2 of the
    RDAP_Response_Profile_2_1."
    }

#### 8.6.5. [rdapResponseProfile_notices_included_Validation]

#### The following steps should be used to test that a notices member appear in the RDAP

#### response:

1. Validate that a _notices_ member appears in the RDAP response.
    {
    "code": - 46500 ,
    "value": "<RDAP response>",
    "message": "A notices members does not appear in the RDAP response."
    }

#### 8.6.6. [rdapResponseProfile_2_6_3_Validation]

#### The following steps should be used to test the RDAP protocol section 2.6.3 of the

#### RDAP_Response_Profile_2_1:

1. Validate that the _notices_ member contains an element in the JSON array with a title

```
“Status Codes”, a description containing the string “For more information on domain
status codes, please visit https://icann.org/epp” and a links member with an href
```
```
containing "https://icann.org/epp".
{
"code": - 46600 ,
```

```
"value": "<notices structure>",
"message": "The notice for https://icann.org/epp was not found."
}
```
#### 8.6.7. [rdapResponseProfile_2_11_Validation]

#### The following steps should be used to test the RDAP protocol section 2.11 of the

#### RDAP_Response_Profile_2_1:

1. Validate that the _notices_ member contains an element in the JSON array with a title
    “RDDS Inaccuracy Complaint Form”, a description containing the string “URL of the
    ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf” and a links member

```
with an href containing "https://icann.org/wicf".
{
"code": - 46700 ,
"value": "<notices structure>",
"message": "The notice for https://icann.org/wicf was not found."
}
```
#### 8.6.8. [rdapResponseProfile_2_10_Validation]

#### The following steps should be used to test the RDAP protocol section 2.10 of the

#### RDAP_Response_Profile_2_1:

1. Validate that a _secureDNS_ member is included in the domain object.
    {
    "code": - 46800 ,
    "value": "<domain object>",
    "message": "A secureDNS member does not appear in the domain object."
    }
2. The JSON name _delegationSigned_ shall appear.
    {
    "code": - 46801 ,
    "value": "<secureDNS structure>",
    "message": "The delegationSigned element does not exist."
    }

#### 3. If delegationSigned has a value of true, one dsData name/values or one keyData

#### name/value shall appear.

##### {

```
"code": - 46802 ,
"value": "<secureDNS structure>",
"message": "delegationSigned value is true, but no dsData nor keyData
name/value pair exists."
}
```
#### 8.6.9. [rdapResponseProfile_rfc5731_Validation]

#### The following steps should be used to test that the status values comply with

#### RFC5731:


1. Validate that the values of the _status_ member in the topmost object comply with the
    following:
       a. "active" status MUST NOT be combined with any other status.

```
b. "pending delete" status MUST NOT be combined with either "client delete
prohibited" or "server delete prohibited" status.
```
```
c. "pending renew" status MUST NOT be combined with either "client renew
prohibited" or "server renew prohibited" status.
```
```
d. "pending transfer" status MUST NOT be combined with either "client transfer
prohibited" or "server transfer prohibited" status.
e. "pending update" status MUST NOT be combined with either "client update
```
```
prohibited" or "server update prohibited" status.
f. The pending create, pending delete, pending renew, pending transfer, and
```
```
pending update status values MUST NOT be combined with each other.
{
"code": - 46900 ,
"value": "<status data structure>",
"message": "The values of the status data structure does not comply with
RFC5731."
}
```
#### 8.6.10. [rdapResponseProfile_rfc3915_Validation]

#### The following steps should be used to test that the status values comply with

#### RFC3915:

1. Validate that the values of the _status_ member in the topmost object comply with the

```
following:
a. "redemption period" status MUST only be combined with "pending delete".
```
```
b. "pending restore" status MUST only be combined with "pending delete".
{
"code": - 47000 ,
"value": "<status data structure>",
"message": "The values of the status data structure does not comply with
RFC3915."
}
```
#### 8.6.11. [rdapResponseProfile_2_6_1_Validation]

#### The following steps should be used to test the RDAP protocol section 2.6.1 of the

#### RDAP_Response_Profile_2_1:

1. Validate that a _status_ member in the topmost object contain at least one value.
    {
    "code": - 47100 ,
    "value": "<status data structure>",
    "message": "The status member does not contain at least one value."
    }

#### 8.6.12. [rdapResponseProfile_2_9_1_and_2_9_2_Validation]


#### The following steps should be used to test the RDAP protocol section 2. 9 .1 and 2.9.2

#### of the RDAP_Response_Profile_2_1:

1. If the _nameservers_ member is included within the _domain_ object, validate that all

```
nameserver objects contain the ldhName element.
{
"code": - 47200 ,
"value": "<nameservers data structure>",
"message": "A nameserver object without ldhName was found."
}
```
2. If the _nameservers_ member is included within the _domain_ object, validate that all

```
handles in the nameserver objects comply with the following format specified in
RFC5730: "(\w|_){1,80}-\w{1,8}".
{
"code": - 47201 ,
"value": "<nameserver object>",
"message": "The handle in the nameserver object does not comply with the
format (\w|_){1,80}-\w{1,8} specified in RFC5730"."
}
```

3. If the _nameservers_ member is included within the _domain_ object, validate that the string
    followed by a hyphen ("-", ASCII value 0x002D) is registered in **EPPROID** for all the
    handles that comply with the format "(\w|_){1,80}-\w{1,8}".
    {
    "code": - 47202 ,
    "value": "<nameserver object>",
    "message": "The globally unique identifier in the nameserver object handle
    is not registered in EPPROID."
    }
4. If the _nameservers_ member is included within the _domain_ object and at least one
    nameserver object contains a _handle_ or a _status_ element, validate that all nameserver

```
objects include a handle and a status element.
{
"code": - 47203 ,
"value": "<nameserver object>",
"message": "The handle or status in the nameserver object is not
included."
}
```
5. If the _nameservers_ member is included within the domain object, validate that all _status_

```
elements included in the nameserver objects comply with the following:
a. "active" status MAY only be combined with "associated" status.
b. "associated" status MAY be combined with any status.
```
```
c. "pending delete" status MUST NOT be combined with either "client delete
prohibited" or "server delete prohibited" status.
```
```
d. "pending update" status MUST NOT be combined with either "client update
prohibited" or "server update prohibited" status.
```
```
e. The pending create, pending delete, pending renew, pending transfer, and
pending update status values MUST NOT be combined with each other.
{
"code": - 47204 ,
"value": "<status data structure>",
"message": "The values of the status data structure does not comply with
RFC5732."
}
```
#### 8.6.13. [rdapResponseProfile_2_4_1_Validation]

#### The following steps should be used to test the RDAP protocol section 2. 4 .1 of the

#### RDAP_Response_Profile_2_1:

1. An _entity_ with the registrar role within the topmost domain object shall exist.
    {
    "code": - 47300 ,
    "value": "<domain object data structure>",
    "message": "An entity with the registrar role was not found in the domain
    topmost object."
    }
2. Only one _entity_ with the registrar role within the topmost domain object shall exist.
    {
    "code": - 47301 ,
    "value": "<domain object data structure>",
    "message": "More than one entities with the registrar role were found in
    the domain topmost object."
    }


3. For the _entity_ with the registrar role within the topmost domain object, validate that a

```
fn member is included in all of the vcard objects.
{
"code": - 47302 ,
"value": "<entity data structure>",
"message": "An fn member was not found in one or more vcard objects of the
entity with the registrar role."
}
```
#### 8.6.14. [rdapResponseProfile_2_4_2_and_2_4_3_Validation]

#### The following steps should be used to test the RDAP protocol section 2. 4. 2 and 2.4.3

#### of the RDAP_Response_Profile_2_1:

1. For the _entity_ with the registrar role within the topmost object, validate that a _publicIds_

```
member is included.
{
"code": - 47400 ,
"value": "<entity data structure>",
"message": "A publicIds member is not included in the entity with the
registrar role."
}
```
2. For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is

```
included, validate that the identifier member is a positive integer.
{
"code": - 47401 ,
"value": "<publicIds data structure>",
"message": "The identifier of the publicIds member of the entity with the
registrar role is not a positive integer."
}
```
3. For the _entity_ with the registrar role within the domain object, validate that the _handle_

```
member is a positive integer.
{
"code": - 47402 ,
"value": "<publicIds data structure>",
"message": "The handle of the entity with the registrar role is not a
positive integer."
}
```
4. For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is
    included, validate that the _identifier_ member equals the _handle_ member.
    {
    "code": - 47403 ,
    "value": "<entity data structure>",
    "message": "The identifier of the publicIds member of the entity with the
    registrar role is not equal to the handle member."
    }
5. For the _entity_ with the registrar role within the domain object, validate that the value of
    the _handle_ member exists in the **registrarId**.
    {
    "code": - 47404 ,
    "value": "<handle> + "\n/\n" + <registrarId>",
    "message": "The handle references an IANA Registrar ID that does not exist
    in the **registrarId** ."


##### }

#### 8.6.15. [rdapResponseProfile_2_4_5_Validation]

1. For the _entity_ with the registrar role within the _domain_ object, validate that an _entity_

```
with the abuse role is included, and the entity with the abuse role includes a tel and
email members in all the vcard objects.
{
"code": - 47500 ,
"value": "<entity data structure>",
"message": "Tel and email members were not found for the entity within the
entity with the abuse role in the topmost domain object."
}
```

