package org.extract;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Nicholas Curl
 */
public class Test {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Test.class);

    public static void main(String[] args) {
        File file = new File("C:\\testing\\");
        RegexFileFilter regexFileFilter = new RegexFileFilter("\\S{5}_.*?_eeDocuments_.*?\\.zip");
        File[] files = file.listFiles((FileFilter) regexFileFilter);
        if(files != null && files.length>0) {
            System.out.println(files.length);
        }
    }
}
