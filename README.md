# rdapct

The RDAP Conformance Tool is divided into two modules with their respective documentation:

- [Command line module (tool)](./tool/README.md) the main entry point
- [Validator module](./validator/README.md) the rdap validation library

# Installation

There is no installation needed. The tool is provided as a JAVA11 jar file that can be executed 
with java, e.g.:

    java -jar rdapct-1.0.jar -c ./rdapct-config.json https://rdap.example.com/com/v1/domain/viagenie.com

where rdapct-config.json is the configuration file.

# Configuration file

Configuration definition
The definition file specifies which single tests are errors, warnings, or ignored (i.e. not tested for).
Note: a definition file is required for starting a test.
A configuration definition file specifies:

- definitionIdentifier: a required JSON string that identifies the configuration definition file. 
The string is copied
verbatim to the definitionIdentifier element of the results file.
- definitionError: an optional JSON array of objects.
    - Each object contains the following elements:
        - code: a required JSON number that identifies a single test.
        - notes: a required JSON string that is copied verbatim if the test fails, generating an 
          entry in the results section in the results file.
- definitionWarning: an optional JSON array of objects.
    - Each object contains the following elements:
        - code: a required JSON number that identifies a single test.
        - notes: an optional JSON string that is copied verbatim if the test fails, generating 
          an entry in the results section in the results file.
- definitionIgnore: an optional JSON array of single test identifiers that are ignored (i.e. not 
          tested for). The contents of this element are copied verbatim to the ignore section in the results file.
- definitionNotes: an optional JSON array of strings that are copied verbatim to the notes 
  section in the results file.
  
An example of a configuration definition file is shown below:
```
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
# Requirements
- Java 11

# Acknowledgements
This RDAP conformance tool has been developed by Viagénie
(Julien Bernard, Guillaume Blanchet, Marc Blanchet, Pierre Larochelle) under a contract from ICANN.
