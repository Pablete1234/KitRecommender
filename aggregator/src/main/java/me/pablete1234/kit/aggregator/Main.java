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
            Pattern.compile("([a-f\\d]{8}(-[a-f\\d]{4}){4}[a-f\\d]{8})");

    public static void main(String[] args) throws IOException {
        new DummyServer();

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
            aggregator.getIgnoredFiles().forEach((reason, count) ->
                    invalidCount.compute(reason, (k, v) -> (v == null ? 0 : v) + count.size()));
        }

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;
        double recordsPerSecond = Math.round(written * 10.0 / seconds) / 10.0;

        int skipped = invalidCount.values().stream().mapToInt(i -> i).sum();

        System.out.println();
        System.out.println("Done!");
        System.out.println("\tWrote " + written + " records in " + seconds + "s (" + recordsPerSecond + " records/s)");
        System.out.println("\tSkipped " + skipped + " records, reasons: ");
        invalidCount.forEach((reason, count) ->
                System.out.println("\t\t" + count + "\t-\t" + reason + "\t(" + reason.getMessage() + ")"));
    }


}
