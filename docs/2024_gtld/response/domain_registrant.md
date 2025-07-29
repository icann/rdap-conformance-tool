# Domain Registrant Tests

## RP 2024 Section 2.7.2

Test group: [[rdapResponseProfile2024_2_7_2_Validation]](#id-rdapResponseProfile2024_2_7_2_Validation){ #id-rdapResponseProfile2024_2_7_2_Validation }

1. Test case [-63000](#id-testCase-63000){ #id-testCase-63000 }: If the queried RDAP server is a registrar (such as with the --gtld-registrar command line parameter or through the configuration object), verify that the domain object has one entity with the “registrant” role.
```json
{
  "code": -63000,
  "value": "<domain structure>",
  "message": "A domain served by a registrar must have one registrant."
}
```

## Registrant Handle

Test group: [[rdapResponseProfile_registrant_handle]](#id-rdapResponseProfile_registrant_handle){ #id-rdapResponseProfile_registrant_handle }

These tests only apply to an entity with the “registrant” role, if present.

If the handle of the entity with the “registrant” role is present, the following tests apply:

1. Test case [-63100](#id-testCase-63100){ #id-testCase-63100 }: 
The handle of the entity object above shall comply with the following format specified in RFC5730: "(\w|\_){1,80}-\w{1,8}".
```json
{
  "code": -63100,
  "value": "<handle>",
  "message": "The handle of the registrant does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730."
}
```
2. Test case [-63101](#id-testCase-63101){ #id-testCase-63101 }: 
If the handle of the entity object above complies with the format: "(\w|_){1,80}-\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is registered in EPPROID.
```json
{
  "code": -63101,
  "value": "<handle>",
  "message": "The globally unique identifier in the registrant handle is not registered in EPPROID."
}
```

If the handle is NOT in the entity object with the registrant, the following tests apply:

1. Test case [-63102](#id-testCase-63102){ #id-testCase-63102 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registry Registrant ID”.
```json
{
  "code": -63102,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registry Registrant ID is required."
}
```
2. Test case [-63103](#id-testCase-63103){ #id-testCase-63103 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -63103,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registry Registrant ID"
}
```
3. Test case [-63104](#id-testCase-63104){ #id-testCase-63104 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -63104,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a zero set for redaction by removal of Registry Registrant ID."
}
```
4. Test case [-63105](#id-testCase-63105){ #id-testCase-63105 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -63105,
  "value": "<redaction object>",
  "message": "Registry Registrant ID redaction method must be removal if present"
}
```

## RP 2024 Section 2.7.4.1

Test group: [[rdapResponseProfile2024_2_7_4_1_Validation]](#id-rdapResponseProfile2024_2_7_4_1_Validation){ #id-rdapResponseProfile2024_2_7_4_1_Validation }

These tests only apply to an entity with the “registrant” role, if present.

1. Test case [-63200](#id-testCase-63200){ #id-testCase-63200 }: Verify the fn property of all the vCard objects of the entity with the “registrant” role is present.
```json
{
  "code": -63200,
  "value": "<vcard array>",
  "message": "The fn property is required on the vcard for the registrant."
}
```

If the fn property above is present but empty, the following tests apply:

1. Test case [-63201](#id-testCase-63201){ #id-testCase-63201 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Name”.
```json
{
  "code": -63201,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registrant Name is required."
}
```
2. Test case [-63202](#id-testCase-63202){ #id-testCase-63202 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the postPath property is present with a valid JSONPath expression.
```json
{
  "code": -63202,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Name"
}
```
3. Test case [-63203](#id-testCase-63203){ #id-testCase-63203 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -63203,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Name."
}
```
4. Test case [-63204](#id-testCase-63204){ #id-testCase-63204 }: In the redaction object from the above test, verify that the method property is present as is a JSON string of “emptyValue”.
```json
{
  "code": -63204,
  "value": "<redaction object>",
  "message": "Registrant Name redaction method must be empytValue"
}
```

## RP 2024 Section 2.7.4.2

Test group: [[rdapResponseProfile2024_2_7_4_2_Validation]](#id-rdapResponseProfile2024_2_7_4_2_Validation){ #id-rdapResponseProfile2024_2_7_4_2_Validation }

These tests only apply to an entity with the “registrant” role, if present.

If the org property on the vCards for the entity with the role of registrant is not present, the following tests apply:

1. Test case [-63300](#id-testCase-63300){ #id-testCase-63300 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Organization”.
```json
{
  "code": -63300,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registrant Organization is required."
}
```
2. Test case [-63301](#id-testCase-63301){ #id-testCase-63301 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -63301,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Organization"
}
```
3. Test case [-63302](#id-testCase-63302){ #id-testCase-63302 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -63302,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a zero set for redaction by removal of Registrant Organization."
}
```
4. Test case [-63303](#id-testCase-63303){ #id-testCase-63303 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -63303,
  "value": "<redaction object>",
  "message": "Registrant Organization redaction method must be removal if present"
}
```

## RP 2024 Section 2.7.4.3

Test group: [[rdapResponseProfile2024_2_7_4_3_Validation]](#id-rdapResponseProfile2024_2_7_4_3_Validation){ #id-rdapResponseProfile2024_2_7_4_3_Validation }

These tests only apply to an entity with the “registrant” role, if present.

1. Test case [-63400](#id-testCase-63400){ #id-testCase-63400 }: Verify the street value (zero-indexed 2) of the adr property of all the vCard objects of the entity with the “registrant” role is present.
```json
{
  "code": -63400,
  "value": "<vcard array>",
  "message": "The street value of the adr property is required on the vcard for the registrant."
}
```

If the street value of the adr property above is present but empty, the following tests apply:

1. Test case [-63401](#id-testCase-63401){ #id-testCase-63401 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Street”.
```json
{
  "code": -63401,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registrant Street is required."
}
```
2. Test case [-63402](#id-testCase-63402){ #id-testCase-63402 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the postPath property is present with a valid JSONPath expression.
```json
{
  "code": -63402,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Street"
}
```
3. Test case [-63403](#id-testCase-63403){ #id-testCase-63403 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -63403,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Street."
}
```
4. Test case [-63404](#id-testCase-63404){ #id-testCase-63404 }: In the redaction object from the above test, verify that the method property is present as is a JSON string of “emptyValue”.
```json
{
  "code": -63404,
  "value": "<redaction object>",
  "message": "Registrant Street redaction method must be empytValue"
}
```

## RP 2024 Section 2.7.4.4

Test group: [[rdapResponseProfile2024_2_7_4_4_Validation]](#id-rdapResponseProfile2024_2_7_4_4_Validation){ #id-rdapResponseProfile2024_2_7_4_4_Validation }

These tests only apply to an entity with the “registrant” role, if present.

1. Test case [-63500](#id-testCase-63500){ #id-testCase-63500 }: Verify the city value (zero-indexed 3) of the adr property of all the vCard objects of the entity with the “registrant” role is present.
```json
{
  "code": -63500,
  "value": "<vcard array>",
  "message": "The city value of the adr property is required on the vcard for the registrant."
}
```

If the city value of the adr property above is present but empty, the following tests apply:

2. Test case [-63501](#id-testCase-63501){ #id-testCase-63501 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant City”.
```json
{
  "code": -63501,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registrant City is required."
}
```
3. Test case [-63502](#id-testCase-63502){ #id-testCase-63502 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the postPath property is present with a valid JSONPath expression.
```json
{
  "code": -63502,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant City"
}
```
4. Test case [-63503](#id-testCase-63503){ #id-testCase-63503 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -63503,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant City."
}
```
5. Test case [-63504](#id-testCase-63504){ #id-testCase-63504 }: In the redaction object from the above test, verify that the method property is present as is a JSON string of “emptyValue”.
```json
{
  "code": -63504,
  "value": "<redaction object>",
  "message": "Registrant City redaction method must be empytValue"
}
```

## RP 2024 Section 2.7.4.6

Test group: [[rdapResponseProfile_2_7_4_6_Validation]](#id-rdapResponseProfile_2_7_4_6_Validation){ #id-rdapResponseProfile_2_7_4_6_Validation }

These tests only apply to an entity with the “registrant” role, if present.

1. Test case [-63600](#id-testCase-63600){ #id-testCase-63600 }: Verify the postal code value (zero-indexed 5) of the adr property of all the vCard objects of the entity with the “registrant” role is present.
```json
{
  "code": -63600,
  "value": "<vcard array>",
  "message": "The postal code value of the adr property is required on the vcard for the registrant."
}
```

If the postal code value of the adr property above is present but empty, the following tests apply:

1. Test case [-63601](#id-testCase-63601){ #id-testCase-63601 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Postal Code”.
```json
{
  "code": -63601,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registrant Postal Code is required."
}
```
2. Test case [-63602](#id-testCase-63602){ #id-testCase-63602 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the postPath property is present with a valid JSONPath expression.
```json
{
  "code": -63602,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Postal Code"
}
```
3. Test case [-63603](#id-testCase-63603){ #id-testCase-63603 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -63603,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Postal Code."
}
```
4. Test case [-63604](#id-testCase-63604){ #id-testCase-63604 }: In the redaction object from the above test, verify that the method property is present as is a JSON string of “emptyValue”.
```json
{
  "code": -63604,
  "value": "<redaction object>",
  "message": "Registrant Postal Code redaction method must be empytValue"
}
```

## RP 2024 Section 2.7.4.8

Test group: [[rdapResponseProfile2024_2_7_4_8_Validation]](#id-rdapResponseProfile2024_2_7_4_8_Validation){ #id-rdapResponseProfile2024_2_7_4_8_Validation }

These tests only apply to an entity with the “registrant” role, if present.

If a tel property with a “voice” parameter on the vCards for the entity with the role of registrant is not present, the following tests apply:

1. Test case [-63700](#id-testCase-63700){ #id-testCase-63700 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Phone”.
```json
{
  "code": -63700,
  "value": "<redacted data structure>",
  "message": "a redaction of type Registrant Phone is required."
}
```
2. Test case [-63701](#id-testCase-63701){ #id-testCase-63701 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -63701,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Phone"
}
```
3. Test case [-63702](#id-testCase-63702){ #id-testCase-63702 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -63702,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone."
}
```
4. Test case [-63703](#id-testCase-63703){ #id-testCase-63703 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -63703,
  "value": "<redaction object>",
  "message": "Registrant Phone redaction method must be removal if present"
}
```

## RP 2024 Section 2.7.5.1

Test group: [[rdapResponseProfile2024_2_7_5_1_Validation]](#id-rdapResponseProfile2024_2_7_5_1_Validation){ #id-rdapResponseProfile2024_2_7_5_1_Validation }

These tests only apply to an entity with the “registrant” role, if present.

If  a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Phone Ext”, these tests apply:

1. Test case [-63800](#id-testCase-63800){ #id-testCase-63800 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -63800,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Phone Ext"
}
```
2. Test case [-63801](#id-testCase-63801){ #id-testCase-63801 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -63801,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone Ext."
}
```
3. Test case [-63802](#id-testCase-63802){ #id-testCase-63802 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -63802,
  "value": "<redaction object>",
  "message": "Registrant Phone Ext redaction method must be removal if present"
}
```

## RP 2024 Section 2.7.5.2

Test group: [[rdapResponseProfile_2_7_5_2_Validation]](#id-rdapResponseProfile_2_7_5_2_Validation){ #id-rdapResponseProfile_2_7_5_2_Validation }

These tests only apply to an entity with the “registrant” role, if present.

If  a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Fax”, these tests apply:

1. Test case [-63900](#id-testCase-63900){ #id-testCase-63900 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -63900,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Fax"
}
```
2. Test case [-63901](#id-testCase-63901){ #id-testCase-63901 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -63901,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a zero set for redaction by removal of Registrant Fax."
}
```
3. Test case [-63902](#id-testCase-63902){ #id-testCase-63902 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -63902,
  "value": "<redaction object>",
  "message": "Registrant Fax redaction method must be removal if present"
}
```

## RP Section 2.7.5.3

Test group: [[rdapResponseProfile_2_7_5_3_Validation]][id-rdapResponseProfile_2_7_5_3_Validation]

These tests only apply to an entity with the “registrant” role, if present.

If  a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Fax Ext”, these tests apply:

1. Test case [-64000](#id-testCase-64000){ #id-testCase-64000 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -64000,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Fax Ext"
}
```
2. Test case [-64001](#id-testCase-64001){ #id-testCase-64001 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -64001,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a zero set for redaction by removal of Registrant Fax Ext."
}
```
3. Test case [-64002](#id-testCase-64002){ #id-testCase-64002 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -64002,
  "value": "<redaction object>",
  "message": "Registrant Fax Ext redaction method must be removal if present"
}
```

## RP 2024 Section 2.7.4.9

Test group: [[rdapResponseProfile2024_2_7_4_9_Validation]](#id-rdapResponseProfile2024_2_7_4_9_Validation){ #id-rdapResponseProfile2024_2_7_4_9_Validation }

These tests only apply to an entity with the “registrant” role, if present.

If a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Registrant Email”, these tests apply:

1. Test case [-64100](#id-testCase-64100){ #id-testCase-64100 }: Verify that the contact-uri property and the email property do not exist together on any vCards for the entity with the role of “registrant”. 
```json
{
  "code": -64100,
  "value": "<vCard/jCard structrue>",
  "message": "a redaction of Registrant Email may not have both the email and contact-uri"
}
```
2. Test case [-64101](#id-testCase-64101){ #id-testCase-64101 }: Verify that either the contact-uri property or the email property exists on all vCards for the entity with the role of “registrant”. 
```json
{
  "code": -64101,
  "value": "<vCard/jCard structrue>",
  "message": "a redaction of Registrant Email must have either the email and contact-uri"
}
```
3. Test case [-64102](#id-testCase-64102){ #id-testCase-64102 }: In the redaction object from the above test, verify that the method property is present as is a JSON string of “replacementValue”.
```json
{
  "code": -64102,
  "value": "<redaction object>",
  "message": "Registrant Email redaction method must be replacementValue"
}
```

Given the above, if the email property exists on any of the vCards, the following tests apply:

1. Test case [-64103](#id-testCase-64103){ #id-testCase-64103 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the postPath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -64103,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Email postPath"
}
```
2. Test case [-64104](#id-testCase-64104){ #id-testCase-64104 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -64104,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email."
}
```

Given the above, if the contact-uri property exists on any of the vCards, the following tests apply:

1. Test case [-64105](#id-testCase-64105){ #id-testCase-64105 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the replacementPath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -64105,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Email replacementPath"
}
```
2. Test case [-64106](#id-testCase-64106){ #id-testCase-64106 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -64106,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Registrant Email prePath"
}
```
3. Test case [-64107](#id-testCase-64107){ #id-testCase-64107 }: With the JSONPath expression from above in replacementPath, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -64107,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email in replacementPath"
}
```
