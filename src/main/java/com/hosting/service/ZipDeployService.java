package com.hosting.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ZipDeployService {

    private static final String CONTROLLER_HOST = "172.141.0.216";
    private static final String CONTROLLER_USER = "controller";
    private static final String CONTROLLER_PORT = "6070";
    private static final String SSH_KEY = "/home/ubuntu/.ssh/controller_call_key";
    private static final String REMOTE_DIR = "/tmp/hosting_deploy";
    private static final String DEPLOY_SCRIPT = "/opt/hosting/provisioner/deploy_zip_to_web.sh";

    public void deployZip(Long requestId, MultipartFile zipFile) {
        if (zipFile == null || zipFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 ZIP 파일이 비어 있습니다.");
        }

        String originalFilename = zipFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("ZIP 파일만 업로드할 수 있습니다.");
        }

        try {
            Path localDir = Path.of("/tmp/hosting_zip_uploads");
            Files.createDirectories(localDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Path localZip = localDir.resolve("request_" + requestId + "_" + timestamp + ".zip");

            zipFile.transferTo(localZip.toFile());

            String remoteZip = REMOTE_DIR + "/request_" + requestId + "_" + timestamp + ".zip";

            runCommand(
                    "ssh",
                    "-p", CONTROLLER_PORT,
                    "-i", SSH_KEY,
                    "-o", "StrictHostKeyChecking=no",
                    "-o", "UserKnownHostsFile=/dev/null",
                    CONTROLLER_USER + "@" + CONTROLLER_HOST,
                    "mkdir -p " + REMOTE_DIR
            );

            runCommand(
                    "scp",
                    "-P", CONTROLLER_PORT,
                    "-i", SSH_KEY,
                    "-o", "StrictHostKeyChecking=no",
                    "-o", "UserKnownHostsFile=/dev/null",
                    localZip.toString(),
                    CONTROLLER_USER + "@" + CONTROLLER_HOST + ":" + remoteZip
            );

            runCommand(
                    "ssh",
                    "-p", CONTROLLER_PORT,
                    "-i", SSH_KEY,
                    "-o", "StrictHostKeyChecking=no",
                    "-o", "UserKnownHostsFile=/dev/null",
                    CONTROLLER_USER + "@" + CONTROLLER_HOST,
                    "sudo " + DEPLOY_SCRIPT + " " + requestId + " " + remoteZip
            );

            Files.deleteIfExists(localZip);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("ZIP 배포 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private void runCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output = new String(process.getInputStream().readAllBytes());

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("명령 실행 실패(" + exitCode + "): " + String.join(" ", command) + "\n" + output);
        }
    }
}
