package br.com.muxe.minigame.engine.world.map;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapFiles {

    public static final String EXTENSION = ".mxw";

    private static final byte[] MAGIC = "MUXEMAP".getBytes(StandardCharsets.US_ASCII);
    private static final int VERSION = 1;
    private static final int MAX_CHUNK_BYTES = 16 * 1024 * 1024;

    private MapFiles() {
    }

    public static void write(Path file, MapTemplate template) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path partial = file.resolveSibling(file.getFileName() + ".part");
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(partial))) {
            write(output, template);
        }
        Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static byte[] toBytes(MapTemplate template) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            write(bytes, template);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        return bytes.toByteArray();
    }

    public static MapTemplate fromBytes(byte[] bytes, String source) throws IOException {
        return read(new ByteArrayInputStream(bytes), source);
    }

    public static MapTemplate read(Path file) throws IOException {
        try (InputStream input = new BufferedInputStream(Files.newInputStream(file))) {
            return read(input, file.toString());
        }
    }

    private static void write(OutputStream stream, MapTemplate template) throws IOException {
        DataOutputStream output = new DataOutputStream(stream);
        {
            output.write(MAGIC);
            output.writeByte(VERSION);
            output.writeInt(template.chunkCount());
            for (Map.Entry<Long, byte[]> chunk : template.compressedChunks().entrySet()) {
                output.writeInt(ChunkData.keyX(chunk.getKey()));
                output.writeInt(ChunkData.keyZ(chunk.getKey()));
                output.writeInt(chunk.getValue().length);
                output.write(chunk.getValue());
            }
        }
        output.flush();
    }

    private static MapTemplate read(InputStream stream, String file) throws IOException {
        DataInputStream input = new DataInputStream(stream);
        {
            byte[] magic = new byte[MAGIC.length];
            input.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException(file + " is not a Muxe map");
            }
            int version = input.readUnsignedByte();
            if (version != VERSION) {
                throw new IOException(file + " has map version " + version + ", expected " + VERSION);
            }
            int count = input.readInt();
            if (count < 0) {
                throw new IOException(file + " has a negative chunk count");
            }
            Map<Long, byte[]> chunks = new LinkedHashMap<>();
            for (int index = 0; index < count; index++) {
                int x = input.readInt();
                int z = input.readInt();
                int length = input.readInt();
                if (length <= 4 || length > MAX_CHUNK_BYTES) {
                    throw new IOException(file + " has a chunk of " + length + " bytes at " + x + "," + z);
                }
                byte[] data = new byte[length];
                input.readFully(data);
                chunks.put(ChunkData.key(x, z), data);
            }
            return MapTemplate.ofCompressed(chunks);
        }
    }
}
