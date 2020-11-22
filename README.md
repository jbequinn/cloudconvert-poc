# CloudConvert Java client PoC

This repository is a "homework assignment" for a Java client for the [CloudConvert](https://cloudconvert.com/) web application. By using CloudConvert's API, this application takes a file as input, sends it for conversion, and downloads the converted file.

It requires the system property `CLOUDCONVERT_ACCESS_TOKEN` to be set with a valid API key.

## How to run

```
mvn compile exec:java -DCLOUDCONVERT_ACCESS_TOKEN=<your-key> -DCLOUDCONVERT_INPUT_FILE=<input-file-path> -DCLOUDCONVERT_OUTPUT_FILE=<output-file-path>
```

The `CLOUDCONVERT_INPUT_FILE` and `CLOUDCONVERT_OUTPUT_FILE` system properties are optional. If not specified, the values `/tmp/test-file.docx` and `/tmp/test-file.pdf`, respectively, are used.
