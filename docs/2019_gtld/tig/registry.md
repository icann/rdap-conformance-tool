# Registry Tests

## TIG Section 1.11.1

Test group: [tigSection_1_11_1_Validation]  [](){ #id-tigSection_1_11_1_Validation }

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

## TIG Section 3.2

Test group: [tigSection_3_2_Validation]  [](){ #id-tigSection_3_2_Validation }

The following steps should be used to test the RDAP protocol section 3.2 of the RDAP_Technical_Implementation_Guide_2_1:

1. Validate that a links data structure in the topmost object exists, and the links object shall contain the elements _rel_ :related and _href_.
``` json
{
  "code": -23200,
  "value": "<links data structure>",
  "message": "A links data structure in the topmost object exists, and the links object shall contain the elements rel:related and href, but they were not found. See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1."
}
```

## TIG Section 6.1

Test group: [tigSection_6_1_Validation]  [](){ #id-tigSection_6_1_Validation }

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

