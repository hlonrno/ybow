// Copyright (c) 2025 Moony.
// 
// Do whatever you want as long as you don't claim you created it.
// I do not take responsibility for any damages.

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.function.BiPredicate;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Nob {
    private static final String mainPackage = "Main";
    public static final File buildDirectory = new File(".build");
    public static final File sourceDirectory = new File(".");
    private static String[] args;
    public static String[] cflags = { };
    public static String[] flags = { };
    public static boolean printFullCommands = false;
    public static long sleepTimeMS = 1000;

    public static void main(String[] args_) throws FileNotFoundException, IOException, InterruptedException {
        args = args_;
        if (!sourceDirectory.exists())
            throw new FileNotFoundException("Source directory doesn't exist.");

        ArrayList<File> sourceFiles, buildFiles;
        boolean firstRun = true;
        do {
            if (!sourceDirectory.exists())
                // the first if exists just for this.
                throw new FileNotFoundException("Source directory ceased to exist.");
            if (!buildDirectory.exists())
                buildDirectory.mkdir();

            sourceFiles = getSourceFiles();
            buildFiles = getBuildFiles();

            if (hasChange(sourceFiles, buildFiles) || firstRun) {
                firstRun = false;
                var process = run(sourceFiles, buildFiles);
                int code = -1;
                if (process != null) {
                    while (process.isAlive()) Thread.yield();
                    code = process.exitValue();
                    System.out.println("exit code: " + code);
                }
                if (code != 0) {
                    final var snapshot = getChanges(sourceFiles, buildFiles);
                    long[] timestamps = new long[snapshot.size()];
                    for (int i = 0; i < timestamps.length; i++)
                        timestamps[i] = snapshot.get(i).lastModified();
                    boolean brek = false;
                    while (!brek) {
                        Thread.sleep(sleepTimeMS);
                        var changes = getChanges(sourceFiles, buildFiles);
                        for (var change : changes) {
                            var file = fileInFiles(change, snapshot,
                                (c, p) -> compareFiles(c, p, sourceDirectory, sourceDirectory));
                            if (file == null || timestamps[snapshot.indexOf(file)] < change.lastModified()) {
                                brek = true;
                                break;
                            }   // you gotta love this
                        }
                    }
                }
            } else Thread.sleep(sleepTimeMS);
        } while (true);
    }

    private static Process run(ArrayList<File> sourceFiles, ArrayList<File> buildFiles)
            throws IOException, InterruptedException {
            int code = new ProcessBuilder("clear").inheritIO().start().waitFor();
            if (code != 0)
                System.err.println("'clear' command exited with code: " + code + ".");

            System.out.print("[FILES]:");
            var useless = getUselessFiles(sourceFiles, buildFiles);
            if (useless.size() > 0) {
                System.out.println();
                for (var file : useless)
                    System.out.printf("File '%s' deleted (success: %b).\n", file, file.delete());
                System.out.println("Count: " + useless.size());
            } else
                System.out.println(" No class files to delete.");

            var changedFiles = getChanges(sourceFiles, buildFiles);
            if (changedFiles.size() > 0) {
                var compileCommand = new ArrayList<String>();
                compileCommand.add("javac");
                compileCommand.add("-d");
                compileCommand.add(buildDirectory.getPath());
                for (int i = 0; i < cflags.length; i++)
                    compileCommand.add(cflags[i]);
                int len = sourceDirectory.getPath().length() + 1;
                for (int i = 0; i < changedFiles.size(); i++)
                    compileCommand.add(changedFiles.get(i).getPath().substring(len));

                System.out.print("[COMPILE]:");
                for (int i = 0; i < compileCommand.size() && (printFullCommands || i < 5); i++)
                    System.out.print(" " + compileCommand.get(i));
                if (compileCommand.size() > 4 && !printFullCommands)
                    System.out.print(" [...]");
                System.out.printf(" (directory '%s')\n", sourceDirectory.getPath());

                var commandBuilder = new ProcessBuilder(compileCommand)
                    .inheritIO()
                    .directory(sourceDirectory);
                code = commandBuilder.start().waitFor();
            } else {
                System.out.println("[COMPILE]: Skipping... No changes detected.");
                code = 0;
            }

            if (code == 0) {
                var runCommand = new ArrayList<String>();
                runCommand.add("java");
                for (int i = 0; i < flags.length; i++)
                    runCommand.add(flags[i]);
                runCommand.add(mainPackage);
                for (int i = 0; i < args.length; i++)
                    runCommand.add(args[i]);

                System.out.print("[RUN]:");
                for (int i = 0; i < runCommand.size() && (printFullCommands || i < 5); i++)
                    System.out.print(" " + runCommand.get(i));
                if (runCommand.size() > 4 && !printFullCommands)
                    System.out.print(" [...]");
                System.out.printf(" (directory '%s')\n", buildDirectory.getPath());

                var runBuilder = new ProcessBuilder(runCommand)
                    .inheritIO()
                    .directory(buildDirectory);
                return runBuilder.start();
            } else {
                System.err.println("exit code: " + code);
                return null;
            }
    }

    private static ArrayList<File> getChanges(ArrayList<File> sourceFiles, ArrayList<File> buildFiles) {
        var changes = new ArrayList<File>();

        for (var source : sourceFiles) {
            var file = fileInFiles(source, buildFiles,
                    (s, b) -> compareFiles(s, b, sourceDirectory, buildDirectory));
            if (file == null || file.lastModified() < source.lastModified())
                changes.add(source);
        }

        return changes;
    }

    private static ArrayList<File> getUselessFiles(ArrayList<File> sourceFiles, ArrayList<File> buildFiles) {
        var files = new ArrayList<File>();
        int i = -1;
        while (++i < buildFiles.size()) {
            var file = buildFiles.get(i);
            var source = fileInFiles(file, sourceFiles,
                (b, s) -> compareFiles(b, s, buildDirectory, sourceDirectory));
            if (source == null)
                files.add(file);
        }

        // TODO: sush... directories aren't gathered.

        return files;
    }

    private static ArrayList<File> getSourceFiles() {
        return getFiles(sourceDirectory, new ArrayList<>(),
                f -> f.getName().endsWith(".java")
                  && !f.isHidden());
    }

    private static ArrayList<File> getBuildFiles() {
        return getFiles(buildDirectory, new ArrayList<>(),
                f -> f.getName().endsWith(".class"));
    }

    private static ArrayList<File> getFiles(File init, ArrayList<File> files, FileFilter filter) {
        files.add(init);
        int i = -1;
        while (++i < files.size()) {
            var file = files.get(i);
            if (file.isFile()) {
                if (!filter.accept(file))
                    files.remove(i--);
                continue;
            }

            files.remove(i--);
            var children = file.listFiles();
            for (var child : children)
                files.add(child);
        }

        return files;
    }

    private static boolean hasChange(ArrayList<File> sourceFiles, ArrayList<File> buildFiles) {
        if (sourceFiles.size() != buildFiles.size())
            return true;

        for (var source : sourceFiles) {
            var file = fileInFiles(source, buildFiles,
                    (s, b) -> compareFiles(s, b, sourceDirectory, buildDirectory));
            if (file == null || file.lastModified() < source.lastModified())
                return true;
        }

        return false;
    }

    private static File fileInFiles(File file, ArrayList<File> files, BiPredicate<File, File> comparefn) {
        for (int i = 0; i < files.size(); i++)
            if (comparefn.test(file, files.get(i)))
                return files.get(i);
        return null;
    }

    /**
     * 
     * @param a a file.
     * @param b a file.
     * @param relativeA must be a parent or parent of a parent or...file of a.
     * @param relativeB must be a parent or parent of a parent or...file of b.
     * @return wether the files are equal or not.
     */
    private static boolean compareFiles(File a, File b, File relativeA, File relativeB) {
        // This one requires a bit of thinking. Idk if it works in *every* case.
        var as = a.getAbsolutePath();
        var bs = b.getAbsolutePath();
        var relAs = relativeA.getAbsolutePath();
        var relBs = relativeB.getAbsolutePath();

        assert as.indexOf(relAs) != -1;
        assert bs.indexOf(relBs) != -1;
        as = as.substring(as.indexOf(relAs) + relAs.length() + 1, as.lastIndexOf('.'));
        bs = bs.substring(bs.indexOf(relBs) + relBs.length() + 1, bs.lastIndexOf('.'));

        return as.equals(bs);
    }
}
