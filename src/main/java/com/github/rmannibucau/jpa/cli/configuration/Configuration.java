package com.github.rmannibucau.jpa.cli.configuration;

public class Configuration {
    public enum Output {
        VERTICAL, HORIZONTAL
    }

    private Output output = Output.HORIZONTAL;

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }
}
