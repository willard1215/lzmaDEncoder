package org.Jasper;

import org.Jasper.compress.VxlCompressor;
import org.Jasper.decompress.VxlDecompressor;

import java.nio.file.*;

public final class VxlTool {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage(); return;
        }
        String mode = args[0].toLowerCase();
        Path in  = Paths.get(args[1]);
        Path out = (args.length >= 3)
                   ? Paths.get(args[2])
                   : deriveOutPath(mode, in);

        switch (mode.charAt(0)) {
            case 'c': // compress
                VxlCompressor.compress(in, out);
                System.out.printf("✔ Compressed %s → %s%n",
                        in.getFileName(), out.getFileName());
                break;

            case 'd': // decompress
                VxlDecompressor.extract(in, out);
                System.out.printf("✔ Decompressed %s → %s%n",
                        in.getFileName(), out.getFileName());
                break;

            default: usage();
        }
    }

    private static Path deriveOutPath(String mode, Path in) {
        String ext = mode.startsWith("c") ? ".vxl" : ".dec_data";
        return changeExtension(in, ext);
    }

    private static Path changeExtension(Path p, String newExt) {
        String name = p.getFileName().toString();
        int i = name.lastIndexOf('.');
        if (i < 0) return p.resolveSibling(name + newExt);
        return p.resolveSibling(name.substring(0, i) + newExt);
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  java org.Jasper.VxlTool c|compress <input.dec_data> [output.vxl]");
        System.out.println("  java org.Jasper.VxlTool d|decompress <input.vxl>    [output.dec_data]");
    }
}
