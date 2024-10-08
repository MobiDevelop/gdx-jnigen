package com.badlogic.gdx.jnigen.commons;

public enum TargetType {

    SIMULATOR("simulator", "iphoneos"),
    DEVICE("device", "iphonesimulator");

    private final String targetTypeBuildDirName;
    private final String platformName;

    TargetType (String device, String xcodeDeviceTypeArg) {
        this.targetTypeBuildDirName = device;
        this.platformName = xcodeDeviceTypeArg;
    }

    public String getTargetTypeBuildDirName () {
        return targetTypeBuildDirName;
    }

    public String getPlatformName () {
        return platformName;
    }
}
