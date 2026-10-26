# Registrar Tests

## TIG Section 1.12.1

Test group: [[tigSection_1_12_1_Validation]](#id-tigSection_1_12_1_Validation){ #id-tigSection_1_12_1_Validation }

The following steps should be used to test the RDAP protocol section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1:

1. Test case [-26100](#id-testCase-26100){ #id-testCase-26100 }: Get the identifier in the _publicIds_ element in the _entity_ with the registrar role.
``` json
{
  "code": -26100,
  "value": "<publicIds data structure>",
  "message": "An identifier in the publicIds within the entity data structure with the registrar role was not found. See section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
2. Test case [-26101](#id-testCase-26101){ #id-testCase-26101 }: For the _identifier_ found in the previous step, validate that an entry exists in the **registrarId**.
``` json
{
  "code": -26101,
  "value": "<identifier> '\n/\n' <registrarId>",
  "message": "The registrar identifier is not included in the registrarId. See section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
3. Test case [-26102](#id-testCase-26102){ #id-testCase-26102 }: For the _identifier_ found in the previous step, verify that every of the base URLs contain a schema of "https".
``` json
{
  "code": -26102,
  "value": "<Registrar entry in registrarId>",
  "message": "One or more of the base URLs for the registrar contain a schema different from https. See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1."
}
```
4. Test case [-26103](#id-testCase-26103){ #id-testCase-26103 }: If the query is a domain query from a gTLD registry (e.g. --gtld-registry) and the identifier found in 
test case -26101 is marked as “Accredited” in the IANA Registrar ID registry, verify that the domain has an object in the “links” array with a “rel” property 
containing “related” and an “href” property with a value starting with one of the base URLs found in test case -26102 followed by “/domain/” and 
a valid domain name (e.g. https://registrar.example.com/domain/domainname.example).
``` json
{
  "code": -26103,
  "value": "<href in link>",
  "message": "Referral to registrar is either unregistered with IANA or invalid."
}
```
