# General Tests

## RP Section 1.2.2

Test group: [rdapResponseProfile_1_2_2_Validation]  [](){ #id-rdapResponseProfile_1_2_2_Validation }

The following steps should be used to test the RDAP protocol section 1.2.2 of the RDAP Response Profile 2.1:

1. Validate that the RDAP response does not contain browser executable code (e.g., JavaScript).
``` json
{
  "code": -40100,
  "value": "<rdap response>",
  "message": "The RDAP response contains browser executable code (e.g., JavaScript). See section 1.2.2 of the RDAP_Response_Profile_2_1.""
}
```

## RP Section 1.3 

Test group: [rdapResponseProfile_1_3_Validation]  [](){ #id-rdapResponseProfile_1_3_Validation }

The following steps should be used to test the RDAP protocol section 1.3 of the  RDAP Response Profile 2.1:

1. Validate that the JSON string value "icann_rdap_response_profile_0" is included in the _RDAP Conformance_ data structure.
``` json
{
  "code": -40200,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include icann_rdap_response_profile_0. See section 1.3 of the RDAP_Response_Profile_2_1."
}
```

## RP Section 1.4 

Test group: [rdapResponseProfile_1_4_Validation]  [](){ #id-rdapResponseProfile_1_4_Validation }

The following steps should be used to test the RDAP protocol section 1. 4 of the RDAP Response Profile 2.1:

1. Validate that the country name parameter is empty in the _adr_ of all the jCard objects in the RDAP response.
``` json
{
  "code": -40400,
  "value": "<vcard object>",
  "message": "A vcard object with a country name parameter with data was found. "
}
```
