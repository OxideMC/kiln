package com.github.glassmc.kiln.standard.mappings;

import org.objectweb.asm.commons.Remapper;

import java.io.File;

public interface IMappingsProvider {
    void setup(File minecraftFile, String version) throws NoSuchMappingsException;
    void destroy();
    Remapper getRemapper(Direction direction);
    String getID();

    enum Direction {
        TO_NAMED,
        TO_OBFUSCATED
    }
}
