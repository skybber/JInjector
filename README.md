# HotswapPatcher

<p align="left">
    <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html">
        <img src="https://img.shields.io/badge/License-GPL%20v2-blue.svg" alt="License: GPL v2">
    </a>
    <a href="https://twitter.com/intent/follow?screen_name=HSwapAgent">
        <img src="https://img.shields.io/twitter/follow/HSwapAgent.svg?style=social&logo=twitter" alt="follow on Twitter">
    </a>
</p>

## Overview

HotswapPatcher is a Java agent crafted to enable on-the-fly modifications of Java classes through patch files in plain text. 
This tool allows altering classes within the JDK or JAR files without the need for recompilation, streamlining the development process. 
Utilizing patch files in the Javassist format, HotswapPatcher allows injecting code directly into existing Java 
methods. 

## Features

- **Dynamic Class Modification**: Modify Java classes at runtime using simple, plain text patch files.
- **No Compilation Required**: Patch files do not require compilation, allowing for rapid development cycles.
- **Javassist Syntax Support**: Utilize the full power of Javassist's syntax for class and method modifications.
- Just now restricted on **Pre and Post Method Injection**: Easily insert custom code before or after method execution 
  with `insertBefore` and `insertAfter` directives.

## Usage

To use HotswapPatcher, specify it as a Java agent upon starting your JVM:

```
-javaagent:hotswap-patcher=patch=patch_file1.patch,patch=patch_file2.patch
```

### Patch File Format

Patch files should be in plain text, detailing the modifications to be applied. Each file specifies the target class name, 
method names for modification, and the location within those methods where the code should be injected. The locations 
can be `insertBefore` (to inject code at the beginning of a method) or `insertAfter` (to inject code at the end of a method).

#### Example Patch File

```
class java.util.ServiceLoader {
    newLookupIterator() {
        insertBefore {
            System.out.println("Hello from start of method ServiceLoader.newLookupIterator!");
        }
        insertAfter {
            System.out.println("Hello from end of method ServiceLoader.newLookupIterator!");
        }
    }
}
```

### Example project

Available at https://github.com/skybber/ServiceLoaderExample

## Installation

1. **Download** the HotswapPatcher agent from the latest releases.
2. **Include** the agent in your JVM startup command using the `-javaagent` option, as demonstrated in the Usage section.

## Building from Source

If you prefer to build HotswapPatcher from the source, follow these steps:

1. Clone the repository: `git clone https://github.com/skybber/HotswapPatcher`
2. Navigate into the project directory: `cd HotswapPatcher`
3. Build the project (requires Maven): `mvn package`

The build process will generate a `hotswap-patcher.jar` in the `target` directory.

## Contributing

Contributions to HotswapPatcher are welcome! Whether it's bug reports, feature suggestions, or direct code contributions, all forms of feedback and help are appreciated.

1. **Fork** the repository on GitHub.
2. **Create** your feature branch: `git checkout -b my-new-feature`
3. **Commit** your changes: `git commit -am 'Add some feature'`
4. **Push** to the branch: `git push origin my-new-feature`
5. **Submit** a pull request.
