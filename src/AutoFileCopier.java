import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoFileCopier {
    private static final Path SOURCE_DIR = Paths.get("D:\\Backup\\SAOLUU");

    // Các thư mục đích
    private static final Path LOCAL_TARGET = Paths.get("E:\\Nam 2025 Auto Copy");
    private static final Path REMOTE_TARGET_1 = Paths.get("\\\\192.168.1.22\\BackUp From MayChu");
    private static final Path REMOTE_TARGET_2 = Paths.get("\\\\192.168.1.225\\Backup File From May Chu To Kien");
    private static final Path REMOTE_TARGET_3 = Paths.get("\\\\192.168.1.33\\Backup MayChu To Loan");
    private static final Path REMOTE_TARGET_4 = Paths.get("\\\\192.168.1.227\\BackUp From May Chu To Hang");

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable copyTask = () -> {
            copyToTarget(SOURCE_DIR, LOCAL_TARGET);
            copyToTarget(SOURCE_DIR, REMOTE_TARGET_1);
            copyToTarget(SOURCE_DIR, REMOTE_TARGET_2);
            copyToTarget(SOURCE_DIR, REMOTE_TARGET_3);
            copyToTarget(SOURCE_DIR, REMOTE_TARGET_4);
        };

        // Chạy mỗi 5 phút (có thể điều chỉnh)
        scheduler.scheduleAtFixedRate(copyTask, 0, 5, TimeUnit.SECONDS); // chạy mỗi 30 giây
    }

    private static void copyToTarget(Path sourceDir, Path targetDir) {
        if (!Files.exists(targetDir)) {
            System.err.println("❌ Không thể truy cập thư mục đích: " + targetDir);
            return;
        }
        System.out.println("⏳ Đang kiểm tra và sao chép file vào: " + targetDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            FileStore store = Files.getFileStore(targetDir);

            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    String fileName = file.getFileName().toString();

                    // Tìm ngày từ tên file theo định dạng _ddMMyyyy_
                    Pattern pattern = Pattern.compile("_(\\d{2})(\\d{2})(\\d{4})_");
                    Matcher matcher = pattern.matcher(fileName);

                    if (matcher.find()) {
                        String day = matcher.group(1);
                        String month = matcher.group(2);
                        String year = matcher.group(3);

                        // Tạo thư mục đích theo năm/tháng
                        Path datedTargetDir = targetDir.resolve(year).resolve(month);
                        Files.createDirectories(datedTargetDir);

                        Path targetFile = datedTargetDir.resolve(file.getFileName());

                        // Kiểm tra trùng file (cùng tên và kích thước)
                        if (Files.exists(targetFile)) {
                            long sourceSize = Files.size(file);
                            long targetSize = Files.size(targetFile);

                            if (sourceSize == targetSize) {
                                System.out.println("⏩ Bỏ qua (đã tồn tại): " + fileName);
                                continue;
                            }
                        }

                        // Kiểm tra dung lượng còn trống
                        long fileSize = Files.size(file);
                        long usableSpace = store.getUsableSpace();

                        if (usableSpace < fileSize) {
                            System.err.println("⚠️ Không đủ dung lượng để sao chép: " + fileName);
                            continue;
                        }

                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("✅ Đã sao chép: " + fileName + " → " + datedTargetDir);
                    } else {
                        System.err.println("⚠️ Không tìm thấy ngày trong tên file: " + fileName);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi xử lý thư mục: " + e.getMessage());
        }
    }
}
