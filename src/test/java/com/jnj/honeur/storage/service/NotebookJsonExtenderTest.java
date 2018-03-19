package com.jnj.honeur.storage.service;

import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class NotebookJsonExtenderTest {

    @Test
    public void addUuid() throws URISyntaxException, IOException {

        UUID uuid = UUID.randomUUID();

        URL jsonInputURL = getClass().getClassLoader().getResource("The Zen of Python.json");
        File jsonInputFile = new File(jsonInputURL.toURI());

        System.out.println("Input file:");
        printFile(jsonInputFile);

        File jsonOutputFile = new File("The Zen of Python - modified.json");
        NotebookJsonExtender jsonExtender = new NotebookJsonExtender();
        jsonExtender.addUuid(new FileReader(jsonInputFile), new FileWriter(jsonOutputFile), uuid);

        System.out.println("Output file:");
        printFile(jsonOutputFile);

        byte[] outputBytes = Files.readAllBytes(jsonOutputFile.toPath());
        String outputString = new String(outputBytes,"UTF-8");

        assertTrue(outputString.contains(uuid.toString()));
    }

    private void printFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = in.readLine();
        while(line != null) {
            System.out.println(line);
            line = in.readLine();
        }
        in.close();
    }
}