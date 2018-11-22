package net.wifiprobe.wifiprobelog.business.util;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by win7 on 2017/5/15.
 */
@Component
public class PropertiesUtil {
    Properties properties = null;

    @PostConstruct
    public Properties init() {
        try {
            Properties properties = new Properties();
            FileSystemResourceLoader fileSystemResourceLoader = new FileSystemResourceLoader();
            Resource resource = fileSystemResourceLoader.getResource("classpath:application.properties");
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return properties;
    }

    public Properties getProperties() {
        return properties=init();
    }


}
