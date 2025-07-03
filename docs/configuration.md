# Configuration

The definition file specifies which single tests are errors, warnings, or ignored (i.e. not tested for).

_Note: a definition file is required for starting a test._

## The Configuration File

- _definitionIdentifier_ : a required JSON string that identifies the configuration definition file. The string is copied
verbatim to the _definitionIdentifier_ element of the results file.

- _definitionError_ : an optional JSON array of objects. Each object contains the following elements:
      - code : a required JSON number that identifies a single test.
      - notes : a required JSON string that is copied verbatim if the test fails, generating an entry in the _results_ section in the results file.

- _definitionWarning_ : an optional JSON array of objects. Each object contains the following elements:
      - code : a required JSON number that identifies a single test.
      - notes : an optional JSON string that is copied verbatim if the test fails, generating an entry in the results section in the results file.
- _definitionIgnore_ : an optional JSON array of single test identifiers that are ignored (i.e. not tested for). The
contents of this element are copied verbatim to the _ignore_ section in the results file.
- _definitionNotes_ : an optional JSON array of strings that are copied verbatim to the _notes_ section in the results file.

## Example Configuration

``` json
{
    "definitionIdentifier": "gTLD Profile Version 1.0",
    "definitionError": [{
        "code": -1102,
        "notes": "If the gTLD is a legacy gTLD, this may not indicate an error, review by a person is required."
    }],
    "definitionWarning": [{
        "code": -2186,
        "notes": "This only applies for a few gTLDs."
    }],
    "definitionIgnore": [-2323, -2345, -2346],
    "definitionNotes": ["This is a configuration definition for a legacy gTLD.", "Developed by ICANN."]
}
```

