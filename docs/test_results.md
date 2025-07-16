# Test Results

Be default, RDAPCT places the results in a file in the `results` directory in the current working
directory, with a file name of `results-YYYYMMDDHHmmsss.json` where YYYYMMDDHHmmss is the UTC time when
the file was written.

This file can be specified using the `--results-file` option (see [Specifying the Results File](overview.md#specifying-the-results-file)).

The results file will look something like this, except with no comments (which are not valid JSON):

``` json
{
    // Information on the version of the software.
    "testedDate": "2025-07-15T14:56:51.894633Z",
    "buildDate": "2025-07-15T14:55:27Z",
    "conformanceToolVersion": "2.0.3",
    
    // Information on the inputs and options.
    "testedURI": "https://example.org/domain/example.net",
    "gtldRegistry": true,
    "gtldRegistrar": false,
    "rdapProfileFebruary2019": false,
    "rdapProfileFebruary2024": true,
    "noIpv4": false,
    "noIpv6": false,
    "thinRegistry": false,
    "additionalConformanceQueries": false,
    "definitionIdentifier": "Standard gTLD RDAP Server Conformance",
    
    // Group results.
    "groupOK": [],
    "groupErrorWarning": [],
    
    // Specific results.
    "results": {
    
        // Tests that are ignored based on the configuration file.
        "ignore": [],

        // Notes about the tests from the configuration file.
        "notes": ["This conformance configuration is typical of gTLD RDAP server needs."],

        // Tests that resulted in warnings.
        "warning": [],
        
        // Tests that resulted in an error.
        "error": [{
        
            // The media type used in the accept header
            "acceptMediaType": null,
            
            // The result code of the test.
            "code": -13019,
            
            // Specific notes about the test.
            "notes": "",
            
            // The IP address of the server tested.
            "serverIpAddress": null,
            
            // Information about the test result.
            "message": "Unable to resolve an IP address endpoint using DNS.",
            
            // The query URI of the test.
            "queriedURI": null,
            
            // The HTTP method of the teset.
            "httpMethod": null,
            
            // Relevant value being tested.
            "value": "no response available",
            
            // Received HTTP status code.
            "receivedHttpStatusCode": null
        }]
    }
}                                                 
```

