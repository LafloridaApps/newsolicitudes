package com.newsolicitudes.newsolicitudes.services.files;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.newsolicitudes.newsolicitudes.config.DocProperties;



@Service
public class ArchivoServiceImpl implements ArchivoService    {

    
    private final String uploadDir;

    public ArchivoServiceImpl(DocProperties docProperties) {
        this.uploadDir = docProperties.getTemplatesPath();
    }

    @Override
    public String guardarArchivo(MultipartFile file) throws IOException {
         Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
       
        String uniqueFileName = originalFilename ;

        Path filePath = uploadPath.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    @Override
    public Path getRutaCompletaArchivo(String nombreGuardado) throws IOException {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(nombreGuardado);
    }

}
