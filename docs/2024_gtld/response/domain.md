# Domain Tests

## RP Section 2.2

Test group: [[rdapResponseProfile_2_2_Validation]][id-rdapResponseProfile_2_2_Validation]

If the handle is in the topmost domain object, the following tests apply:

1. Test case [-46200](#id-testCase-46200){ #id-testCase-46200 }: 
The handle in the topmost domain object shall comply with the following format specified in RFC5730: "(\w|\_){1,80}-\w{1,8}".
```json
{
  "code": -46200,
  "value": "<domain object>",
  "message": "The handle in the domain object does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730."
}
```
2. Test case [-46201](#id-testCase-46201){ #id-testCase-46201 }: 
If the handle in the topmost domain object comply with the format: "(\w|_){1,80}-\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is registered in EPPROID.
```json
{
  "code": -46201,
  "value": "<domain object>",
  "message": "The globally unique identifier in the domain object handle is not registered in EPPROID."
}
```

If the handle is NOT in the topmost domain object, the following tests apply:

1. Test case [-46202](#id-testCase-46202){ #id-testCase-46202 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registry Domain ID”.
```json
{
  "code": -46202,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registry Domain ID is required."
}
```
2. Test case [-46203](#id-testCase-46203){ #id-testCase-46203 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present as a JSON string of “$.handle”.
```json
{
  "code": -46203,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registry Domain ID"
}
```
3. Test case [-46204](#id-testCase-46204){ #id-testCase-46204 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -46204,
  "value": "<redaction object>",
  "message": "Registry Domain ID redaction method must be removal if present"
}
```

## RP Section 2.6.3

Test group: [[rdapResponseProfile_2_6_3_Validation]][id-rdapResponseProfile_2_6_3_Validation]

1. Test case [-46601](#id-testCase-46601){ #id-testCase-46601 }: Validate that the notices member contains an element in the JSON array with a title “Status Codes”.
```json
{
  "code": -46601,
  "value": "<notices array structure>",
  "message": "The notice for Status Codes was not found."
}
```
2. Test case [-46602](#id-testCase-46602){ #id-testCase-46602 }: With the notice object above, validate that it contains a description array containing one string of “For more information on domain status codes, please visit https://icann.org/epp”. This test should ignore extra whitespace and trailing punctuation.
```json
{
  "code": -46602,
  "value": "<notice structure>",
  "message": "The notice for Status Codes does not have the proper description."
}
```
3. Test case [-46603](#id-testCase-46603){ #id-testCase-46603 }: With the notice object above, validate that it contains a links array.
```json
{
  "code": -46603,
  "value": "<notice structure>",
  "message": "The notice for Status Codes does not have links."
}
```
4. Test case [-46604](#id-testCase-46604){ #id-testCase-46604 }: With the links array above, validate that it contains one link object with an href property of “https://icann.org/epp”.
```json
{
  "code": -46604,
  "value": "<notice structure>",
  "message": "The notice for Status Codes does not have a link to the status codes."
}
```
5. Test case [-46605](#id-testCase-46605){ #id-testCase-46605 }: With the link object above, validate that it contains a rel property that is a string of “glossary”.
```json
{
  "code": -46605,
  "value": "<notice structure>",
  "message": "The notice for Status Codes does not have a link relation type of glossary"
}
```
6. Test case [-46606](#id-testCase-46606){ #id-testCase-46606 }: With the link object above, validate that it contains a value property that is a string of the URL used to query for this response.
```json
{
  "code": -46606,
  "value": "<notice structure>",
  "message": "The notice for Status Codes does not have a link value of the request URL."
}
```
## RP Section 2.10

Test group: [[rdapResponseProfile_2_10_Validation]][id-rdapResponseProfile_2_10_Validation]

1. Test case [-46701](#id-testCase-46701){ #id-testCase-46701 }: Validate that the notices member contains an element in the JSON array with a title ““RDDS Inaccuracy Complaint Form”.
```json
{
  "code": -46701,
  "value": "<notices array structure>",
  "message": "The notice for RDDS Inaccuracy Complaint Form was not found."
}
```
2. Test case [-46702](#id-testCase-46702){ #id-testCase-46702 }: With the notice object above, validate that it contains a description array containing one string of “URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf”. This test should ignore extra whitespace and trailing punctuation.
```json
{
  "code": -46702,
  "value": "<notice structure>",
  "message": "The notice for RDDS Inaccuracy Complaint Form does not have the proper description."
}
```
3. Test case [-46703](#id-testCase-46703){ #id-testCase-46703 }: With the notice object above, validate that it contains a links array.
```json
{
  "code": -46703,
  "value": "<notice structure>",
  "message": "The notice for RDDS Inaccuracy Complaint Form does not have links."
}
```
4. Test case [-46704](#id-testCase-46704){ #id-testCase-46704 }: With the links array above, validate that it contains one link object with an href property of “https://icann.org/wicf”.
```json
{
  "code": -46704,
  "value": "<notice structure>",
  "message": "The notice for RDDS Inaccuracy Complaint Form does not have a link to the complaint form."
}
```
5. Test case [-46705](#id-testCase-46705){ #id-testCase-46705 }: With the link object above, validate that it contains a rel property that is a string of “help”.
```json
{
  "code": -46705,
  "value": "<notice structure>",
  "message": "The notice for RDDS Inaccuracy Complaint Form does not have a link relation type of help"
}
```
6. Test case [-46706](#id-testCase-46706){ #id-testCase-46706 }: With the link object above, validate that it contains a value property that is a string of the URL used to query for this response.
```json
{
  "code": -46706,
  "value": "<notice structure>",
  "message": "The notice for RDDS Inaccuracy Complaint Form does not have a link value of the request URL."
}
```

## RP 2024 Section 2.7.3

Test group: [[rdapResponseProfile2024_2_7_3_Validation]](#id-rdapResponseProfile2024_2_7_3_Validation){ #id-rdapResponseProfile2024_2_7_3_Validation }

1. Test case [-47600](#id-testCase-47600){ #id-testCase-47600 }: 
For every entity of the domain excluding entities with the roles “registrar”, “registrant”, or “technical”, verify the handle is of the format: "(\w|_){1,80}-\w{1,8}".
```json
{
  "code": -47600,
  "value": "<entity handle>",
  "message": "The handle in the entity object does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730."
}
```
2. Test case [-47601](#id-testCase-47601){ #id-testCase-47601 }: With the handle above, verify the string followed by a hyphen (“-”, ASCII value 0x002D) is registered in the EPPROID dataset.
```json
{
  "code": -47601,
  "value": "<entity handle>",
  "message": "The globally unique identifier in the entity object handle is not registered in EPPROID."
}
```

## RP 2024 Section 2.4.6

Test group: [[rdapResponseProfile2024_2_4_6_Validation]](#id-rdapResponseProfile2024_2_4_6_Validation){ #id-rdapResponseProfile2024_2_4_6_Validation }

1. Test case [-47700](#id-testCase-47700){ #id-testCase-47700 }: Verify that the domain object has one entity with the role “registrar” with one link object in the links array with a rel property of the string “about”.
```json
{
  "code": -47700,
  "value": "<domain structure>",
  "message": "A domain must have link to the RDAP base URL of the registrar."
}
```
2. Test case [-47701](#id-testCase-47701){ #id-testCase-47701 }: With the link object above, validate that it contains a value property that is a string of the URL used to query for this response.
```json
{
  "code": -47701,
  "value": "<link structure>",
  "message": "The link for registar RDAP base URL does not have a link value of the request URL."
}
```
3. Test case [-47702](#id-testCase-47702){ #id-testCase-47702 }: With the link object above, validate the href property contains an URL with the “https” scheme.
```json
{
  "code": -47702,
  "value": "<link structure>",
  "message": "The registrar RDAP base URL must have an https scheme."
}
```
4. Test case [-47703](#id-testCase-47703){ #id-testCase-47703 }: With the handle of the “registrar” entity, verify that the href property of the link object above matches one of the base URLs in the registarId data set.
```json
{
  "code": -47703,
  "value": "<link structure>",
  "message": "The registrar base URL is not registered with IANA."
}
```