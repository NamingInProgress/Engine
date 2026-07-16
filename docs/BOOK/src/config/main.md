# Config API

**vke** comes equipped with a powerful configuration API to control and customize various aspects of the engine. 

Config files can be written in standard **JSON** or vke's custom **[VCL](./vcl.md)** file format. Both formats are parsed into the exact same internal data structure, meaning you can generally use whichever you prefer unless a specific engine service strictly requires one over the other.

> **Note:** The **vke** Config API is strictly **read-only**!

---

## The Node Hierarchy

All config files are parsed into a `ConfigDocument` instance which contains a root `ConfigNode`. Depending on the data it represents, a `ConfigNode` can be one or many of several specialized sub-types:

| Node Type              | Description                                                               |
|:-----------------------|:--------------------------------------------------------------------------|
| `ConfigObjectNode`     | A node containing a named mapping to other child nodes (key-value pairs). |
| `ConfigArrayNode`      | A sequential collection of other nodes.                                   |
| `ConfigValueNode`      | A simple string node.                                                     |
| `ConfigNumberNode`     | A number (floating-point) node.                                           |
| `ConfigBooleanNode`    | A boolean node.                                                           |
| `NamedConfigNode`      | A node that has an explicit name attached to it.                          |
| `AttributedConfigNode` | A node containing a named mapping to string attributes.                   |

The base `ConfigNode` class provides many QoL utility methods to easily fetch children or convert values to different primitive types.

### Auto-Detecting Parsers
You do not need to manually specify how to parse a file. `ConfigDocument` automatically selects the appropriate parser based on the file extension:

```java
Context ctx = ...;

// The engine automatically detects the format and parses it
ConfigDocument doc = ConfigDocument.parseIdentifier(ctx.id("test.json"));
ConfigObjectNode root = doc.getRoot().getObject("my-root-tag");

```

---

## Config Validation & Schemas

To ensure your configurations are correct and secure, `ConfigDocument` instances can be validated against a `ConfigSchema`.

`ConfigSchema`s are themselves structurally represented as `ConfigDocument`s (and are validated against a master schema internally). While you can write a schema in JSON or VCL, it is highly encouraged to use **VKS**—a file format specifically designed for writing schemas in vke.

*(For a deep dive into the syntax, check out the [VKS File Format](./vks.md) page).*

### Example Schema (`person.vks`)

This schema defines a structure with a required name, age, and a list of jobs with specific number boundaries:

```vks
{
    schema: {
        !name: string;
        !age: number;
        jobs: [{
            !name: string;
            from: number[0, 24];
            to: number[0, 24];
        }];
    };
}

```

### Valid Configuration (`person.json`)

This JSON matches the schema constraints perfectly:

```json
{
  "name": "John Doe",
  "age": 35,
  "jobs": [
    {
      "name": "Programmer",
      "from": 9,
      "to": 5
    }
  ]
}

```

### Java Implementation

Here is how you load both files and execute a validation pass:

```java
Context ctx = ...;

// Load and read the VKS schema file
ConfigDocument schemaDoc = ConfigDocument.parseIdentifier(ctx.id("person.vks"));
// Note: Providing the filename string here helps generate clear, readable error paths
ConfigSchema schema = ConfigSchema.readVke(schemaDoc, "person.vks"); 

// Load and validate the actual config file
ConfigDocument doc = ConfigDocument.parseIdentifier(ctx.id("person.json"));
doc.validate(schema, "person.json"); 

```

---

## Error Handling & Validation Failures

The `validate()` function is highly detailed. If we modify the JSON to introduce invalid keys or out-of-bounds numbers:

```json
{
  "Name": "John Doe", // Error: Uppercase 'N' violates schema
  "age": 35,
  "jobs": [
    {
      "name": "Programmer",
      "from": 9,
      "to": 25        // Error: 25 is outside of the [0, 24] range!
    }
  ]
}

```

Calling `doc.validate(schema, "person.json")` will fail and throw a highly descriptive exception showing you exactly what went wrong and where:

```text
Exception in thread "main" com.vke.api.parsing.config.schema.SchemaMismatchException: There were validation errors when validating input with schema:
Illegal field found "Name"!
    at: 
    at: person.json
Missing required field "name"!
    at: 
    at: person.json
25.000000 is bigger than maximum value 24.000000!
    at: jobs.[0].to
    at: person.json

```