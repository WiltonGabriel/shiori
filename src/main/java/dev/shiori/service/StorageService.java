package dev.shiori.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Realiza o upload de um arquivo para o bucket de armazenamento de objetos.
     *
     * @param file arquivo multipart a ser enviado
     * @param folder subpasta de destino no bucket (ex: "avatares", "capas")
     * @return URL pública final acessível do arquivo armazenado
     */
    String uploadFile(MultipartFile file, String folder);
}
