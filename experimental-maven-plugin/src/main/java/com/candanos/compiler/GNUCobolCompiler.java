package com.candanos.compiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import com.google.common.io.Files;
import org.codehaus.plexus.compiler.*;
import org.codehaus.plexus.util.*;
import org.codehaus.plexus.util.cli.*;

import java.io.*;
import java.util.*;

public class GNUCobolCompiler
        extends AbstractCompiler {
    private static final String ARGUMENTS_FILE_NAME = "cobc-arguments";
    private Log log;

    public void setLog(Log log) {
        this.log = log;
    }


    private static final String[] DEFAULT_INCLUDES = {"**/**"};

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GNUCobolCompiler() {
//            super( CompilerOutputStyle.ONE_OUTPUT_FILE_FOR_ALL_INPUT_FILES,
//            ".cs", null, null );
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, ".cbl",
                ".o", null);
    }

    // ----------------------------------------------------------------------
    // Compiler Implementation
    // ----------------------------------------------------------------------

    public Log getLog() {
        if (this.log == null) {
            this.log = new SystemStreamLog();
        }

        return this.log;
    }

    public static List<CompilerMessage> parseCompilerOutput(
            BufferedReader bufferedReader)
            throws IOException {
        List<CompilerMessage> messages = new ArrayList<CompilerMessage>();

        String line = bufferedReader.readLine();

//            while ( line != null )
//            {
//                CompilerMessage compilerError = DefaultCSharpCompilerParser
//                .parseLine( line );
//
//                if ( compilerError != null )
//                {
//                    messages.add( compilerError );
//                }
//
//                line = bufferedReader.readLine();
//            }

        return messages;
    }

    // added for debug purposes....
    protected static String[] getSourceFiles(CompilerConfiguration config) {
        Set<String> sources = new HashSet<String>();
        //Set sourceFiles = null;
        //was:
        Set<File> sourceFiles = config.getSourceFiles();

        if (sourceFiles != null && !sourceFiles.isEmpty()) {
            for (File sourceFile : sourceFiles) {
                sources.add(sourceFile.getAbsolutePath());
            }
        }
        else {
            for (String sourceLocation : config.getSourceLocations()) {
                sources.addAll(
                        getSourceFilesForSourceRoot(config, sourceLocation));
            }
        }

        String[] result;

        if (sources.isEmpty()) {
            result = new String[0];
        }
        else {
            result = (String[]) sources.toArray(new String[sources.size()]);
        }

        return result;
    }

    protected File getCompilationSyslib(CompilerConfiguration config) {
        File x = new File(config.getOutputLocation());
        return new File(x.toPath()
                .getParent()
                .toString()
                .concat("/cobc-syslib"));
    }

    /**
     * This method is just here to maintain the public api. This is now
     * handled in the parse
     * compiler output function.
     *
     * @author Chris Stevenson
     * @deprecated
     */
    public static CompilerMessage parseLine(String line) {
//            return DefaultCSharpCompilerParser.parseLine( line );
        return new CompilerMessage("dummy");
    }

    protected static Set<String> getSourceFilesForSourceRoot(
            CompilerConfiguration config, String sourceLocation) {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(sourceLocation);

        Set<String> includes = config.getIncludes();

        if (includes != null && !includes.isEmpty()) {
            String[] inclStrs = includes.toArray(new String[includes.size()]);
            scanner.setIncludes(inclStrs);
        }
        else {
            scanner.setIncludes(new String[]{"**/*.cs"});
        }

        Set<String> excludes = config.getExcludes();

        if (excludes != null && !excludes.isEmpty()) {
            String[] exclStrs = excludes.toArray(new String[excludes.size()]);
            scanner.setIncludes(exclStrs);
        }

        scanner.scan();

        String[] sourceDirectorySources = scanner.getIncludedFiles();

        Set<String> sources = new HashSet<String>();

        for (String source : sourceDirectorySources) {
            File f = new File(sourceLocation, source);
            sources.add(f.getPath());
        }

        return sources;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------
    public CompilerResult performCompile(CompilerConfiguration config)
            throws CompilerException {

/*      this.success = success;
        this.compilerMessages = compilerMessages;
            private List<CompilerMessage> compilerMessages;
                this.file = file;
                this.kind = kind;  enum Kind
                this.startline = startline;
                this.startcolumn = startcolumn;
                this.endline = endline;
                this.endcolumn = endcolumn;
                this.message = cleanupMessage( message );
*/


        List<String> msgs = new ArrayList<String>();
        String[] sourceFiles = getSourceFiles(config);

        if ((sourceFiles == null) || (sourceFiles.length == 0)) {
            return new CompilerResult();
        }

        File destinationDir = new File(config.getOutputLocation());
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        getLog().info("Compiling " + sourceFiles.length + " " +
                "source file" + (sourceFiles.length == 1 ? "" :
                "s") + " to " + destinationDir.getAbsolutePath());

        String executable = findExecutable(config);
        getLog().info("executable:" + executable);
        File workingDirectory = config.getWorkingDirectory();
        getLog().info("working directory:" + workingDirectory.toString());
        Commandline cli = new Commandline();

        cli.setWorkingDirectory(workingDirectory.getAbsolutePath());
        cli.setExecutable(executable);

        CommandLineUtils.StringStreamConsumer out =
                new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer err =
                new CommandLineUtils.StringStreamConsumer();

        cli.addArguments(buildCompilerArguments(config, sourceFiles));
        for (String s : buildCompilerArguments(config, sourceFiles)) {
            getLog().info("Compiler argument: " + s);
        }

        int returnCode = 0;

        getLog().info("cli was executed and returnCode is " + returnCode);
        List<CompilerMessage> messages = null;

        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, err);
            messages = parseModernStream(returnCode,
                    new BufferedReader(new StringReader(out.getOutput())),
                    CompilerMessage.Kind.NOTE);
            messages.addAll(parseModernStream(returnCode,
                    new BufferedReader(new StringReader(err.getOutput())),
                    CompilerMessage.Kind.ERROR));
        } catch (CommandLineException e) {
            throw new CompilerException(
                    "Error while executing the external compiler.", e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while executing the external compiler.", e);
        }

        for (CompilerMessage x : messages) {
            msgs.add(x.getMessage());
        }
        boolean success = returnCode == 0;
        // that means if we didn't catch an exception we return success.
        return new CompilerResult(success, messages);
    }

    /*
 Asagiyi GNUCobol a gore uyarla
$ mcs --help
Mono C# compiler, (C) 2001 - 2003 Ximian, Inc.
mcs [options] source-files
   --about            About the Mono C# compiler
   -addmodule:MODULE  Adds the module to the generated assembly
   -checked[+|-]      Set default context to checked
   -codepage:ID       Sets code page to the one in ID (number, utf8, reset)
   -clscheck[+|-]     Disables CLS Compliance verifications
   -define:S1[;S2]    Defines one or more symbols (short: /d:)
   -debug[+|-], -g    Generate debugging information
   -delaysign[+|-]    Only insert the public key into the assembly (no signing)
   -doc:FILE          XML Documentation file to generate
   -keycontainer:NAME The key pair container used to strongname the assembly
   -keyfile:FILE      The strongname key file used to strongname the assembly
   -langversion:TEXT  Specifies language version modes: ISO-1 or Default
   -lib:PATH1,PATH2   Adds the paths to the assembly link path
   -main:class        Specified the class that contains the entry point
   -noconfig[+|-]     Disables implicit references to assemblies
   -nostdlib[+|-]     Does not load core libraries
   -nowarn:W1[,W2]    Disables one or more warnings
   -optimize[+|-]     Enables code optimalizations
   -out:FNAME         Specifies output file
   -pkg:P1[,Pn]       References packages P1..Pn
   -recurse:SPEC      Recursively compiles the files in SPEC ([dir]/file)
   -reference:ASS     References the specified assembly (-r:ASS)
   -target:KIND       Specifies the target (KIND is one of: exe, winexe,
                      library, module), (short: /t:)
   -unsafe[+|-]       Allows unsafe code
   -warnaserror[+|-]  Treat warnings as errors
   -warn:LEVEL        Sets warning level (the highest is 4, the default is 2)
   -help2             Show other help flags

Resources:
   -linkresource:FILE[,ID] Links FILE as a resource
   -resource:FILE[,ID]     Embed FILE as a resource
   -win32res:FILE          Specifies Win32 resource file (.res)
   -win32icon:FILE         Use this icon for the output
   @file                   Read response file for more options

Options can be of the form -option or /option
    */

    public String getOutputFile(CompilerConfiguration configuration)
            throws CompilerException {
        return configuration.getOutputFileName() + "." + getTypeExtension(
                configuration);
    }

    public boolean canUpdateTarget(CompilerConfiguration configuration)
            throws CompilerException {
//            return false; original in C#
        return true;
    }

    public String[] createCommandLine(CompilerConfiguration config)
            throws CompilerException {
        return buildCompilerArguments(config,
                GNUCobolCompiler.getSourceFiles(config));
    }

    private String findExecutable(CompilerConfiguration config) {
        String executable = config.getExecutable();
        if (!StringUtils.isEmpty(executable)) {
            return executable;
        }

        String scriptrRunner = "bash";
        if (Os.isFamily("windows")) {
//            scriptrRunner =
//                    Paths.get("C:\\msys64\\usr\\bin\\bash.exe").toString();
            scriptrRunner = "bash.exe";
        }
        if (Os.isFamily("z/os")) {
            scriptrRunner = "bash";
        }

        String script = "gnucobol_compiler.sh";
//        executable = scriptrRunner + " " + script;
        executable = scriptrRunner;
        return executable;
    }

    private String[] buildCompilerArguments(
            CompilerConfiguration config,
            String[] sourceFiles) {

/*
        TARGETDIR=$1 /target/objects
        COMPILER_ARGS=$2 "ibm fixed"
        COBOLFILES=$3
        SYSLIB=$4
        STEPS=$5
*/

        List<String> args = new ArrayList<String>();
        String compile_script = null;
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(
                "gnucobol_compiler.sh");


        File tmpFile = null;
        try {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            tmpFile = File.createTempFile("compile", ".sh");
            Files.write(buffer, tmpFile);
            compile_script = tmpFile.getAbsolutePath();
            tmpFile.deleteOnExit();
            args.add(compile_script);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        args.add(config.getWorkingDirectory().toString()); // bu nereden
        // geliyor buraya yaz.
        args.add(config.getOutputLocation()); //$1

        args.add("-std=ibm -fixed"); //$2 bunu da pomdan ya da daha sistematik

        try {
            String sourceRoot = config.getSourceLocations().get(0);
            String sourceFilesAsString =
                    convertSourceFilesToPackagedFilesAsJson(sourceRoot,
                            sourceFiles);
            //TODO: this is for converting string into bash style. Find a
            // better way.
            sourceFilesAsString =
                    sourceFilesAsString.replace("\\\\", "/").replace("C:",
                            "/C");
            args.add("'" + sourceFilesAsString + "'"); //$3
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        String syslibAsString = getCompilationSyslib(config).toString();
        args.add(syslibAsString); //$4
//        args.add("comp or link"); //$5

        return args.toArray(new String[args.size()]);
    }

    private String convertSourceFilesToPackagedFilesAsJson(
            String sourceRoot,
            String[] sourceFiles)
            throws JsonProcessingException {
        File cobolSourceRoot = new File(sourceRoot);
        CobolPackaging cobolPackaging = new CobolPackaging(cobolSourceRoot);
        return cobolPackaging.getSourceFilesInPackagesAsJson(sourceFiles);
    }


    private void addResourceArgs(CompilerConfiguration config,
                                 List<String> args) {
        File filteredResourceDir = this.findResourceDir(config);
        if ((filteredResourceDir != null) && filteredResourceDir.exists()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(filteredResourceDir);
            scanner.setIncludes(DEFAULT_INCLUDES);
            scanner.addDefaultExcludes();
            scanner.scan();

            List<String> includedFiles =
                    Arrays.asList(scanner.getIncludedFiles());
            for (String name : includedFiles) {
                File filteredResource = new File(filteredResourceDir, name);
                String assemblyResourceName =
                        this.convertNameToAssemblyResourceName(name);
                String argLine =
                        "/resource:\"" + filteredResource + "\",\"" + assemblyResourceName + "\"";
                if (config.isDebug()) {
                    System.out.println("adding resource arg line:" + argLine);
                }
                args.add(argLine);

            }
        }
    }

    private File findResourceDir(CompilerConfiguration config) {
        if (config.isDebug()) {
            System.out.println("Looking for resourcesDir");
        }
        Map<String, String> compilerArguments =
                config.getCustomCompilerArgumentsAsMap();
        String tempResourcesDirAsString =
                (String) compilerArguments.get("-resourceDir");
        File filteredResourceDir = null;
        if (tempResourcesDirAsString != null) {
            filteredResourceDir = new File(tempResourcesDirAsString);
            if (config.isDebug()) {
                System.out.println(
                        "Found resourceDir at: " + filteredResourceDir.toString());
            }
        }
        else {
            if (config.isDebug()) {
                System.out.println("No resourceDir was available.");
            }
        }
        return filteredResourceDir;
    }

    private String convertNameToAssemblyResourceName(String name) {
        return name.replace(File.separatorChar, '.');
    }

    private File createFileWithArguments(String[] args, String outputDirectory)
            throws IOException {
        PrintWriter writer = null;
        try {
            File tempFile;
            if ((getLogger() != null) && getLogger().isDebugEnabled()) {
                tempFile =
                        File.createTempFile(GNUCobolCompiler.class.getName(),
                                "arguments", new File(outputDirectory));
            }
            else {
                tempFile = File.createTempFile(GNUCobolCompiler.class.getName(),
                        "arguments");
                tempFile.deleteOnExit();
            }

            writer = new PrintWriter(new FileWriter(tempFile));

            for (int i = 0; i < args.length; i++) {
                String argValue = args[i].replace(File.separatorChar, '/');

                writer.write("\"" + argValue + "\"");

                writer.println();
            }

            writer.flush();

            return tempFile;

        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    static List<CompilerMessage> parseModernStream(int exitCode,
                                                   BufferedReader input,
                                                   CompilerMessage.Kind kind)
            throws IOException {
// this method is specific to compiler type
        List<CompilerMessage> messages = new ArrayList<CompilerMessage>();
        String line;
        StringBuilder buffer; // this is main buffer of input but we need to
        // filter it.
        StringBuilder warningBuffer;
        while (true) {
            // cleanup the buffer
            buffer = new StringBuilder(); // this is quicker than clearing it
            warningBuffer = new StringBuilder();
            do {
                line = input.readLine();
                if (line == null) {
                    if (buffer.length() > 0) {
                        messages.add(new CompilerMessage(buffer.toString(),
                                kind)); // here kind is either ERROR or NOTE.
                    }

                    if (warningBuffer.length() > 0) {
                        messages.add(
                                new CompilerMessage(warningBuffer.toString(),
                                        CompilerMessage.Kind.WARNING));
                    }
                    return messages;
                }

                else {
                    if (line.contains("warning: ")) {
                        warningBuffer.append(line);
                        warningBuffer.append(EOL);
                    }
                    else {
                        buffer.append(line);
                        buffer.append(EOL);
                    }

                }

            }
//            while (!line.endsWith("^"));
            while (!line.endsWith("^") && !line.isEmpty());

            // add the error bean
            // errors.add(parseModernError(exitCode, buffer.toString()));
//            return null;
        }
    }


    private String getType(Map<String, String> compilerArguments) {
        String type = compilerArguments.get("-target");

        if (StringUtils.isEmpty(type)) {
            return "library";
        }

        return type;
    }

    private String getTypeExtension(CompilerConfiguration configuration)
            throws CompilerException {
        String type = getType(configuration.getCustomCompilerArgumentsAsMap());

        if ("exe".equals(type) || "winexe".equals(type)) {
            return "exe";
        }

        if ("library".equals(type) || "module".equals(type)) {
            return "dll";
        }

        throw new CompilerException("Unrecognized type '" + type + "'.");
    }

}