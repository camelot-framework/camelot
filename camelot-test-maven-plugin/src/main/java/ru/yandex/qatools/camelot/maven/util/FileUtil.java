package ru.yandex.qatools.camelot.maven.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class FileUtil {

    public static final String UTF_8 = "UTF-8";

    FileUtil() {
    }

    /**
     * Replace all replaces in given file
     *
     * @param toReplace file to replace in
     * @param replaces  replaces to replace
     * @throws IOException if an I/O error occurs while reading files
     */
    public static void replaceInFile(File toReplace, Map<String, String> replaces) throws IOException {
        replaceInFile(toReplace, toReplace, replaces);
    }

    public static void replaceInFile(File from, File to, Map<String, String> replaces) throws IOException {
        String content = IOUtils.toString(new FileInputStream(from), UTF_8);
        for (String key : replaces.keySet()) {
            content = content.replaceAll(key, replaces.get(key));
        }
        IOUtils.write(content, new FileOutputStream(to), UTF_8);
    }


    public static void processTemplate(Configuration cfg, String from, Writer to, Map replaces) throws TemplateException, IOException {
        Template template = cfg.getTemplate(from, UTF_8);
        template.process(replaces, to);
    }
}
