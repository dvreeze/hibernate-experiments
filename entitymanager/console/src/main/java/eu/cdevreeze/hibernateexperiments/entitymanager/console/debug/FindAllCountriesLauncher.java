/*
 * Copyright 2026-2026 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.hibernateexperiments.entitymanager.console.debug;

import module java.base;
import module jdk.jdi;
import com.google.common.base.Preconditions;
import eu.cdevreeze.hibernateexperiments.entitymanager.console.FindAllCountries;

/**
 * Program launching {@link FindAllCountries} via the Java Debug Interface (JDI).
 * <p>
 * For inspiration, one could look at code in <a href="https://www.baeldung.com/java-debug-interface">Java Debug Interface</a>,
 * <a href="https://itsallbinary.com/java-debug-interface-api-jdi-hello-world-example-programmatic-debugging-for-beginners/">programmatic debugging for beginners</a>
 * and <a href="https://itsallbinary.com/java-debug-interface-api-jdi-hello-world-example-programmatic-stepping-through-the-code-lines/">programmatic stepping through code lines</a>.
 *
 * @author Chris de Vreeze
 */
public class FindAllCountriesLauncher {

    static void main(String... args) {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();

        LaunchingConnector connector = vmm.defaultConnector();
        System.out.println(connector);
        Map<String, Connector.Argument> connectorArguments = connector.defaultArguments();
        System.out.println();
        connectorArguments.forEach((k, v) -> System.out.printf("Key: %s; Value: %s%n", k, v));

        connectorArguments.get("main").setValue(FindAllCountries.class.getName());
        // Same class path for debuggee as for this debugging program (ignoring the module path and putting everything on the class path)
        connectorArguments.get("options").setValue(String.format("-cp \"%s\"", classPath()));
        System.out.println();
        connectorArguments.forEach((k, v) -> System.out.printf("Key: %s; Value: %s%n", k, v));

        System.out.println();
        Process process = null;
        try {
            VirtualMachine vm = connector.launch(connectorArguments);
            enableClassPrepareRequest(vm);
            process = vm.process();

            // See https://www.baeldung.com/java-debug-interface
            EventSet eventSet;
            long timeoutInMs = 100;
            while ((eventSet = vm.eventQueue().remove(timeoutInMs)) != null) {
                for (Event event : eventSet) {
                    vm.resume();
                }
            }
        } catch (VMDisconnectedException e) {
            System.out.println("VM is disconnected");
        } catch (Exception e) {
            System.err.println("Error launching process: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                try {
                    System.out.println();
                    process.getInputStream().transferTo(System.out);
                    process.getErrorStream().transferTo(System.out);
                } catch (IOException e) {
                    System.err.println("Error reading debuggee output/error streams: " + e.getMessage());
                }
            }
        }
    }

    private static void enableClassPrepareRequest(VirtualMachine vm) {
        // See https://www.baeldung.com/java-debug-interface
        ClassPrepareRequest request = vm.eventRequestManager().createClassPrepareRequest();
        request.addClassFilter(FindAllCountries.class.getName());
        request.enable();
    }

    private static String classPath() {
        return String.format("%s:%s", libraryJarFileClassPath(), applicationTargetDirClassPath());
    }

    private static String libraryJarFileClassPath() {
        String m2Repo = String.format("%s/.m2/repository", Objects.requireNonNull(System.getProperty("user.home")));
        return Stream.of(
                "/ch/qos/logback/logback-classic/1.5.32/logback-classic-1.5.32.jar",
                "/ch/qos/logback/logback-core/1.5.32/logback-core-1.5.32.jar",
                "/com/fasterxml/jackson/core/jackson-annotations/2.21/jackson-annotations-2.21.jar",
                "/com/google/errorprone/error_prone_annotations/2.36.0/error_prone_annotations-2.36.0.jar",
                "/com/google/guava/failureaccess/1.0.3/failureaccess-1.0.3.jar",
                "/com/google/guava/guava/33.4.8-jre/guava-33.4.8-jre.jar",
                "/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar",
                "/com/google/j2objc/j2objc-annotations/3.0.0/j2objc-annotations-3.0.0.jar",
                "/com/sun/istack/istack-commons-runtime/4.1.2/istack-commons-runtime-4.1.2.jar",
                "/jakarta/activation/jakarta.activation-api/2.1.4/jakarta.activation-api-2.1.4.jar",
                "/jakarta/inject/jakarta.inject-api/2.0.1/jakarta.inject-api-2.0.1.jar",
                "/jakarta/persistence/jakarta.persistence-api/4.0.0-M1/jakarta.persistence-api-4.0.0-M1.jar",
                "/jakarta/transaction/jakarta.transaction-api/2.0.1/jakarta.transaction-api-2.0.1.jar",
                "/jakarta/xml/bind/jakarta.xml.bind-api/4.0.4/jakarta.xml.bind-api-4.0.4.jar",
                "/net/bytebuddy/byte-buddy/1.17.8/byte-buddy-1.17.8.jar",
                "/org/antlr/antlr4-runtime/4.13.2/antlr4-runtime-4.13.2.jar",
                "/org/eclipse/angus/angus-activation/2.0.3/angus-activation-2.0.3.jar",
                "/org/glassfish/jaxb/jaxb-core/4.0.6/jaxb-core-4.0.6.jar",
                "/org/glassfish/jaxb/jaxb-runtime/4.0.6/jaxb-runtime-4.0.6.jar",
                "/org/glassfish/jaxb/txw2/4.0.6/txw2-4.0.6.jar",
                "/org/hibernate/models/hibernate-models/1.0.1/hibernate-models-1.0.1.jar",
                "/org/hibernate/orm/hibernate-core/8.0.0.Alpha1/hibernate-core-8.0.0.Alpha1.jar",
                "/org/jboss/logging/jboss-logging/3.6.1.Final/jboss-logging-3.6.1.Final.jar",
                "/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar",
                "/org/postgresql/postgresql/42.7.11/postgresql-42.7.11.jar",
                "/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar",
                "/tools/jackson/core/jackson-core/3.1.1/jackson-core-3.1.1.jar",
                "/tools/jackson/core/jackson-databind/3.1.1/jackson-databind-3.1.1.jar",
                "/tools/jackson/datatype/jackson-datatype-guava/3.1.1/jackson-datatype-guava-3.1.1.jar"
        ).map(p -> m2Repo + p).collect(Collectors.joining(":"));
    }

    private static String applicationTargetDirClassPath() {
        String currentDir = System.getProperty("user.dir");
        Preconditions.checkArgument(Path.of(currentDir).endsWith(Path.of("hibernate-experiments")));
        return Stream.of(
                "/entitymanager/model/target/classes",
                "/entitymanager/service/target/classes",
                "/entitymanager/console/target/classes"
        ).map(p -> currentDir + p).collect(Collectors.joining(":"));
    }
}
