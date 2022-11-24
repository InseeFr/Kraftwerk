package fr.insee.kraftwerk.batch.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import lombok.NonNull;

public class DirectoryItemReader implements ItemReader<Path> {

    private List<Path> directories= new ArrayList<>();

    public DirectoryItemReader(@NonNull String inDirectory) throws IOException {   	        
        try (Stream<Path> stream = Files.list(Path.of(inDirectory))) {
        	directories = stream.filter(Files::isDirectory)
              .collect(Collectors.toList());
        } 
    }

    @Override
    public Path read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        return directories.isEmpty()?null:directories.remove(0);
    }
}
