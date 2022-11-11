package com.snapchat.launchpad.conversion;


import com.snapchat.launchpad.conversion.configs.StorageConfig;
import com.snapchat.launchpad.conversion.utils.FileStorage;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Profile("conversion-log")
@RestController
public class ConversionFilesController {

    private static final Logger logger = LoggerFactory.getLogger(ConversionFilesController.class);

    private final StorageConfig storageConfig;

    @Autowired
    public ConversionFilesController(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @RequestMapping(
            value = {"/conversion/files/{filename}"},
            method = RequestMethod.PUT)
    @ResponseBody
    public RedirectView redirectConversionFilesPutRequest(
            @PathVariable("filename") String filename) {
        URL url =
                FileStorage.getPresignedUrl(
                        String.format("%s/files/%s", storageConfig.getStoragePrefix(), filename));
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(url.toString());
        redirectView.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        return redirectView;
    }
}
