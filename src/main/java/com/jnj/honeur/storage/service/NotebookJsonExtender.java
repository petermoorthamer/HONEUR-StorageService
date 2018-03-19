package com.jnj.honeur.storage.service;


import com.jnj.honeur.storage.exception.NotebookFileParseException;
import com.jnj.honeur.storage.exception.NotebookFileWriteException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;

public class NotebookJsonExtender {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookJsonExtender.class);

    private static final String UUID_PLACEHOLDER = "uuid-placeholder";

    public void addUuid(Reader jsonReader, Writer jsonWriter, UUID uuid) throws NotebookFileParseException {
        // Parse input
        JSONObject inputObject;
        JSONParser parser = new JSONParser();

        try {
            inputObject = (JSONObject)parser.parse(jsonReader);
        } catch (IOException | ParseException e) {
            LOGGER.error(e.getMessage(), e);
            throw new NotebookFileParseException(e.getMessage(), e);
        }

        String outputJson = inputObject.toJSONString();
        final JSONObject metaDataObject = (JSONObject) inputObject.get("metadata");
        if(metaDataObject != null) {
            metaDataObject.put("uuid", uuid.toString());
            outputJson = inputObject.toJSONString();
        } else if(outputJson.contains(UUID_PLACEHOLDER)) {
            outputJson = outputJson.replaceAll(UUID_PLACEHOLDER, uuid.toString());
        } else {
            throw new NotebookFileParseException("Unknown notebook file format: missing metadata property or uuid-placeholder");
        }

        // Write output
        try {
            jsonWriter.write(outputJson);
            jsonWriter.flush();
        } catch (IOException e) {
            throw new NotebookFileWriteException(e.getMessage(), e);
        }
    }


}
