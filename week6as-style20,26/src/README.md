# Usage
```shell
javac *.java
```
```shell
jar cfm framework.jar Manifest.txt Framework.class CountingInterface.class ExtractInterface.class
```
```shell
jar cf extractNormal.jar ExtractNormal.class
```
```shell
jar cf countNormal.jar CountNormal.class
```
```shell
jar cf extractZ.jar ExtractContainZ.class
```
```shell
jar cf firstLetterCount.jar CountWordsBasedOnFirstLetter.class 
```
```shell
java -cp framework.jar:extractNormal.jar:countNormal.jar:extractZ.jar:firstLetterCount.jar Framework
```
> You can change the output without repackaging the compilation by modifying `extractClassName`, `countingClassName` in `config.properties`

For example :
```properties
extractClassName=ExtractNormal
countingClassName=CountWordsBasedOnFirstLetter
extractMethod=extract
countingMethod=count
```

---
## Plugins switching
By change the `coutingClassName` from `countNormal` to `CountWordsBasedOnFirstLetter` you now get the ouput with different count way. 

By simply execute the command line below you will see the different. This command same as the last command executed in the usage instructions
```shell
java -cp framework.jar:extractNormal.jar:countNormal.jar:extractZ.jar:firstLetterCount.jar Framework
```

Similarly
```properties
extractClassName=ExtractContainZ
countingClassName=CountNormal
extractMethod=extract
countingMethod=count
```
You can change the extract class to simply change the extracting method. Also execute the command to see the different output

```shell
java -cp framework.jar:extractNormal.jar:countNormal.jar:extractZ.jar:firstLetterCount.jar Framework
```


