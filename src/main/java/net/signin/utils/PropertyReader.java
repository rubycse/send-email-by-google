package net.signin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author lutfun
 * @since 5/9/16
 */
public class PropertyReader {

    private static String FILE_LOC_PROP = "prop.loc";
    private static Map<String, String> resolvedProps = new HashMap<String, String>();

    static {
        String fileName = System.getProperty(FILE_LOC_PROP);
        File file = fileName != null ? new File(fileName) : null;
        Properties props = new Properties();

        try {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                props.load(is);
            } finally {
                if (is != null) is.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = (String) props.get(key);
            resolvedProps.put(key, value);
        }
    }

    public static String getProperty(String key) {
        return resolvedProps.get(key);
    }
}
