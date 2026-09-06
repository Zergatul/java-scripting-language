package com.zergatul.scripting.utility;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class Paths {

    public static Path of(String first, String... more) {
        return FileSystems.getDefault().getPath(first, more);
    }
}