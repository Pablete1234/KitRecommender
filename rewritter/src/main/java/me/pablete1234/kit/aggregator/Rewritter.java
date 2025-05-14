package me.pablete1234.kit.aggregator;

import blue.strategic.parquet.Dehydrator;
import blue.strategic.parquet.Hydrator;
import blue.strategic.parquet.HydratorSupplier;
import blue.strategic.parquet.ParquetReader;
import blue.strategic.parquet.ParquetWriter;
import me.pablete1234.kit.util.StreamUtil;
import me.pablete1234.kit.util.serialized.KitPreferenceRecord;
import org.apache.parquet.column.ParquetProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class Rewritter {

    private static final Pattern FILENAME_PATTERN = Pattern.compile("([a-f\\d]{8}(-[a-f\\d]{4}){4}[a-f\\d]{8}).parquet");

    public static void main(String[] args) throws IOException {
        Path kitData = Paths.get("kit_data", "all");
        Path output = Paths.get("kit_data", "rewritten");
        Files.createDirectories(output);


        long startTime = System.currentTimeMillis();

        for (Path playerFile : StreamUtil.toIterable(Files.list(kitData))) {
            if (Files.isDirectory(playerFile) ||
                    !FILENAME_PATTERN.matcher(playerFile.getFileName().toString()).matches()) continue;

            System.out.println("Transforming " + playerFile.getFileName());

            try (ParquetWriter<Map<String, Object>> writer = ParquetWriter.builder(
                            output.resolve(playerFile.getFileName()).toFile(),
                            KitPreferenceRecord.SCHEMA,
                            (Dehydrator<Map<String, Object>>) (o, valueWriter) -> o.forEach(valueWriter::write))
                    .withWriterVersion(ParquetProperties.WriterVersion.PARQUET_1_0).buildWriter()) {

                Iterator<Map<String, Object>> it = ParquetReader.streamContent(
                        playerFile.toFile(), HydratorSupplier.constantly(new SimpleMapHydrator())).iterator();

                while (it.hasNext()) {
                    writer.write(it.next());
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;
        System.out.println();
        System.out.println("Done! (took " + seconds + "s)");
    }

    private static class SimpleMapHydrator implements Hydrator<Map<String, Object>, Map<String, Object>> {
        @Override
        public Map<String, Object> start() {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> add(Map<String, Object> stringObjectMap, String s, Object o) {
            stringObjectMap.put(s, o);
            return stringObjectMap;
        }

        @Override
        public Map<String, Object> finish(Map<String, Object> stringObjectMap) {
            return stringObjectMap;
        }
    }


}
