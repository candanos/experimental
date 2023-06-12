package com.candanos;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.candanos.compiler.ExperimentalCompilerConfiguration;
import com.candanos.compiler.GNUCobolCompiler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.compiler.CompilationFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


import org.codehaus.plexus.compiler.*;
import org.codehaus.plexus.compiler.Compiler;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


// plexus-compiler-javac is the package that runs the javac as a seperate
// process

@Mojo(name = "experiment", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ExperimentalMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    /**
     * A list of inclusion filters for the compiler.
     */
    @Parameter
    private Set<String> includes = new HashSet<>();

    /**
     * A list of exclusion filters for the compiler.
     */
    @Parameter
    private Set<String> excludes = new HashSet<>();

    /**
     * A list of exclusion filters for the incremental calculation.
     *
     * @since 3.11
     */
    @Parameter
    private Set<String> incrementalExcludes = new HashSet<>();

    @Parameter(defaultValue = "${project.build.directory}", required = true,
            readonly = true)
    private File buildDirectory;

    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = false,
            required = true)
    private List<String> compileSourceRoots;

    /**
     * The directory for compiled classes.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}",
            required = true, readonly = true)
    private File outputDirectory;

    @Parameter(defaultValue = "true",
            property = "maven.compiler.showCompilationChanges")
    private boolean showCompilationChanges = false;

    @Parameter(
            defaultValue = "${project.build.directory}/generated-sources" +
                    "/annotations")
    private File generatedSourcesDirectory;

    /**
     * Sets the executable of the compiler to use when {@link #'fork'} is
     * <code>true</code>.
     */
    @Parameter(property = "maven.compiler.executable")
    private String executable;

    /**
     * The directory to run the compiler from if fork is true.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    private File basedir;
    @Parameter(property = "maven.compiler.failOnError", defaultValue = "true")
    private boolean failOnError = true;


    /*    configuration parameters of the plugin. */
    @Parameter(property = "scriptRunner", required = true)
    private String scriptRunner;

    @Parameter(property = "scriptFile", required = true)
    private String scriptFile;

    @Parameter(property = "scriptOptions", required = false)
    private String[] scriptOptions;

    public void execute()
            throws MojoExecutionException, CompilationFailureException {
        CompilerConfiguration compilerConfiguration =
                getCompilerConfiguration();

        /*
        // this is the constructor of AbstractCompiler, extending compilers
        // usage examples are following.
        protected AbstractCompiler( CompilerOutputStyle compilerOutputStyle,
        String inputFileEnding, String outputFileEnding, String outputFile )
   {
        this.compilerOutputStyle = compilerOutputStyle;
        this.inputFileEnding = inputFileEnding;
        this.outputFileEnding = outputFileEnding;
        this.outputFile = outputFile;
    }
    // instantiating compilers using AbstractCompiler constructor.
        public JavacCompiler() {
            super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, ".java",
                ".class", null);
        }

        public EclipseJavaCompiler() {
        super( CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, ".java", "
        .class", null );
        }

    public CSharpCompiler() {
        super( CompilerOutputStyle.ONE_OUTPUT_FILE_FOR_ALL_INPUT_FILES, "
        .cs", null, null );
    }

    public GNUCobolCompiler() {
       super(CompilerOutputStyle.ONE_OUTPUT_FILE_FOR_ALL_INPUT_FILES, ".cbl",
                "class", null);
    }

 */

        GNUCobolCompiler compiler = (GNUCobolCompiler) getCompiler();
        compiler.setLog(getLog());

//        boolean sourceChanged =
//                isSourceChanged(compilerConfiguration, compiler);
        Set<File> staleSources = null;
        try {
            SourceInclusionScanner scanner = getSourceInclusionScanner(0);
            staleSources = computeStaleSources(compilerConfiguration, compiler,
                    scanner);
        } catch (CompilerException e) {
            throw new MojoExecutionException(e);
        }

        compilerConfiguration.setSourceFiles(staleSources);

        String cobolSourceRoot = project.getCompileSourceRoots().stream()
                .collect(Collectors.toList())
                .get(0)
                .replace("java", "cobol");
        compilerConfiguration.addSourceLocation(cobolSourceRoot);
        // 3. lets compile sources

        CompilerResult compilerResult;

        try {
            compilerResult =
                    compiler.performCompile(compilerConfiguration);
        } catch (Exception e) {
            getLog().info(e.toString());
            // TODO: don't catch Exception
            throw new MojoExecutionException("Fatal error compiling", e);
        }

        printCompilerMessages(compilerResult);

// 5. lets distribute compile artifacts to target libraries.
//        distributeArtifacts();

    }

    private void printCompilerMessages(CompilerResult compilerResult)
            throws CompilationFailureException {

        List<CompilerMessage> warnings = new ArrayList<>();
        List<CompilerMessage> errors = new ArrayList<>();
        List<CompilerMessage> others = new ArrayList<>();
        for (CompilerMessage message : compilerResult.getCompilerMessages()) {
            if (message.getKind() == CompilerMessage.Kind.ERROR) {
                errors.add(message);
            }
            else if (message.getKind() == CompilerMessage.Kind.WARNING
                    || message.getKind() == CompilerMessage.Kind.MANDATORY_WARNING) {
                warnings.add(message);
            }
            else {
                others.add(message);
            }
        }

        if (failOnError && !compilerResult.isSuccess()) {
            for (CompilerMessage message : others) {
                assert message.getKind() != CompilerMessage.Kind.ERROR
                        && message.getKind() != CompilerMessage.Kind.WARNING
                        && message.getKind() != CompilerMessage.Kind.MANDATORY_WARNING;
                getLog().info(message.toString());
            }
            if (!warnings.isEmpty()) {
                getLog().info(
                        "-------------------------------------------------------------");
                getLog().warn("COMPILATION WARNING : ");
                getLog().info(
                        "-------------------------------------------------------------");
                for (CompilerMessage warning : warnings) {
                    getLog().warn(warning.toString());
                }
                getLog().info(warnings.size() + ((warnings.size() > 1) ?
                        " warnings " : " warning"));
                getLog().info(
                        "-------------------------------------------------------------");
            }

            if (!errors.isEmpty()) {
                getLog().info(
                        "-------------------------------------------------------------");
                getLog().error("COMPILATION ERROR : ");
                getLog().info(
                        "-------------------------------------------------------------");
                for (CompilerMessage error : errors) {
                    getLog().error(error.toString());
                }
                getLog().info(
                        errors.size() + ((errors.size() > 1) ? " errors " :
                                " error"));
                getLog().info(
                        "-------------------------------------------------------------");
            }

            if (!errors.isEmpty()) {
                throw new CompilationFailureException(errors);
            }
            else {
                throw new CompilationFailureException(warnings);
            }
        }
        else {
            for (CompilerMessage message :
                    compilerResult.getCompilerMessages()) {
                switch (message.getKind()) {
                    case NOTE:
                    case OTHER:
                        getLog().info(message.toString());
                        break;

                    case ERROR:
                        getLog().error(message.toString());
                        break;

                    case MANDATORY_WARNING:
                    case WARNING:
                    default:
                        getLog().warn(message.toString());
                        break;
                }
            }
        }
    }

    // maven-compiler-plugin bunu compilerManager.getCompiler(compilerId) ile
    // yapiyor. @Component annotation i ile compilerManager i plexus tan
    // aliyor. It is a plexus compiler manager.
    // Plexux compiler manager ise plexus-compiler projesinde;
    // plexus-compiler-manager modulu icinde DefaultCompilerManager olarak
    // implemente edilmis. Bu default implementasyon icinde asagidaki gibi
    // bir compilers attribute ve getCompiler() methodu var. getCompiler
    // (compilerId) Mapteki uygun compiler i donuyor.
    //    private Map<String, Compiler> compilers;
    // simdi asagidaki methodda CompilerManager i kendimiz yaratiyoruz.
    // this is (getCompiler() method) our CompilerManager
    private Compiler getCompiler() {
        Compiler compiler = new GNUCobolCompiler();
        return compiler;
    }

    private CompilerConfiguration getCompilerConfiguration() {
        ExperimentalCompilerConfiguration compilerConfiguration =
                new ExperimentalCompilerConfiguration();
        compilerConfiguration.setOutputLocation(
                getOutputDirectory().getAbsolutePath());

        compilerConfiguration.setWorkingDirectory(basedir);
//        @Parameter(property = "maven.compiler.executable") or
//      if empty your compiler should check if empty
//        compilerConfiguration.setExecutable(executable);
//        getLog().info("executable:" + executable);
//        "This compiler doesn't support in-process compilation."
//        compilerConfiguration.setFork(true);
        // basedir is root of repo, directory where pom.xml is.

        compilerConfiguration.setBuildDirectory(buildDirectory);
        compilerConfiguration.setScriptExecutable(scriptRunner);
        compilerConfiguration.setScriptFile(scriptFile);
        compilerConfiguration.setScriptOptions(scriptOptions);
        return compilerConfiguration;
    }

    // this method is originally called from AbstractCompilerMojo
    private Set<File> computeStaleSources(
            CompilerConfiguration compilerConfiguration, Compiler compiler,
            SourceInclusionScanner scanner)
            throws MojoExecutionException, CompilerException {

        SourceMapping
                mapping = getSourceMapping(compilerConfiguration, compiler);
        scanner.addSourceMapping(mapping);

/*        File outputDirectory;
        CompilerOutputStyle outputStyle = compiler.getCompilerOutputStyle();
        if (outputStyle == CompilerOutputStyle
        .ONE_OUTPUT_FILE_FOR_ALL_INPUT_FILES) {
            outputDirectory = buildDirectory;
        }
        else {
            outputDirectory = getOutputDirectory();
        }*/
        File outputDirectory = getOutputDirectory();

        Set<File> staleSources = new HashSet<>();

        for (String sourceRoot : getCompileSourceRoots()) {
            File rootFile = new File(sourceRoot);
            if (!rootFile.isDirectory()) {
                continue;
            }

            try {
                staleSources.addAll(
                        scanner.getIncludedSources(rootFile, outputDirectory));
                getLog().info("rootFile is: " + rootFile.getAbsolutePath());
                getLog().info(
                        "outputDirectory is: " + outputDirectory.getAbsolutePath());
                staleSources.stream().forEach(f -> getLog().info("compile " +
                        "source:" + f.getAbsolutePath()));

            } catch (InclusionScanException e) {
                throw new MojoExecutionException(
                        "Error scanning source root: \'" + sourceRoot + "\' " +
                                "for stale files to recompile.",
                        e);
            }
        }

        return staleSources;
    }

//    private void getSources() throws MojoExecutionException {
//        List<String> compileSourceRoots = project.getCompileSourceRoots();
//
//        String cobolSourceRoot = compileSourceRoots.stream()
//                .collect(Collectors.toList())
//                .get(0)
//                .replace("java", "cobol");
//
//        project.addCompileSourceRoot(cobolSourceRoot);
//        Path path = Paths.get(cobolSourceRoot);
//        if (!Files.isDirectory(path)) {
//            throw new MojoExecutionException("Path must be a directory!");
//        }
//
//        List<Path> result;
//        try (Stream<Path> walk = Files.walk(path)) {
////            String fileExtension = "cbl";
//            result = walk
//                    .filter(Files::isRegularFile)   // is a file
//                    .filter(p -> p.getFileName()
//                            .toString()
//                            .endsWith(".cbl"))
//                    .collect(Collectors.toList());
//        } catch (IOException e) {
//            throw new MojoExecutionException(e.toString());
//        }
//        for (Path p : result) {
//            getLog().info(p.toString());
//        }
//
//    }

    // this method is originally called from CompilerMojo
    protected SourceInclusionScanner getSourceInclusionScanner(
            int staleMillis) {

        if (includes.isEmpty()) {
            includes.add("**/*.cbl");
        }

        if (excludes.isEmpty()) {
            excludes.add("**/*.java");
        }

        Set<String> excludesIncr = new HashSet<>(excludes);
        excludesIncr.addAll(this.incrementalExcludes);
        return new StaleSourceScanner(staleMillis, includes,
                excludesIncr);

    }

    //    protected SourceInclusionScanner getSourceInclusionScanner(
//            String inputFileEnding) {
//        // it's not defined if we get the ending with or without the dot '.'
//        String defaultIncludePattern = "**/*" + (inputFileEnding.startsWith
//                (".") ? "" : ".") + inputFileEnding;
//
//        if (includes.isEmpty()) {
//            includes.add(defaultIncludePattern);
//        }
//        Set<String> excludesIncr = new HashSet<>(excludes);
//        excludesIncr.addAll(excludesIncr);
//        return new SimpleSourceInclusionScanner(includes, excludesIncr);
//    }


    // this method is originally called from AbstractCompilerMojo
    private SourceMapping getSourceMapping(
            CompilerConfiguration compilerConfiguration, Compiler compiler)
            throws CompilerException, MojoExecutionException {

        CompilerOutputStyle outputStyle = compiler.getCompilerOutputStyle();

        SourceMapping mapping;
        if (outputStyle == CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE) {
            mapping = new SuffixMapping(
                    compiler.getInputFileEnding(compilerConfiguration),
                    compiler.getOutputFileEnding(compilerConfiguration));
            getLog().info("input  file ending:" + compiler.getInputFileEnding(
                    compilerConfiguration));
            getLog().info("output file ending:" + compiler.getOutputFileEnding(
                    compilerConfiguration));
        }
        else if (outputStyle == CompilerOutputStyle.ONE_OUTPUT_FILE_FOR_ALL_INPUT_FILES) {
            mapping = new SingleTargetSourceMapping(
                    compiler.getInputFileEnding(compilerConfiguration),
                    compiler.getOutputFile(compilerConfiguration));
        }
        else {
            throw new MojoExecutionException(
                    "Unknown compiler output style: '" + outputStyle + "'.");
        }
        return mapping;
    }

    // this method is originally called from CompilerMojo
    protected File getOutputDirectory() {

        File dir =
                new File(outputDirectory.toPath()
                        .getParent()
                        .toString()
                        .concat("/objects"));
////        if (!multiReleaseOutput) {
//        dir = outputDirectory;
//        }
//        else {
//            dir = new File(outputDirectory, "META-INF/versions/" + release);
//        }
        return dir;
    }


    // this method is originally called from CompilerMojo
    protected List<String> getCompileSourceRoots() {
        List<String> newSourceRoots = new ArrayList<String>();
        String cobolSourceRoot = project.getCompileSourceRoots().stream()
                .collect(Collectors.toList())
                .get(0)
                .replace("java", "cobol");
        newSourceRoots.add(cobolSourceRoot);
        return newSourceRoots;
    }

    /**
     * @param compilerConfiguration
     * @param compiler
     * @return <code>true</code> if at least a single source file is newer
     * than it's class file
     */
    // this method is originally called from AbstractCompilerMojo
 /*   private boolean isSourceChanged(CompilerConfiguration
 compilerConfiguration,
                                    Compiler compiler)
            throws CompilerException, MojoExecutionException {
        Set<File> staleSources =
                computeStaleSources(compilerConfiguration, compiler,
                        getSourceInclusionScanner(0));

        if (getLog().isDebugEnabled() || showCompilationChanges) {
            for (File f : staleSources) {
                if (showCompilationChanges) {
                    getLog().info(
                            "Stale source detected: " + f.getAbsolutePath());
                }
                else {
                    getLog().debug(
                            "Stale source detected: " + f.getAbsolutePath());
                }
            }
        }
        return !staleSources.isEmpty();
    }*/
}