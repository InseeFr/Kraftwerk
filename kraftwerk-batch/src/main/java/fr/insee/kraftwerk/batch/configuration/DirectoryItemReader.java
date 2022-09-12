package fr.insee.kraftwerk.batch.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import lombok.NonNull;

public class DirectoryItemReader implements ItemReader<Path> {

    private final List<Path> directories;

    public DirectoryItemReader(@NonNull String inDirectory) throws IOException {
        directories = Files.list(Path.of(inDirectory)).filter(path -> path.toFile().isDirectory()).collect(Collectors.toList());
    }

    @Override
    public Path read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        return directories.isEmpty()?null:directories.remove(0);
    }
}
