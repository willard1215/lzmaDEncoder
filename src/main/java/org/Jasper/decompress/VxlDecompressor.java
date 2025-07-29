package org.Jasper.decompress;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.Arrays;

import SevenZip.Compression.LZMA.Decoder;

public class VxlDecompressor {
    private static final int HEADER_SIZE = 21;
    private static final int PROPS_OFFSET = 16;
    private static final int LZMA_PROPS_SIZE = 5;
    private static final int EXPECTED_VERSION = 20151001;

    public static void extract(Path in, Path out) throws IOException {
        try (InputStream src = Files.newInputStream(in, StandardOpenOption.READ)) {
            byte[] header = readExact(src, HEADER_SIZE);

            // ── Parse header ───────────────────────────────────────────────────────
            String magic = new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);
            if (!"csov".equals(magic))
                throw new IOException("Bad magic: expected 'csov', got '" + magic + "'");

            int version = ByteBuffer.wrap(header, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if (version != EXPECTED_VERSION)
                System.err.printf("⚠ Unknown VXL version %d (expected %d) – continuing...%n", version, EXPECTED_VERSION);

            long outSize = Integer.toUnsignedLong(
                    ByteBuffer.wrap(header, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());

            byte[] props = new byte[LZMA_PROPS_SIZE];
            System.arraycopy(header, PROPS_OFFSET, props, 0, LZMA_PROPS_SIZE);
            System.out.println("props: " + Arrays.toString(props));

            Decoder dec = new Decoder();
            if (!dec.SetDecoderProperties(props))
                throw new IOException("Invalid LZMA props block");

            // Ensure output dir exists
            if (out.getParent() != null)
                Files.createDirectories(out.getParent());

            try (OutputStream dst = Files.newOutputStream(out, StandardOpenOption.CREATE,
                                                           StandardOpenOption.TRUNCATE_EXISTING)) {
                boolean ok = dec.Code(src, dst, outSize);
                if (!ok) throw new IOException("LZMA data corruption detected");
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    private static byte[] readExact(InputStream in, int len) throws IOException {
        byte[] buf = new byte[len];
        int off = 0; int r;
        while (off < len && (r = in.read(buf, off, len - off)) != -1) off += r;
        if (off != len) throw new EOFException("Unexpected EOF (needed " + len + " bytes, got " + off + ")");
        return buf;
    }

    public static Path withExtension(Path p, String ext) {
        String name = p.getFileName().toString();
        int idx = name.lastIndexOf('.');
        if (idx >= 0) name = name.substring(0, idx);
        return p.resolveSibling(name + ext);
    }
}

