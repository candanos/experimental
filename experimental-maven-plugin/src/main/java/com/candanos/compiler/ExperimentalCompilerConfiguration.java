package com.candanos.compiler;

import org.codehaus.plexus.compiler.CompilerConfiguration;

public class ExperimentalCompilerConfiguration extends CompilerConfiguration {
    private String scriptExecutable;
    private String scriptFile;
    private String[] scriptOptions;

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public String[] getScriptOptions() {
        return scriptOptions;
    }

    public void setScriptOptions(String[] scriptOptions) {
        this.scriptOptions = scriptOptions;
    }

    public String getScriptExecutable() {
        return scriptExecutable;
    }

    public void setScriptExecutable(String scriptExecutable) {
        this.scriptExecutable = scriptExecutable;
    }
}