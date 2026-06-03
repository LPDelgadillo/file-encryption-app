package com.lpdelgadillo.encryption_api.infrastructure.adapter.in;

import com.lpdelgadillo.encryption_api.domain.model.EncryptionResult;
import com.lpdelgadillo.encryption_api.domain.port.in.EncryptionUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/encryption")
public class EncryptionController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    // Tipos permitidos para encriptar
    private static final Set<String> ALLOWED_TYPES = Set.of(
        // Documentos
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/plain",
        // Imágenes
        "image/jpeg", "image/png", "image/gif", "image/webp",
        // Audio/Video
        "audio/mpeg", "video/mp4", "video/x-msvideo",
        // Código
        "application/javascript", "text/javascript",
        "application/json", "application/xml", "text/xml",
        "text/x-python", "text/x-java-source",
        // Comprimidos
        "application/zip",
        "application/x-rar-compressed",
        "application/vnd.rar"
    );

    private final EncryptionUseCase encryptionUseCase;

    public EncryptionController(EncryptionUseCase encryptionUseCase) {
        this.encryptionUseCase = encryptionUseCase;
    }

    @PostMapping("/encrypt")
    public ResponseEntity<byte[]> encrypt(
            @RequestParam("file") MultipartFile file,
            @RequestParam("secretKey") String secretKey) {
        try {
            // Validación tamaño
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                        .body("File size exceeds 5MB limit".getBytes());
            }

            // Validación tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                return ResponseEntity.badRequest()
                        .body(("File type not allowed: " + contentType).getBytes());
            }

            byte[] fileBytes = file.getBytes();
            String fileName  = file.getOriginalFilename();

            EncryptionResult result = encryptionUseCase.encrypt(fileBytes, fileName, secretKey);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + result.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(result.getProcessedData());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage().getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error processing file: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<byte[]> decrypt(
            @RequestParam("file") MultipartFile file,
            @RequestParam("secretKey") String secretKey) {
        try {
            // Validación tamaño
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                        .body("File size exceeds 5MB limit".getBytes());
            }

            // Para decrypt NO validamos el tipo — el archivo
            // ya viene como .encrypted (application/octet-stream)
            // La validación real la hace AES-GCM con la clave

            byte[] fileBytes = file.getBytes();
            String fileName  = file.getOriginalFilename();

            EncryptionResult result = encryptionUseCase.decrypt(fileBytes, fileName, secretKey);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + result.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(result.getProcessedData());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage().getBytes());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(e.getMessage().getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error processing file: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Encryption API running 🔐");
    }
}