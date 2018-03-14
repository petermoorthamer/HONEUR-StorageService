package com.jnj.honeur.storage.controller;

import com.jnj.honeur.storage.exception.StorageException;
import com.jnj.honeur.storage.model.*;
import com.jnj.honeur.storage.service.StorageService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * HONEUR Storage Service REST API
 *
 * @author Peter Moorthamer
 */

@Controller
public class StorageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageController.class);

    private StorageService storageService;

    @Value("${amazon.s3.bucketName}")
    private String bucketName;

    public StorageController(@Autowired StorageService storageService) {
        this.storageService = storageService;
    }

    @RequestMapping("/home")
    public String home() {
        return "index";
    }

    @RequestMapping("/test")
    public String test(HttpServletRequest request, Model model) {
        //final Subject subject = SecurityUtils.getSubject();
        //model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("bucketName", bucketName);
        model.addAttribute("allFileKeys", storageService.getAllStorageFileKeys());
        model.addAttribute("cohortDefinitions", storageService.getMatchingStorageFileKeys(CohortDefinitionFile.class, ""));
        return "storage";
    }

    @RequestMapping("/file")
    public ResponseEntity<Object> downloadFile(@RequestParam String fileKey)  {
        try {
            final AbstractStorageFile storageFile = storageService.getStorageFileWithKey(fileKey);
            return createResponse(storageFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The file cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<Object> deleteFile(@RequestParam String fileKey)  {
        try {
            storageService.deleteStorageFile(fileKey);
            return ResponseEntity.noContent().build();
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The file cannot be deleted!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping("/file/{uuid}")
    public ResponseEntity<Object> getStorageFile(@PathVariable String uuid)  {
        try {
            final AbstractStorageFile storageFile = storageService.getStorageFileWithUuid(uuid);
            return createResponse(storageFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort definition cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    //@RequiresAuthentication
    @PostMapping("/cohort-definitions/{uuid}")
    public ResponseEntity<Object> saveCohortDefinition(@PathVariable final String uuid, @RequestParam("file") final MultipartFile file) {
        try {
            String cohortDefinitionUuid;
            try {
                cohortDefinitionUuid = UUID.fromString(uuid).toString();
            } catch (IllegalArgumentException e) {
                cohortDefinitionUuid = UUID.randomUUID().toString();
            }
            final CohortDefinitionFile cohortDefinitionFile = new CohortDefinitionFile(
                    new StorageFileInfo(cohortDefinitionUuid, file.getOriginalFilename()),
                    createTempFile(file));
            storageService.saveStorageFile(cohortDefinitionFile);
            return ResponseEntity.created(new URI("/cohort-definitions/" + uuid)).build();
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort definition cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //@RequiresAuthentication
    @RequestMapping("/cohort-definitions/{uuid}")
    public ResponseEntity<Object> getCohortDefinition(@PathVariable String uuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final CohortDefinitionFile cohortDefinitionFile = storageService.getStorageFile(CohortDefinitionFile.class, uuid);
            return createResponse(cohortDefinitionFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort definition cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping("/cohort-definitions")
    public ResponseEntity<Object> getCohortDefinitions() {
        try {
            List<StorageFileInfo> cohortDefinitions = storageService.getMatchingStorageFileKeys(CohortDefinitionFile.class, "");
            return new ResponseEntity<>(cohortDefinitions, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of cohort inclusion results cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cohort-results/{cohortDefinitionUuid}")
    public ResponseEntity<Object> saveCohortInclusionResult(@PathVariable String cohortDefinitionUuid, @RequestParam("file") MultipartFile file) {
        try {
            final String cohortResultsUuid = UUID.randomUUID().toString();
            final CohortInclusionResultsFile cohortInclusionResultsFile = new CohortInclusionResultsFile(
                    cohortDefinitionUuid,
                    new StorageFileInfo(cohortResultsUuid, file.getOriginalFilename()),
                    createTempFile(file));
            storageService.saveStorageFile(cohortInclusionResultsFile);
            return ResponseEntity.created(new URI(buildPath("/cohort-results/", cohortDefinitionUuid, cohortResultsUuid))).build();
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort inclusion result cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/cohort-results/{cohortDefinitionUuid}/{cohortResultsUuid}")
    public ResponseEntity<Object> getCohortInclusionResult(@PathVariable String cohortDefinitionUuid, @PathVariable String cohortResultsUuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final CohortInclusionResultsFile cohortInclusionResultsFile = storageService.getStorageFile(CohortInclusionResultsFile.class, cohortDefinitionUuid, cohortResultsUuid);
            return createResponse(cohortInclusionResultsFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort inclusion result cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping("/cohort-results/{cohortDefinitionUuid}")
    public ResponseEntity<Object> getCohortInclusionResults(@PathVariable String cohortDefinitionUuid) {
        try {
            List<StorageFileInfo> cohortInclusionResults = storageService.getMatchingStorageFileKeys(CohortInclusionResultsFile.class, cohortDefinitionUuid);
            return new ResponseEntity<>(cohortInclusionResults, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of cohort inclusion results cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/notebooks/{studyId}")
    public ResponseEntity<Object> saveNotebook(@PathVariable String studyId, @RequestParam("file") MultipartFile file) {
        try {
            String uuid = UUID.randomUUID().toString();
            final NotebookFile notebookFile = new NotebookFile(
                    studyId,
                    new StorageFileInfo(uuid, file.getOriginalFilename()),
                    createTempFile(file));
            storageService.saveStorageFile(notebookFile);
            return ResponseEntity.created(new URI("/notebooks/" + studyId + "/ " + uuid)).build();
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/notebooks/{studyId}/{notebookUuid}")
    public ResponseEntity<Object> getNotebook(@PathVariable String studyId, @PathVariable String notebookUuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final NotebookFile notebookFile = storageService.getStorageFile(NotebookFile.class, studyId, notebookUuid);
            return createResponse(notebookFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping("/notebooks/{studyId}")
    public ResponseEntity<Object> getNotebooks(@PathVariable String studyId) {
        try {
            List<StorageFileInfo> notebooks = storageService.getMatchingStorageFileKeys(NotebookFile.class, studyId);
            return new ResponseEntity<>(notebooks, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of notebooks cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/notebook-results/{studyId}/{notebookUuid}")
    public ResponseEntity<Object> saveNotebookResult(@PathVariable String studyId, @PathVariable String notebookUuid, @RequestParam("file") MultipartFile file)  {
        try {
            String uuid = UUID.randomUUID().toString();
            final NotebookResultsFile notebookResultsFile = new NotebookResultsFile(
                    studyId,
                    notebookUuid,
                    new StorageFileInfo(uuid, file.getOriginalFilename()),
                    createTempFile(file));
            storageService.saveStorageFile(notebookResultsFile);
            return ResponseEntity.created(new URI(buildPath("/notebook-results", studyId, notebookUuid, uuid))).build();
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/notebook-results/{studyId}/{notebookUuid}/{notebookResultUuid}")
    public ResponseEntity<Object> getNotebookResult(@PathVariable String studyId, @PathVariable String notebookUuid, @PathVariable String notebookResultUuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final NotebookFile notebookFile = storageService.getStorageFile(NotebookFile.class, studyId, notebookUuid);
            return createResponse(notebookFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping("/notebook-results/{studyId}/{notebookUuid}")
    public ResponseEntity<Object> getNotebookResults(@PathVariable String studyId, @PathVariable String notebookUuid) {
        try {
            List<StorageFileInfo> notebookResults = storageService.getMatchingStorageFileKeys(NotebookResultsFile.class, studyId, notebookUuid);
            return new ResponseEntity<>(notebookResults, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of notebooks cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildPath(String... pathParts) {
        StringBuilder pathBuilder = new StringBuilder();
        for(String pathPart:pathParts) {
            pathBuilder.append(pathPart).append("/");
        }
        return pathBuilder.substring(0, pathBuilder.length()-1);
    }

    private ResponseEntity<Object> createResponse(final AbstractStorageFile storageFile) throws IOException {
        Path path = Paths.get(storageFile.getFile().getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + storageFile.getOriginalFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(storageFile.getFile().length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    private File createTempFile(final MultipartFile file) throws IOException {
        File tmpFile = StorageService.createTempFile(file.getOriginalFilename());
        file.transferTo(tmpFile);
        return tmpFile;
    }

}
