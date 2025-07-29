# General Entity Within Domain Tests

## RP Sections 2.7.1.x, 2.7.2.x, 2.7.3.x, and 2.7.4.x

Test group: [[rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation]](#id-rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation){ #id-rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation }

The following steps should be used to test the RDAP protocol section 2.7.1.X and 2.7.2.X and 2.7.3.X and 2.7.4.X of the RDAP Response Profile 2.1:

1. Test case [-52100](#id-testCase-52100){ #id-testCase-52100 }: For entities with the registrant, administrative, technical and billing role within the
domain object, if a remarks member with the title "REDACTED FOR PRIVACY" is
included, validate that the type member is "object redacted due to authorization".
```json
{
  "code": -52100,
  "value": "<entity data structure>",
  "message": "An entity with the registrant, administrative, technical or billing role with a remarks members with the title 'REDACTED FOR PRIVACY' was found, but the description and type does not contain the value in 2.7.4.3 of the RDAP_Response_Profile_2_1."
}
```
2. Test case [-52101](#id-testCase-52101){ #id-testCase-52101 }: For entities with the registrant, administrative, technical and billing role within the
    domain object, if a _remarks_ member with the title "REDACTED FOR PRIVACY" is not
    included, validate that valid handle , fn , adr , tel members are included. For the adr
    member, validate that the following RDDS fields are included: Street and City.
```json
{
  "code": -52101,
  "value": "<entity data structure>",
  "message": "An entity without a remark titled “REDACTED FOR PRIVACY” does not have all the necessary information of handle, fn, adr, tel, street and city."
}
```
3. Test case [-52102](#id-testCase-52102){ #id-testCase-52102 }: For entities with the registrant, administrative, technical and billing role within the
    domain object, if a _remarks_ member with the title "REDACTED FOR PRIVACY" is not
    included, validate that the handle comply with the following format specified in
    RFC5730: "(\w|_){1,80}-\w{1,8}".
```json
{
  "code": -52102,
  "value": "<nameserver object>",
  "message": "The handle in the entity object does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730."
}
```
4. Test case [-52103](#id-testCase-52103){ #id-testCase-52103 }: For entities with the registrant, administrative, technical and billing role within the
    domain object, if a _remarks_ member with the title "REDACTED FOR PRIVACY" is not
    included and the handle conforms to the format: "(\w|_){1,80}-\w{1,8}", validate that
    the string followed by a hyphen ("-", ASCII value 0x002D) is registered in EPPROID.
```json
{
  "code": -52103,
  "value": "<nameserver object>",
  "message": "The globally unique identifier in the entity object handle is not registered in EPPROID."
}
```
5. Test case [-52104](#id-testCase-52104){ #id-testCase-52104 }: Only one entity shall be assigned the following roles: registrant, administrative,
    technical and billing.
```json
{
  "code": -52104,
  "value": "<entities data structure>",
  "message": "More than one entity with the following roles were found: registrant, administrative, technical and billing."
}
```
See [Registrant Without CC Parameter][registrant-without-cc-parameter] in the conformance considerations for more information.
6. Test case [-52105](#id-testCase-52105){ #id-testCase-52105 }: For entities with the registrant role within the domain object, validate that the CC
parameter is included in the entity as defined in RFC8605.
```json
{
  "code": -52105,
  "value": "<entity data structure>",
  "message": "An entity with the registrant role without the CC parameter was found. See section 2.7.3.1 of the RDAP_Response_Profile_2_1."
}
```
7. Test case [-52106](#id-testCase-52106){ #id-testCase-52106 }: 
For entities with the registrant, administrative, technical and billing role within the domain object, if a remarks member with the title "REDACTED FOR PRIVACY" is not included and the handle conforms to the format: "(\w|_){1,80}-\w{1,8}", validate that the string followed by a hyphen ("-", ASCII value 0x002D) is not “ICANNRST”.
```json
{
  "code": -52106,
  "value": "<entity data structure>",
  "message": "The globally unique identifier in the entity object handle is using an EPPROID reserved for testing by ICANN."
}
```
