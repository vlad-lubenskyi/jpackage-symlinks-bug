# Demo for a symlink-related bug in jpackage

## Requirements

* Java 21+.
* macOS.

## Description

In [JDK-8274346](https://bugs.openjdk.org/browse/JDK-8274346), `jpackage` got a new parameter `--app-conteont`,
which added the ability to configure arbitrary additional content for the app.

The implemented solution recursively copies the content and follows the symlinks,
**which it shouldn't do**.

In my case, the additional content is another app bundle. Nesting app bundles a normal practice in macOS world.

App bundles actively use symlinks to manage framework versions, like this:

```text
- My App.app
  - Contents
    - Frameworks
      - My Framework.framework
        - Versions
          - 134.0.6998.89 # This is actual version
            - My Framework # This is actual executable
          - Current # Symlink to 134.0.6998.89
          - My Framework # Symlink to the executable
```

When I add such a bundle to `--app-content`, `jpackage` materializes all symlinks which
leads to an incorrect directory structure and respective `codesign` failure.

## How to reproduce

1. Download Chromium bundle into `build/additional-content`:

   ```bash
   ./gradlew unpackChromiumBundle
   ```

   This will download the Chromium bundle into `build/additional-content/Helpers`.

2. Build the JAR file:

   ```bash
   ./gradlew jar
   ```
3. Execute `jpackage`:

   ```bash
   jpackage --app-version 1.0.0 \
        --app-content ./build/additional-content/Helpers \
        --dest ./build/jpackage-result \
        --input ./build/libs \
        --main-class App \
        --main-jar main.jar \
        --name "Nested bundles example" \
        --temp ./build/jpackage-temp \
        --type app-image \
        --mac-package-identifier com.example.nested.bundles \
        --mac-package-name "Nested bundles example" \
        --verbose
   ```
4. Once the command fails, take a look at the error:

   ```text
   Caused by: java.io.IOException: Command
       [/usr/bin/codesign, -s, -, -vvvv, /.../jpackage-symlinks-bug/build/jpackage-result/Nested bundles example.app/Contents/Helpers/Chromium.app/Contents/MacOS/Chromium] exited with 1 code
   ```

5. Execute the command from the error message:

   ```bash
   /usr/bin/codesign -s - -vvvv \
        /.../jpackage-symlinks-bug/build/jpackage-result/Nested\ bundles\ example.app/Contents/Helpers/Chromium.app/Contents/MacOS/Chromium
   ```
6. See the cause of `jpackage` failure:

   ```text
   /.../.../Helpers/Chromium.app/Contents/MacOS/Chromium: bundle format unrecognized, invalid, or unsuitable
   ```

7. Analyze the contents of result directory:

   ```bash
   # No symlinks will be found. Only materialized files.
   ls -Al build/jpackage-result/Nested bundles example.app/Contents/Helpers/Chromium.app/Contents/Frameworks/Chromium Framework.framework
   ```
