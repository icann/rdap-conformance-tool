# Domain Technical Contact Tests

## RP 2024 Section 2.7.6.1

Test group: [[rdapResponseProfile2024_2_7_6_1_Validation]](#id-rdapResponseProfile2024_2_7_6_1_Validation){ #id-rdapResponseProfile2024_2_7_6_1_Validation }

These tests only apply to an entity with the “technical” role, if present.

1. Test case [-65000](#id-testCase-65000){ #id-testCase-65000 }: Verify the fn property of all the vCard objects of the entity with the “technical” role is present.
```json
{
  "code": -65000,
  "value": "<vcard array>",
  "message": "The fn property is required on the vcard for the technical contact."
}
```

If the fn property above is present but empty, the following tests apply:

1. Test case [-65001](#id-testCase-65001){ #id-testCase-65001 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Tech Name”.
```json
{
  "code": -65001,
  "value": "<redacted data structure>",
  "message": "a redaction of type Tech Name is required."
}
```
2. Test case [-65002](#id-testCase-65002){ #id-testCase-65002 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the postPath property is present with a valid JSONPath expression.
```json
{
  "code": -65002,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Tech Name"
}
```
3. Test case [-65003](#id-testCase-65003){ #id-testCase-65003 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -65003,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a non-empty set for redaction by empty value of Tech Name."
}
```
4. Test case [-65004](#id-testCase-65004){ #id-testCase-65004 }: In the redaction object from the above test, verify that the method property is present as is a JSON string of “emptyValue”.
```json
{
  "code": -65004,
  "value": "<redaction object>",
  "message": "Tech Name redaction method must be emptyValue"
}
```

If the fn property above is present but NOT empty, the following tests apply:

1. Test case [-65005](#id-testCase-65005){ #id-testCase-65005 }: If a redaction object (see RFC 9537) is in the redacted array with a 
name object containing the type property which is a JSON string of “Tech Name”.
```json
{
  "code": -65005,
  "value": "<redacted data structure>",
  "message": "a redaction of type Tech Name was found but tech name was not redacted."
}
```

## RP 2024 Section 2.7.6.2

Test group: [[rdapResponseProfile2024_2_7_6_2_Validation]](#id-rdapResponseProfile2024_2_7_6_2_Validation){ #id-rdapResponseProfile2024_2_7_6_2_Validation }

These tests only apply to an entity with the “technical” role, if present.

If a tel property with a “voice” parameter on the vCards for the entity with the role of “technical” is not present, the following tests apply:

1. Test case [-65100](#id-testCase-65100){ #id-testCase-65100 }: Verify that a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Tech Phone”.
```json
{
  "code": -65100,
  "value": "<redacted data structure>",
  "message": "a redaction of type Tech Phone is required."
}
```
2. Test case [-65101](#id-testCase-65101){ #id-testCase-65101 }: In the redaction object from the above test, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -65101,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Tech Phone"
}
```
3. Test case [-65102](#id-testCase-65102){ #id-testCase-65102 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to an empty set.
```json
{
  "code": -65102,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a zero set for redaction by removal of Tech Phone."
}
```
4. Test case [-65103](#id-testCase-65103){ #id-testCase-65103 }: In the redaction object from the above test, verify that the method property is either absent or is present as is a JSON string of “removal”.
```json
{
  "code": -65103,
  "value": "<redaction object>",
  "message": "Tech Phone redaction method must be removal if present"
}
```

If a tel property with a “voice” parameter on the vCards for the entity with the role of “technical” is present, the following tests apply:

1. Test case [-65104](#id-testCase-65104){ #id-testCase-65104 }: If a redaction object (see RFC 9537) is in the redacted array with a 
name object containing the type property which is a JSON string of “Tech Phone”.
```json
{
  "code": -65104,
  "value": "<redacted data structure>",
  "message": "a redaction of type Tech Phone was found but tech phone was not redacted."
}
```

## RP 2024 Section 2.7.6.3

Test group: [[rdapResponseProfile2024_2_7_6_3_Validation]](#id-rdapResponseProfile2024_2_7_6_3_Validation){ #id-rdapResponseProfile2024_2_7_6_3_Validation }

These tests only apply to an entity with the “technical” role, if present.

If a redaction object (see RFC 9537) is in the redacted array with a name object containing the type property which is a JSON string of “Tech Email”, these tests apply:

1. Test case [-65200](#id-testCase-65200){ #id-testCase-65200 }: Verify that the contact-uri property and the email property do not exist together on any vCards for the entity with the role of “technical”. 
```json
{
  "code": -65200,
  "value": "<vCard/jCard structrue>",
  "message": "a redaction of Tech Email may not have both the email and contact-uri"
}
```
2. Test case [-65201](#id-testCase-65201){ #id-testCase-65201 }: Verify that either the contact-uri property or the email property exists on all vCards for the entity with the role of “technical”. 
```json
{
  "code": -65201,
  "value": "<vCard/jCard structrue>",
  "message": "a redaction of Tech Email must have either the email or contact-uri"
}
```
3. Test case [-65202](#id-testCase-65202){ #id-testCase-65202 }: In the redaction object from the above test, verify that the method property is present and is a JSON string of “replacementValue”.
```json
{
  "code": -65202,
  "value": "<redaction object>",
  "message": "Tech Email redaction method must be replacementValue"
}
```

Given the above, if the email property exists on any of the vCards, the following tests apply:

1. Test case [-65203](#id-testCase-65203){ #id-testCase-65203 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the postPath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -65203,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Tech Email postPath"
}
```
2. Test case [-65204](#id-testCase-65204){ #id-testCase-65204 }: With the JSONPath expression from above, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -65204,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email."
}
```

Given the above, if the contact-uri property exists on any of the vCards, the following tests apply:

1. Test case [-65205](#id-testCase-65205){ #id-testCase-65205 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the replacementPath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -65205,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Tech Email replacementPath"
}
```
2. Test case [-65206](#id-testCase-65206){ #id-testCase-65206 }: In the redaction object from the above, if the pathLang property is either absent or is present as a JSON string of “jsonpath”, then verify that the prePath property is either absent or is present with a valid JSONPath expression.
```json
{
  "code": -65206,
  "value": "<redaction object>",
  "message": "jsonpath is invalid for Tech Email prePath"
}
```
3. Test case [-65207](#id-testCase-65207){ #id-testCase-65207 }: With the JSONPath expression from above in replacementPath, if the pathLang property is either absent or is present as a string of “jsonpath” then verify that the expression evaluates to a non-empty set.
```json
{
  "code": -65207,
  "value": "<redaction object>",
  "message": "jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email in replacementPath"
}
```
