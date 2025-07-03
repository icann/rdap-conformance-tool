# Registry Entities Within Domain Tests

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


