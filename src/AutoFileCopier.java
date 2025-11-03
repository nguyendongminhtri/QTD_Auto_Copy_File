import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;

public class AutoFileCopier {

    private static final Path SOURCE_DIR = Paths.get("D:\\Chinh Copy Test");
    private static final Path TARGET_DIR = Paths.get("F:\\Nam 2025 Auto Copy");

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable copyTask = () -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(SOURCE_DIR)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        Path targetFile = TARGET_DIR.resolve(file.getFileName());
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Đã sao chép: " + file.getFileName());
                    }
                }
            } catch (IOException e) {
                System.err.println("Lỗi khi sao chép file: " + e.getMessage());
            }
        };

        // Lập lịch chạy mỗi 10 phút
        scheduler.scheduleAtFixedRate(copyTask, 0, 1, TimeUnit.MINUTES);
    }
}
