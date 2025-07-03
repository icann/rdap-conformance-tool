# Registrar Entity Tests

#### 8.11.1. [rdapResponseProfile_3_1_Validation]

#### The following steps should be used to test the RDAP protocol section 3.1 of the

#### RDAP_Response_Profile_2_1:

1. An _entity_ with the registrar role as the topmost object shall exist.
    {
    "code": - 60100 ,
    "value": "<topmost object>",
    "message": "An entity with the registrar role was not found as the topmost
    object. See section 3.1 of the RDAP_Response_Profile_2_1"
    }
2. Validate that valid _handle_ , _fn_ , _adr_ , _tel_ , _email_ members are included in the topmost
    object. For the _adr_ member, validate that the following RDDS fields are included: Street,
    City and Country.
    {
    "code": - 60101 ,
    "value": "<topmost object>",
    "message": "The required members for a registrar entity were not found.
    See section 3.1 of the RDAP_Response_Profile_2_1."
    }

#### 8.11.2. [rdapResponseProfile_3_2_Validation]

#### The following steps should be used to test the RDAP protocol section 3.2 of the

#### RDAP_Response_Profile_2_1:

1. If entities with the administrative and technical roles within the topmost object exist,
    validate that valid _fn_ , _tel_ , _email_ members are included.
    {
    "code": - 60200 ,
    "value": "<entity data structure>",
    "message": "The required members for entities with the administrative and
    technical roles were not found. See section 3.2 of the RDAP_Response_Profile_2_1."
    }


