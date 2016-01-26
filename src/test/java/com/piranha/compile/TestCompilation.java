package com.piranha.compile;

import org.apache.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Padmaka on 1/26/16.
 */
public class TestCompilation {
    private static final Logger log = Logger.getLogger(TestCompilation.class);

    public static void main(String[] args) throws Exception {
        String code = "package com.piranha;" +
                "import com.piranha.compile.Compiler;" +
                "import com.piranha.dist.Scheduler;" +
                "public class Bootstrap {" +
                "private static final Logger log = Logger.getLogger(Bootstrap.class);\n\n    public static void main(String[] args) {\n        ArrayList<JsonObject> classes;\n        ArrayList<JsonObject> detailedClassList;\n\n        // Listening for other nodes to connect\n        //----------------------------------------------------------------------\n//        CommunicationPipe communicationPipe = new CommunicationPipe(9005);\n//\n//        communicationPipe.start();\n//        while (true) {\n//            if (communicationPipe.getNodes().size() > 0) {\n//                break;\n//            }\n//        }\n        //----------------------------------------------------------------------\n\n        Scanner scanner = new Scanner();\n        Collection fileCollection = scanner.readFiles();\n        ArrayList<File> files = new ArrayList<File>();\n\n        for(Object obj :fileCollection){\n            File f = (File)obj;\n            files.add(f);\n        }\n\n        try {\n            classes = scanner.scan(files);\n        } catch (IOException e) {\n            e.printStackTrace();\n        }\n\n        classes = scanner.findDependencies();\n        detailedClassList = scanner.getDetailedClassList();\n\n        Scheduler scheduler = new Scheduler();\n        ArrayList<ArrayList<JsonObject>> schedule = scheduler.makeSchedule(classes, detailedClassList);\n\n        //compilation test\n        Compiler compiler = new Compiler(\"/Users/Padmaka/Desktop\");\n\n        for (ArrayList<JsonObject> currentRound : schedule) {\n            for (JsonObject currentClass : currentRound) {\n                StringBuilder packageName = new StringBuilder(currentClass.get(\"package\").getAsString());\n                StringBuilder classString = new StringBuilder(\"package \" + packageName.replace(packageName.length()-1, packageName.length(), \"\") + \";\\n\");\n\n                for (JsonElement importStatement : currentClass.get(\"importStatements\").getAsJsonArray()) {\n                    classString.append(\"import \" + importStatement.getAsString() + \";\\n\");\n                }\n\n                classString.append(currentClass.get(\"classDeclaration\").getAsString());\n                classString.append(currentClass.get(\"classString\").getAsString() + \"}\");\n                try {\n                    compiler.compile(currentClass.get(\"className\").getAsString(), classString.toString());\n                } catch (Exception e) {\n                    log.error(\"\", e);\n                }\n//                log.debug(classString);\n            }\n        }}";


        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if( jc == null) throw new Exception( "Compiler unavailable");


        JavaSourceFromString jsfs = new JavaSourceFromString( "Bootstrap", code);

        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList( jsfs);

        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add("/Users/Padmaka/Desktop");
        options.add( "-classpath");
        URLClassLoader urlClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
        StringBuilder sb = new StringBuilder();
        for (URL url : urlClassLoader.getURLs()) {
            sb.append(url.getFile()).append(File.pathSeparator);
        }
        sb.append("/Users/Padmaka/Desktop");
        options.add(sb.toString());

        StringWriter output = new StringWriter();
        boolean success = jc.getTask( output, null, null, options, null, fileObjects).call();
        if( success) {
            log.info("Class has been successfully compiled");
        } else {
            throw new Exception( "Compilation failed :" + output);
        }

    }

//    class sds{
//        public static void main(String[] args) {
//            System.out.println("New class");
//        }
//    }
}
