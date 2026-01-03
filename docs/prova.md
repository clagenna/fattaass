You're encountering an issue because **commons-math3** is required by `org.apache.poi`, but Maven warns against using filename-based automatic modules.

### How to Solve It:

1. **Declare `commons-math3` as an Automatic Module**  
   Instead of excluding `commons-math3`, add it back but **do not rely on modular dependencies**. Maven warns against filename-based automatic modules for public repositories, but you **can** use them locally.

   Modify your `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.apache.commons</groupId>
       <artifactId>commons-math3</artifactId>
       <version>3.6.1</version>
   </dependency>
   ```

2. **Ensure `commons-math3` is in the Module Path**  
   When running your tests, make sure `commons-math3` is included in the module path.

   Example:
   ```sh
   java --module-path out:commons-math3-3.6.1.jar -m my.module.tests/com.example.MyTest
   ```

3. **Consider a `module-info.java` Workaround**  
   Since `commons-math3` does not have an official module name, you may need to **require it dynamically** using reflection instead of `requires` in `module-info.java`.

4. **Use a Custom Named Automatic Module (Advanced Option)**  
   You can manually rename the `commons-math3-3.6.1.jar` file to `commons.math3.jar` and place it in your module path. Then, modify `module-info.java`:
   ```java
   module my.module {
       requires commons.math3;
       requires org.apache.poi.poi;
   }
   ```

Would you like help setting up a test run with a valid module path?
