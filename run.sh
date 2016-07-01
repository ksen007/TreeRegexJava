mvn clean package
mvn exec:java -Dexec.mainClass=ProcessTree -Dexec.args="Java8 compilationUnit src/main/examples/TObjectPrimitiveHashMapTest.java"
mvn exec:java -Dexec.mainClass=ProcessTree -Dexec.args="ECMAScript program src/main/examples/TRegex.js"
mvn exec:java -Dexec.mainClass=ProcessTree -Dexec.args="ECMAScript program src/main/examples/no-dupe-keys.js"
mvn exec:java -Dexec.mainClass=examples.JSLint -Dexec.args="src/main/examples/no-dupe-keys.js.st"
