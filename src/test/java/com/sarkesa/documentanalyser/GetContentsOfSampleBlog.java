package com.sarkesa.documentanalyser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class GetContentsOfSampleBlog {

    public static String getContentsFromBlogFile() throws FileNotFoundException {
        FileReader fileReader = new FileReader("src/test/resources/text/OhaloBlog.txt");

        BufferedReader bufferedReader = new BufferedReader(fileReader);

        StringBuilder stringBuilder = new StringBuilder();
        bufferedReader.lines().forEach(stringBuilder::append);

        return stringBuilder.toString();
    }
}
