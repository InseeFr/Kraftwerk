package fr.insee.kraftwerk.configuration;

import lombok.NonNull;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DirectoryItemReader implements ItemReader<Path> {

    private final List<Path> directories;

    public DirectoryItemReader(@NonNull String inDirectory) throws IOException {
        directories = Files.list(Path.of(inDirectory)).filter(path -> path.toFile().isDirectory()).collect(Collectors.toList());
    }

    @Override
    public Path read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return directories.isEmpty()?null:directories.remove(0);
    }
}
