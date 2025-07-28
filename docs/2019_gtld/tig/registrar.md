# Registrar Tests

## TIG Section 1.12.1

Test group: [[tigSection_1_12_1_Validation]](#id-tigSection_1_12_1_Validation){ #id-tigSection_1_12_1_Validation }

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

