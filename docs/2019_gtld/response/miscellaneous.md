# Miscellaneous Tests

Test Group: [[rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation]](id-rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation){ #id-rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation }

The following steps should be used to test the RDAP protocol section 2.3.1.3, 2.7.6, 3.3 and 4.4 of the RDAP Response Profile 2.1:

1. Validate that an _eventAction_ type "last update of RDAP database" exists in events structure included in the topmost object.
``` json
{
  "code": -43100,
  "value": "<events data structure>",
  "message": "An eventAction type last update of RDAP database does not exists in the topmost events data structure. See section 2.3.1.3, 2.7.6, 3.3 and 4.4 of the RDAP_Response_Profile_2_1."
}
```

