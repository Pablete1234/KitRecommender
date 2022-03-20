package me.pablete1234.kit.aggregator;

import me.pablete1234.kit.aggregator.exception.InvalidKitDataException;
import me.pablete1234.kit.aggregator.util.DummyServer;
import me.pablete1234.kit.aggregator.util.PlayerRecordAggregator;
import me.pablete1234.kit.util.StreamUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

    public static void main(String[] args) throws IOException {
        DummyServer.setup();

        Path kitData = Paths.get("kit_data");
        Path output = kitData.resolve("aggregated");
        Files.createDirectories(output);

        int written = 0;
        Map<InvalidKitDataException.Reason, Integer> invalidCount = new EnumMap<>(InvalidKitDataException.Reason.class);
        long startTime = System.currentTimeMillis();

        for (Path playerFolder : StreamUtil.toIterable(Files.list(kitData))) {
            if (!Files.isDirectory(playerFolder) ||
                    !UUID_PATTERN.matcher(playerFolder.getFileName().toString()).matches()) continue;

            System.out.println("Aggregating " + playerFolder.getFileName());
            PlayerRecordAggregator aggregator = new PlayerRecordAggregator(playerFolder,
                    output.resolve(playerFolder.getFileName() + ".parquet"));
            aggregator.aggregate();

            written += aggregator.getWritten();
            aggregator.getIgnoredCount().forEach((reason, count) -> {
                invalidCount.compute(reason, (k, v) -> v == null ? count : v + count);
            });
        }

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        System.out.println("");
        System.out.println("");
        System.out.println("Done! Wrote " + written + " records in " + seconds +
                "s (" + (Math.round(written * 10 / seconds) / 10) + " records/s)");
        System.out.println("Skipped records by reason: ");
        invalidCount.forEach((reason, count) -> System.out.println("\t" + reason + ": " + count));
    }


}
