# RDAP SchemaValidator System - Detailed Technical Implementation

## Introduction

The RDAP SchemaValidator system is the heart of the RDAP Conformance Tool's validation engine. If you're new to this project and wondering "how does this thing actually work?", this document will walk you through every detail of the implementation. By the time you finish reading this, you'll understand exactly how validation works, where to find specific functionality, and how to debug or modify the system.

## What Does the SchemaValidator Actually Do?

The SchemaValidator takes JSON content (like an RDAP response) and checks if it follows the rules. But it doesn't just check basic JSON syntax - it validates:

1. **Structure**: Is the JSON shaped correctly? (Does a domain object have the required fields?)
2. **Data Types**: Are strings actually strings, numbers actually numbers?
3. **Format**: Is a domain name properly formatted? Is an IP address valid?
4. **Business Rules**: Are RDAP-specific rules followed? (No duplicate event actions, proper vCard structure)
5. **Registry Compliance**: Are values valid according to IANA registries? (Valid status codes, media types, etc.)

## The Big Picture: How Validation Works

Here's the simplified flow:
```
JSON Content → Schema Loading → Validation Execution → Error Processing → Structured Results
```

But the reality is much more complex. Let's dive into each piece.

## Deep Dive: Schema Loading

### Where It All Starts: SchemaValidator.getSchema()

Located in `validator/src/main/java/org/icann/rdapconformance/validator/SchemaValidator.java` (lines 80-127), this method is where the magic begins.

**Step 1: Loading the JSON Schema File**

```java
JSONObject jsonSchema = new JSONObject(
    new JSONTokener(
        Objects.requireNonNull(
            classLoader.getResourceAsStream(scope + name))));
```

This loads a JSON schema file from `validator/src/main/resources/json-schema/`. For example, when validating "zz--main-1234", it loads `rdap_domain_name.json`. These files define the rules that JSON must follow.

**Step 2: Creating Custom Validators**

Before the schema is compiled, the system creates specialized validators:

```java
// IP address validators that know about IANA registries
Ipv4FormatValidator ipv4FormatValidator = new Ipv4FormatValidator(
    ds.get(Ipv4AddressSpace.class),      // IANA IPv4 registry data
    ds.get(SpecialIPv4Addresses.class)   // Special purpose addresses
);
```

**Step 3: The SchemaLoader Builder Chain**

This is where all the pieces come together:

```java
SchemaLoader schemaLoader = SchemaLoader.builder()
    .schemaClient(SchemaClient.classPathAwareClient())  // Can reference other schema files
    .schemaJson(jsonSchema)                             // Our main schema
    .resolutionScope("classpath://" + scope)            // Where to find referenced schemas
    .addFormatValidator(new IdnHostNameFormatValidator()) // Custom hostname validation
    .addFormatValidator(ipv4FormatValidator)              // Custom IPv4 validation
    // ... 15+ more custom validators
    .draftV7Support().build();
```

Each `.addFormatValidator()` call registers a custom validator that will be automatically triggered when the schema contains a matching `"format": "validator-name"` property.

**Step 4: Schema Compilation**

```java
return schemaLoader.load().build();
```

This compiles everything into a `Schema` object that can validate JSON.

### Understanding JSON Schema Files

JSON schema files are like templates that define what valid JSON should look like. Let's look at `rdap_domain_name.json`:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "$id": "rdap_domain_name.json",
  "title": "domainName",
  "type": "object",
  "properties": {
    "domain": {
      "type": "string",
      "format": "idn-hostname",
      "validationName": "DomainNameValidation"
    }
  }
}
```

This says: "Valid JSON must be an object with a 'domain' property that's a string in IDN hostname format."

The key here is `"format": "idn-hostname"`. This triggers the `IdnHostNameFormatValidator` during validation.

## Deep Dive: Error Code System

### How Error Codes Are Embedded in Schemas

Error codes are embedded directly in JSON schema files. Here's a complex example from `rdap_common.json`:

```json
{
  "uri": {
    "allOf": [
      {
        "pattern": "^(https?|http?)://",
        "errorCode": -10401,
        "errorMsg": "The scheme of the URI is not 'http' nor 'https'"
      },
      {
        "type": "string",
        "format": "uri",
        "errorCode": -10400,
        "errorMsg": "The URI is not syntactically valid according to RFC3986."
      }
    ],
    "validationName": "webUriValidation"
  },
  
  // Global error codes for this schema
  "structureInvalid": -12200,
  "unknownKeys": -12201,
  "duplicateKeys": -12202
}
```

### The SchemaNode System: Finding Error Codes

The `SchemaNode` class (`validator/src/main/java/org/icann/rdapconformance/validator/schema/SchemaNode.java`) creates a tree structure of the schema that can be searched for error codes.

**Key method: searchBottomMostErrorCode()**

```java
public int searchBottomMostErrorCode(String searchKey, String errorKey) {
    Optional<SchemaNode> optNode = findChild(searchKey);
    if (optNode.isEmpty()) {
        throw new IllegalArgumentException("No such error key in hierarchy");
    }
    SchemaNode node = optNode.get();
    SchemaNode parent = node;
    
    // Walk up the tree to find the error code
    while (parent != null && !parent.containsErrorKey(errorKey)) {
        parent = parent.parentNode;
    }
    return parent.getErrorCode(errorKey);
}
```

This walks up the schema tree until it finds the requested error code. For example, if a string validation fails, it looks for "structureInvalid" error codes starting from the failed property and moving up to parent schema nodes.

## Deep Dive: The Validation Process

### SchemaValidator.validate() Step by Step

Located at lines 133-173 in `SchemaValidator.java`, this is where actual validation happens.

**Step 1: Setup and Preparation**

```java
int startingCount = results.getResultCount();
results.addGroups(schemaRootNode.findAllValuesOf("validationName"));
```

This counts current errors and registers all validation groups defined in the schema.

**Step 2: JSON Parsing with Smart Caching**

```java
JSONObject jsonObject;
try {
    jsonObject = JsonCacheUtil.getCachedJsonObject(content);
} catch (Exception e) {
    RDAPValidationResult result = parseJsonException(new JSONException(e.getMessage()), content);
    results.add(result);
    return false;
}
```

**JsonCacheUtil Deep Dive** (`validator/src/main/java/org/icann/rdapconformance/validator/workflow/JsonCacheUtil.java`):

The cache uses a clever strategy:
- **Cache key**: `content.length() + "_" + content.hashCode()`
- **Thread-safe**: Uses `ConcurrentHashMap`
- **Size management**: When cache exceeds 100 items, randomly removes 20% of entries
- **Smart eviction**: Skips caching when duplicate JSON keys are detected

**Step 3: Core Schema Validation**

```java
try {
    schema.validate(jsonObject);  // This is where the Everit JSON Schema library does its magic
} catch (ValidationException e) {
    parseException(e, jsonObject);  // Process all validation failures
}
```

During `schema.validate()`, custom format validators are automatically triggered. When the schema encounters `"format": "idn-hostname"`, it calls `IdnHostNameFormatValidator.validate("zz--main-1234")`.

**Step 4: Custom RDAP-Specific Validations**

```java
// Check for duplicate event actions
verifyUnicityOfEventAction("events", -10912, jsonObject);
verifyUnicityOfEventAction("asEventActor", -11310, jsonObject);

// Conditional validations based on content
if (content.contains("\"vcardArray\"")) {
    new VcardArrayGeneralValidation(jsonObject.toString(), results).validate();
}
if (content.contains("\"notices\"")) {
    new NoticesTopMostValidation(jsonObject.toString(), results, schemaRootNode).validate();
}
```

**Step 5: Result Calculation**

```java
int endingCount = results.getResultCount();
boolean isValid = endingCount == startingCount;  // No new errors means validation passed
return isValid;
```

### Custom Validation Logic: verifyUnicityOfEventAction()

This is a perfect example of business logic validation that goes beyond JSON schema capabilities:

```java
private void verifyUnicityOfEventAction(String schemaId, int errorCode, JSONObject jsonObject) {
    // Find all arrays with the given name (e.g., "events")
    Set<String> eventsJsonPointers = jpathUtil.getPointerFromJPath(jsonObject, "$.." + schemaId);
    
    for (String jsonPointer : eventsJsonPointers) {
        try {
            JSONArray events = (JSONArray) jsonObject.query(jsonPointer);
            Set<String> eventActions = new HashSet<>();
            int i = 0;
            for (Object event : events) {
                String eventAction = ((JSONObject) event).getString("eventAction");
                if (!eventActions.add(eventAction)) {  // Set.add() returns false if already exists
                    // Duplicate found!
                    results.add(RDAPValidationResult.builder()
                        .code(errorCode)
                        .value(jsonPointer + "/" + i + "/eventAction:" + eventAction)
                        .message("An eventAction value exists more than once within the events array.")
                        .build());
                    
                    // Also add group validation error
                    ExceptionParser.validateGroupTest(jsonPointer + "/" + i + "/eventAction", 
                                                    jsonObject, results, schema);
                }
                i++;
            }
        } catch (Exception e) {
            logger.error("Exception during evaluation of eventAction String: {}", 
                        jsonObject.query(jsonPointer), e);
        }
    }
}
```

This checks that within each "events" array, no eventAction value appears twice. It's business logic that can't be expressed in JSON schema alone.

## Deep Dive: Exception Processing System

### How Validation Failures Become Error Codes

When `schema.validate()` fails, it throws a `ValidationException`. This exception contains a tree of specific failure information. The `parseException()` method (lines 237-253) processes these failures:

```java
private void parseException(ValidationException e, JSONObject jsonObject) {
    // Create all possible parsers for this exception
    List<ExceptionParser> exceptionParsers = ExceptionParser.createParsers(e, schema, jsonObject, results);
    
    // Each parser tries to handle the exception
    for (ExceptionParser exceptionParser : exceptionParsers) {
        exceptionParser.parse();
    }
    
    // Check if any exception went unprocessed
    List<ValidationExceptionNode> validationExceptions = new ValidationExceptionNode(null, e).getAllExceptions();
    for (ValidationExceptionNode validationException : validationExceptions) {
        if (exceptionParsers.stream().noneMatch(exceptionParser -> exceptionParser.matches(validationException))) {
            logger.error("We found this error with no exception parser {}", validationException.getMessage());
        }
    }
}
```

### ExceptionParser Factory: createParsers()

Located in `validator/src/main/java/org/icann/rdapconformance/validator/exception/parser/ExceptionParser.java` (lines 38-69):

```java
public static List<ExceptionParser> createParsers(ValidationException e, Schema schema, 
                                                 JSONObject object, RDAPValidatorResults results) {
    List<ExceptionParser> parsers = new ArrayList<>();
    ValidationExceptionNode rootException = new ValidationExceptionNode(null, e);
    List<ValidationExceptionNode> basicExceptions = rootException.getAllExceptions();
    
    // Create one of each parser type for each exception
    for (ValidationExceptionNode basicException : basicExceptions) {
        parsers.add(new UnknowKeyExceptionParser(basicException, schema, object, results));
        parsers.add(new BasicTypeExceptionParser(basicException, schema, object, results));
        parsers.add(new EnumExceptionParser(basicException, schema, object, results));
        parsers.add(new MissingKeyExceptionParser(basicException, schema, object, results));
        parsers.add(new ConstExceptionParser(basicException, schema, object, results));
        parsers.add(new DatetimeExceptionParser(basicException, schema, object, results));
        parsers.add(new IdnHostNameExceptionParser(basicException, schema, object, results));
        parsers.add(new HostNameInUriExceptionParser(basicException, schema, object, results));
        parsers.add(new Ipv4ValidationExceptionParser(basicException, schema, object, results));
        parsers.add(new Ipv6ValidationExceptionParser(basicException, schema, object, results));
        parsers.add(new VcardExceptionParser(basicException, schema, object, results));
        parsers.add(new DatasetExceptionParser(basicException, schema, object, results));
        parsers.add(new MinItemsExceptionParser(basicException, schema, object, results));
        parsers.add(new MaxItemsExceptionParser(basicException, schema, object, results));
        parsers.add(new MinLengthExceptionParser(basicException, schema, object, results));
        parsers.add(new MaxLengthExceptionParser(basicException, schema, object, results));
        parsers.add(new PatternExceptionParser(basicException, schema, object, results));
        parsers.add(new FormatExceptionParser(basicException, schema, object, results));
        parsers.add(new RequiredExceptionParser(basicException, schema, object, results));
        parsers.add(new IfThenElseExceptionParser(basicException, schema, object, results));
        parsers.add(new AllOfExceptionParser(basicException, schema, object, results));
        parsers.add(new AnyOfExceptionParser(basicException, schema, object, results));
        parsers.add(new OneOfExceptionParser(basicException, schema, object, results));
    }
    return parsers;
}
```

For every validation exception, the system creates 22+ different parser instances. Each parser checks if it can handle that specific type of exception.

### Individual Parser Example: BasicTypeExceptionParser

Located in `validator/src/main/java/org/icann/rdapconformance/validator/exception/parser/BasicTypeExceptionParser.java`:

```java
public class BasicTypeExceptionParser extends ExceptionParser {
    // Regex to match type error messages
    static Pattern basicTypePattern = Pattern.compile("expected type: (.+), found: (.+)");
    
    // Which schema types this parser handles
    static Set<Class<? extends Schema>> basicTypes = Set.of(
        StringSchema.class, BooleanSchema.class, NullSchema.class, 
        NumberSchema.class, IntegerSchema.class
    );

    @Override
    public boolean matches(ValidationExceptionNode e) {
        return basicTypePattern.matcher(e.getMessage()).find() && 
               basicTypes.contains(e.getViolatedSchema().getClass());
    }

    @Override
    public void doParse() {
        String basicType = extractBasicType(e.getViolatedSchema());
        results.add(RDAPValidationResult.builder()
            .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
            .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
            .message(e.getMessage("The JSON value is not a " + basicType.toLowerCase() + "."))
            .build());
    }
}
```

The parser:
1. **Checks if it matches**: Does the error message match the pattern and is it a basic type schema?
2. **Extracts error code**: Gets the error code from the violated schema
3. **Creates result**: Builds a structured error result with code, value, and message

### ValidationExceptionNode: Navigating Exception Hierarchy

The `ValidationExceptionNode` class (`validator/src/main/java/org/icann/rdapconformance/validator/exception/ValidationExceptionNode.java`) wraps the complex exception hierarchy from the Everit library and provides easy navigation:

```java
public int getErrorCodeFromViolatedSchema() {
    return (int) getPropertyFromViolatedSchema("errorCode");
}

public Object getPropertyFromViolatedSchema(String key) {
    ValidationExceptionNode parent = this;
    // Walk up the exception tree looking for the property
    while (!parent.getViolatedSchema().getUnprocessedProperties().containsKey(key)) {
        parent = parent.getParentException();
        // Only follow certain types of schemas up the tree
        if (!containerSchemas.contains(parent.getViolatedSchema().getClass())) {
            break;
        }
    }
    return parent.getViolatedSchema().getUnprocessedProperties().get(key);
}
```

This walks up the exception tree to find error codes. If a specific property validation fails but doesn't have an error code, it looks at parent schemas until it finds one.

## Deep Dive: Custom Format Validators

### How Format Validators Work

Custom format validators are triggered automatically when JSON schema validation encounters a `"format": "format-name"` property. Here's how the `IdnHostNameFormatValidator` works:

```java
public class IdnHostNameFormatValidator implements FormatValidator {
    @Override
    public Optional<String> validate(String subject) {
        try {
            // Use java.net.IDN to validate internationalized domain names
            String ascii = java.net.IDN.toASCII(subject);
            // Additional validation logic...
            return Optional.empty();  // Valid
        } catch (Exception e) {
            return Optional.of("Invalid IDN hostname: " + e.getMessage());  // Invalid
        }
    }

    @Override
    public String formatName() {
        return "idn-hostname";  // This matches the schema "format" property
    }
}
```

When the schema contains `"format": "idn-hostname"`, the validation engine automatically calls this validator's `validate()` method.

### DatasetValidator: IANA Registry Validation

The `DatasetValidator` class validates values against IANA registries:

```java
public class DatasetValidator implements FormatValidator {
    protected final DatasetValidatorModel datasetValidatorModel;
    private final String formatName;

    @Override
    public Optional<String> validate(String s) {
        if (datasetValidatorModel.isInvalid(s)) {
            return Optional.of("Invalid value for dataset " + datasetValidatorModel.getClass().getSimpleName());
        }
        return Optional.empty();
    }

    @Override
    public String formatName() {
        return formatName;  // e.g., "role", "status", "eventAction"
    }
}
```

The magic is in the `DatasetValidatorModel` implementations:

- `RoleJsonValues`: Contains all valid IANA entity role values
- `StatusJsonValues`: Contains all valid IANA status values  
- `EventActionJsonValues`: Contains all valid IANA event actions
- etc.

These models are loaded from IANA datasets at startup and used for real-time validation.

## Deep Dive: Validation Groups

### What Are Validation Groups?

Validation groups allow schemas to define compound validations. For example, in `rdap_common.json`:

```json
{
  "ldhName": {
    "type": "string",
    "format": "hostname",
    "validationName": "stdRdapLdhNameValidation"
  },
  "stdRdapLdhNameValidation": -12205
}
```

The `validationName` creates a group called "stdRdapLdhNameValidation" with error code -12205.

### Group Validation Execution

When a validation fails, `ExceptionParser.validateGroupTest()` is called:

```java
public static void validateGroupTest(String jsonPointer, JSONObject jsonObject,
                                   RDAPValidatorResults results, Schema schema) {
    SchemaNode tree = SchemaNode.create(null, schema);
    Set<ValidationNode> validationNodes = tree.findValidationNodes(jsonPointer, "validationName");
    
    for (ValidationNode validationNode : validationNodes) {
        results.addGroupErrorWarning(validationNode.getValidationKey());
        if (validationNode.hasParentValidationCode()) {
            results.add(RDAPValidationResult.builder()
                .code(parseErrorCode(validationNode::getParentValidationCode))
                .value(jsonPointer + ":" + jsonObject.query(jsonPointer))
                .message(MessageFormat.format("The value for the JSON name value does not pass {0} validation [{1}].", 
                        jsonPointer, validationNode.getValidationKey()))
                .build());
        }
    }
}
```

This finds all validation groups that the failed property belongs to and adds group-level errors.

## Real Example: Domain Name Validation with "zz--main-1234"

Let's trace through exactly what happens when we validate `{"domain": "zz--main-1234"}` against `rdap_domain_name.json`:

1. **Schema Loading**: `rdap_domain_name.json` is loaded with `IdnHostNameFormatValidator` attached and proper error codes defined (-10300 through -10303)
2. **JSON Parsing**: `{"domain": "zz--main-1234"}` is parsed successfully
3. **Schema Validation**: The schema sees `"format": "idn-hostname"` and calls `IdnHostNameFormatValidator.validate("zz--main-1234")`
4. **Format Validation Fails**: The IDN hostname validator (using IBM ICU IDNA library) detects two specific violations:
   - `LESS_THAN_TWO_LABELS`: Domain contains no dots, treated as single label instead of proper domain format
   - `HYPHEN_3_4`: Hyphens appear in positions 3-4 ("zz--") without the required "xn--" A-label prefix, violating IDNA UTS#46 rules
5. **ValidationException Thrown**: The Everit JSON Schema library throws a format validation exception
6. **Exception Processing**: `IdnHostNameExceptionParser` matches the exception and extracts proper error codes from the schema definition
7. **Error Results Generated**: Two structured error results are created with appropriate error codes:
   - **Error Code -10302**: "A domain name with less than two labels was found"  
   - **Error Code -10303**: "A DNS label not being a valid 'A-label', 'U-label', or 'NR-LDH label' was found"
8. **Return False**: `validate()` returns `false` because validation failed
9. **Status Set**: `RDAPHttpQueryTypeProcessor` properly sets `ToolResult.BAD_USER_INPUT` status for error handling
10. **Proper Error Handling**: Calling code can safely access `getErrorStatus().getCode()` to get error code 25 (BAD_USER_INPUT)

## Key Files and Where to Find Things

### Core Validation Engine
- **`SchemaValidator.java`**: Main validation orchestrator
- **`SchemaValidatorCache.java`**: Performance optimization through caching
- **`SchemaNode.java`**: Schema tree navigation and error code resolution

### JSON Schema Files
- **Location**: `validator/src/main/resources/json-schema/`
- **Core objects**: `rdap_domain.json`, `rdap_entity.json`, `rdap_nameserver.json`, etc.
- **Common definitions**: `rdap_common.json` (shared components and error codes)
- **Special validation**: `jcard.json` (vCard validation), `rdap_error.json` (error responses)

### Exception Processing
- **Base class**: `ExceptionParser.java`
- **Individual parsers**: `validator/src/main/java/org/icann/rdapconformance/validator/exception/parser/`
- **Exception navigation**: `ValidationExceptionNode.java`

### Custom Format Validators
- **Location**: `validator/src/main/java/org/icann/rdapconformance/validator/customvalidator/`
- **Key validators**: `IdnHostNameFormatValidator.java`, `Ipv4FormatValidator.java`, `DatasetValidator.java`

### Dataset Models (IANA Registry Data)
- **Location**: `validator/src/main/java/org/icann/rdapconformance/validator/workflow/rdap/dataset/model/`
- **Examples**: `RoleJsonValues.java`, `StatusJsonValues.java`, `EventActionJsonValues.java`

### Custom Validation Logic
- **Event uniqueness**: `SchemaValidator.verifyUnicityOfEventAction()`
- **vCard validation**: `VcardArrayGeneralValidation.java`
- **Notice validation**: `NoticesTopMostValidation.java`

## How to Debug Validation Issues

### Step 1: Identify the Validation Type
- **Structure/Type errors**: Look at `BasicTypeExceptionParser`, `MissingKeyExceptionParser`
- **Format errors**: Check custom format validators (`IdnHostNameFormatValidator`, etc.)
- **Business rule errors**: Look at custom validation methods in `SchemaValidator`
- **Dataset errors**: Check `DatasetValidator` and IANA dataset models

### Step 2: Find the Relevant Schema
- Look in `validator/src/main/resources/json-schema/`
- For domain validation: `rdap_domain.json` and `rdap_domain_name.json`
- For common properties: `rdap_common.json`
- For error codes: Search for the error number in schema files

### Step 3: Trace the Error Code
- Error codes are embedded in schema files as `"errorCode": -12345`
- Use `SchemaNode.searchBottomMostErrorCode()` to understand code resolution
- Check `ValidationExceptionNode.getErrorCodeFromViolatedSchema()` for extraction logic

### Step 4: Understand the Exception Processing
- All exceptions go through `ExceptionParser.createParsers()`
- Each parser type has specific matching logic in its `matches()` method
- Error code resolution happens in `parseErrorCode()` with fallback to -999

## How to Modify or Extend the System

### Adding a New Custom Format Validator
1. Create a new class implementing `FormatValidator` in the `customvalidator` package
2. Add it to the `SchemaLoader` chain in `SchemaValidator.getSchema()`
3. Use the format name in your JSON schemas
4. Create corresponding exception parser if needed

### Adding New Error Codes
1. Add error codes to the appropriate JSON schema file
2. Ensure the error code follows the numbering convention
3. Update any relevant exception parsers
4. Add tests to verify error code assignment

### Modifying Validation Logic
1. **Schema changes**: Modify JSON schema files directly
2. **Custom business logic**: Add methods to `SchemaValidator` class
3. **Dataset validation**: Modify dataset model classes
4. **Exception processing**: Modify or add exception parsers

## Common Gotchas and Pitfalls

### Domain Name Validation Error Codes
The `rdap_domain_name.json` schema defines specific error codes (-10300 through -10303) for different validation failures. These align with the ICANN RDAP Conformance Tool documentation and provide precise error reporting for domain name format issues.

### Caching Issues
The JSON parsing cache can sometimes mask issues during development. Clear the cache or check `JsonCacheUtil` if you see inconsistent behavior.

### Error Code Resolution
Error codes are resolved hierarchically. If you can't find why a specific error code is being used, trace through `ValidationExceptionNode.getPropertyFromViolatedSchema()`.

### Format Validator Registration
Format validators must be registered in the `SchemaLoader` chain and the format name must exactly match the schema property.

### Schema Reference Resolution
Schemas can reference other schemas. Make sure the `resolutionScope` is correct and referenced schemas are available in the classpath.

This system is complex but once you understand the flow from schema loading → validation execution → exception processing → error reporting, you can effectively debug and modify any part of the validation system.
