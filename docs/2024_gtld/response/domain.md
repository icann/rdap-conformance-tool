# Domain Tests

## RP Section 2.2

Test group: [[rdapResponseProfile_2_2_Validation]][id-rdapResponseProfile_2_2_Validation]

If the handle is in the topmost domain object, the following tests apply:

1. Run test case [-46200][id-testCase-46200]. 
2. Run test case [-46201][id-testCase-46201]. 
3. Test case [-46206](#id-testCase-46206){ #id-testCase-46206 }: If a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registry Domain ID”.
```json
{
  "code": -46206,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registry Domain ID was found but the domain handle was not redacted."
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
Note [-46205][id-testCase-46205] proceeds immediately after [-46201][id-testCase-46201] in [[rdapResponseProfile_2_2_Validation]][id-rdapResponseProfile_2_2_Validation].
Note [-46206][id-testCase-46206] proceeds immediately after [-46202][id-testCase-46202] above.

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
For every entity of the domain (i.e. the top-level entities of the domain, not entities subordinate to other entities) 
excluding entities with the roles "reseller", “registrar”, “registrant”, or “technical”, verify the handle is of the format: "(\w|_){1,80}-\w{1,8}".
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
2. Test case [-47701](#id-testCase-47701){ #id-testCase-47701 }: With the handle of the “registrar” entity, verify that the “value” property of the link object above matches one of the base URLs in the registrarId data set.
```json
{
  "code": -47701,
  "value": "<link structure>",
  "message": "The registrar base URL is not registered with IANA."
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
4. Test case [-47703](#id-testCase-47703){ #id-testCase-47703 }: With the link object above, verify that the href property is a valid URI according to the rules in [webUriValidation][id-webUriValidation].
```json
{
  "code": -47703,
  "value": "<link structure>",
  "message": "The ‘value’ property is not a valid Web URI according to [webUriValidation]."
}
```

## Registrar Expiration

1. Test case [-65600](#id-testCase-65600){ #id-testCase-65600 }: If the query is of a gTLD Registrar (e.g., --gtld-registrar), 
validate that an eventAction type “registrar expiration” exists in the topmost events structure.
```json
{
  "code": -65600,
  "value": "<events data structure>",
  "message": "An eventAction of type 'registrar expiration' is expected."
}
```

## Registry Tech

1. Test case [-65700](#id-testCase-65700){ #id-testCase-65700 }: If an entity with the role “technical” at the top level of the domain is 
present and that entity has a ‘handle’ property, verify the format complies with the format specified in RFC 5730: “(\w|_){1,80}-\w{1,8}”.
```json
{
  "code": -65700,
  "value": "<handle>",
  "message": "The handle of the technical entity does not comply with with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730."
} 
```
2. Test case [-65701](#id-testCase-65701){ #id-testCase-65701 }: If the handle in the entity object from -65700 complies with the RFC 5730 format, 
validate that the string followed by the hyphen (“-”, ASCII value 0x2D) is a registered EPPROID.
```json
{
  "code": -65701,
  "value": "<handle>",
  "message": "The globally unique identifier in the technical entity handle is not registered in EPPROID."
}
```
3. Test case [-65702](#id-testCase-65702){ #id-testCase-65702 }: If the handle from test -65700 exists and a redaction object (see RFC 9537) 
is in the redacted array with a name object containing the type property which is a JSON string of “Registry Tech ID”.
```json
{
  "code": -65702,
  "value": "<redacted data structure>",
  "message" : "a redaction of type Registry Tech ID was found but the technical handle was not redacted."
}
```
4. Test case [-65703](#id-testCase-65703){ #id-testCase-65703 }: In the redaction object from -65703, if the ‘pathLang’ property is either 
absent or present as a JSON string of “jsonpath”, then verify that the ‘prePath’ property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -65704,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registry Tech ID",
}
```
5. Test case [-65704](#id-testCase-65704){ #id-testCase-65704 }: In the redaction object from -65703, if the ‘pathLang’ property is either absent or present as a 
JSON string of “jsonpath”, then verify that the ‘prePath’ property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -65704,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registry Tech ID",
}
```
6. Test case [-65705](#id-testCase-65705){ #id-testCase-65705 }: With the JSONPath expression from test case -65703, if the pathLang property is either 
absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -65705,
  "value": "<redaction object>",
  "message" : "jsonpath must evaluate to a zero set for redaction removal of Registry Tech ID."
}
```
6. Test case [-65706](#id-testCase-65706){ #id-testCase-65706 }: In the redaction object from -65703, verify that the method property is either absent 
or is present as a JSON string of “removal”.
```json
{
  "code": -65706,
  "value": "<redaction object>",
  "message": "Registry Tech ID redaction method must be removal if present."
}
```

## Redaction Warnings

1. Test case [-65800](#id-testCase-65800){ #id-testCase-65800 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object
containing a ‘description’ property which is a JSON string of “Registry Domain ID” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65800,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registry Domain ID exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
2. Test case [-65801](#id-testCase-65801){ #id-testCase-65801 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registry Registrant ID” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65801,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registry Registrant ID exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
3. Test case [-65802](#id-testCase-65802){ #id-testCase-65802 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Name” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65802,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Name exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
4. Test case [-65803](#id-testCase-65803){ #id-testCase-65803 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Organization” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65803,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Organization exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
5. Test case [-65804](#id-testCase-65804){ #id-testCase-65804 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Street” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65804,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Street exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
6. Test case [-65805](#id-testCase-65805){ #id-testCase-65805 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant City” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65805,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant City exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
7. Test case [-65806](#id-testCase-65806){ #id-testCase-65806 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Postal Code” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65806,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Postal Code exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
8. Test case [-65807](#id-testCase-65807){ #id-testCase-65807 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Phone” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65807,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Phone exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
9. Test case [-65808](#id-testCase-65808){ #id-testCase-65808 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Phone Ext” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65808,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Phone Ext exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
10. Test case [-65809](#id-testCase-65809){ #id-testCase-65809 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Fax” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65809,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Fax exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
11. Test case [-65810](#id-testCase-65810){ #id-testCase-65810 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Fax Ext” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65810,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Fax Ext exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
12. Test case [-65811](#id-testCase-65811){ #id-testCase-65811 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registrant Email” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65811,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registrant Email exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
13. Test case [-65812](#id-testCase-65812){ #id-testCase-65812 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Registry Tech ID” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65812,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Registry Tech ID exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
14. Test case [-65813](#id-testCase-65813){ #id-testCase-65813 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Tech Name” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65813,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Tech Name exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
15. Test case [-65814](#id-testCase-65814){ #id-testCase-65814 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Tech Phone” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65814,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Tech Phone exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
16. Test case [-65815](#id-testCase-65815){ #id-testCase-65815 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Tech Phone Ext” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65815,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Tech Phone Ext exists. This warning may be ignored if the redaction should not use the 'type' property."
}
```
17. Test case [-65816](#id-testCase-65816){ #id-testCase-65816 }: If a redaction object (see RFC 9537) is in the “redacted” array with a ‘name’ object 
containing a ‘description’ property which is a JSON string of “Tech Email” then this validation should appear in the WARNING array of the results:
```json
{
  "code": -65816,
  "value" : "<redaction object>",
  "message" : "A redaction object with a description of Tech Email exists. This warning may be ignored if the redaction is should not use the 'type' property."
}
```
