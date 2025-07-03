# Registrar Entities Within Domain Tests

#### 8.10.1. [rdapResponseProfile_2_7_5_2_Validation]

#### The following steps should be used to test the RDAP protocol section 2.7.5.2 of the

#### RDAP_Response_Profile_2_1:

1. For the _entities_ with the registrant, administrative, technical and billing role within the

```
domain object, if the email property is omitted, validate that a CONTACT-URI member is
included.
{
"code": - 58000 ,
"value": "<entity data structure>",
"message": "An entity with the administrative, technical, or billing role
without a CONTACT-URI member was found. See section 2.7.5.2 of the
RDAP_Response_Profile_2_1."
}
```
2. For the _entities_ with the registrant, administrative, technical and billing role within the
    domain object, if a CONTACT-URI member is included, validate that the content is an

```
email address or an http/https link.
{
"code": - 58001 ,
"value": "<entity data structure>",
"message": "The content of the CONTACT-URI member of an entity with the
administrative, technical, or billing role does not contain an email or http/https
link. See section 2.7.5.2 of the RDAP_Response_Profile_2_1."
}
```
#### Note: this test also includes 2.7.5.1.


