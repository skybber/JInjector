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

HotswapPatcher is a Java agent designed to enable modifications of Java classes on-the-fly through patch text files, 
using a syntax that extends from Java. This tool enables the modification of classes within the JDK or JAR files without 
requiring recompilation, thus streamlining the development process. Utilizing patch files in the Javassist format, 
HotswapPatcher allows injecting code directly into existing Java methods. 

## Features

- **Dynamic Class Modification**: Modify Java classes at runtime using simple, plain text patch files using syntax extended from Java.
- **No Compilation Required**: Patch files do not require compilation, allowing for rapid development cycles.
- **Javassist Syntax Support**: Utilize the full power of Javassist's syntax for class and method modifications.

## Usage

To use HotswapPatcher, specify it as a Java agent upon starting your JVM:

```
-javaagent:hotswap-patcher=patch=patch_file1.hswp,patch=patch_file2.hswp
```

### Patch File Format

Patch files are in text format and contain details of the changes to be applied to the running program. Each file lists, for each tranformed class, the names of the fields, constructors, and methods to be modified and the location in those methods where the code should be inserted. The location can be `insertBefore` (to insert code at the beginning of the method), `insertAfter` (to insert code at the end of the method), or `setBody` to replace the entire method. It is also possible to create new fields, constructors or methods.

#### Example Patch File

```
@Transform(onStart)
class java.util.ServiceLoader {

    $field(firstName).rename(_firstName);

    $field.new() {
        private String field1;
    }
   
    $method(newLookupIterator())
        .insertBefore {
            System.out.println("Start newLookupIterator().");
        }
        .insertAfter {
            System.out.println("End newLookupIterator().");
        }
    
    $method(newLookupIterator())
        .setBody {
            System.out.println("Hello from patcher.");
        }

    $method.new() {
            public void someMethod() {
                System.out.println("new method.");
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
