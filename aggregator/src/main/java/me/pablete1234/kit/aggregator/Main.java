package me.pablete1234.kit.aggregator;

import blue.strategic.parquet.ParquetReader;
import me.pablete1234.kit.aggregator.util.DummyServer;
import me.pablete1234.kit.util.InventoryImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException{
        DummyServer.setup();

        Path kitData = Paths.get("kit_data");
        for (Path playerFolder : toIterable(Files.list(kitData))) {
            if (Files.isDirectory(playerFolder)) handlePlayer(playerFolder);
        }
    }

    private static void handlePlayer(Path playerFolder) throws IOException {
        for (Path file : toIterable(Files.list(playerFolder))) {
            if (!Files.isRegularFile(file) || !file.toString().endsWith(".parquet")) continue;
            ParquetReader.streamContent(file.toFile(), InventoryImage.Deserializer.SUPPLIER_INSTANCE)
                    .forEach(image -> {
                        System.out.println(image.toString());

                        // TODO: handle the list of cases, and derive a final kit
                    });
        }
    }

    private static <T> Iterable<T> toIterable(Stream<T> stream) {
        return stream::iterator;
    }

}
