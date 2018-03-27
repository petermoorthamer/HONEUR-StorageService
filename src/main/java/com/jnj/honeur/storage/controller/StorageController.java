package com.jnj.honeur.storage.controller;

import com.jnj.honeur.security.SecurityUtils2;
import com.jnj.honeur.storage.exception.StorageException;
import com.jnj.honeur.storage.model.*;
import com.jnj.honeur.storage.service.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
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
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
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

    private static final String TEST_STUDY_UUID = "9a915138-903b-4515-96a3-87d2efba799e";

    private StorageService storageService;
    private StorageLogService storageLogService;
    private ZeppelinRestClient zeppelinRestClient;
    private MailService mailService;

    @Value("${amazon.s3.bucketName}")
    private String bucketName;

    @Value("${shiro.server}")
    private String serverName;

    @Autowired()
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public StorageController(
            @Autowired StorageService storageService,
            @Autowired StorageLogService storageLogService,
            @Autowired ZeppelinRestClient zeppelinRestClient,
            @Autowired MailService mailService) {
        this.storageService = storageService;
        this.storageLogService = storageLogService;
        this.zeppelinRestClient = zeppelinRestClient;
        this.mailService = mailService;
    }

    @ApiOperation(value = "Home page showing the API is up and running", response = String.class)
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home() {
        return "index";
    }

    @ApiOperation(value = "Lists all end points of the HONEUR Storage Service (HSS)", response = String.class)
    @RequestMapping(value = "/api", method = RequestMethod.GET)
    public @ResponseBody Object showEndpointsAction() {
        return requestMappingHandlerMapping.getHandlerMethods().keySet().stream().map(t ->
                (t.getMethodsCondition().getMethods().size() == 0 ? "GET" : t.getMethodsCondition().getMethods().toArray()[0]) + " " +
                        t.getPatternsCondition().getPatterns().toArray()[0]
        ).toArray();
    }

    @ApiOperation(value = "Endpoint for demo and testing purposes.  Shows the logged in user, the objects in the configured Amazon S3 bucket and the storage log", response = String.class)
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test(HttpServletRequest request, Model model) {
        final Subject subject = SecurityUtils.getSubject();
        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("bucketName", bucketName);
        model.addAttribute("allFileKeys", storageService.getAllStorageFileInfo());
        model.addAttribute("storageLog", storageLogService.findAll());
        return "bucket";
    }

    @RequestMapping(value = "/testPost", method = RequestMethod.GET)
    public String testPost(HttpServletRequest request, Model model) {
        StorageRestClient restClient = new StorageRestClient();
        try {
            LOGGER.info("Request: " + request);
            restClient.postCohortResults(request.getSession(false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index";
    }

    @ApiOperation(value = "Endpoint for demo and testing purposes.  The user can upload and download cohort definitions and view the storage log", response = String.class)
    @RequestMapping(value = "/testCohorts", method = RequestMethod.GET)
    public String testCohorts(HttpServletRequest request, Model model) {
        final Subject subject = SecurityUtils.getSubject();
        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("bucketName", bucketName);
        model.addAttribute("cohortDefinitions", storageService.getMatchingStorageFileInfo(CohortDefinitionFile.class, ""));
        final StorageLogEntry probe = StorageLogEntry.createProbe(CohortDefinitionFile.class);
        model.addAttribute("storageLog", storageLogService.findByCriteria(probe));
        return "cohort";
    }

    @ApiOperation(value = "Endpoint for demo and testing purposes.  The user can upload and download cohort inclusion results and view the storage log", response = String.class)
    @RequestMapping(value = "/testCohortResults", method = RequestMethod.GET)
    public String testCohortResults(HttpServletRequest request, Model model) {
        final Subject subject = SecurityUtils.getSubject();
        final String cohortDefinitionUuid = request.getParameter("cohortDefinitionUuid");
        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("cohortDefinitionUuid", cohortDefinitionUuid);
        model.addAttribute("cohortInclusionResults", storageService.getMatchingStorageFileInfo(CohortInclusionResultsFile.class, cohortDefinitionUuid));
        final StorageLogEntry probe = StorageLogEntry.createProbe(CohortInclusionResultsFile.class);
        model.addAttribute("storageLog", storageLogService.findByCriteria(probe));
        return "cohortResults";
    }

    @ApiOperation(value = "Endpoint for demo and testing purposes.  The user can upload and download notebooks and view the storage log", response = String.class)
    @RequestMapping(value = "/testStudy", method = RequestMethod.GET)
    public String testStudy(HttpServletRequest request, Model model) {
        final Subject subject = SecurityUtils.getSubject();
        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("bucketName", bucketName);
        model.addAttribute("notebooks", storageService.getMatchingStorageFileInfo(NotebookFile.class, TEST_STUDY_UUID));
        final StorageLogEntry probe = StorageLogEntry.createProbe(NotebookFile.class);
        model.addAttribute("storageLog", storageLogService.findByCriteria(probe));
        return "study";
    }

    @ApiOperation(value = "Endpoint for demo and testing purposes.  The user can upload and download notebook results and view the storage log", response = String.class)
    @RequestMapping(value = "/testNotebook", method = RequestMethod.GET)
    public String testNotebook(HttpServletRequest request, Model model) {
        final Subject subject = SecurityUtils.getSubject();
        final String notebookUuid = request.getParameter("notebookUuid");
        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("notebookUuid", notebookUuid);
        model.addAttribute("notebookResults", storageService.getMatchingStorageFileInfo(NotebookResultsFile.class, TEST_STUDY_UUID, notebookUuid));
        final StorageLogEntry probe = StorageLogEntry.createProbe(NotebookResultsFile.class);
        model.addAttribute("storageLog", storageLogService.findByCriteria(probe));
        return "notebook";
    }

    @ApiOperation(value = "Retrieves the file with the given file key", response = ResponseEntity.class)
    @RequestMapping(value = "/file", method = RequestMethod.GET)
    public ResponseEntity<Object> downloadFile(@RequestParam String fileKey)  {
        try {
            final AbstractStorageFile storageFile = storageService.getStorageFileWithKey(fileKey);
            logStorageAction(StorageLogEntry.Action.DOWNLOAD, storageFile);
            return createResponse(storageFile);
        } catch (StorageException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The file cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Deletes the file with the given file key", response = ResponseEntity.class)
    @DeleteMapping("/file")
    public ResponseEntity<Object> deleteFile(@RequestParam String fileKey)  {
        try {
            storageService.deleteStorageFile(fileKey);
            logStorageAction(StorageLogEntry.Action.DELETE, fileKey);
            return ResponseEntity.noContent().build();
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The file cannot be deleted!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Retrieves the file with the given UUID", response = ResponseEntity.class)
    @RequestMapping(value = "/file/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> getStorageFile(@PathVariable String uuid)  {
        try {
            final AbstractStorageFile storageFile = storageService.getStorageFileWithUuid(uuid);
            logStorageAction(StorageLogEntry.Action.DOWNLOAD, storageFile);
            return createResponse(storageFile);
        } catch (StorageException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort definition cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    //@RequiresAuthentication
    @ApiOperation(value = "Stores a cohort definition file and assigns a UUID to it", response = ResponseEntity.class)
    @PostMapping("/cohort-definitions")
    public ResponseEntity<Object> saveCohortDefinition(@RequestParam("file") final MultipartFile file) {
        try {
            final String cohortDefinitionUuid = UUID.randomUUID().toString();;
            final CohortDefinitionFile cohortDefinitionFile = new CohortDefinitionFile(
                    new StorageFileInfo(cohortDefinitionUuid, file.getOriginalFilename()),
                    createTempFile(file));
            storageService.saveStorageFile(cohortDefinitionFile);
            logStorageAction(StorageLogEntry.Action.UPLOAD, cohortDefinitionFile);
            return ResponseEntity.created(new URI("/cohort-definitions/" + cohortDefinitionUuid)).build();
        } catch (StorageException | IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort definition cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //@RequiresAuthentication
    @ApiOperation(value = "Retrieves the cohort definition file with the given UUID", response = ResponseEntity.class)
    @RequestMapping(value = "/cohort-definitions/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> getCohortDefinition(@PathVariable String uuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final CohortDefinitionFile cohortDefinitionFile = storageService.getStorageFile(CohortDefinitionFile.class, uuid);
            logStorageAction(StorageLogEntry.Action.DOWNLOAD, cohortDefinitionFile);
            return createResponse(cohortDefinitionFile);
        } catch (StorageException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort definition cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Returns a list of all cohort definitions the logged on user has access to", response = ResponseEntity.class)
    @RequestMapping(value = "/cohort-definitions", method = RequestMethod.GET)
    public ResponseEntity<Object> getCohortDefinitions() {
        try {
            List<StorageFileInfo> cohortDefinitions = storageService.getMatchingStorageFileInfo(CohortDefinitionFile.class, "");
            return new ResponseEntity<>(cohortDefinitions, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of cohort inclusion results cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Stores the cohort inclusion results file and links it to the cohort definition with the given UUID", response = ResponseEntity.class)
    @PostMapping("/cohort-results/{cohortDefinitionUuid}")
    public ResponseEntity<Object> saveCohortInclusionResult(@PathVariable String cohortDefinitionUuid, @RequestParam("file") MultipartFile file) {
        try {
            final String cohortResultsUuid = UUID.randomUUID().toString();
            final CohortInclusionResultsFile cohortInclusionResultsFile = new CohortInclusionResultsFile(
                    cohortDefinitionUuid,
                    new StorageFileInfo(cohortResultsUuid, file.getOriginalFilename()),
                    createTempFile(file));
            storageService.saveStorageFile(cohortInclusionResultsFile);
            logStorageAction(StorageLogEntry.Action.UPLOAD, cohortInclusionResultsFile);
            return ResponseEntity.created(new URI(buildPath("/cohort-results/", cohortDefinitionUuid, cohortResultsUuid))).build();
        } catch (StorageException | IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort inclusion result cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieves the cohort inclusion results file with the given UUID", response = ResponseEntity.class)
    @RequestMapping(value = "/cohort-results/{cohortDefinitionUuid}/{cohortResultsUuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> getCohortInclusionResult(@PathVariable String cohortDefinitionUuid, @PathVariable String cohortResultsUuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final CohortInclusionResultsFile cohortInclusionResultsFile = storageService.getStorageFile(CohortInclusionResultsFile.class, cohortDefinitionUuid, cohortResultsUuid);
            logStorageAction(StorageLogEntry.Action.DOWNLOAD, cohortInclusionResultsFile);
            return createResponse(cohortInclusionResultsFile);
        } catch (StorageException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The cohort inclusion result cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Returns a list of cohort inclusion results for the cohort definition with the given UUID", response = ResponseEntity.class)
    @RequestMapping(value = "/cohort-results/{cohortDefinitionUuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> getCohortInclusionResults(@PathVariable String cohortDefinitionUuid) {
        try {
            List<StorageFileInfo> cohortInclusionResults = storageService.getMatchingStorageFileInfo(CohortInclusionResultsFile.class, cohortDefinitionUuid);
            return new ResponseEntity<>(cohortInclusionResults, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of cohort inclusion results cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Stores a notebook file for a study with the give ID", response = ResponseEntity.class)
    @PostMapping("/notebooks/{studyId}")
    public ResponseEntity<Object> saveNotebook(@PathVariable String studyId, @RequestParam("file") MultipartFile file) {
        try {
            final File uploadedNotebookFile = createTempFile(file);
            final UUID uuid = saveNotebook(studyId, uploadedNotebookFile, file.getOriginalFilename());

            return ResponseEntity.created(new URI("/notebooks/" + studyId + "/" + uuid.toString())).build();
        } catch (StorageException | IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Endpoint for demo and testing purposes.  Downloads the notebook with the given Zeppelin ID from the configured Zeppelin server and stores the notebook for the study with the give ID", response = ResponseEntity.class)
    @PostMapping("/notebooks/zeppelin/ui/{studyId}")
    public String uploadZeppelinNotebook(@PathVariable String studyId, @RequestParam("zeppelinNotebookId") String zeppelinNotebookId) {
        saveNotebookFromZeppelin(studyId, zeppelinNotebookId);
        return "redirect:/testStudy";
    }

    @ApiOperation(value = "Downloads the notebook with the given Zeppelin ID from the configured Zeppelin server and stores the notebook for the study with the give ID", response = ResponseEntity.class)
    @PostMapping("/notebooks/zeppelin/{studyId}")
    public ResponseEntity<Object> saveNotebookFromZeppelin(@PathVariable String studyId, @RequestParam("zeppelinNotebookId") String zeppelinNotebookId) {
        try {
            // Download notebook from the Zeppelin server
            final String filename = zeppelinNotebookId + ".json";
            final File zeppelinNotebookFile = StorageService.createTempFile(filename);
            zeppelinRestClient.downloadNotebook(zeppelinNotebookId, zeppelinNotebookFile);

            final UUID notebookUuid = saveNotebook(studyId, zeppelinNotebookFile, filename);

            return ResponseEntity.created(new URI("/notebooks/" + studyId + "/" + notebookUuid.toString())).build();
        } catch (StorageException | IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private UUID saveNotebook(String studyId, File notebook, String filename) throws IOException {
        // Generate a UUID
        final UUID notebookUuid = UUID.randomUUID();

        // Add a UUID to the notebook file
        final File modifiedNotebook = addUuidToNotebook(notebook, filename, notebookUuid);

        // Store the notebook
        final NotebookFile notebookFile = new NotebookFile(
                studyId,
                new StorageFileInfo(notebookUuid.toString(), filename),
                modifiedNotebook);
        storageService.saveStorageFile(notebookFile);

        // Log the upload of the notebook
        logStorageAction(StorageLogEntry.Action.UPLOAD, notebookFile);

        // Send email
        mailService.sendNewNotebookMail(serverName + buildPath("/notebooks", notebookFile.getStudyId(), notebookFile.getUuid()));

        return notebookUuid;
    }

    private File addUuidToNotebook(File notebook, String filename, UUID uuid) throws IOException {
        final File modifiedNotebookFile = StorageService.createTempFile(filename);
        new NotebookJsonExtender().addUuid(
                new FileReader(notebook),
                new FileWriter(modifiedNotebookFile),
                uuid);
        return modifiedNotebookFile;
    }

    @ApiOperation(value = "Retrieves the notebook file with the given UUID", response = ResponseEntity.class)
    @RequestMapping(value = "/notebooks/{studyId}/{notebookUuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> getNotebook(@PathVariable String studyId, @PathVariable String notebookUuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final NotebookFile notebookFile = storageService.getStorageFile(NotebookFile.class, studyId, notebookUuid);
            logStorageAction(StorageLogEntry.Action.DOWNLOAD, notebookFile);
            return createResponse(notebookFile);
        } catch (StorageException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Returns a list of notebooks for the study with the given ID", response = ResponseEntity.class)
    @RequestMapping(value = "/notebooks/{studyId}", method = RequestMethod.GET)
    public ResponseEntity<Object> getNotebooks(@PathVariable String studyId) {
        try {
            List<StorageFileInfo> notebooks = storageService.getMatchingStorageFileInfo(NotebookFile.class, studyId);
            return new ResponseEntity<>(notebooks, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of notebooks cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Stores the notebook results file for the notebook with the given ID", response = ResponseEntity.class)
    @PostMapping("/notebook-results/{notebookUuid}")
    public ResponseEntity<Object> saveNotebookResult(@PathVariable String notebookUuid, @RequestParam("file") MultipartFile file, @RequestHeader HttpHeaders headers)  {
        final StorageFileInfo notebookFileInfo = storageService.getStorageFileInfoByUuid(NotebookFile.class, notebookUuid);
        if(notebookFileInfo == null) {
            return new ResponseEntity<>(String.format("No notebook with UUID %s found!", notebookUuid), new HttpHeaders(), HttpStatus.NOT_FOUND);
        } else {
            String studyId = getStudyUuid(notebookFileInfo.getKey());
            return saveNotebookResult(studyId, notebookFileInfo.getUuid(), file, headers);
        }
    }

    private boolean checkHttpHeader(HttpHeaders headers) {
        return getUsernamePasswordCredentials(headers) != null;
    }

    private UsernamePasswordCredentials getUsernamePasswordCredentials(final HttpHeaders headers) {
        final List<String> authorizationHeader = headers.get("Authorization");
        if(authorizationHeader == null || authorizationHeader.isEmpty()) {
            LOGGER.warn("No authorization header found in request!");
            return null;
        }
        String authorization = authorizationHeader.get(0);
        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), Charset.forName("UTF-8"));
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            return new UsernamePasswordCredentials(values[0], values[1]);
        }
        LOGGER.warn("No basic authentication info found in HTTP header!");
        return null;
    }

    @ApiOperation(value = "Stores the notebook results file for the study and notebook with the given ID", response = ResponseEntity.class)
    @PostMapping("/notebook-results/{studyId}/{notebookUuid}")
    public ResponseEntity<Object> saveNotebookResult(@PathVariable String studyId, @PathVariable String notebookUuid, @RequestParam("file") MultipartFile file, @RequestHeader HttpHeaders headers)  {
        try {
            // Generate a UUID
            String uuid = UUID.randomUUID().toString();

            // Store the notebook results file
            final NotebookResultsFile notebookResultsFile = new NotebookResultsFile(
                    studyId,
                    notebookUuid,
                    new StorageFileInfo(uuid, file.getOriginalFilename()),
                    createTempFile(file));
            storageService.saveStorageFile(notebookResultsFile);

            // Log the upload of the notebook results
            logStorageAction(StorageLogEntry.Action.UPLOAD, notebookResultsFile, getUsername(headers));

            // Send a response with the URI of the newly created notebook results file
            final URI notebookResultURI = buildURI("/notebook-results", studyId, notebookUuid, uuid);
            return ResponseEntity.created(notebookResultURI).build();
        } catch (StorageException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be saved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieves the notebook results file with the given UUID", response = ResponseEntity.class)
    @RequestMapping(value = "/notebook-results/{studyId}/{notebookUuid}/{notebookResultUuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> getNotebookResult(@PathVariable String studyId, @PathVariable String notebookUuid, @PathVariable String notebookResultUuid)  {
        final Subject subject = SecurityUtils.getSubject();

        try {
            final NotebookResultsFile notebookResultsFile = storageService.getStorageFile(NotebookResultsFile.class, studyId, notebookUuid, notebookResultUuid);
            logStorageAction(StorageLogEntry.Action.DOWNLOAD, notebookResultsFile);
            return createResponse(notebookResultsFile);
        } catch (StorageException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The notebook cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Returns a list of notebook results for the notebook with the given UUID", response = ResponseEntity.class)
    @RequestMapping(value = "/notebook-results/{studyId}/{notebookUuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> getNotebookResults(@PathVariable String studyId, @PathVariable String notebookUuid) {
        try {
            List<StorageFileInfo> notebookResults = storageService.getMatchingStorageFileInfo(NotebookResultsFile.class, studyId, notebookUuid);
            return new ResponseEntity<>(notebookResults, HttpStatus.OK);
        } catch (StorageException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("The list of notebooks cannot be retrieved!", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private URI buildURI(String... pathParts) {
        return ServletUriComponentsBuilder.fromCurrentRequest().replacePath(buildPath(pathParts)).build().toUri();
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

    private void logStorageAction(StorageLogEntry.Action action, AbstractStorageFile storageFile) {
        final String user = SecurityUtils2.getSubjectName(SecurityUtils.getSubject());
        logStorageAction(action, storageFile, user);
    }

    private void logStorageAction(StorageLogEntry.Action action, AbstractStorageFile storageFile, String user) {
        if(storageFile == null) {
            LOGGER.warn("Missing file: cannot log storage action!");
            return;
        }
        storageLogService.save(new StorageLogEntry(user, action, storageFile));
    }

    private void logStorageAction(StorageLogEntry.Action action, String fileKey) {
        final StorageLogEntry logEntry = new StorageLogEntry();
        final String user = SecurityUtils2.getSubjectName(SecurityUtils.getSubject());
        logEntry.setUser(user);
        logEntry.setAction(action);
        logEntry.setStorageFileUuid(getUuid(fileKey));
        logEntry.setStorageFileKey(fileKey);
        storageLogService.save(logEntry);
    }

    /**
     * Returns the username of the logged in user.  If no user is logged in,
     * it tries to find the username in the "Authorization" section of the HTTP header.
     * Returns null if no username can be found.
     */
    private String getUsername(final HttpHeaders headers) {
        String user = SecurityUtils2.getSubjectName(SecurityUtils.getSubject());
        if(user != null) {
            return user;
        }
        if(headers != null) {
            UsernamePasswordCredentials usernamePasswordCredentials = getUsernamePasswordCredentials(headers);
            if(usernamePasswordCredentials != null) {
                return usernamePasswordCredentials.getUserName();
            }
        }
        return null;
    }

    private String getUuid(String fileKey) {
        return new AmazonS3StorageKeyBuilder().getUuid(fileKey);
    }

    private String getStudyUuid(String fileKey) {
        return new AmazonS3StorageKeyBuilder().getKeyPartsWithoutRootFolder(fileKey)[0];
    }

}
