package org.Jasper.compress;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import SevenZip.Compression.LZMA.Encoder;


public class VxlCompressor {
    // ─────────────────── 상수 ────────
    private static final int HEADER_SIZE = 21;
    private static final int VERSION     = 20151001;
    private static final byte[] MAGIC    = { 'c', 's', 'o', 'v' };
    private static final byte[] MOREMAG  = { '.', '.', '.', '.' };

   
    public static void compress(Path inFile, Path outFile) throws IOException {
        long inSize = Files.size(inFile);
        if (inSize > 0xFFFF_FFFFL)
            throw new IOException("Input larger than 4 GiB not supported by VXL spec");

        // LZMA encoder 설정 
        Encoder enc = new Encoder();
        enc.SetLcLpPb(3, 0, 2);                  // lc=3, lp=0, pb=2
        enc.SetDictionarySize(64 * 1024);       // 64 KiB
        enc.SetAlgorithm(2);            // default 
        enc.SetNumFastBytes(32);
        enc.SetMatchFinder(Encoder.EMatchFinderTypeBT4);
        enc.SetEndMarkerMode(false);

        // 5바이트 속성 가져오기
        ByteArrayOutputStream propBuf = new ByteArrayOutputStream(5);
        enc.WriteCoderProperties(propBuf);
        byte[] props = propBuf.toByteArray();
        if (props.length != 5)
            throw new IllegalStateException("LZMA property length != 5 (" + props.length + ")");

        // Ensure output directory exists
        if (outFile.getParent() != null)
            Files.createDirectories(outFile.getParent());

        try (InputStream src = Files.newInputStream(inFile);
             OutputStream dst = Files.newOutputStream(outFile, StandardOpenOption.CREATE,
                                                     StandardOpenOption.TRUNCATE_EXISTING)) {

            // ── 21바이트 헤더 ──────────────────────────────
            byte[] header = new byte[HEADER_SIZE];
            System.arraycopy(MAGIC,   0, header, 0, 4);
            System.arraycopy(MOREMAG, 0, header, 4, 4);
            ByteBuffer.wrap(header, 8,  4).order(ByteOrder.LITTLE_ENDIAN).putInt(VERSION);
            ByteBuffer.wrap(header, 12, 4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) inSize);
            System.arraycopy(props,   0, header, 16, 5);
            dst.write(header);

            // ── 압축 페이로드 ─────────────────────────────────
            enc.Code(src, dst, inSize, -1, null);
        }
    }

   
    public static Path withExtension(Path p, String ext) {
        String name = p.getFileName().toString();
        int idx = name.lastIndexOf('.');
        if (idx >= 0) name = name.substring(0, idx);
        return p.resolveSibling(name + ext);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java org.Jasper.compress.VxlCompressor <raw.dec_data> [...]\n" +
                               "Each input produces <name>.vxl next to it.");
            System.exit(1);
        }
        int fails = 0;
        for (String arg : args) {
            Path in = Paths.get(arg);
            if (!Files.isRegularFile(in)) {
                System.err.printf("%s: not a regular file – skipped%n", in);
                fails++; continue;
            }
            Path out = withExtension(in, ".vxl");
            try {
                compress(in, out);
                System.out.printf("✔ %s → %s%n", in.getFileName(), out.getFileName());
            } catch (IOException e) {
                System.err.printf("✖ %s: %s%n", in.getFileName(), e.getMessage());
                fails++;
            }
        }
        if (fails != 0) System.exit(2);
    }
}