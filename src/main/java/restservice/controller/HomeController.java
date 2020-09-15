package restservice.controller;

import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import restservice.process.ShowAllFilesFromFile;
import restservice.service.FileDownloadService;
import restservice.process.ReadAndCountService;
import restservice.payload.UploadFileResponse;
import restservice.service.FileUploadService;
import restservice.process.ReplaceEverySecond;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private FileUploadService fileUploadService;
    private FileDownloadService fileDownloadService;

    public HomeController(FileUploadService fileUploadService, FileDownloadService fileDownloadService) {
        this.fileUploadService = fileUploadService;
        this.fileDownloadService = fileDownloadService;
    }

    @ApiOperation(value = "Upload File for ReadAndCount Processing", notes = "Upload File for ReadAndCount Processing")
    @PostMapping("/readAndCount")
    public ResponseEntity<UploadFileResponse> readAndCountUpload(@RequestParam("file") MultipartFile file) {
        if (!file.getContentType().equals("text/plain"))
            return new ResponseEntity<>(
                    new UploadFileResponse(StringUtils.cleanPath(
                            file.getOriginalFilename()),
                            "Forbidden type!",
                            file.getContentType(),
                            file.getSize()),
                    HttpStatus.FORBIDDEN);

        String fileName = fileUploadService.storeFile(file);
        ReadAndCountService.processAndSave(fileName);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api")
                .pathSegment(fileName)
                .toUriString();

        return new ResponseEntity<>(
                new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize()),
                HttpStatus.OK);
    }


    @ApiOperation(value = "Upload File for 'ReplaceEverySecond' Processing", notes = "Upload File for 'ReplaceEverySecond' Processing")
    @PostMapping("/replaceEverySecond")
    public ResponseEntity<UploadFileResponse> replaceEverySecond(@RequestParam("file") MultipartFile file) {
        if (!file.getContentType().equals("text/plain"))
            return new ResponseEntity<>(
                    new UploadFileResponse(StringUtils.cleanPath(
                            file.getOriginalFilename()),
                            "Forbidden type!",
                            file.getContentType(),
                            file.getSize()),
                    HttpStatus.FORBIDDEN);

        String fileName = fileUploadService.storeFile(file);
        ReplaceEverySecond.processAndSave(fileName);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api")
                .pathSegment(fileName)
                .toUriString();

        return new ResponseEntity<>(
                new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize()),
                HttpStatus.OK);
    }


    @ApiOperation(value = "Download processed file 'ReadAndCountDownload' or 'ReplaceEverySecond'"
            , notes = "Download processed file 'ReadAndCountDownload' or 'ReplaceEverySecond'")
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFileReadAndCountUpload(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileDownloadService.loadFileAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type");
        }
        if (contentType == null) contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    @ApiOperation(value = "Show All Uploaded Files From File", notes = "Show All Uploaded Files")
    @GetMapping("/show")
    public List showAllFromFile() {
        logger.warn("Showing records from File");
        return ShowAllFilesFromFile.showAllFiles();
    }


    @ApiOperation(value = "Clean All Files", notes = "Clean All Files")
    @DeleteMapping("/clean")
    public String clean() {
        File downloads = new File("downloads");
        File uploads = new File("uploads");
        try {
            FileUtils.cleanDirectory(downloads);
            FileUtils.cleanDirectory(uploads);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.warn("All the Files Upload/Download where deleted");
        return "All the Files Upload/Download where cleaned successfully";
    }
}