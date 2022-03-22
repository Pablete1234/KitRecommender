package me.pablete1234.kit.aggregator.util;

import blue.strategic.parquet.ParquetReader;
import blue.strategic.parquet.ParquetWriter;
import me.pablete1234.kit.aggregator.exception.InvalidKitDataException;
import me.pablete1234.kit.util.StreamUtil;
import me.pablete1234.kit.util.serialized.InventoryRecord;
import me.pablete1234.kit.util.serialized.KitPreferenceRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerRecordAggregator {

    private final Path playerFolder, outputFile;
    private final Map<InvalidKitDataException.Reason, List<String>> ignoredFiles =
            new EnumMap<>(InvalidKitDataException.Reason.class);
    private int written;

    public PlayerRecordAggregator(Path playerFolder, Path outputFile) {
        this.playerFolder = playerFolder;
        this.outputFile = outputFile;
    }

    public void aggregate() throws IOException {
        System.out.print("\t");
        try (ParquetWriter<KitPreferenceRecord> writer = ParquetWriter.writeFile(KitPreferenceRecord.SCHEMA,
                outputFile.toFile(), KitPreferenceRecord.Serializer.INSTANCE)) {

            PlayerRecordResolver resolver = new PlayerRecordResolver();

            for (Path file : StreamUtil.toIterable(Files.list(playerFolder))) {
                if (!Files.isRegularFile(file) || !file.toString().endsWith(".parquet")) continue;
                KitPreferenceRecord kitPreference;
                try {
                    Iterator<InventoryRecord> records = readRecords(file).iterator();
                    kitPreference = resolver.resolve(records);
                    System.out.print(".");
                } catch (InvalidKitDataException e) {
                    System.out.print("x");
                    ignoredFiles.computeIfAbsent(e.getReason(), r -> new ArrayList<>())
                            .add(file.getFileName().toString());
                    continue;
                }
                writer.write(kitPreference);
                written++;
            }
        }
        System.out.println();
        //ignoredFiles.forEach((reason, files) ->
        //        System.out.println("\t" + reason + " (" + files.size() + "): " + files));
    }

    public Map<InvalidKitDataException.Reason, List<String>> getIgnoredFiles() {
        return ignoredFiles;
    }

    public int getWritten() {
        return written;
    }

    private static List<InventoryRecord> readRecords(Path file) throws InvalidKitDataException {
        try {
            return ParquetReader
                    .streamContent(file.toFile(), InventoryRecord.Deserializer.SUPPLIER_INSTANCE)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new InvalidKitDataException(InvalidKitDataException.Reason.FAILED_READ, e);
        }
    }

}