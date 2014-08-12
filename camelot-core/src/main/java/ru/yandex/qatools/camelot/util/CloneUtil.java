package ru.yandex.qatools.camelot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CloneUtil {
    private static final Logger logger = LoggerFactory.getLogger(CloneUtil.class);

    public static Object deepCopy(Object orig) throws IOException {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        } catch (Exception e) {
            logger.error("Failed to clone object", e);
        }
        return obj;
    }
}
