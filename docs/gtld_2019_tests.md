# 2019 gTLD RDAP Profile Tests

## 8.1. Technical Implementation Guide – General

### 8.1.1. TIG 1.2

Test group: [tigSection_1_2_Validation]

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

### 8.1.2. TIG Section 1.3

Test group: [tigSection_1_3_Validation]

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

### 8.1.3. TIG Section 1.6

Test group: [tigSection_1_6_Validation]

The following steps should be used to test the RDAP protocol section 1.6 of the RDAP_Technical_Implementation_Guide_2_1:

1. The tool shall use the HTTP HEAD method on the URI to be tested. If the HTTP Status code is different from the status code obtained when doing the GET method:
``` json
{
  "code": -20300,
  "value": "<HTTP Status code when using the GET method> + '\n/\n' + <HTTP Status code when using the HEAD method>",
  "message": "The HTTP Status code obtained when using the HEAD method is different from the GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

### 8.1.4. TIG Section 1.8

Test group: [tigSection_1_8_Validation]

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

### 8.1.5. TIG Section 1.13

Test group: [tigSection_1_13_Validation]

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

### 8.1.6. TIG Section 1.14

Test group: [tigSection_1_14_Validation]

The following steps should be used to test the RDAP protocol section 1.14 of the RDAP_Technical_Implementation_Guide_2_1:

1. Validate that the JSON string value "icann_rdap_technical_implementation_guide_0" is included in the RDAP Conformance data structure.
``` json
{
  "code": -20600,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

### 8.1.7. TIG Section 3.3 and 3.4

Test group: [tigSection_3_3_and_3_4_Validation]

The following steps should be used to test the RDAP protocol section 3.3 and 3.4 of the RDAP_Technical_Implementation_Guide_2_1:

1. Validate that at least one links data structure exists within the notices object in the topmost object.
``` json
{
  "code": -20700,
  "value": "<notices data structure>",
  "message": "A links object was not found in the notices object in the topmost object. See section 3.3 and 3.4 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

### 8.1.8. TIG Section 4.1

Test group: [tigSection_4_1_Validation]

The following steps should be used to test the RDAP protocol section 4.1 of the TIG:

1. Validate that all the _entities_ in the RDAP Response contain structured address. If a street address has more than one line, it MUST be structured as an array of strings.
``` json
{
  "code": -20800,
  "value": "<entity data structure>",
  "message": "An entity with a non-structured address was found. See section 4.1 of the TIG."
}
```

### 8.1.9. TIG Section 7.1 and 7.2

Test group: [tigSection_7_1_and_7_2_Validation]

The following steps should be used to test the RDAP protocol section 7. 1 and 7. 2 of the TIG:

1. Validate that at all the _tel_ properties in the _entities_ in the RDAP Response contain voice or fax as type parameter.
``` json
{
  "code": -20900,
  "value": "<entity data structure>",
  "message": "An entity with a tel property without a voice or fax type was found. See section 7.1 and 7.2 of the TIG."
}
```

## 8.2. Technical Implementation Guide - Registry

### 8.2.1. TIG Section 1.11.1

Test group: [tigSection_1_11_1_Validation]

The following steps should be used to test the RDAP protocol section 1.11.1 and 1.2 of the RDAP_Technical_Implementation_Guide_2_1:

1. Verify that the TLD of the domain name is listed in the bootstrapDomainNameSpace.
``` json
{
  "code": -23100,
  "value": "<TLD> + '\n/\n' <bootstrapDomainNameSpace>",
  "message": "The TLD is not included in the bootstrapDomainNameSpace. See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
2. Validate that at least one base URL exists in the bootstrapDomainNameSpace for the TLD.
``` json
{
  "code": -23101,
  "value": "<TLD element in bootstrapDomainNameSpace>",
  "message": "The TLD entry in bootstrapDomainNameSpace does not contain a base URL. See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
3. For the entry of the TLD in bootstrapDomainNameSpace verify that every one of the base URLs contain a schema of "https".
``` json
{
  "code": -23102,
  "value": "<TLD entry in bootstrapDomainNameSpace>",
  "message": "One or more of the base URLs for the TLD contain a schema different from https. See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

### 8.2.2. TIG Section 3.2

Test group: [tigSection_3_2_Validation]

The following steps should be used to test the RDAP protocol section 3.2 of the RDAP_Technical_Implementation_Guide_2_1:

1. Validate that a links data structure in the topmost object exists, and the links object shall contain the elements _rel_ :related and _href_.
``` json
{
  "code": -23200,
  "value": "<links data structure>",
  "message": "A links data structure in the topmost object exists, and the links object shall contain the elements rel:related and href, but they were not found. See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

### 8.2.3. TIG Section 6.1

Test group: [tigSection_6_1_Validation]

The following steps should be used to test the RDAP protocol section 6.1 of the RDAP_Technical_Implementation_Guide_2_1:

1. For the _entity_ with the registrar role within the domain object, validate that a _publicIds_ member is included.
``` json
{
  "code": -23300,
  "value": "<entity data structure>",
  "message": "A publicIds member is not included in the entity with the registrar role."
}
```
2. For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is included, validate that the identifier member is a positive integer.
``` json
{
  "code": -23301,
  "value": "<publicIds data structure>",
  "message": "The identifier of the publicIds member of the entity with the registrar role is not a positive integer."
}
```

## 8.3. Technical Implementation Guide – Registrar

### 8.3.1. TIG Section 1.12.1

Test group:[tigSection_1_12_1_Validation]

The following steps should be used to test the RDAP protocol section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1:

1. Get the identifier in the _publicIds_ element in the _entity_ with the registrar role.
``` json
{
  "code": -26100,
  "value": "<publicIds data structure>",
  "message": "An identifier in the publicIds within the entity data structure with the registrar role was not found. See section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
2. For the _identifier_ found in the previous step, validate that an entry exists in the **registrarId**.
``` json
{
  "code": -26101,
  "value": "<identifier> '\n/\n' <registrarId>",
  "message": "The registrar identifier is not included in the registrarId. See section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
3. For the _identifier_ found in the previous step, verify that every of the base URLs contain a schema of "https".
``` json
{
  "code": -26102,
  "value": "<Registrar entry in registrarId>",
  "message": "One or more of the base URLs for the registrar contain a schema different from https. See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1."
}
```


## 8.4. RDAP Response Profile - General

### 8.4.1. RP 1.2.2

Test group: [rdapResponseProfile_1_2_2_Validation]

The following steps should be used to test the RDAP protocol section 1.2.2 of the RDAP_Response_Profile_2_1:

1. Validate that the RDAP response does not contain browser executable code (e.g., JavaScript).
``` json
{
  "code": -40100,
  "value": "<rdap response>",
  "message": "The RDAP response contains browser executable code (e.g., JavaScript). See section 1.2.2 of the RDAP_Response_Profile_2_1.""
}
```

Note: a library for HTML sanitizing (https://en.wikipedia.org/wiki/HTML_sanitization) may be used for this test.

#### 8.4.2. [rdapResponseProfile_1_3_Validation]

#### The following steps should be used to test the RDAP protocol section 1.3 of the

#### RDAP_Response_Profile_2_1:

1. Validate that the JSON string value "icann_rdap_response_profile_0" is included in the
    _RDAP Conformance_ data structure.
    {
    "code": - 40200 ,
    "value": "<rdapConformance data structure>",
    "message": "The RDAP Conformance data structure does not include
    icann_rdap_response_profile_0. See section 1.3 of the RDAP_Response_Profile_2_1."
    }

#### 8.4.3. [rdapResponseProfile_1_4_Validation]

#### The following steps should be used to test the RDAP protocol section 1. 4 of the

#### RDAP_Response_Profile_2_1:

1. Validate that the country name parameter is empty in the _adr_ of all the jCard objects in the RDAP response.
```
{
"code": - 40400 ,
"value": "<vcard object>",
"message": "A vcard object with a country name parameter with data was
found. "
}
```


### 8.5. RDAP Response Profile - Miscellaneous

#### 8.5.1. [rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation]

#### The following steps should be used to test the RDAP protocol section 2.3.1.3, 2.7.6, 3.3

#### and 4.4 of the RDAP_Response_Profile_2_1:

1. Validate that an _eventAction_ type "last update of RDAP database" exists in events structure included in the topmost object.
```
{
"code": - 43100 ,
"value": "<events data structure>",
"message": "An eventAction type last update of RDAP database does not
exists in the topmost events data structure. See section 2.3.1.3, 2.7.6, 3.3 and
4.4 of the RDAP_Response_Profile_2_1."
}
```

### 8.6. RDAP Response Profile - Domain

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

### 8.7. RDAP Response Profile - Nameserver.............................................................................................

#### 8.7.1. [rdapResponseProfile_4_1_Validation]

#### The following steps should be used to test the RDAP protocol section 4 .1 of the

#### RDAP_Response_Profile_2_1:

1. If nameserver/<nameserver name> in the RDAP Query URI contains only A-label or NR-

```
LDH labels, the topmost domain object shall contain a ldhName.
{
"code": - 49100 ,
"value": "<nameserver object>",
"message": "The RDAP Query URI contains only A-label or NR-LDH labels, the
topmost nameserver object does not contain a ldhName member. See section 2.1 of
the RDAP_Response_Profile_2_1."
}
```
2. If nameserver/<nameserver name> in the RDAP Query URI contains one or more U-

```
label, the topmost domain object shall contain an unicodeName.
{
"code": - 49101 ,
"value": "<nameserver object>",
"message": " The RDAP Query URI contains one or more U-label, the topmost
nameserver object does not contain a unicodeName member. See section 2.1 of the
RDAP_Response_Profile_2_1."
}
```
3. The handle in the topmost _nameserver_ object shall comply with the following format

```
specified in RFC5730: "(\w|_){1,80}-\w{1,8}".
{
"code": - 49102 ,
"value": "<nameserver object>",
"message": "The handle in the nameserver object does not comply with the
format (\w|_){1,80}-\w{1,8} specified in RFC5730"."
}
```
4. If the handle in the topmost _nameserver_ object comply with the format: "(\w|_){1,80}-

```
\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is
registered in EPPROID.
{
"code": - 49103 ,
"value": "<nameserver object>",
"message": "The globally unique identifier in the nameserver object handle
is not registered in EPPROID."
}
```

#### 8.7.2. [rdapResponseProfile_4_3_Validation]

#### The following steps should be used to test the RDAP protocol section 4. 3 of the

#### RDAP_Response_Profile_2_1.

#### The following steps shall only be executed if an entity with the registrar role exists

#### within the topmost object, and the handle is different from "not applicable":

1. For the _entity_ with the registrar role within the topmost object, validate that a _publicIds_
    member is included.
    {
    "code": - 49200 ,
    "value": "<entity data structure>",
    "message": "A publicIds member is not included in the entity with the
    registrar role."
    }
2. For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is
    included, validate that the identifier member is a positive integer.
    {
    "code": - 49201 ,
    "value": "<publicIds data structure>",
    "message": "The identifier of the publicIds member of the entity with the
    registrar role is not a positive integer."
    }
3. For the _entity_ with the registrar role within the domain object, validate that the _handle_

```
member is a positive integer.
{
"code": - 49202 ,
"value": "<publicIds data structure>",
"message": "The handle of the entity with the registrar role is not a
positive integer."
}
```
4. For the _entity_ with the registrar role within the domain object, if a _publicIds_ member is

```
included, validate that the identifier member equals the handle member.
{
"code": - 49203 ,
"value": "<entity data structure>",
"message": "The identifier of the publicIds member of the entity with the
registrar role is not equal to the handle member."
}
```
5. For the _entity_ with the registrar role within the domain object, validate that the value of

```
the handle member exists in the registrarId.
{
"code": - 49204 ,
"value": "<handle> + "\n/\n" + <registrarId>",
"message": "The handle references an IANA Registrar ID that does not exist
in the registrarId ."
}
```

#### The following steps shall only be executed if an entity with the registrar role exists

#### within the topmost object, and the handle is "not applicable":

6. For the _entity_ with the registrar role within the topmost object, validate that a _publicIds_

```
member is not included.
{
"code": - 49205 ,
"value": "<entity data structure>",
"message": "A publicIds member is included in the entity with the
registrar role."
}
```
#### 8.7.3. [nameserver_status]

1. If a _status_ element is included in the nameserver object, validate that it complies with

```
the following:
f. "active" status MAY only be combined with "associated" status.
```
```
g. "associated" status MAY be combined with any status.
h. "pending delete" status MUST NOT be combined with either "client delete
```
```
prohibited" or "server delete prohibited" status.
i. "pending update" status MUST NOT be combined with either "client update
prohibited" or "server update prohibited" status.
```
```
j. The pending create, pending delete, pending renew, pending transfer, and
pending update status values MUST NOT be combined with each other.
{
"code": - 49300 ,
"value": "<status data structure>",
"message": "The values of the status data structure does not comply with
RFC5732."
}
```

### 8.8. RDAP Response Profile – Entities within Domain

#### 8.8.1. [rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation]

#### The following steps should be used to test the RDAP protocol section 2.7.1.X and

#### 2.7.2.X and 2.7.3.X and 2.7.4.X of the RDAP_Response_Profile_2_1:

1. For entities with the registrant, administrative, technical and billing role within the

```
domain object, if a remarks member with the title "REDACTED FOR PRIVACY" is
included, validate that the type member is "object redacted due to authorization".
{
"code": - 52100 ,
"value": "<entity data structure>",
"message": "An entity with the registrant, administrative, technical or
billing role with a remarks members with the title "REDACTED FOR PRIVACY" was
found, but the description and type does not contain the value in 2.7.4.3 of the
RDAP_Response_Profile_2_1."
}
```
2. For entities with the registrant, administrative, technical and billing role within the
    domain object, if a _remarks_ member with the title "REDACTED FOR PRIVACY" is not

```
included, validate that valid handle , fn , adr , tel members are included. For the adr
member, validate that the following RDDS fields are included: Street and City.
{
"code": - 52101 ,
"value": "<entity data structure>",
"message": "An entity with the registrant, administrative, technical or
billing role with a remarks members with the title "REDACTED FOR PRIVACY" was
found, but the description and type does not contain the value in 2.7.4.3 of the
RDAP_Response_Profile_2_1."
}
```
3. For entities with the registrant, administrative, technical and billing role within the
    domain object, if a _remarks_ member with the title "REDACTED FOR PRIVACY" is not

```
included, validate that the handle comply with the following format specified in
RFC5730: "(\w|_){1,80}-\w{1,8}".
{
"code": - 52102 ,
"value": "<nameserver object>",
"message": "The handle in the entity object does not comply with the
format (\w|_){1,80}-\w{1,8} specified in RFC5730"."
}
```
4. For entities with the registrant, administrative, technical and billing role within the
    domain object, if a _remarks_ member with the title "REDACTED FOR PRIVACY" is not
    included and the handle conforms to the format: "(\w|_){1,80}-\w{1,8}", validate that

```
the string followed by a hyphen ("-", ASCII value 0x002D) is registered in EPPROID.
{
"code": - 52103 ,
"value": "<nameserver object>",
"message": "The globally unique identifier in the entity object handle is
not registered in EPPROID."
}
```

5. Only one entity shall be assigned the following roles: registrant, administrative,

```
technical and billing.
{
"code": - 52104 ,
"value": "<entities data structure>",
"message": "More than one entity with the following roles were found:
registrant, administrative, technical and billing."
}
```
6. For entities with the registrant role within the domain object, validate that the CC

```
parameter is included in the entity as defined in RFC8605.
{
"code": - 52105 ,
"value": "<entity data structure>",
"message": "An entity with the registrant role without the CC parameter
was found. See section 2.7.3.1 of the RDAP_Response_Profile_2_1."
}
```

### 8.9. RDAP Response Profile – Entities within Domain - Registry

#### 8.9.1. [rdapResponseProfile_2_7_5_3_Validation]

#### The following steps should be used to test the RDAP protocol section 2.7.5.3 of the

#### RDAP_Response_Profile_2_1:

1. For the _entities_ with the registrant, administrative, technical and billing role within the

```
domain object, if the email property is omitted, validate that a remarks element
containing a title member with a value "EMAIL REDACTED FOR PRIVACY" and a type
```
```
member with a value "object redacted due to authorization" is included in the entity
object.
{
"code": - 55000 ,
"value": "<entity data structure>",
"message": "An entity with the administrative, technical, or billing role
without a valid "EMAIL REDACTED FOR PRIVACY" remark was found. See section 2.7.5. 3
of the RDAP_Response_Profile_2_1."
}
```
#### Note: this test also includes 2.7.5.1.


### 8.10. RDAP Response Profile - Entities within Domain - Registrar.........................................................

#### 8.10.1. [rdapResponseProfile_2_7_5_2_Validation]

#### The following steps should be used to test the RDAP protocol section 2.7.5.2 of the

#### RDAP_Response_Profile_2_1:

1. For the _entities_ with the registrant, administrative, technical and billing role within the

```
domain object, if the email property is omitted, validate that a CONTACT-URI member is
included.
{
"code": - 58000 ,
"value": "<entity data structure>",
"message": "An entity with the administrative, technical, or billing role
without a CONTACT-URI member was found. See section 2.7.5.2 of the
RDAP_Response_Profile_2_1."
}
```
2. For the _entities_ with the registrant, administrative, technical and billing role within the
    domain object, if a CONTACT-URI member is included, validate that the content is an

```
email address or an http/https link.
{
"code": - 58001 ,
"value": "<entity data structure>",
"message": "The content of the CONTACT-URI member of an entity with the
administrative, technical, or billing role does not contain an email or http/https
link. See section 2.7.5.2 of the RDAP_Response_Profile_2_1."
}
```
#### Note: this test also includes 2.7.5.1.


### 8.11. RDAP Response Profile – Entity - Registrar

#### 8.11.1. [rdapResponseProfile_3_1_Validation]

#### The following steps should be used to test the RDAP protocol section 3.1 of the

#### RDAP_Response_Profile_2_1:

1. An _entity_ with the registrar role as the topmost object shall exist.
    {
    "code": - 60100 ,
    "value": "<topmost object>",
    "message": "An entity with the registrar role was not found as the topmost
    object. See section 3.1 of the RDAP_Response_Profile_2_1"
    }
2. Validate that valid _handle_ , _fn_ , _adr_ , _tel_ , _email_ members are included in the topmost
    object. For the _adr_ member, validate that the following RDDS fields are included: Street,
    City and Country.
    {
    "code": - 60101 ,
    "value": "<topmost object>",
    "message": "The required members for a registrar entity were not found.
    See section 3.1 of the RDAP_Response_Profile_2_1."
    }

#### 8.11.2. [rdapResponseProfile_3_2_Validation]

#### The following steps should be used to test the RDAP protocol section 3.2 of the

#### RDAP_Response_Profile_2_1:

1. If entities with the administrative and technical roles within the topmost object exist,
    validate that valid _fn_ , _tel_ , _email_ members are included.
    {
    "code": - 60200 ,
    "value": "<entity data structure>",
    "message": "The required members for entities with the administrative and
    technical roles were not found. See section 3.2 of the RDAP_Response_Profile_2_1."
    }


