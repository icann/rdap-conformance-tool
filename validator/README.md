# RDAP Conformance Validator

Validator for the RDAP conformance tool

# Architecture of json validation

Currently, the architecture leverage the json schema draft 07 to validate rdap json responses.
The main validation library used is [org.everit.json](https://github.com/everit-org/json-schema).

We inspired our json-schemas from the one from [Mario Loffredo and Maurizio Martinelli](https://gitlab.centralnic.com/centralnic/rdap-json-schemas).
But with multiple additions and modifications to comply with the specification from ICANN.

In order to raise the right error codes when a specification is violated, we introduce two new 
unprocessed properties in the json schema:

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
    
As you can see, we took the liberty to improve the value format in the specification by using a
 json pointer. This is better to locate the error, when the violation is deep in the json
  hierarchy. We could be unfortunate and have to locate the 999 violation really far from the top
  , e.g.:
  
    "#/someKey/someArray/2/someNestedKey/aString:999"

## 1. The validationName property

Say, we validate this json:

    {
        "anObject": {
            "aString": 999
        }
    }
    
Against this schema:

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
    
We are going to get now two errors:

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

Thus, we can always refer to this "validationName" and change the errorCode (-555 here) depending on
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

Even if we can write a combined schema like this:

    "rdapExtensions": {
      "type": "string",
      "enum": [
        "rdap_level_0",
        "arin_originas0",
        "artRecord"
      ],
      "errorCode": -10502
    }

ICANN wants a different error code when the rdapExtensions is not a string and when the string is
 not part of the enum. Since we can't write:
 
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
     
and hope the json schema lib will understand, we need to explicitly write the json-schema in 
its long form (allOf keyword) to support this:

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

1. matches(e) that tell if the ValidationException of org.everit.json matches the subclass at
     hand. For instance, MissingKeyExceptionParser matches telling a required key is missing.
2. parse() that will generate the right formatted errors for ICANN standards.

Most of the parser exception classes are implemented right now, but if you see new cases, or new
edge cases, feel free to implement a new one suiting to your specific schema violations.

## SchemaNode(s)

The base class SchemaNode is a classic tree structure (representing the schema tree). This
 structure is only there to find the schemas associated to violation and pickup the error codes
 or the detailed information to display when it occurs. 