package fr.insee.kraftwerk.core.outputs.parquet;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;

/**
 * Thanks
 * https://github.com/tideworks/arvo2parquet/blob/master/src/main/java/com/tideworks/data_load/io/OutputFile.java
 */
class NioPathOutputFile implements OutputFile {

	Path file;
	private static final int IO_BUF_SIZE = 16 * 1024;

	public NioPathOutputFile(Path path) {
		file = path;
	}

	@Override
	public PositionOutputStream create(long blockSizeHint) throws IOException {
		return makePositionOutputStream(file, IO_BUF_SIZE, false);
	}

	@Override
	public PositionOutputStream createOrOverwrite(long blockSizeHint) throws IOException {
		return makePositionOutputStream(file, IO_BUF_SIZE, true);
	}

	@Override
	public boolean supportsBlockSize() {
		return false;
	}

	@Override
	public long defaultBlockSize() {
		return 0;
	}

	private static PositionOutputStream makePositionOutputStream(Path file, int ioBufSize, boolean trunc)
			throws IOException {
		try (final OutputStream output = new BufferedOutputStream(
				Files.newOutputStream(file, CREATE, trunc ? TRUNCATE_EXISTING : APPEND), ioBufSize)) {

			return new PositionOutputStream() {
				private long position = 0;

				@Override
				public void write(int b) throws IOException {
					output.write(b);
					position++;
				}

				@Override
				public void write(byte[] b) throws IOException {
					output.write(b);
					position += b.length;
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					output.write(b, off, len);
					position += len;
				}

				@Override
				public void flush() throws IOException {
					output.flush();
				}

				@Override
				public void close() throws IOException {
					output.close();
				}

				@Override
				public long getPos() throws IOException {
					return position;
				}
			};
		}
	}
}
