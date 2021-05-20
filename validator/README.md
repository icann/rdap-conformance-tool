# RDAP Conformance Validator

Validator library module for the RDAP conformance tool

# Architecture of json validation

The architecture leverages the json schema draft 07 to validate rdap json responses.
The main validation library used is [org.everit.json](https://github.com/everit-org/json-schema).

The json-schemas were adapted from the one from [Mario Loffredo and Maurizio Martinelli](https://gitlab.centralnic.com/centralnic/rdap-json-schemas).

In order to raise the right error codes when a specification is violated, two new unprocessed properties in the json schema:

1. errorCode (for basic type validation violation)
2. validationName (for object validation violation)

## 1. The errorCode property
For instance, the following JSON:

    {
        "aString": 999
    }
 
 validated against the following json schema:
 
    "aString": {
        "type": "string",
        "errorCode": -1
    }

will produce the following error: 

    {
        "error": -1,
        "value": "#/aString:999"
        "message": "The JSON value is not a string"
    }
    
The value format is using a json pointer. This is better to locate the error, when the violation is
deep in the json hierarchy. For instance, locating the 999 violation can be really far from the top,
e.g.:
  
    "#/someKey/someArray/2/someNestedKey/aString:999"

## 1. The validationName property

As an example, this json need to be validated:

    {
        "anObject": {
            "aString": 999
        }
    }
    
against this schema:

    {
        "anObject": {
                "aString": {
                    "type": "string",
                    "errorCode": -1
                },
                "validationName": "stdRdapAnObjectValidation"
        },
        "stdRdapAnObjectValidation": -555
    }
    
This triggers two errors:

        {
            "error": -1,
            "value": "#/anObject/aString:999"
            "message": "The JSON value is not a string"
        },
        {
            "error": -555,
            "value": "#/anObject:{ "aString": { "type": "string", "errorCode": -1 }, "validationName": "stdRdapAnObjectValidation" }"
            "message": "The value for the JSON name value does not pass #/anObject validation [stdRdapAnObjectValidation]"
        }

Thus, one can always refer to this "validationName" and change the errorCode (-555 here) depending on
 the top most structure at hand. This is required by the specifications: the domain structure has:
 
      "stdRdapEntitiesValidation": -12210,
      
while the nameserver structure has:

      "stdRdapEntitiesValidation": -12408,
      
even if both refer to the same json schema:
 
        "entities": {
          "type": "array",
          "items": {
            "$ref": "rdap_entity_object.json#"
          },
          "validationName": "stdRdapEntitiesValidation"
        }

## Special case for errors when key missing

A known corner case that cannot use the above 2 categories (errorCode or validationName)
is the one where an error occurs when a required key is missing. For instance, lets take the
 links schema:
 
    "link": {
          "type": "object",
          "properties": {
            "value": { ... },
            "rel": { ... },
            "href": { ... },
            "hreflang": { ... },
            "title": { ... },
            "media": { ... },
            "type": { ... },
          },
          "additionalProperties": false,
          "hrefMissing": -10610,
          "required": ["href"]
        }

The `required` array from the draft07 tells that the `href` key is mandatory. The annotation 
used here is:

    {{key}}Missing: {{errorCode}}
 
like the one above for `href`:

    "hrefMissing": -10610
 
## Known Limitations

Even if a combined schema like this can be used:

    "rdapExtensions": {
      "type": "string",
      "enum": [
        "rdap_level_0",
        "arin_originas0",
        "artRecord"
      ],
      "errorCode": -10502
    }

However, the specifications require a different error code when the rdapExtensions is not a string and when the string is not part of the enum. Since one can't write:
 
     "rdapExtensions": {
       "type": "string",
       "errorCode": -10501,
       "enum": [
         "rdap_level_0",
         "arin_originas0",
         "artRecord"
       ],
       "errorCode": -10502
     }
     
and hope the json schema lib will understand, the json-schema in 
its long form (allOf keyword) is needed to support this:

     "rdapExtensions": {
           "allOf": [
             {
               "type": "string",
               "errorCode": -10501
             },
             {
               "enum": [
                 "rdap_level_0",
                 "arin_originas0",
                 "artRecord"
               ],
               "errorCode": -10502
             }
           ]
         }

The two writing are equivalent in terms of the json schema draft 07, but the long form allows
us to specify different error codes for each of the combined schemas.

## ExceptionParser(s)

The base class ExceptionParser defined two methods:

1. matches(e) that tell if the ValidationException of org.everit.json matches the subclass at hand.
   For instance, MissingKeyExceptionParser matches telling a required key is missing.
2. parse() that will generate the right formatted errors for ICANN standards.

Most of the parser exception classes are implemented right now, but if you see new cases, or new
edge cases, feel free to implement a new one suiting to your specific schema violations.

Every time a parser exception class does not exist, the tool will log an error like this:

    [main] ERROR ... We found this error with no exception parser {{#/jsonPointer/of/exception}}: {{exception details}}

## SchemaNode(s)

The base class SchemaNode is a classic tree structure (representing the schema tree). This structure
is only there to find the schemas associated to violation and pickup the error codes or the detailed
information to display when it occurs.

## Profile February 2019 validations

The [RDAP profile February 2019](https://www.icann.org/gtld-rdap-profile) validations consist more
of data validations than schema validations as the other ones. Thus, these validations don't use
json-schema but rather leverage the [jpath query language](https://github.com/json-path/JsonPath)
to get the data relevant to perform each checks.

Of course, if the json-schema is wrong, these queries can, as expected, failed (e.g. we expect an
string, but we get an array). When they fail, we log an error like this:

    [main] ERROR ... Exception during validation of : TigValidation1Dot12Dot1 details: Missing property in path $['something'][1]['somethingelse']

and we continue the execution. TigValidation1Dot12Dot1 stands for Tig
-> [Technical Implementation Guide](https://www.icann.org/en/system/files/files/rdap-technical-implementation-guide-15feb19-en.pdf)
section 1.12.1.